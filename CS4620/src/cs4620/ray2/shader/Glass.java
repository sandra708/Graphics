package cs4620.ray2.shader;

import cs4620.ray2.RayTracer;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Colord;
import egl.math.Matrix3;
import egl.math.Vector2d;
import egl.math.Vector3;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Glass extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }


	public Glass() { 
		refractiveIndex = 1.0;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "glass " + refractiveIndex + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Glass shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		outIntensity.setZero();
		//establish basis - all useful vectors are 0 in the o-direction, b is +x
		//the normal is +y
		Vector3d normal = new Vector3d(record.normal);
		normal.normalize();
		Vector3d view = ((new Vector3d(ray.direction)).negate()).normalize();
		if(normal.dot(view) == 0.0){ //parallel to within epsilon
			Ray onward = new Ray(record.location, ray.direction);
			onward.makeOffsetRay();
			onward.end = Double.POSITIVE_INFINITY;
			RayTracer.shadeRay(outIntensity, scene, onward, depth + 1);
			return;
		}
		Vector3d o = (new Vector3d(normal)).cross(view);
		Vector3d b = (new Vector3d(o)).cross(normal);
		b.normalize();
		
		//useful vectors
		Vector2d V = new Vector2d(view.dot(b), view.dot(normal));
		V.normalize();
		Vector2d R = new Vector2d(-V.x, V.y);
		//Vector3d reflection = ((new Vector3d(normal)).mul(2).mul(normal.dot(view))).sub(view);
		R.normalize();
		Vector2d N = new Vector2d(0, 1);
		
		//quantities that differ depending on direction
		double viewingAngle;
		double criticalAngle; 
		double refractiveAngle;
		int refractiveDir;
		if(N.dot(V) < 0){
			//we are coming from inside the glass toward air
			refractiveDir = 1;
			criticalAngle = Math.asin(1.0 / refractiveIndex);
			viewingAngle = V.angle(new Vector2d(0, -1));
			refractiveAngle = Math.asin(refractiveIndex * Math.sin(viewingAngle));
		}else{
			//from air into the glass
			refractiveDir = -1;
			criticalAngle = Math.asin(refractiveIndex);
			viewingAngle = V.angle(N);
			refractiveAngle = Math.asin(1.0 / refractiveIndex * Math.sin(viewingAngle));

		}
		
		if(criticalAngle != Double.NaN && viewingAngle > criticalAngle){
			//total internal reflection
			Vector3d reflection = (new Vector3d(normal)).mul(R.y).add((new Vector3d(b).mul(R.x)));
			Ray reflectiveRay = new Ray(record.location, reflection);
			reflectiveRay.makeOffsetRay();
			reflectiveRay.end = Double.POSITIVE_INFINITY;
			RayTracer.shadeRay(outIntensity, scene, reflectiveRay, depth + 1);
			return;
		}
		
		double fresnel = fresnel(normal, view, refractiveIndex);
		
		//reflective portion
		Colord reflC = new Colord();
		Vector3d reflection = (new Vector3d(normal)).mul(R.y).add((new Vector3d(b).mul(R.x)));
		Ray reflectiveRay = new Ray(record.location, reflection);
		reflectiveRay.end = Double.POSITIVE_INFINITY;
		reflectiveRay.makeOffsetRay();
		RayTracer.shadeRay(reflC, scene, reflectiveRay, depth + 1);
		
		if(depth < 3){
			int x = 10;
		}
		//refractive portion
		Colord refrC = new Colord(); 
		Vector2d F = new Vector2d(- Math.sin(refractiveAngle), refractiveDir * Math.cos(refractiveAngle));
		Vector3d refraction = (new Vector3d(b)).mul(F.x).add((new Vector3d(normal)).mul(F.y));
		Ray refractiveRay = new Ray(record.location, refraction);
		refractiveRay.end = Double.POSITIVE_INFINITY;
		refractiveRay.makeOffsetRay();
		RayTracer.shadeRay(refrC, scene, refractiveRay, depth + 1);
		
		//combination
		outIntensity.add(reflC.mul(fresnel)).add(refrC.mul(1 - fresnel));
		
		// TODO#A7: fill in this function.
	}
	

}