package cs4620.ray2.surface;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3d;
import egl.math.Vector3i;
import cs4620.ray2.shader.Shader;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
	/** The normal vector of this triangle, if vertex normals are not specified */
	Vector3d norm;

	/** The mesh that contains this triangle */
	Mesh owner;

	/** 3 indices to the vertices of this triangle. */
	Vector3i index;

	double a, b, c, d, e, f;

	public Triangle(Mesh owner, Vector3i index, Shader shader) {
		this.owner = owner;
		this.index = new Vector3i(index);

		Vector3d v0 = owner.getPosition(index.x);
		Vector3d v1 = owner.getPosition(index.y);
		Vector3d v2 = owner.getPosition(index.z);

		if (!owner.hasNormals()) {
			Vector3d e0 = new Vector3d(), e1 = new Vector3d();
			e0.set(v1).sub(v0);
			e1.set(v2).sub(v0);
			norm = new Vector3d();
			norm.set(e0).cross(e1);
			norm.normalize();
		}
		a = v0.x - v1.x;
		b = v0.y - v1.y;
		c = v0.z - v1.z;

		d = v0.x - v2.x;
		e = v0.y - v2.y;
		f = v0.z - v2.z;

		this.setShader(shader);
	}

	/**
	 * Tests this surface for intersection with ray. If an intersection is found
	 * record is filled out with the information about the intersection and the
	 * method returns true. It returns false otherwise and the information in
	 * outRecord is not modified.
	 *
	 * @param outRecord
	 *            the output IntersectionRecord
	 * @param rayIn
	 *            the ray to intersect
	 * @return true if the surface intersects the ray
	 */
	public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
	  	//TODO#A7: Modify the intersect method: transform the ray to object space
	  	//transform the resulting intersection point and normal to world space
		
		Ray ray = new Ray(rayIn);		
		untransformRay(ray);
		
		////code past this point not mine!
		Vector3d v0 = owner.getPosition(index.x).clone();
		
		double g = ray.direction.x;
		double h = ray.direction.y;
		double i = ray.direction.z;
		double j = v0.x - ray.origin.x;
		double k = v0.y - ray.origin.y;
		double l = v0.z - ray.origin.z;
		double M = a * (e * i - h * f) + b * (g * f - d * i) + c
				* (d * h - e * g);

		double ei_hf = e * i - h * f;
		double gf_di = g * f - d * i;
		double dh_eg = d * h - e * g;
		double ak_jb = a * k - j * b;
		double jc_al = j * c - a * l;
		double bl_kc = b * l - k * c;

		double t = -(f * (ak_jb) + e * (jc_al) + d * (bl_kc)) / M;
		if (t > ray.end || t < ray.start)
			return false;

		double beta = (j * (ei_hf) + k * (gf_di) + l * (dh_eg)) / M;
		if (beta < 0 || beta > 1)
			return false;

		double gamma = (i * (ak_jb) + h * (jc_al) + g * (bl_kc)) / M;
		if (gamma < 0 || gamma + beta > 1)
			return false;

		// There was an intersection, fill out the intersection record
		if (outRecord != null) {
			outRecord.t = t;
			ray.evaluate(outRecord.location, t);
			
			
			outRecord.surface = this;

			if (norm != null) {
				outRecord.normal.set(norm);
			} else {
				outRecord.normal
						.setZero()
						.addMultiple(1 - beta - gamma, owner.getNormal(index.x))
						.addMultiple(beta, owner.getNormal(index.y))
						.addMultiple(gamma, owner.getNormal(index.z));
			}
			
			
			outRecord.normal.normalize();
			if (owner.hasUVs()) {
				outRecord.texCoords.setZero()
						.addMultiple(1 - beta - gamma, owner.getUV(index.x))
						.addMultiple(beta, owner.getUV(index.y))
						.addMultiple(gamma, owner.getUV(index.z));
			}
			
			//this code now mine!
			//transforms intersection back to world space
			tMat.mulPos(outRecord.location);
			tMatTInv.mulDir(outRecord.normal).normalize();
			return true;
		}

		return false;
	}

	public void computeBoundingBox() {
		Vector3d v0 = new Vector3d(owner.getPosition(index.x));
		Vector3d v1 = new Vector3d(owner.getPosition(index.y));
		Vector3d v2 = new Vector3d(owner.getPosition(index.z));
		
		tMat.mulPos(v0);
		tMat.mulPos(v1);
		tMat.mulPos(v2);
		
		double[] maxs = new double[3];
		double[] mins = new double[3];
		double[] avgs = new double[3];
		
		for(int i = 0; i < 3; i++){
			mins[i] = Math.min(v0.get(i), Math.min(v1.get(i), v2.get(i)));
			maxs[i] = Math.max(v0.get(i), Math.max(v1.get(i), v2.get(i)));
			avgs[i] = (v0.get(i) + v1.get(i) + v2.get(i)) / 3;
		}
		
		minBound = new Vector3d(mins[0], mins[1], mins[2]);
		maxBound = new Vector3d(maxs[0], maxs[1], maxs[2]);
		
		averagePosition = new Vector3d(avgs[0], avgs[1], avgs[2]);
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Triangle ";
	}
}