package cs4620.gl.manip;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import blister.input.KeyboardEventDispatcher;
import blister.input.KeyboardKeyEventArgs;
import blister.input.MouseButton;
import blister.input.MouseButtonEventArgs;
import blister.input.MouseEventDispatcher;
import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.UUIDGenerator;
import cs4620.common.event.SceneTransformationEvent;
import cs4620.gl.PickingProgram;
import cs4620.gl.RenderCamera;
import cs4620.gl.RenderEnvironment;
import cs4620.gl.RenderObject;
import cs4620.gl.Renderer;
import cs4620.scene.form.ControlWindow;
import cs4620.scene.form.ScenePanel;
import egl.BlendState;
import egl.DepthState;
import egl.IDisposable;
import egl.RasterizerState;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;
import ext.csharp.ACEventFunc;

public class ManipController implements IDisposable {
	public final ManipRenderer renderer = new ManipRenderer();
	public final HashMap<Manipulator, UUIDGenerator.ID> manipIDs = new HashMap<>();
	public final HashMap<Integer, Manipulator> manips = new HashMap<>();
	
	private final Scene scene;
	private final ControlWindow propWindow;
	private final ScenePanel scenePanel;
	private final RenderEnvironment rEnv;
	private ManipRenderer manipRenderer = new ManipRenderer();
	
	private final Manipulator[] currentManips = new Manipulator[3];
	private RenderObject currentObject = null;
	
	private Manipulator selectedManipulator = null;
	
	/**
	 * Is parent mode on?  That is, should manipulation happen in parent rather than object coordinates?
	 */
	private boolean parentSpace = false;
	
	/**
	 * Last seen mouse position in normalized coordinates
	 */
	private final Vector2 lastMousePos = new Vector2();
	
	public ACEventFunc<KeyboardKeyEventArgs> onKeyPress = new ACEventFunc<KeyboardKeyEventArgs>() {
		@Override
		public void receive(Object sender, KeyboardKeyEventArgs args) {
			if(selectedManipulator != null) return;
			switch (args.key) {
			case Keyboard.KEY_T:
				setCurrentManipType(Manipulator.Type.TRANSLATE);
				break;
			case Keyboard.KEY_R:
				setCurrentManipType(Manipulator.Type.ROTATE);
				break;
			case Keyboard.KEY_Y:
				setCurrentManipType(Manipulator.Type.SCALE);
				break;
			case Keyboard.KEY_P:
				parentSpace = !parentSpace;
				break;
			}
		}
	};
	public ACEventFunc<MouseButtonEventArgs> onMouseRelease = new ACEventFunc<MouseButtonEventArgs>() {
		@Override
		public void receive(Object sender, MouseButtonEventArgs args) {
			if(args.button == MouseButton.Right) {
				selectedManipulator = null;
			}
		}
	};
	
	public ManipController(RenderEnvironment re, Scene s, ControlWindow cw) {
		scene = s;
		propWindow = cw;
		Component o = cw.tabs.get("Object");
		scenePanel = o == null ? null : (ScenePanel)o;
		rEnv = re;
		
		// Give Manipulators Unique IDs
		manipIDs.put(Manipulator.ScaleX, scene.objects.getID("ScaleX"));
		manipIDs.put(Manipulator.ScaleY, scene.objects.getID("ScaleY"));
		manipIDs.put(Manipulator.ScaleZ, scene.objects.getID("ScaleZ"));
		manipIDs.put(Manipulator.RotateX, scene.objects.getID("RotateX"));
		manipIDs.put(Manipulator.RotateY, scene.objects.getID("RotateY"));
		manipIDs.put(Manipulator.RotateZ, scene.objects.getID("RotateZ"));
		manipIDs.put(Manipulator.TranslateX, scene.objects.getID("TranslateX"));
		manipIDs.put(Manipulator.TranslateY, scene.objects.getID("TranslateY"));
		manipIDs.put(Manipulator.TranslateZ, scene.objects.getID("TranslateZ"));
		for(Entry<Manipulator, UUIDGenerator.ID> e : manipIDs.entrySet()) {
			manips.put(e.getValue().id, e.getKey());
		}
		
		setCurrentManipType(Manipulator.Type.TRANSLATE);
	}
	@Override
	public void dispose() {
		manipRenderer.dispose();
		unhook();
	}
	
	private void setCurrentManipType(int type) {
		switch (type) {
		case Manipulator.Type.TRANSLATE:
			currentManips[Manipulator.Axis.X] = Manipulator.TranslateX;
			currentManips[Manipulator.Axis.Y] = Manipulator.TranslateY;
			currentManips[Manipulator.Axis.Z] = Manipulator.TranslateZ;
			break;
		case Manipulator.Type.ROTATE:
			currentManips[Manipulator.Axis.X] = Manipulator.RotateX;
			currentManips[Manipulator.Axis.Y] = Manipulator.RotateY;
			currentManips[Manipulator.Axis.Z] = Manipulator.RotateZ;
			break;
		case Manipulator.Type.SCALE:
			currentManips[Manipulator.Axis.X] = Manipulator.ScaleX;
			currentManips[Manipulator.Axis.Y] = Manipulator.ScaleY;
			currentManips[Manipulator.Axis.Z] = Manipulator.ScaleZ;
			break;
		}
	}
	
	public void hook() {
		KeyboardEventDispatcher.OnKeyPressed.add(onKeyPress);
		MouseEventDispatcher.OnMouseRelease.add(onMouseRelease);
	}
	public void unhook() {
		KeyboardEventDispatcher.OnKeyPressed.remove(onKeyPress);		
		MouseEventDispatcher.OnMouseRelease.remove(onMouseRelease);
	}
	
	/**
	 * Get the transformation that should be used to draw <manip> when it is being used to manipulate <object>.
	 * 
	 * This is just the object's or parent's frame-to-world transformation, but with a rotation appended on to 
	 * orient the manipulator along the correct axis.  One problem with the way this is currently done is that
	 * the manipulator can appear very small or large, or very squashed, so that it is hard to interact with.
	 * 
	 * @param manip The manipulator to be drawn (one axis of the complete widget)
	 * @param mViewProjection The camera (not needed for the current, simple implementation)
	 * @param object The selected object
	 * @return
	 */
	public Matrix4 getTransformation(Manipulator manip, RenderCamera camera, RenderObject object) {
		Matrix4 mManip = new Matrix4();
		
		switch (manip.axis) {
		case Manipulator.Axis.X:
			Matrix4.createRotationY((float)(Math.PI / 2.0), mManip);
			break;
		case Manipulator.Axis.Y:
			Matrix4.createRotationX((float)(-Math.PI / 2.0), mManip);
			break;
		case Manipulator.Axis.Z:
			mManip.setIdentity();
			break;
		}
		if (parentSpace) {
			if (object.parent != null)
				mManip.mulAfter(object.parent.mWorldTransform);
		} else
			mManip.mulAfter(object.mWorldTransform);

		return mManip;
	}
	
	/**
	 * Apply a transformation to <b>object</b> in response to an interaction with <b>manip</b> in which the user moved the mouse from
 	 * <b>lastMousePos</b> to <b>curMousePos</b> while viewing the scene through <b>camera</b>.  The manipulation happens differently depending
 	 * on the value of ManipController.parentMode; if it is true, the manipulator is aligned with the parent's coordinate system, 
 	 * or if it is false, with the object's local coordinate system.  
	 * @param manip The manipulator that is active (one axis of the complete widget)
	 * @param camera The camera (needed to map mouse motions into the scene)
	 * @param object The selected object (contains the transformation to be edited)
	 * @param lastMousePos The point where the mouse was last seen, in normalized [-1,1] x [-1,1] coordinates.
	 * @param curMousePos The point where the mouse is now, in normalized [-1,1] x [-1,1] coordinates.
	 */
	public void applyTransformation(Manipulator manip, RenderCamera camera, RenderObject object, Vector2 lastMousePos, Vector2 curMousePos) {
//		Matrix4 mCamToWorld = (new Matrix4(camera.mViewProjection)).invert();
//		//ray-identifiers created in world space
//		Vector3 last1 = makeVector(mCamToWorld, camera, lastMousePos, true);
//		Vector3 last2 = makeVector(mCamToWorld, camera, lastMousePos, false);
//		Vector3 cur1 = makeVector(mCamToWorld, camera, curMousePos, true);
//		Vector3 cur2 = makeVector(mCamToWorld, camera, curMousePos, false);
//		//conversion of manipulator axis to world space
		Matrix4 mManipToWorld;
		if(parentSpace){
			mManipToWorld = object.parent.mWorldTransform;
		}else{
			mManipToWorld = object.mWorldTransform;
		}
//		Vector3 axisPos = mManipToWorld.mulPos(new Vector3(0));
//		Vector3 axisPos2;
//		switch(manip.axis){
//		case 0: axisPos2 = new Vector3(1, 0, 0); break;
//		case 1: axisPos2 = new Vector3(0, 1, 0); break;
//		default: axisPos2 = new Vector3(0, 0, 1); break;
//		}
//		axisPos2 = mManipToWorld.mulPos(axisPos2);
//		Vector3 axisDir = axisPos2.sub(axisPos);
//		
//		Matrix4 info1 = new Matrix4(last1, last2.sub(last1), axisPos, axisDir);
//		Matrix4 info2 = new Matrix4(cur2, cur2.sub(cur1), axisPos, axisDir);
//		
//		float lastIntersection = findIntersection(info1);
//		float curIntersection = findIntersection(info2);
		
		Matrix4 mManip;
		switch(manip.type){
		case 0: 
			mManip = getScale(manip.axis, camera, mManipToWorld, lastMousePos, curMousePos); break;
		case 1: 
			mManip = getRotation(manip.axis, lastMousePos, curMousePos); break;
		default: 
			mManip = getTranslation(manip.axis, camera, mManipToWorld, lastMousePos, curMousePos); break; //not following mouse: always positive
		}
		if(parentSpace){
			object.sceneObject.transformation.mulAfter(mManip);
		} else{
			object.sceneObject.transformation.mulBefore(mManip);
		}
		
		// There are three kinds of manipulators; you can tell which kind you are dealing with by looking at manip.type.
		// Each type has three different axes; you can tell which you are dealing with by looking at manip.axis.

		// For rotation, you just need to apply a rotation in the correct space (either before or after the object's current
		// transformation, depending on the parent mode this.parentSpace).

		// For translation and scaling, the object should follow the mouse.  Following the assignment writeup, you will achieve
		// this by constructing the viewing rays and the axis in world space, and finding the t values *along the axis* where the
		// ray comes closest (not t values along the ray as in ray tracing).  To do this you need to transform the manipulator axis
		// from its frame (in which the coordinates are simple) to world space, and you need to get a viewing ray in world coordinates.

		// There are many ways to compute a viewing ray, but perhaps the simplest is to take a pair of points that are on the ray,
		// whose coordinates are simple in the canonical view space, and map them into world space using the appropriate matrix operations.
		
		// You may find it helpful to structure your code into a few helper functions; ours is about 150 lines.
		
		// TODO#A3
	}
	
	//returns the click as a point in world space
	private Vector3 makeVector(Matrix4 camToWorld, RenderCamera cam, Vector2 click, boolean near){
		float x = (float) (click.x);
		float y = (float) (click.y);
		float z = 0;
		if(near){
			z = (float) cam.sceneCamera.zPlanes.x;
		} else{
			z = (float) cam.sceneCamera.zPlanes.y;
		}
		return /*cam.mWorldTransform.mulPos*/(camToWorld.mulPos(new Vector3(x, y, z)));
	}
	
	private Matrix4 getRotation(int axis, Vector2 lastMousePos, Vector2 curMousePos){
		Matrix4 rot = new Matrix4();
		float delta = curMousePos.y - lastMousePos.y;
		
		switch(axis){
		case 0: 
			Matrix4.createRotationX(delta, rot);
			break;
		case 1:
			Matrix4.createRotationY(delta, rot);
			break;
		case 2:
			Matrix4.createRotationZ(delta, rot);
		default:
		}
		return rot;
	}
	
	private Matrix4 getTranslation(int axis, RenderCamera camera, Matrix4 manipToWorld, Vector2 lastMousePos, Vector2 curMousePos){
		float t1 = getT(axis, camera, manipToWorld, lastMousePos);
		float t2 = getT(axis, camera, manipToWorld, lastMousePos);
		float factor = t2 - t1;
		Matrix4 trans;
		switch(axis){
		case 0: trans = Matrix4.createTranslation(new Vector3(factor, 0, 0)); break;
		case 1: trans = Matrix4.createTranslation(new Vector3(0, factor, 0)); break;
		default: trans = Matrix4.createTranslation(new Vector3(0, 0, factor)); break;
		}
		return trans;
	}
	
	private Matrix4 getScale(int axis, RenderCamera camera, Matrix4 manipToWorld, Vector2 lastMousePos, Vector2 curMousePos){
		float t1 = getT(axis, camera, manipToWorld, lastMousePos);
		float t2 = getT(axis, camera, manipToWorld, curMousePos);
		float factor = t2 / t1;
		Matrix4 scale;
		switch(axis){
		case 0: scale = Matrix4.createScale(new Vector3(factor, 1, 1)); break;
		case 1: scale = Matrix4.createScale(new Vector3(1, factor, 1)); break;
		default: scale = Matrix4.createScale(new Vector3(1, 1, factor)); break;
		}
		return scale;
	}
	
	private float getT(int axis, RenderCamera camera, Matrix4 manipToWorld, Vector2 mouseClick){
		//Do everything in world space
		Matrix4 camToWorld = (new Matrix4(camera.mViewProjection)).invert();
		Vector3 rayPos = makeVector(camToWorld, camera, mouseClick, true);
		Vector3 rayDir = makeVector(camToWorld, camera, mouseClick, false);
		rayDir.sub(rayPos);
		
		Vector3 axisPos = (manipToWorld.mulPos(new Vector3()));
		Vector3 axisDir;
		switch(axis){
		case 0: axisDir = manipToWorld.getX(); break;
		case 1: axisDir = manipToWorld.getY(); break;
		default: axisDir = manipToWorld.getZ(); break;
		}
		
		Vector3 iNorm = camToWorld.getZ().normalize();
		Vector3 w = (new Vector3(axisDir)).cross(iNorm);
		
		//axisDir*t + w*s + rayDir*r = rayPos - axisPos
		//technically, rayDir is reversed, but we won't ever care (it just leaves the result negated)
		Matrix3 coeffs = new Matrix3(axisDir, w, rayDir).transpose();
		Vector3 affine = (new Vector3(rayPos)).sub(axisPos);
		coeffs.invert();
		Vector3 result = coeffs.mul(new Vector3(affine));
		return result.x;
	}
	
	/**
	 * 
	 * @param storage: position and direction of both the mouse-click ray and the axis
	 * @return t-value along axis to identify closest ray approach
	 */
	private float findIntersection(Matrix4 storage){ //error here! (do written q's now
		Vector3 rayPos = storage.getX();
		Vector3 rayDir = storage.getY();
		Vector3 axisPos = storage.getZ();
		Vector3 axisDir = storage.getTrans();
		
		//(-rayDir*s + axisDir*t - perpDir*r = axisPos - rayPos
		Vector3 perpDir = (new Vector3 (rayDir)).cross(axisDir);
		perpDir.normalize();
		Vector3 c = (new Vector3(rayPos)).sub(axisPos);
		rayDir.negate().normalize();
		
		Matrix3 affine = (new Matrix3(rayDir, axisDir, perpDir)).transpose();
		Matrix3 inverse = (new Matrix3(affine)).invert();
		inverse.mul(c);
		return c.y;
		//Ray(t) - Axis(s) -> set to direction of perpDir
		//(-rayDir*s + axisDir*t - perpDir*r = axisPos - rayPos
//		Matrix3 cramerNum = new Matrix3(rayDir, axisDir, perpDir);
//		Matrix3 cramerDenom = new Matrix3(rayDir, c, perpDir);
//		float numDet = cramerNum.determinant();
//		float denomDet = cramerDenom.determinant();
//		return cramerNum.determinant() / cramerDenom.determinant();
	}
	
	public void checkMouse(int mx, int my, RenderCamera camera) {
		Vector2 curMousePos = new Vector2(mx, my).add(0.5f).mul(2).div(camera.viewportSize.x, camera.viewportSize.y).sub(1);
		if(curMousePos.x != lastMousePos.x || curMousePos.y != lastMousePos.y) {
			if(selectedManipulator != null && currentObject != null) {
				applyTransformation(selectedManipulator, camera, currentObject, lastMousePos, curMousePos);
				scene.sendEvent(new SceneTransformationEvent(currentObject.sceneObject));
			}
			lastMousePos.set(curMousePos);
		}
	}

	public void checkPicking(Renderer renderer, RenderCamera camera, int mx, int my) {
		if(camera == null) return;
		
		// Pick An Object
		renderer.beginPickingPass(camera);
		renderer.drawPassesPick();
		if(currentObject != null) {
			// Draw Object Manipulators
			GL11.glClearDepth(1.0);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			
			DepthState.DEFAULT.set();
			BlendState.OPAQUE.set();
			RasterizerState.CULL_NONE.set();
			
			drawPick(camera, currentObject, renderer.pickProgram);
		}
		int id = renderer.getPickID(Mouse.getX(), Mouse.getY());
		
		selectedManipulator = manips.get(id);
		if(selectedManipulator != null) {
			// Begin Manipulator Operations
			System.out.println("Selected Manip: " + selectedManipulator.type + " " + selectedManipulator.axis);
			return;
		}
		
		SceneObject o = scene.objects.get(id);
		if(o != null) {
			System.out.println("Picked An Object: " + o.getID().name);
			if(scenePanel != null) {
				scenePanel.select(o.getID().name);
				propWindow.tabToForefront("Object");
			}
			currentObject = rEnv.findObject(o);
		}
		else if(currentObject != null) {
			currentObject = null;
		}
	}
	
	public void draw(RenderCamera camera) {
		if(currentObject == null) return;
		
		DepthState.NONE.set();
		BlendState.ALPHA_BLEND.set();
		RasterizerState.CULL_CLOCKWISE.set();
		
		for(Manipulator manip : currentManips) {
			Matrix4 mTransform = getTransformation(manip, camera, currentObject);
			manipRenderer.render(mTransform, camera.mViewProjection, manip.type, manip.axis);
		}
		
		DepthState.DEFAULT.set();
		BlendState.OPAQUE.set();
		RasterizerState.CULL_CLOCKWISE.set();
		
		for(Manipulator manip : currentManips) {
			Matrix4 mTransform = getTransformation(manip, camera, currentObject);
			manipRenderer.render(mTransform, camera.mViewProjection, manip.type, manip.axis);
		}

}
	public void drawPick(RenderCamera camera, RenderObject ro, PickingProgram prog) {
		for(Manipulator manip : currentManips) {
			Matrix4 mTransform = getTransformation(manip, camera, ro);
			prog.setObject(mTransform, manipIDs.get(manip).id);
			manipRenderer.drawCall(manip.type, prog.getPositionAttributeLocation());
		}
	}
	
}
