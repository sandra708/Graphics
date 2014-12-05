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
		Vector3d view = (new Vector3d(ray.direction)).negate();
		Vector3d reflectDir = ((new Vector3d(normal)).mul(2).mul(normal.dot(view))).sub(view);
		double r = fresnel(normal, reflectDir, refractiveIndex);
		double thetaL = normal.angle(reflectDir);
		double thetaR;
		
		//reflective portion
		Colord reflC = new Colord();
		
		//refractive portion
		Colord refrC = new Colord();
		
		//combination
		outIntensity.add(reflC.mul(1 - r)).add(refrC.mul(r));
		
		// TODO#A7: fill in this function.
		


	}
	

}