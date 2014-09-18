package cs4620.ray1.surface;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Matrix3d;
import egl.math.Vector2d;
import egl.math.Vector3d;
import egl.math.Vector3i;
import cs4620.ray1.shader.Shader;

/**
 * Represents a single triangle, part of a triangle mesh
 *
 * @author ags
 */
public class Triangle extends Surface {
  /** The normal vector of this triangle, if vertex normals are not specified */
  Vector3d norm;
  
  /** The mesh that contains this triangle */
  Mesh owner;
  
  /** 3 indices to the vertices of this triangle. */
  Vector3i index;
  
  double a, b, c, d, e, f;
  public Triangle(Mesh owner, Vector3i index, Shader shader) {
    this.owner = owner;
    this.index = new Vector3i(index);
    
    Vector3d v0 = owner.getPosition(index.x);
    Vector3d v1 = owner.getPosition(index.y);
    Vector3d v2 = owner.getPosition(index.z);
    
    if (!owner.hasNormals()) {
    	Vector3d e0 = new Vector3d(), e1 = new Vector3d();
    	e0.set(v1).sub(v0);
    	e1.set(v2).sub(v0);
    	norm = new Vector3d();
    	norm.set(e0).cross(e1);
    }
    a = v0.x-v1.x;
    b = v0.y-v1.y;
    c = v0.z-v1.z;
    
    d = v0.x-v2.x;
    e = v0.y-v2.y;
    f = v0.z-v2.z;
    
    this.setShader(shader);
  }

  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param rayIn the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
    Vector3d trinorm = (new Vector3d(a, b, c)).cross(new Vector3d(d, e, f));
    if(trinorm.dot(rayIn.direction) < 1e-8) return false;
	//p + td = a + beta(b - a) + gamma(c - a), a, b, c vertices
    //use Cramer's rule
    Vector3d a = (new Vector3d(owner.getPosition(index.x)));
    Vector3d b = new Vector3d(owner.getPosition(index.y));
    Vector3d c = new Vector3d(owner.getPosition(index.z));
    Vector3d[] vecs = {
    		(new Vector3d(a)).sub(b),
    		(new Vector3d(a)).sub(c),
    		(new Vector3d(rayIn.direction)),
    		(new Vector3d(a)).sub(rayIn.origin)
    };
    double[] dets = new double[3];
    for(int i = 0; i < 3; i++){
    	Vector3d[] v = new Vector3d[3];
    	for(int j = 0; j < v.length; j++){
    		if(i == j) v[j] = vecs[4];
    		else v[j] = vecs[j];
    	}
    	Matrix3d cramer = new Matrix3d(v[0], v[1], v[2]);
    	dets[i] = cramer.determinant();
    }
    
    if(dets[0] < 0 || dets[1] < 0 || 1 - (dets[0] + dets[1]) < 0){
    	return false;
    }
    
    outRecord.t = dets[2];
    outRecord.surface = this;
    outRecord.location.set(new Vector3d(rayIn.origin).add(new Vector3d(rayIn.direction).mul(outRecord.t)));
    
    Vector3d normA = (owner.getNormal(index.x));
    Vector3d normB = owner.getNormal(index.y);
    Vector3d normC = owner.getNormal(index.z);
    
    Vector3d alpha = (new Vector3d(normA)).mul(1 - dets[0] - dets[1]);
    Vector3d beta = (new Vector3d(normB)).mul(dets[0]);
    Vector3d gamma = (new Vector3d(normC)).mul(dets[1]);
    outRecord.normal.set(new Vector3d(alpha)).add(beta).add(gamma).normalize();
    
    Vector2d textA = owner.getUV(index.x);
    Vector2d textB = owner.getUV(index.y);
    Vector2d textC = owner.getUV(index.z);
    
    Vector2d alphaUV = (new Vector2d(textA)).mul(1 - dets[0] - dets[1]);
    Vector2d betaUV = (new Vector2d(textB)).mul(dets[0]);
    Vector2d gammaUV = (new Vector2d(textC)).mul(dets[1]);
    outRecord.texCoords.set(new Vector2d(alphaUV)).add(betaUV).add(gammaUV);
    
	return true;
	
	//TODO#A2 remember that rays can have ending points!
	//check if textures are available
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "Triangle ";
  }
}