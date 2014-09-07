package cs4620.mesh.gen;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;

/**
 * Generates A Sphere Mesh
 * @author Cristian
 *
 */
public class MeshGenSphere extends MeshGenerator {
	@Override
	public void generate(MeshData outData, MeshGenOptions opt) {
		// TODO#A1: Create A Sphere Mesh

		// Calculate Vertex And Index Count
		outData.vertexCount = (opt.divisionsLatitude + 1) * (opt.divisionsLongitude + 1);
		int tris = (opt.divisionsLatitude) * (opt.divisionsLongitude) * 2;
		outData.indexCount = tris * 3;

		// Create Storage Spaces
		outData.positions = BufferUtils.createFloatBuffer(outData.vertexCount * 3);
		outData.normals = BufferUtils.createFloatBuffer(outData.vertexCount * 3);
		outData.uvs = BufferUtils.createFloatBuffer(outData.vertexCount * 2);
		outData.indices = BufferUtils.createIntBuffer(outData.indexCount);
		
		// Create The Vertices and Normals (which are vertices)
		for(int i = 0; i < opt.divisionsLatitude + 1; i++){
			//Calculate y-position
			float p = (float) i / (float) opt.divisionsLatitude;
			double theta = p * Math.PI; //angle up from bottom
			float y = (float) -Math.cos(theta);
			
			for(int j = 0; j < opt.divisionsLongitude + 1; j++){
				float q = (float) j / (float) opt.divisionsLongitude;
				double phi = q * Math.PI * 2.0; //angle from -z toward +x
				float x = (float) (Math.sin(phi) * Math.sin(theta));
				float z = (float) (Math.cos(phi) * Math.sin(theta));
				
				outData.positions.put(x); outData.positions.put(y); outData.positions.put(z);
				outData.normals.put(x); outData.normals.put(y); outData.normals.put(z);
			}
		}
		
		//Create the UVs
		for(int i = 0; i < opt.divisionsLatitude + 1; i++){
			float u = (float) i / (float) opt.divisionsLatitude;
			for(int j = 0; j < opt.divisionsLongitude + 1; j++){
				float v = (float) j / (float) opt.divisionsLongitude;
				outData.uvs.put(u); outData.uvs.put(v); 
			}
		}
		
		// Create The Indices
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
