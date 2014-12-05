package cs4620.ray2.shader;

import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Light;
import cs4620.ray2.Ray;
import cs4620.ray2.Scene;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;
import egl.math.Vector4d;

public class CookTorrance extends Shader {

	/** The color of the diffuse reflection. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseColor.set(diffuseColor); }

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The roughness controlling the roughness of the surface. */
	protected double roughness = 1.0;
	public void setRoughness(double roughness) { this.roughness = roughness; }

	/**
	 * The index of refraction of this material. Used when calculating Snell's Law.
	 */
	protected double refractiveIndex;
	public void setRefractiveIndex(double refractiveIndex) { this.refractiveIndex = refractiveIndex; }
	
	public CookTorrance() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "CookTorrance " + diffuseColor + " " + specularColor + " " + roughness + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the CookTorrance shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record, int depth) {
		Vector3d N = new Vector3d(record.normal);
		Vector3d V = new Vector3d(ray.direction);
		Vector3d P = new Vector3d(record.location);
		V.normalize();
		Vector3d R = ((new Vector3d(N)).mul(2).mul(N.dot(V))).sub(V);

		for (Light l : scene.getLights()) {
			Vector3d L = l.getDirection(P).negate();
			L.normalize();
			if(isShadowed(scene, l, record, new Ray(P, L))){
				continue;
			}
			double r = l.getRSq(P);
			Vector3d H = (new Vector3d(L)).add(V).normalize();

			// calculate diffuse term
			Colord Idiff = new Colord((new Vector3d(diffuseColor)).mul(Math.max(N.dot(L), 0.0)));
			Idiff.clamp(0, 1);

			// calculate specular term - Cook-Torrence model
			double fresnel = fresnel(N, R, refractiveIndex);

			double microfacetExp = (Math.pow(N.dot(H), 2.0) - 1) / 
					(Math.pow(roughness, 2.0) * Math.pow(N.dot(H), 2.0));

			double microfacet = (1.0 / (Math.pow(roughness, 2.0) * Math.pow(N.dot(H), 4.0))) * 
					Math.pow(Math.E, microfacetExp);

			double g = Math.min(V.dot(H), Math.min(2.0 * N.dot(H) * N.dot(V), (2.0 * N.dot(H) * N.dot(L))));
			g /= V.dot(H);

			double specCoeff = (fresnel * microfacet * g) / (3.1415927 * N.dot(V) * N.dot(L));

			Colord Ispec = new Colord(new Vector3d(specularColor).mul(specCoeff * Math.max(N.dot(L), 0.0)));
			Ispec.clamp(0.0, 1.0);

			outIntensity.add(l.intensity.mul((Idiff.add(Ispec))).div(r)); 
		}
		// TODO#A7 Fill in this function.
		// 1) Loop through each light in the scene.
		// 2) If the intersection point is shadowed, skip the calculation for the light.
		//	  See Shader.java for a useful shadowing function.
		// 3) Compute the incoming direction by subtracting
		//    the intersection point from the light's position.
		// 4) Compute the color of the point using the CookTorrance shading model. Add this value
		//    to the output.
	}
}
