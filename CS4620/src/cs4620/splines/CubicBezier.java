package cs4620.splines;

import java.util.ArrayList;
import egl.math.*;
/*
 * Cubic Bezier class for the splines assignment
 */

public class CubicBezier {
	
	//This Bezier's control points
	Vector2 p0, p1, p2, p3;
	
	//Control parameter for curve smoothness
	float epsilon;
	
	//The points on the curve represented by this Bezier
	private ArrayList<Vector2> curvePoints;
	
	//The normals associated with curvePoints
	private ArrayList<Vector2> curveNormals;
	
	//The tangent vectors of this bezier
	private ArrayList<Vector2> curveTangents;
	
	//This bezier's owner
	private BSpline owner;
	
	/**
	 * 
	 * Cubic Bezier Constructor
	 * 
	 * Given 2-D BSpline Control Points correctly set self.{p0, p1, p2, p3},
	 * self.uVals, self.curvePoints, and self.curveNormals
	 * 
	 * @param bs0 First BSpline Control Point
	 * @param bs1 Second BSpline Control Point
	 * @param bs2 Third BSpline Control Point
	 * @param bs3 Fourth BSpline Control Point
	 * @param eps Maximum angle between line segments
	 */
	public CubicBezier(Vector2 bs0, Vector2 bs1, Vector2 bs2, Vector2 bs3, float eps, BSpline own) {
		curvePoints = new ArrayList<Vector2>();
		curveTangents = new ArrayList<Vector2>();
		curveNormals = new ArrayList<Vector2>();
		epsilon = eps;
		owner = own;
		
		Vector4 col0 = new Vector4(1, 0, 0, 0);
		Vector4 col1 = new Vector4(4, 4, 2, 1);
		Vector4 col2 = new Vector4(1, 2, 4, 4);
		Vector4 col3 = new Vector4(0, 0, 0, 1);
		Matrix4 converter = new Matrix4(col0.mul(1/6.0f), col1.mul(1/6.0f), col2.mul(1/6.0f), col3.mul(1/6.0f));
		converter.transpose();
		
		Vector4 xs = new Vector4(bs0.x, bs1.x, bs2.x, bs3.x);
		Vector4 ys = new Vector4(bs0.y, bs1.y, bs2.y, bs3.y);
		
		converter.mul(xs);
		converter.mul(ys);
		
		p0 = new Vector2(xs.x, ys.x);
		p1 = new Vector2(xs.y, ys.y);
		p2 = new Vector2(xs.z, ys.z);
		p3 = new Vector2(xs.w, ys.w);
		
		tessellate();
	}
	

    /**
     * Approximate a Bezier segment with a number of vertices, according to an appropriate
     * smoothness criterion for how many are needed.  The points on the curve are written into the
     * array self.curvePoints, the tangents into self.curveTangents, and the normals into self.curveNormals.
     * The final point, p3, is not included, because cubic beziers will be "strung together"
     */
    private void tessellate() {
    	 // TODO A5
    	//placeholder code
    	//curvePoints.add(new Vector2(0,0));
    	//curveNormals.add(new Vector2(0,0));
    	//curveTangents.add(new Vector2(0,0));
    	tesselateHelper(p0, p1, p2, p3, 0);
    }
    
    private void tesselateHelper(Vector2 p0, Vector2 p1, Vector2 p2, Vector2 p3, int depth){
    	float theta1 = getAngle(p0, p1, p2);
    	float theta2 = getAngle(p3, p2, p1);
    	
    	if((theta1 < epsilon / 2 && theta2 < epsilon / 2) || depth > 10){
    		//Set curve points
    		curvePoints.add(new Vector2(p0)); //curvePoints.add(new Vector2(p1)); curvePoints.add(new Vector2(p2)); 
    		//Set curve tangents: the tangent at p0 is by definition 3(p1 - p0); 
    		//we calculate the tangents of p1 and p2 as p11-p10 and p12-p11 respectively
    		Vector2 t0 = (new Vector2(p1)).sub(p0).normalize();
    		//Vector2 t1 = interpolate(p2, p1).sub(interpolate(p1, p0)).normalize();
    		//Vector2 t2 = interpolate(p3, p2).sub(interpolate(p2, p1)).normalize();
    		curveTangents.add(new Vector2(t0)); //curveTangents.add(new Vector2(t1)); curveTangents.add(new Vector2(t2));
    		//Set curve normals; we rotate tangents 90 degrees CCW
    		Matrix3 normal = Matrix3.createRotation((float) (-Math.PI / 2.0));
    		normal.mul(t0); //normal.mul(t1); normal.mul(t2);
    		curveNormals.add(t0); //curveNormals.add(t1); curveNormals.add(t2);
    		return;
    	}
    	
    	Vector2 p10 = interpolate(p0, p1);
    	Vector2 p11 = interpolate(p1, p2);
    	Vector2 p12 = interpolate(p2, p3);
    	
    	Vector2 p20 = interpolate(p10, p11);
    	Vector2 p21 = interpolate(p11, p12);
    	
    	Vector2 p30 = interpolate(p20, p21);
    	
    	tesselateHelper(p0, p10, p20, p30, depth + 1);
    	tesselateHelper(p30, p21, p12, p3, depth + 1);
    }
    
    private float getAngle(Vector2 p0, Vector2 p1, Vector2 p2){
    	return (new Vector2(p1)).sub(p0).angle((new Vector2(p2)).sub(p1));
    }
    
    private Vector2 interpolate(Vector2 p, Vector2 q){
    	return ((new Vector2(p)).add(new Vector2(q))).mul(0.5f);
    }
	
    
    /**
     * @return The points on this cubic bezier
     */
    public ArrayList<Vector2> getPoints() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curvePoints) returnList.add(p.clone());
    	return returnList;
    }
    
    /**
     * @return The tangents on this cubic bezier
     */
    public ArrayList<Vector2> getTangents() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveTangents) returnList.add(p.clone());
    	return returnList;
    }
    
    /**
     * @return The normals on this cubic bezier
     */
    public ArrayList<Vector2> getNormals() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveNormals) returnList.add(p.clone());
    	return returnList;
    }
    
    
    /**
     * @return The references to points on this cubic bezier
     */
    public ArrayList<Vector2> getPointReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curvePoints) returnList.add(p);
    	return returnList;
    }
    
    /**
     * @return The references to tangents on this cubic bezier
     */
    public ArrayList<Vector2> getTangentReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveTangents) returnList.add(p);
    	return returnList;
    }
    
    /**
     * @return The references to normals on this cubic bezier
     */
    public ArrayList<Vector2> getNormalReferences() {
    	ArrayList<Vector2> returnList = new ArrayList<Vector2>();
    	for(Vector2 p : curveNormals) returnList.add(p);
    	return returnList;
    }
}
