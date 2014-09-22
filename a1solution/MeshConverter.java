package cs4620.mesh;

import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import egl.math.Vector3;
import egl.math.Vector3i;

/**
 * Performs Normals Reconstruction Upon A Mesh Of Positions
 * @author Cristian
 *
 */
public class MeshConverter {
	/**
	 * Reconstruct a mesh's normals so that it appears to have sharp creases
	 * @param positions List of positions
	 * @param tris List of triangles (Each is a group of three indices into the positions list)
	 * @return A mesh with all faces separated and normals at vertices that lie normal to faces
	 */
	public static MeshData convertToFaceNormals(ArrayList<Vector3> positions, ArrayList<Vector3i> tris) {
		MeshData data = new MeshData();

		// Notice
		System.out.println("Face normals are not implemented");
		
		// No need to implement this function, not part of the Mesh assignment.
		
		return data;
	}
	/**
	 * Reconstruct a mesh's normals so that it appears to be smooth
	 * @param positions List of positions
	 * @param tris List of triangles (Each is a group of three indices into the positions list)
	 * @return A mesh with normals at vertices
	 */
	public static MeshData convertToVertexNormals(ArrayList<Vector3> positions, ArrayList<Vector3i> tris) {
		MeshData data = new MeshData();

		//Calculate Vertex and Index Count
		data.vertexCount = positions.size();
		data.indexCount = tris.size() * 3;
		
		//Create Storage Space
		data.positions = BufferUtils.createFloatBuffer(data.vertexCount * 3);
		data.normals = BufferUtils.createFloatBuffer(data.vertexCount * 3);
		data.indices = BufferUtils.createIntBuffer(data.indexCount);
		
		//Add Vertices
		for(Vector3 v : positions){
			data.positions.put(v.x); data.positions.put(v.y); data.positions.put(v.z);
		}
		
		//Construct Normals
		Vector3[] normals = new Vector3[data.vertexCount];
		for(int i = 0; i < normals.length; i++){
			normals[i] = new Vector3();
		}
		for(Vector3i tri : tris){
			Vector3 x = positions.get(tri.x);
			Vector3 y = positions.get(tri.y);
			Vector3 z = positions.get(tri.z);
			Vector3 fst = x.clone().sub(y);
			Vector3 snd = x.clone().sub(z);
			Vector3 normal = fst.cross(snd);
			if(!normal.equalsApprox(new Vector3(0))){
				normals[tri.x].add(normal);
				normals[tri.y].add(normal);
				normals[tri.z].add(normal);
			}
		}
		for(Vector3 nv : normals){
			nv.normalize();
			data.normals.put(nv.x); data.normals.put(nv.y); data.normals.put(nv.z);
		}
		
		//Add Indices
		for(Vector3i v : tris){
			data.indices.put(v.x); data.indices.put(v.y); data.indices.put(v.z);
		}
		
		// TODO#A1: Allocate mesh data and create mesh positions, normals, and indices (Remember to set mesh Vertex/Index counts)
		// Note that the vertex data has been supplied as a list of egl.math.Vector3 objects.  Take a
		// look at that class, which contains methods that are very helpful for performing vector
		// math.
		
		return data;
	}
}
