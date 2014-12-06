package cs4620.ray2.surface;

import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import cs4620.mesh.MeshData;
import cs4620.ray2.IntersectionRecord;
import cs4620.ray2.Ray;
import egl.math.Vector3;
import egl.math.Vector3d;
import egl.math.Vector4;
import egl.math.Vector4d;

/**
 * A class that represents an Axis-Aligned box. When the scene is built, the Box
 * is split up into a Mesh of 12 Triangles.
 * 
 * @author sjm324
 *
 */
public class Box extends Surface {

	/* The mesh that represents this Box. */
	private Mesh mesh;

	/* The corner of the box with the smallest x, y, and z components. */
	protected final Vector3d minPt = new Vector3d();

	public void setMinPt(Vector3d minPt) {
		this.minPt.set(minPt);
	}

	/* The corner of the box with the largest x, y, and z components. */
	protected final Vector3d maxPt = new Vector3d();

	public void setMaxPt(Vector3d maxPt) {
		this.maxPt.set(maxPt);
	}

	/* Generate a Triangle mesh that represents this Box. */
	private void buildMesh() {
		// Create the OBJMesh
		MeshData box = new MeshData();

		box.vertexCount = 8;
		box.indexCount = 36;

		// Add positions
		box.positions = BufferUtils.createFloatBuffer(box.vertexCount * 3);
		box.positions.put(new float[] { (float) minPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) minPt.z, (float) maxPt.x, (float) minPt.y,
				(float) minPt.z, (float) minPt.x, (float) minPt.y,
				(float) maxPt.z, (float) minPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) maxPt.y,
				(float) maxPt.z, (float) maxPt.x, (float) minPt.y,
				(float) maxPt.z });

		box.indices = BufferUtils.createIntBuffer(box.indexCount);
		box.indices.put(new int[] { 0, 1, 2, 0, 2, 3, 0, 5, 1, 0, 4, 5, 0, 7,
				4, 0, 3, 7, 4, 6, 5, 4, 7, 6, 2, 5, 6, 2, 1, 5, 2, 6, 7, 2, 7,
				3 });
		this.mesh = new Mesh(box);
		
		//set transformations and absorptioins
		this.mesh.setTransformation(this.tMat, this.tMatInv, this.tMatTInv);
		
		this.mesh.shader = this.shader;
	}

	public void computeBoundingBox() {
		if(minPt == null || maxPt == null) return;
		Vector3d[] pts = new Vector3d[8]; //six corners and the center of the box
		Vector3d[] minimax = new Vector3d[2]; //stores minPt and maxPt
		minimax[0] = minPt; minimax[1] = maxPt;
		
		//generate the seven points: six corners on the box and one center
		averagePosition = ((new Vector3d(minPt)).add(maxPt)).mul(0.5);
		for(int i = 0; i < pts.length; i++){
			pts[i] = new Vector3d(minimax[i % 2].x, minimax[i / 4].y, minimax[(i / 2) % 2].z);
		}
		
		//transform to world space
		for(int i = 0; i < pts.length; i++){
			tMat.mulPos(pts[i]);
		}
		
		minBound = new Vector3d(Integer.MAX_VALUE);
		maxBound = new Vector3d(Integer.MIN_VALUE);
		
		for(int i = 1; i < pts.length; i++){
			minBound.set(Math.min(minBound.x, pts[i].x), Math.min(minBound.y, pts[i].y), 
					Math.min(minBound.z, pts[i].z));
			maxBound.set(Math.max(maxBound.x, pts[i].x), Math.max(maxBound.y, pts[i].y), 
					Math.max(maxBound.z, pts[i].z));
		}
		
//		minBound.set(Double.MIN_VALUE);
//		maxBound.set(Double.MAX_VALUE);
		
		// TODO#A7: Compute the bounding box and store the result in
		// averagePosition, minBound, and maxBound.
		// Hint: The bounding box is not the same as just minPt and maxPt,
		// because
		// this object can be transformed by a transformation matrix.


	}

	public boolean intersect(IntersectionRecord outRecord, Ray ray) {
		return false;
	}

	public void appendRenderableSurfaces(ArrayList<Surface> in) {
		buildMesh();
		mesh.appendRenderableSurfaces(in);
	}

	private String debugPrint(){
		return minPt + " " + maxPt;
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return "Box " + debugPrint();
	}

}