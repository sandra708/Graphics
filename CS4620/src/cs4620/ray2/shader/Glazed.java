package cs4620.ray2.shader;

import cs4620.ray2.shader.Shader;
import cs4620.ray2.RayTracer;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Colord;
import egl.math.Vector2d;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Glazed extends Shader {

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }

	/**
	 * The underlying material beneath the glaze.
	 */
	protected Shader substrate;
	public void setSubstrate(Shader substrate) {
		this.substrate = substrate; 
	}
	
	public Glazed() { 
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
		//establish basis
		Vector3d normal = new Vector3d(record.normal);
		Vector3d view = (new Vector3d(ray.direction)).negate();
		view.normalize();
		Vector3d o = (new Vector3d(normal)).cross(view);
		Vector3d b = (new Vector3d(o)).cross(normal);
		
		//useful vectors
		Vector3d reflectDir = ((new Vector3d(normal)).mul(2).mul(normal.dot(view))).sub(view);
		double r = fresnel(normal, view, refractiveIndex);
		
		//for debugging
		Vector2d V = new Vector2d(view.dot(b), view.dot(normal));
		Vector2d R = new Vector2d(reflectDir.dot(b), reflectDir.dot(normal));
		double viewingAngle = view.angle(normal);
		double reflectiveAngle = reflectDir.angle(normal);
		
		//reflection - make v's new
		Ray reflection = new Ray(new Vector3d(record.location), new Vector3d(reflectDir));
		reflection.makeOffsetRay();
		//reflection.end = Double.POSITIVE_INFINITY;
		Colord reflC = new Colord();
		RayTracer.shadeRay(reflC, scene, reflection, depth + 1);
		
		//substrate
		substrate.shade(outIntensity, scene, ray, record, depth + 1);
		
		//combine
		outIntensity.mul(1 - r);
		reflC.mul(r);
		outIntensity.add(reflC);
		
		// TODO#A7: fill in this function.
		

	}
}