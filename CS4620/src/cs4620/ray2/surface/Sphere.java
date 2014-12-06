package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

	/** The center of the sphere. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the sphere. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	protected final double M_2PI = 2 * Math.PI;

	public Sphere() {
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param ray
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
	  	//TODO#A7: Modify the intersect method: transform the ray to object space
	  	//transform the resulting intersection point and normal to world space

		//transform the ray into object space
		Ray ray = untransformRay(rayIn);
		
		// Rename the common vectors so I don't have to type so much
		Vector3d d = ray.direction;
		Vector3d c = center;
		Vector3d o = ray.origin;

		// Compute some factors used in computation
		double qx = o.x - c.x;
		double qy = o.y - c.y;
		double qz = o.z - c.z;
		double dd = d.lenSq();
		double qd = qx * d.x + qy * d.y + qz * d.z;
		double qq = qx * qx + qy * qy + qz * qz;

		// solving the quadratic equation for t at the pts of intersection
		// dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		double discriminantsqr = (qd * qd - dd * (qq - radius * radius));

		// If the discriminant is less than zero, there is no intersection
		if (discriminantsqr < 0) {
			return false;
		}

		// Otherwise check and make sure that the intersections occur on the ray
		// (t
		// > 0) and return the closer one
		double discriminant = Math.sqrt(discriminantsqr);
		double t1 = (-qd - discriminant) / dd;
		double t2 = (-qd + discriminant) / dd;
		double t = 0;
		if (t1 > ray.start && t1 < ray.end) {
			t = t1;
		} else if (t2 > ray.start && t2 < ray.end) {
			t = t2;
		} else {
			return false; // Neither intersection was in the ray's half line.
		}

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
			outRecord.surface = this;
			outRecord.normal.set(outRecord.location).sub(center).normalize();
			double theta = Math.asin(outRecord.normal.y);
			double phi = Math.atan2(outRecord.normal.x, outRecord.normal.z);
			double u = (phi + Math.PI) / (2 * Math.PI);
			double v = (theta - Math.PI / 2) / Math.PI;
			outRecord.texCoords.set(u, v);
			
			//transform back to world space
			tMat.mulPos(outRecord.location);
			tMatTInv.mulDir(outRecord.normal);
			
			return true;
		}
		return false;
	}

	public void computeBoundingBox() {
		averagePosition = tMat.mulPos(new Vector3d(center));
		
		Vector3d[] minimax = new Vector3d[]{
			(new Vector3d(center)).sub(radius), (new Vector3d(center)).add(radius)
		};
		
		Vector3d[] box = new Vector3d[8];
		for(int i = 0; i < box.length; i++){
			box[i] = new Vector3d(minimax[i % 2].x, minimax[i / 4].y, minimax[(i / 2) % 2].z);
			tMat.mulPos(box[i]);
		}
		
		double[] maxes = {
				Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE
		};
		
		double[] mins = {
				Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE
		};
		
		for(int i = 0; i < box.length; i++){
			for(int j = 0; j < maxes.length; j++){
				maxes[j] = Math.max(maxes[j], box[i].get(j));
				mins[j] = Math.min(mins[j], box[i].get(j));
			}
		}
		
//		minBound = new Vector3d(Double.MAX_VALUE);
//		maxBound = new Vector3d(Double.MIN_VALUE);
////		
//		for(int i = 0; i < box.length; i++){
//			minBound.set(Math.min(minBound.x, box[i].x), Math.min(minBound.y, box[i].y), 
//					Math.min(minBound.z, box[i].z));
//			maxBound.set(Math.max(maxBound.x, box[i].x), Math.max(maxBound.y, box[i].y), 
//					Math.max(maxBound.z, box[i].z));
//		}
		
		minBound = new Vector3d(mins[0], mins[1], mins[2]);
		maxBound = new Vector3d(maxes[0], maxes[1], maxes[2]);
		
//		//the axis-directions translated into object-space
//		Vector3d[] box = {
//				new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1),
//				new Vector3d(-1, 0, 0), new Vector3d(0, -1, 0), new Vector3d(0, 0, -1)
//		};
//		
//		for(int i = 0; i < box.length; i++){
//			tMatInv.mulDir(box[i]);
//			box[i].normalize();
//			box[i].mul(radius);
//			tMat.mulDir(box[i]);
//			box[i].add(averagePosition);
//		}
//		
//		maxBound = new Vector3d(box[0].x, box[1].y, box[2].z);
//		minBound = new Vector3d(box[3].x, box[4].y, box[5].z);
//		
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "sphere " + center + " " + radius + " " + shader + " end";
	}

}