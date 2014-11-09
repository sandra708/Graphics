package cs4620.splines;
import java.util.ArrayList;
import java.util.Collections;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;
import egl.NativeMem;
import egl.math.*;

public class BSpline {
	
	private float epsilon;
	
	//BSpline Control Points
	private ArrayList<Vector2> controlPoints;
	
	//Bezier Curves that make up this BSpline
	private ArrayList<CubicBezier> approximationCurves;
	
	//Whether or not this curve is a closed curve
	private boolean isClosed;
	
	public static final float DIST_THRESH = 0.15f;
	public static final int MIN_OPEN_CTRL_POINTS= 4,
			                           MIN_CLOSED_CTRL_POINTS= 3,
			                           MAX_CTRL_POINTS= 20;

	public BSpline(ArrayList<Vector2> controlPoints, boolean isClosed, float epsilon) throws IllegalArgumentException {
		if(isClosed) {
			if(controlPoints.size() < MIN_CLOSED_CTRL_POINTS)
				throw new IllegalArgumentException("Closed Splines must have at least 3 control points.");
		} else {
			if(controlPoints.size() < MIN_OPEN_CTRL_POINTS)
				throw new IllegalArgumentException("Open Splines must have at least 4 control points.");
		}

		this.controlPoints = controlPoints;
		this.isClosed = isClosed;
		this.epsilon = epsilon;
		setBeziers();
	}
	
	public boolean isClosed() {
		return this.isClosed;
	}
	
	public boolean setClosed(boolean closed) {
		if(this.isClosed && this.controlPoints.size() == 3) {
			System.err.println("You must have at least 4 control points to make an open spline.");
			return false;
		}
		this.isClosed= closed;
		setBeziers();
		return true;
	}
	
	public ArrayList<Vector2> getControlPoints() {
		return this.controlPoints;
	}
	
	public void setControlPoint(int index, Vector2 point) {
		this.controlPoints.set(index, point);
		setBeziers();
	}
	
	public boolean addControlPoint(Vector2 point) {
		if(this.controlPoints.size() == MAX_CTRL_POINTS) {
			System.err.println("You can only have "+BSpline.MAX_CTRL_POINTS+" control points per spline.");
			return false;
		}
		/* point= (x0, y0), prev= (x1, y1), curr= (x2,y2)
		 * 
		 * v= [ (y2-y1), -(x2-x1) ]
		 * 
		 * r= [ (x1-x0), (y1-y0) ]
		 * 
		 * distance between point and line prev -> curr is v . r
		 */
		Vector2 curr, prev;
		Vector2 r= new Vector2(), v= new Vector2();
		float distance= Float.POSITIVE_INFINITY;
		int index= -1;
		for(int i= 0; i < controlPoints.size(); i++) {
			curr= controlPoints.get(i);
			if(i == 0) {
				if(isClosed) {
					// add line between first and last ctrl points
					prev= controlPoints.get(controlPoints.size()-1);
				} else {
					continue;
				}
			} else {
				prev= controlPoints.get(i-1);
			}
			v.set(curr.y-prev.y, -(curr.x-prev.x)); v.normalize();
			r.set(prev.x-point.x, prev.y-point.y);
			float newDist = Math.abs(v.dot(r));
			Vector2 v2 = curr.clone().sub(prev);
			v2.mul(1.0f / v2.lenSq());
			float newParam = -v2.dot(r);
			if(newDist < DIST_THRESH && newDist <= distance && 0 < newParam && newParam < 1) {
				distance= newDist;
				index= i;
			}
		}
		
		if (index >= 0) {
			controlPoints.add(index, point);
			setBeziers();
			return true;
		}
		System.err.println("Invalid location, try selecting a point closer to the spline.");
		return false;
	}
	
	public boolean removeControlPoint(int index) {
		if(this.isClosed) {
			if(this.controlPoints.size() == MIN_CLOSED_CTRL_POINTS) {
				System.err.println("You must have at least "+MIN_CLOSED_CTRL_POINTS+" for a closed Spline.");
				return false;
			}
		} else {
			if(this.controlPoints.size() == MIN_OPEN_CTRL_POINTS) {
				System.err.println("You must have at least "+MIN_OPEN_CTRL_POINTS+" for an open Spline.");
				return false;
			}
		}
		this.controlPoints.remove(index);
		setBeziers();
		return true;
	}
	
	public void modifyEpsilon(float newEps) {
		epsilon = newEps;
		setBeziers();
	}
	
	/**
	 * Returns the sequence of normals on this BSpline specified by the sequence of approximation curves
	 */
	public ArrayList<Vector2> getPoints() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : approximationCurves)
			for(Vector2 p : b.getPoints())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Returns the sequence of normals on this BSpline specified by the sequence of approximation curves
	 */
	public ArrayList<Vector2> getNormals() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : approximationCurves)
			for(Vector2 p : b.getNormals())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Returns the sequence of normals on this BSpline specified by the sequence of approximation curves
	 */
	public ArrayList<Vector2> getTangents() {
		ArrayList<Vector2> returnList = new ArrayList<Vector2>();
		for(CubicBezier b : approximationCurves)
			for(Vector2 p : b.getTangents())
				returnList.add(p.clone());
		return returnList;
	}
	
	/**
	 * Using this.controlPoints, create the CubicBezier objects that approxmiate this curve and
	 * save them to this.approximationCurves. Assure that the order of the Bezier curves that you
	 * add to approximationCurves is the order in which they approximate the overall BSpline
	 */
	private void setBeziers() {
		//TODO A5
		approximationCurves = new ArrayList<CubicBezier>();
		Vector2[] pts = new Vector2[3];
		for(Vector2 ctrl : controlPoints){
			if(pts[0] == null) pts[0] = ctrl;
			else if(pts[1] == null) pts[1] = ctrl;
			else if(pts[2] == null) pts[2] = ctrl;
			else{
				addBezier(pts, ctrl);
			}
		}
		if(isClosed){
			addBezier(pts, controlPoints.get(0)); //the segment right before the "end" of the curve
			addBezier(pts, controlPoints.get(1)); //the segment that wraps around the ends of the curve
			addBezier(pts, controlPoints.get(2)); //the segment right after the "beginning" of the curve
		}
	}
	
	private void addBezier(Vector2[] pts, Vector2 ctrl){
		CubicBezier bez = new CubicBezier(pts[0], pts[1], pts[2], ctrl, epsilon, this);
		approximationCurves.add(bez);
		pts[0] = pts[1];
		pts[1] = pts[2];
		pts[2] = ctrl;
	}
	
	/**
	 * Reverses the tangents and normals associated with this BSpline
	 */
	public void reverseNormalsAndTangents() {
		for(CubicBezier b : approximationCurves) {
			for(Vector2 p : b.getNormalReferences())
				p.mul(-1);
			for(Vector2 p : b.getTangentReferences())
				p.mul(-1);
		}
	}
	
	
	/**
	 * Given a closed curve and a sweep curve, fill the three GLBuffer objects appropriately. Here, we sweep the
	 * closed curve along the sweep curve
	 * @param crossSection, the BSpline cross section
	 * @param sweepCurve, the BSpline we are sweeping along
	 * @param data, a MeshData where we will output our triangle mesh
	 * @param scale > 0, parameter that controls how big the closed curve with respect to the sweep curve
	 */
	public static void build3DSweep(BSpline crossSection, BSpline sweepCurve, MeshData data, float scale) {
		//TODO A5
		ArrayList<Vector2> sweepVertices = new ArrayList<Vector2>();
		ArrayList<Vector2> sweepTansList = new ArrayList<Vector2>();
		ArrayList<Vector2> sweepNormsList = new ArrayList<Vector2>();
		ArrayList<Vector2> crossVertices = new ArrayList<Vector2>();
		ArrayList<Vector2> crossNormsList = new ArrayList<Vector2>();
		
		for(CubicBezier bez : crossSection.approximationCurves){
			crossVertices.addAll(bez.getPoints());
			crossNormsList.addAll(bez.getNormals());
		} 
		
		if(crossSection.isClosed()){
			crossVertices.add(crossVertices.get(0).clone()); //repeat vertex for a seam
			crossNormsList.add(crossNormsList.get(0).clone());
		}else{
			//the final control point
			CubicBezier last = crossSection.approximationCurves.get(crossSection.approximationCurves.size() - 1);
			crossVertices.add(last.p3.clone()); 
			crossNormsList.add((last.p2.clone()).sub(last.p3));
		}
		
		for(CubicBezier bez : sweepCurve.approximationCurves){
			sweepVertices.addAll(bez.getPoints());
			sweepTansList.addAll(bez.getTangents());
			sweepNormsList.addAll(bez.getNormals());
		} 
		if(sweepCurve.isClosed()){ //repeat a seam
			sweepVertices.add(sweepVertices.get(0).clone());
			sweepTansList.add(sweepTansList.get(0).clone());
			sweepNormsList.add(sweepNormsList.get(0).clone());
		}else{ //add the last point
			CubicBezier last = sweepCurve.approximationCurves.get(sweepCurve.approximationCurves.size() - 1);
			sweepVertices.add(last.p3);
			Vector2 tanL = (last.p3.clone()).sub(last.p2);
			sweepTansList.add(tanL.clone());
			Matrix3.createRotation((float) (Math.PI / 2)).mul(tanL);
			sweepNormsList.add(tanL);
		}
		
		Vector2[] sweepPos = sweepVertices.toArray(new Vector2[sweepVertices.size()]);
		Vector2[] sweepTans = sweepTansList.toArray(new Vector2[sweepTansList.size()]);
		Vector2[] sweepNorms = sweepNormsList.toArray(new Vector2[sweepNormsList.size()]);
		Vector2[] crossPos = crossVertices.toArray(new Vector2[crossVertices.size()]);
		Vector2[] crossNorms = crossNormsList.toArray(new Vector2[crossNormsList.size()]);
		
		data.vertexCount = crossPos.length * sweepPos.length;
		
		int tris = 2 * (sweepPos.length - 1) * (crossPos.length - 1);
		data.indexCount = tris * 3;
		
		data.positions = BufferUtils.createFloatBuffer(data.vertexCount * 3);
		data.normals = BufferUtils.createFloatBuffer(data.vertexCount * 3);
		data.indices = BufferUtils.createIntBuffer(data.indexCount);
		data.uvs = BufferUtils.createFloatBuffer(data.vertexCount * 2);
		
		//map x/y/z of cross-frame into n/t/b of sweep-frame, and then map sweep frame into x-z plane
		for(int i = 0; i < sweepPos.length; i++){
			Vector3 posCtr = new Vector3(sweepPos[i].x, 0, sweepPos[i].y);
			Vector3 tan = new Vector3(sweepTans[i].x, 0, sweepTans[i].y);
			Vector3 norm = new Vector3(sweepNorms[i].x, 0, sweepNorms[i].y);
			Vector3 b = (new Vector3(tan)).cross(norm);
			for(int j = 0; j < crossPos.length; j++){
				Vector3 disp = (norm.clone()).mul(crossPos[j].y).add(b.clone().mul(crossPos[j].x)).mul(scale);
				Vector3 pos = (posCtr.clone()).add(disp);
				data.positions.put(pos.x); data.positions.put(pos.y); data.positions.put(pos.z);
				//compute normal (cross-section normal in global frame)
				Vector3 normC = (norm.clone()).mul(crossNorms[j].y).add(b.clone().mul(crossNorms[j].x));
				normC.normalize();
				data.normals.put(normC.x); data.normals.put(normC.y); data.normals.put(normC.z);
			}
		}
		
		//indices: 
		for(int i = 0; i < sweepPos.length - 1; i++){
			int si = i * crossPos.length;
			for(int j = 0; j < crossPos.length - 1; j++){
				data.indices.put(si);
				data.indices.put(si + crossPos.length);
				data.indices.put(si + 1);
				data.indices.put(si + 1);
				data.indices.put(si + crossPos.length);
				data.indices.put(si + crossPos.length + 1);
				si++;
			}
		}
	}

	public float getEpsilon() {
		return epsilon;
	}
}
