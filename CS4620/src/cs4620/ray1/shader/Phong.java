package cs4620.ray1.shader;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Light;
import cs4620.ray1.Ray;
import cs4620.ray1.Scene;
import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector3d;

/**
 * A Phong material.
 *
 * @author ags, pramook
 */
public class Phong extends Shader {

	/** The color of the diffuse reflection. */
	protected final Colord diffuseColor = new Colord(Color.White);
	public void setDiffuseColor(Colord diffuseColor) { this.diffuseColor.set(diffuseColor); }

	/** The color of the specular reflection. */
	protected final Colord specularColor = new Colord(Color.White);
	public void setSpecularColor(Colord specularColor) { this.specularColor.set(specularColor); }

	/** The exponent controlling the sharpness of the specular reflection. */
	protected double exponent = 1.0;
	public void setExponent(double exponent) { this.exponent = exponent; }

	public Phong() { }

	/**
	 * @see Object#toString()
	 */
	public String toString() {    
		return "phong " + diffuseColor + " " + specularColor + " " + exponent + " end";
	}

	/**
	 * Evaluate the intensity for a given intersection using the Phong shading model.
	 *
	 * @param outIntensity The color returned towards the source of the incoming ray.
	 * @param scene The scene in which the surface exists.
	 * @param ray The ray which intersected the surface.
	 * @param record The intersection record of where the ray intersected the surface.
	 * @param depth The recursion depth.
	 */
	@Override
	public void shade(Colord outIntensity, Scene scene, Ray ray, IntersectionRecord record) {
		Colord diffuseHue = new Colord(diffuseColor);
		if(getTexture() != null) diffuseHue = getTexture().getTexColor(record.texCoords);
		
		outIntensity.set(0);
		for(Light light : scene.getLights()){
			Vector3d toLight = (new Vector3d(light.position).sub(record.location));
			double len = toLight.len();
			Ray shadowRay = new Ray();
			if(!isShadowed(scene, light, record, shadowRay)){
				Vector3d l = (new Vector3d(toLight)).normalize();
				Vector3d n = (new Vector3d(record.normal)).normalize();
				Vector3d v = (new Vector3d(ray.direction)).negate().normalize();
				Vector3d h = (new Vector3d(l)).add(v).normalize();
				double phong = Math.max(0, h.dot(n));
				double lambert = l.dot(n);
				if(!(lambert < 0)){
					Colord specIntensity = (Colord) (new Colord(light.intensity)).mul(specularColor);
					Colord lambIntensity = (Colord) (new Colord(light.intensity)).mul(diffuseHue);
					Colord phongC = (Colord) specIntensity.mul(Math.pow(phong, exponent) / Math.pow(len, 2));
					Colord lambertC = (Colord) lambIntensity.mul(Math.max(l.clone().dot(n), 0) / Math.pow(len, 2));
					outIntensity.add(phongC).add(lambertC);
				}
				
				//how to add/multiply colors? we need intensity, diffuse color, specular color
			}
		}
		// TODO#A2: Fill in this function.
		// 1) Loop through each light in the scene.
		// 2) If the intersection point is shadowed, skip the calculation for the light.
		//	  See Shader.java for a useful shadowing function.
		// 3) Compute the incoming direction by subtracting
		//    the intersection point from the light's position.
		// 4) Compute the color of the point using the Phong shading model. Add this value
		//    to the output.
		
	}

}