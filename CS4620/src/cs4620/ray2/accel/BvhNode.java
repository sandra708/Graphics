package cs4620.ray2.accel;

import cs4620.ray2.Ray;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * A class representing a node in a bounding volume hierarchy.
 * 
 * @author pramook 
 */
public class BvhNode {

	/** The current bounding box for this tree node.
	 *  The bounding box is described by 
	 *  (minPt.x, minPt.y, minPt.z) - (maxBound.x, maxBound.y, maxBound.z).
	 */
	public final Vector3d minBound, maxBound;
	
	/**
	 * The array of children.
	 * child[0] is the left child.
	 * child[1] is the right child.
	 */
	public final BvhNode child[];

	/**
	 * The index of the first surface under this node. 
	 */
	public int surfaceIndexStart;
	
	/**
	 * The index of the surface next to the last surface under this node.	 
	 */
	public int surfaceIndexEnd; 
	
	/**
	 * Default constructor
	 */
	public BvhNode()
	{
		minBound = new Vector3d();
		maxBound = new Vector3d();
		child = new BvhNode[2];
		child[0] = null;
		child[1] = null;		
		surfaceIndexStart = -1;
		surfaceIndexEnd = -1;
	}
	
	/**
	 * Constructor where the user can specify the fields.
	 * @param minBound
	 * @param maxBound
	 * @param leftChild
	 * @param rightChild
	 * @param start
	 * @param end
	 */
	public BvhNode(Vector3d minBound, Vector3d maxBound, BvhNode leftChild, BvhNode rightChild, int start, int end) 
	{
		this.minBound = new Vector3d();
		this.minBound.set(minBound);
		this.maxBound = new Vector3d();
		this.maxBound.set(maxBound);
		this.child = new BvhNode[2];
		this.child[0] = leftChild;
		this.child[1] = rightChild;		   
		this.surfaceIndexStart = start;
		this.surfaceIndexEnd = end;
	}
	
	/**
	 * @return true if this node is a leaf node
	 */
	public boolean isLeaf()
	{
		return child[0] == null && child[1] == null; 
	}
	
	/** 
	 * Check if the ray intersects the bounding box.
	 * @param ray
	 * @return true if ray intersects the bounding box
	 */
	public boolean intersects(Ray ray) {
		Vector3d tMin = (new Vector3d(minBound)).sub(ray.origin).div(ray.direction);
		Vector3d tMax = (new Vector3d(maxBound)).sub(ray.origin).div(ray.direction);
		
		Vector3d tEnter = new Vector3d(Math.min(tMin.x, tMax.x), 
				Math.min(tMin.y, tMax.y), Math.min(tMin.z, tMax.z));
		Vector3d tExit = new Vector3d(Math.max(tMin.x, tMax.x), 
				Math.max(tMin.y, tMax.y), Math.max(tMin.z, tMax.z));
		
		double rStart = ray.start;
		double rEnd = ray.end;
		if(rEnd == 0.0) rEnd = Double.POSITIVE_INFINITY;
		
		double tStart = Math.max(Math.max(tEnter.x, tEnter.y), Math.max(tEnter.z, rStart));
		double tEnd = Math.min(Math.min(tExit.x, tExit.y), Math.min(tExit.z, rEnd));
		
		return tStart < tEnd;
		//return true;
//		//localize ray coordinates
//		Vector3d min = (new Vector3d(minBound)).sub(ray.origin);
//		Vector3d max = (new Vector3d(maxBound)).sub(ray.origin);
//		Vector3d dir = new Vector3d(ray.direction);
//		double[] iFace = {
//				min.x, max.x, min.y, max.y, min.z, max.z
//		};
//		
//		//first we check if the ray intersects some plane of the cube
//		for(int i = 0; i < 6; i++){
//			if(iFace[i] * dir.get(i / 2) < ray.start) continue; //the face is behind the ray
//			if(iFace[i] * dir.get(i / 2) > ray.end && ray.end > 0.0) continue; //the face is past the end of the ray
//
//			//check intersection point is within the face
//			int j;
//			int k;
//			switch(i / 2){
//			case 0: j = 1; k = 2; break;
//			case 1: j = 0; k = 2; break;
//			default: j = 0; k = 1; break;
//			}
//			if(intersectSq(Math.abs(iFace[i]) * dir.get(j), Math.abs(iFace[i]) * dir.get(k), iFace[j * 2], 
//					iFace[k * 2], iFace[j * 2 + 1], iFace[k * 2 + 1])){
//				return true;
//			}
//		}
//		
//		return false;
//		// TODO#A7: fill in this function.
		// Check whether the given ray intersects the AABB of this BvhNode
	}
	
//	//checks that the first element of box is within the square delimited by the other two
//	private boolean intersectSq(double ptx, double pty, double minx, double miny, double maxx, double maxy){
//		return (ptx > minx && ptx < maxx && pty > miny && pty < maxy);
//	}
}
