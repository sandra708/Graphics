package cs4620.anim;

import java.util.HashMap;

import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.event.SceneEvent;
import cs4620.common.event.SceneTransformationEvent;
import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Quat;
import egl.math.Vector3;

/**
 * A Component Resting Upon Scene That Gives
 * Animation Capabilities
 * @author Cristian
 *
 */
public class AnimationEngine {
	/**
	 * The First Frame In The Global Timeline
	 */
	private int frameStart = 0;
	/**
	 * The Last Frame In The Global Timeline
	 */
	private int frameEnd = 100;
	/**
	 * The Current Frame In The Global Timeline
	 */
	private int curFrame = 0;
	/**
	 * Scene Reference
	 */
	private final Scene scene;
	/**
	 * Animation Timelines That Map To Object Names
	 */
	public final HashMap<String, AnimTimeline> timelines = new HashMap<>();

	/**
	 * An Animation Engine That Works Only On A Certain Scene
	 * @param s The Working Scene
	 */
	public AnimationEngine(Scene s) {
		scene = s;
	}
	
	/**
	 * Set The First And Last Frame Of The Global Timeline
	 * @param start First Frame
	 * @param end Last Frame (Must Be Greater Than The First
	 */
	public void setTimelineBounds(int start, int end) {
		// Make Sure Our End Is Greater Than Our Start
		if(end < start) {
			int buf = end;
			end = start;
			start = buf;
		}
		
		frameStart = start;
		frameEnd = end;
		moveToFrame(curFrame);
	}
	/**
	 * Add An Animating Object
	 * @param oName Object Name
	 * @param o Object
	 */
	public void addObject(String oName, SceneObject o) {
		timelines.put(oName, new AnimTimeline(o));
	}
	/**
	 * Remove An Animating Object
	 * @param oName Object Name
	 */
	public void removeObject(String oName) {
		timelines.remove(oName);
	}

	/**
	 * Set The Frame Pointer To A Desired Frame (Will Be Bounded By The Global Timeline)
	 * @param f Desired Frame
	 */
	public void moveToFrame(int f) {
		if(f < frameStart) f = frameStart;
		else if(f > frameEnd) f = frameEnd;
		curFrame = f;
	}
	/**
	 * Looping Forwards Play
	 * @param n Number Of Frames To Move Forwards
	 */
	public void advance(int n) {
		curFrame += n;
		if(curFrame > frameEnd) curFrame = frameStart + (curFrame - frameEnd - 1);
	}
	/**
	 * Looping Backwards Play
	 * @param n Number Of Frames To Move Backwards
	 */
	public void rewind(int n) {
		curFrame -= n;
		if(curFrame < frameStart) curFrame = frameEnd - (frameStart - curFrame - 1);
	}

	public int getCurrentFrame() {
		return curFrame;
	}
	public int getFirstFrame() {
		return frameStart;
	}
	public int getLastFrame() {
		return frameEnd;
	}
	public int getNumFrames() {
		return frameEnd - frameStart + 1;
	}

	/**
	 * Adds A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void addKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.addKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	/**
	 * Removes A Keyframe For An Object At The Current Frame
	 * Using The Object's Transformation - (CONVENIENCE METHOD)
	 * @param oName Object Name
	 */
	public void removeKeyframe(String oName) {
		AnimTimeline tl = timelines.get(oName);
		if(tl == null) return;
		tl.removeKeyFrame(getCurrentFrame(), tl.object.transformation);
	}
	
	/**
	 * Loops Through All The Animating Objects And Updates Their Transformations To
	 * The Current Frame - For Each Updated Transformation, An Event Has To Be 
	 * Sent Through The Scene Notifying Everyone Of The Change
	 */
	public void updateTransformations() {
		for(SceneObject o: scene.objects){
			AnimTimeline time = timelines.get(o.getID().name);
			if(time == null){
				continue;
			}
			AnimKeyframe prev = time.frames.ceiling(new AnimKeyframe(curFrame));
			AnimKeyframe next = time.frames.floor(new AnimKeyframe(curFrame));
			if(next == null){
				next = time.frames.last();
			}
			if(prev == null){
				prev = time.frames.first();
			}
			Matrix4 transformation = interpolate(prev, next);
			o.transformation.set(transformation);
			scene.sendEvent(new SceneTransformationEvent(o));
		}
		// TODO: Loop Through All The Timelines
		// And Update Transformations Accordingly
		// (You WILL Need To Use this.scene)
	}
	
	//does not modify the parameters: calculates the transformation matrix
	private Matrix4 interpolate(AnimKeyframe prev, AnimKeyframe next){
		float u = (curFrame - prev.frame) / ((float) (next.frame - prev.frame));
		if(next.frame == prev.frame) u = 0;
		Vector3 t1 = prev.transformation.getTrans();
		Matrix3 r1 = new Matrix3();
		Matrix3 s1 = new Matrix3();
		
		Vector3 t2 = next.transformation.getTrans();
		Matrix3 r2 = new Matrix3();
		Matrix3 s2 = new Matrix3();
		
		Vector3 t0 = (t1.mul(1 - u).add(t2.mul(u))).mul(0.5f);
		
		prev.transformation.getAxes().polar_decomp(r1, s1);
		next.transformation.getAxes().polar_decomp(r2, s2);
		Matrix3 s0 = s1.interpolate(s1, s2, u);
		Matrix3 r0 = getRotation(new Quat(r1), new Quat(r2), u);
		
		return Matrix4.createTranslation(t0).mulBefore(new Matrix4(r0)).mulBefore(new Matrix4(s0));
	}
	
	//calculates the spherically interpolated rotation matrix
	private Matrix3 getRotation(Quat q1, Quat q2, float u){
		double theta = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w;
		
		float f1 = (float) (Math.sin((1 - u) * theta) / Math.sin(theta));
		float f2 = (float) (Math.sin(u * theta) / Math.sin(theta));
		Quat q11 = (new Quat(q1)).setScaled(f1, new Quat(q1));
		Quat q21 = (new Quat(q2)).setScaled(f2, new Quat(q2));
		Quat q0 = (q11).add(q21);
		
		return q0.toRotationMatrix(new Matrix3());
	}
}
