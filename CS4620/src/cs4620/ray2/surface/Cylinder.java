package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector2d;
import egl.math.Vector3d;

public class Cylinder extends Surface {

	/** The center of the bottom of the cylinder x , y ,z components. */
	protected final Vector3d center = new Vector3d();

	public void setCenter(Vector3d center) {
		this.center.set(center);
	}

	/** The radius of the cylinder. */
	protected double radius = 1.0;

	public void setRadius(double radius) {
		this.radius = radius;
	}

	/** The height of the cylinder. */
	protected double height = 1.0;

	public void setHeight(double height) {
		this.height = height;
	}

	public Cylinder() {
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
		//TODO#A7: Modify the intersect method: 
		//1. transform the ray to object space (use untransformRay)
		//2. transform the resulting intersection point and normal to world space		  
		Ray ray = untransformRay(rayIn);

		// Rename the common vectors so I don't have to type so much
		Vector3d d = ray.direction;
		Vector3d c = center;
		Vector3d o = ray.origin;

		double tMin = ray.start, tMax = ray.end;
		// Compute some factors used in computation
		double qx = o.x - c.x;
		double qy = o.y - c.y;
		//double qz = o.z - c.z;
		double rr = radius * radius;

		double dd = d.x * d.x + d.y *d.y;
		double qd = d.x * qx + d.y * qy;
		double qq =  qx * qx + qy * qy;

		double t = 0, td1=0, td2=0;
		double zMin = c.z - height/2;
		double zMax = c.z + height/2;

		// z-plane cap calculations
		if (d.z >= 0) {
			td1 = (zMin- o.z) / d.z;
			td2 = (zMax - o.z) / d.z;
		}
		else {
			td1 = (zMax - o.z) / d.z;
			td2 = (zMin - o.z) / d.z;
		}
		if (tMin > td2 || td1 > tMax)
			return false;
		if (td1 > tMin)
			tMin = td1;
		if (td2 < tMax)
			tMax = td2;

		// solving the quadratic equation for t at the pts of intersection
		// dd*t^2 + (2*qd)*t + (qq-r^2) = 0
		double discriminantsqr = (qd * qd - dd * (qq - rr));

		// If the discriminant is less than zero, there is no intersection
		if (discriminantsqr < 0) {
			return false;
		}

		// Otherwise check and make sure that the intersections occur on the ray (t
		// > 0) and return the closer one
		double discriminant = Math.sqrt(discriminantsqr);
		double t1 = (-qd - discriminant) / dd;
		double t2 = (-qd + discriminant) / dd;

		if (t1 > ray.start && t1 < ray.end) {
			t = t1;
		}
		else if (t2 > ray.start && t2 < ray.end) {
			t = t2;
		}

		Vector3d thit1 = new Vector3d(0); 
		ray.evaluate(thit1, tMin);
		Vector3d thit2 = new Vector3d(0); 
		ray.evaluate(thit2, tMax);

		double dx1 = thit1.x-c.x;  
		double dy1 = thit1.y-c.y; 
		double dx2 = thit2.x-c.x;  
		double dy2 = thit2.y-c.y; 

		if ((t < tMin || t > tMax) && dx1 * dx1 + dy1 * dy1 > rr && dx2 * dx2 + dy2 * dy2 > rr) {
			return false;
		}

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			double tside =Math.min( td1, td2);

			if (t <tside) {
				outRecord.t = tside;
				ray.evaluate(outRecord.location, tside);
				outRecord.normal.set(0, 0, 1);
			}
			else {
				outRecord.t = t;
				ray.evaluate(outRecord.location, t);        
				outRecord.normal.set(outRecord.location.x, outRecord.location.y, 0).sub(c.x, c.y, 0);
			}

			if (outRecord.normal.dot(ray.direction) > 0)
				outRecord.normal.negate();

			outRecord.surface = this;

			//transform position and normal to world-space
			tMat.mulPos(outRecord.location);
			tMatTInv.mulDir(outRecord.normal);

			return true;
		}
		return false;
	}

	public void computeBoundingBox() {
		Vector3d centerUp = (new Vector3d(center)).add(0, 0, height / 2.0);
		averagePosition = tMat.mulPos(new Vector3d(centerUp));
		
		Vector3d[] minimax = new Vector3d[]{
			((new Vector3d(center)).sub(radius, radius, 0)), (new Vector3d(center)).add(radius, radius, height)
		};
		Vector3d[] box =  new Vector3d[8];
		minBound = new Vector3d(Double.POSITIVE_INFINITY);
		maxBound = new Vector3d(Double.NEGATIVE_INFINITY);
		
		for(int i = 0; i < box.length; i++){
			box[i] = new Vector3d(minimax[i % 2].x, minimax[i / 4].y, minimax[i / 2 % 2].y);
			tMat.mulPos(box[i]);
			minBound.set(Math.min(minBound.x, box[i].x), Math.min(minBound.y, box[i].y), Math.min(minBound.z, box[i].z));
			maxBound.set(Math.max(maxBound.x, box[i].x), Math.max(maxBound.y, box[i].y), Math.max(maxBound.z, box[i].z));
		}
		
//		Vector3d[] box = {
//				new Vector3d(1, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 1),
//				new Vector3d(-1, 0, 0), new Vector3d(0, -1, 0), new Vector3d(0, 0, -1)
//		};
//		
//		Vector3d[] obj = new Vector3d[6];
//		
//		for(int i = 0; i < box.length; i++){
//			tMatInv.mulDir(box[i]);
//			box[i].normalize();
//			//check if intersects cap or band
//			Vector2d band = new Vector2d(box[i].x, box[i].y);
//			double lenR = band.len();
//			double lenH = new Vector2d(0, box[i].z).len();
//			
//			if(lenR * radius < lenH * height / 2.0){ //intersects a cap
//				double z = height / 2.0;
//				if(box[i].z < 0) z = -z;
//				obj[i] = new Vector3d(band.x, band.y, z);
//			}else{ //intersects the band
//				band.normalize().mul(radius);
//				obj[i] = new Vector3d(band.x, band.y, box[i].z * height / 2.0);
//			}
//			obj[i].add(centerUp);
//			tMat.mulPos(centerUp);
//		}
//		
//		maxBound = new Vector3d(obj[0].x, obj[1].y, obj[2].z);
//		minBound = new Vector3d(obj[3].x, obj[4].y, obj[5].z);
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		// Hint: The bounding box may be transformed by a transformation matrix.


	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Cylinder " + center + " " + radius + " " + height + " "
				+ shader + " end";
	}
}