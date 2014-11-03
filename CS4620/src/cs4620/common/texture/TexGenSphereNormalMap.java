package cs4620.common.texture;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2;
import egl.math.Vector2i;


public class TexGenSphereNormalMap extends ACTextureGenerator {
	// 0.5f means that the discs are tangent to each other
	// For greater values discs intersect
	// For smaller values there is a "planar" area between the discs
	private float bumpRadius;
	// The number of rows and columns
	// There should be one disc in each row and column
	private int resolution;
	
	public TexGenSphereNormalMap() {
		this.bumpRadius = 0.5f;
		this.resolution = 10;
		this.setSize(new Vector2i(256));
	}
	
	public void setBumpRadius(float bumpRadius) {
		this.bumpRadius = bumpRadius;
	}
	
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	//not debugged!
	@Override
	public void getColor(float u, float v, Color outColor) {
		// TODO A4: Implement the sphere-disk normal map generation
		float[] uv = new float[] {u, v};
		setDiskCoord(uv);
		
		double phi = (1 - uv[1]) * Math.PI;
		double y = Math.cos(phi);
		double theta = uv[0] * 2 * Math.PI;
		double x = - Math.sin(theta) * Math.sin(phi);
		double z = - Math.cos(theta) * Math.sin(phi);
		
		Colord color = new Colord(x, y, z);
		color.normalize();
		color.mul(0.5);
		color.add(0.5);
		
		outColor.set(color);
	}
	
	//if u,v is on a disk, resets values to the center of the disk
	private void setDiskCoord(float[] uv){
		//convert to [0,res] x [0,res]
		float uN = uv[0] * resolution;
		float vN = uv[1] * resolution;
		
		//the nearest disc-center in [0,res] x [0,res] (epsilon for rounding-error)
		float uR = Math.round(uN);
		float vR = Math.round(vN);
		
		//if out-of-bounds there isn't an actual disc there (shouldn't ever happen)
		if(uR < 0 || uR > resolution) return;
		if(vR < 0 || vR > resolution) return;
		
		Vector2 n = new Vector2(uN, vN);
		Vector2 r = new Vector2(uR, vR);
		
		//if point within disc, substitute disc-center for normaling purposes
		float dist = (n.sub(r)).len();
		if(dist <= bumpRadius){
			uv[0] = uR / resolution;
			uv[1] = vR / resolution;
		}
	}
}
