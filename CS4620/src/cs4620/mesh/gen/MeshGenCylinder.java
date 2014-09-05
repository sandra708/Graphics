package cs4620.mesh.gen;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;

/**
 * Generates A Cylinder Mesh
 * @author Cristian
 *
 */
public class MeshGenCylinder extends MeshGenerator {
	@Override
	public void generate(MeshData outData, MeshGenOptions opt) {
		// TODO#A1: Add Normals And Texture Coordinates Into The Mesh

		// Calculate Vertex And Index Count
		outData.vertexCount = (opt.divisionsLongitude) * 4 + 2;
		int tris = (opt.divisionsLongitude * 2) + (2 * (opt.divisionsLongitude - 2));
		outData.indexCount = tris * 3;

		// Create Storage Spaces
		outData.positions = BufferUtils.createFloatBuffer(outData.vertexCount * 3);
		outData.uvs = BufferUtils.createFloatBuffer(outData.vertexCount * 2);
		outData.normals = BufferUtils.createFloatBuffer(outData.vertexCount * 3);
		outData.indices = BufferUtils.createIntBuffer(outData.indexCount);
		
		// Create The Vertices
		for(int i = 0;i < opt.divisionsLongitude;i++) {
			// Calculate XZ-Plane Position
			float p = (float)i / (float)opt.divisionsLongitude;
			double theta = p * Math.PI * 2.0;
			float z = (float)-Math.cos(theta);
			float x = (float)-Math.sin(theta);
			
			// Middle Tube Top
			outData.positions.put(x); outData.positions.put(1); outData.positions.put(z);
			
			// Middle Tube Bottom
			outData.positions.put(x); outData.positions.put(-1); outData.positions.put(z);

			// Top Cap
			outData.positions.put(x); outData.positions.put(1); outData.positions.put(z);

			// Bottom Cap
			outData.positions.put(x); outData.positions.put(-1); outData.positions.put(z);
		}
		// Extra Vertices For U = 1
		outData.positions.put(0); outData.positions.put(1); outData.positions.put(-1);
		
		outData.positions.put(0); outData.positions.put(-1); outData.positions.put(-1);
		
		//Create the Normals 
		for(int i = 0; i < opt.divisionsLongitude; i++){
			//Calculate the XZ-plane position
			float p = (float)i / (float)opt.divisionsLongitude;
			double theta = p * Math.PI * 2.0;
			float z = (float)-Math.cos(theta);
			float x = (float)-Math.sin(theta);
			
			//Tube
			outData.normals.put(x); outData.normals.put(0); outData.normals.put(z);
			outData.normals.put(x); outData.normals.put(0); outData.normals.put(z);
			
			//Cap
			outData.normals.put(0); outData.normals.put(1); outData.normals.put(0);
			outData.normals.put(0); outData.normals.put(-1); outData.normals.put(0);
		} 
		//For the extra vertices at U = 1
		outData.normals.put(0); outData.normals.put(1); outData.normals.put(-1);
		outData.normals.put(0); outData.normals.put(-1); outData.normals.put(-1);
		
		//Create UVs
		for(int i = 0; i < opt.divisionsLongitude; i++){
			float p = (float)i / (float)opt.divisionsLongitude;
			double theta = p * Math.PI * 2.0;
			float u = (float) (-Math.cos(theta) * 0.5);
			float v = (float) (-Math.sin(theta) * 0.5 + 0.5);
			
			//Tube
			outData.uvs.put(p); outData.uvs.put(0);
			outData.uvs.put(p); outData.uvs.put((float) 0.5);
			
			//Caps
			outData.uvs.put((float) (u + 0.5)); outData.uvs.put(v);
			outData.uvs.put(u); outData.uvs.put(v);
		}
		//Extra Vertices
		outData.uvs.put(0); outData.uvs.put(0);
		outData.uvs.put(0); outData.uvs.put((float) 0.5);
		
		// Create The Indices For The Tube
		for(int i = 0;i < opt.divisionsLongitude;i++) {
			int si = i * 4;
			outData.indices.put(si);
			outData.indices.put(si + 1);
			outData.indices.put(si + 4);
			outData.indices.put(si + 4);
			outData.indices.put(si + 1);
			outData.indices.put(si + 5);
		}
		
		// Create The Indices For The Caps
		for(int i = 0;i < opt.divisionsLongitude - 2;i++) {
			int si = (i + 1) * 4 + 2;
			
			// Top Fan Piece
			outData.indices.put(2);
			outData.indices.put(si);
			outData.indices.put(si + 4);

			// Bottom Fan Piece
			outData.indices.put(3);
			outData.indices.put(si + 5);
			outData.indices.put(si + 1);
		}
	}

}
