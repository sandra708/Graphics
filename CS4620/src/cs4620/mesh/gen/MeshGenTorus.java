package cs4620.mesh.gen;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;

/**
 * Generates A Torus Mesh
 * @author Cristian
 *
 */
public class MeshGenTorus extends MeshGenerator {
	@Override
	public void generate(MeshData outData, MeshGenOptions opt) {
		//Generate Vertex and Index Count
		outData.vertexCount = (opt.divisionsLatitude + 1) * (opt.divisionsLongitude + 1);
		int tris = opt.divisionsLatitude * opt.divisionsLongitude * 2;
		outData.indexCount = tris * 3;
		
		//Allocate Space
		outData.positions = BufferUtils.createFloatBuffer(outData.vertexCount * 3);
		outData.normals = BufferUtils.createFloatBuffer(outData.vertexCount * 3);
		outData.uvs = BufferUtils.createFloatBuffer(outData.vertexCount * 2);
		outData.indices = BufferUtils.createIntBuffer(outData.indexCount);
		
		//Torus Geometry
		float rInner = (1 - opt.innerRadius) / 2;
		float rOuter = 1 - rInner;
		
		//Vertex Positions and Normals
		for(int i = 0; i < opt.divisionsLatitude + 1; i++){
			float p = (float) i / (float) opt.divisionsLatitude;
			double theta = p * Math.PI * 2;
			float y = (float) (Math.sin(theta) * rInner);
			float r = (float) (rOuter - Math.cos(theta) * rInner); 
			
			float ny = y;
			float nr = (float) (-Math.cos(theta) * rInner);
			
			for(int j = 0; j < opt.divisionsLongitude + 1; j++){
				float q = (float) j / (float) opt.divisionsLongitude;
				double phi = q * Math.PI * 2;
				
				//Positions
				float x = (float) (Math.sin(phi) * r);
				float z = (float) (-Math.cos(phi) * r);
	
				outData.positions.put(x); outData.positions.put(y); outData.positions.put(z);
				
				//Normals
				float nx = (float) (Math.sin(phi) * nr);
				float nz = (float) (-Math.cos(phi) * nr);
				
				outData.normals.put(nx); outData.normals.put(ny); outData.normals.put(nz);
			}
 		}
		
		//Create the UVs
		for(int i = 0; i < opt.divisionsLatitude + 1; i++){
			float v = (float) 1 - i / (float) opt.divisionsLatitude;
			for(int j = 0; j < opt.divisionsLongitude + 1; j++){
				float u = (float) 1 - j / (float) opt.divisionsLongitude;
				outData.uvs.put(u); outData.uvs.put(v); 
			}
		}
		
		//Indices
		for(int i = 0; i < opt.divisionsLatitude; i++){
			for(int j = 0; j < opt.divisionsLongitude; j++){
				int br = i * (opt.divisionsLongitude + 1) + j;
				int tr = (i + 1) * (opt.divisionsLongitude + 1) + j;
				outData.indices.put(br); outData.indices.put(br + 1); outData.indices.put(tr);
				outData.indices.put(tr); outData.indices.put(br + 1); outData.indices.put(tr + 1);
			}
		}
	}
}
