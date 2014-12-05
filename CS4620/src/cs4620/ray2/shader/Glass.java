package cs4620.ray2.shader;

import cs4620.ray2.RayTracer;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Colord;
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
		//useful vectors
		Vector3d normal = record.normal;
		Vector3d view = ((new Vector3d(ray.direction)).negate()).normalize();
		Vector3d reflection = ((new Vector3d(normal)).mul(2).mul(normal.dot(view))).sub(view);
		reflection.normalize();
		double viewingAngle;
		double criticalAngle; 
		double refractiveAngle;
		if(normal.dot(view) < 0){
			//we are coming from inside the glass toward air
			criticalAngle = Math.asin(1.0 / refractiveIndex);
			viewingAngle = (new Vector3d(normal).negate()).angle(view);
			refractiveAngle = Math.asin((1.0 / refractiveIndex) * viewingAngle);
		}else{
			//from air into the glass
			criticalAngle = Math.asin(refractiveIndex);
			viewingAngle = normal.angle(view);
			refractiveAngle = Math.asin(refractiveIndex) * viewingAngle;
		}
		
		if(criticalAngle != Double.NaN && viewingAngle > criticalAngle){
			//total internal reflection
			RayTracer.shadeRay(outIntensity, scene, new Ray(record.location, reflection), depth + 1);
			return;
		}
		
		double R = fresnel(normal, view, refractiveIndex);
		
		//reflective portion
		Colord reflC = new Colord();
		RayTracer.shadeRay(reflC, scene, new Ray(record.location, reflection), depth + 1);
		
		//refractive portion
		Colord refrC = new Colord(); 
		Vector3d basis = (new Vector3d(normal)).cross(view);
		//orth is orthogonal to the normal but in the same plane as the viewing ray
		Vector3d orth = (new Vector3d(basis)).cross(normal);
		orth.normalize();
		orth.mul(Math.cos(refractiveAngle));
		Vector3d refraction = (new Vector3d(normal)).mul(Math.sin(refractiveAngle)).add(orth);
		RayTracer.shadeRay(refrC, scene, new Ray(record.location, refraction), depth + 1);
		
		//combination
		outIntensity.add(reflC.mul(1 - R)).add(refrC.mul(R));
		
		// TODO#A7: fill in this function.
	}
	

}