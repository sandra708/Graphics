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
		
		//Vertex Positions
		for(int i = 0; i < opt.divisionsLatitude + 1; i++){
			float p = (float) i / (float) opt.divisionsLatitude;
			double theta = p * Math.PI * 2;
			float r = 1 - opt.innerRadius;
			float y = (float) (Math.sin(theta) * r);
			for(int j = 0; j < opt.divisionsLongitude + 1; j++){
				float q = (float) j / (float) opt.divisionsLongitude;
				double phi = q * Math.PI * 2;
				float x = (float) (Math.cos(theta) * r + Math.sin(phi));
				float z = (float) (Math.cos(theta) * r + Math.cos(phi));
				
				outData.positions.put(x); outData.positions.put(y); outData.positions.put(z);
			}
 		}
		
		//Vertex Normals
		
		//UVs
		
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
