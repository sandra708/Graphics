package cs4620.ray1.surface;

import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Ray;
import egl.math.Vector3d;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {
  
  /** The center of the sphere. */
  protected final Vector3d center = new Vector3d();
  public void setCenter(Vector3d center) { this.center.set(center); }
  
  /** The radius of the sphere. */
  protected double radius = 1.0;
  public void setRadius(double radius) { this.radius = radius; }
  
  protected final double M_2PI = 2*Math.PI;
  
  public Sphere() { }
  
  /**
   * Tests this surface for intersection with ray. If an intersection is found
   * record is filled out with the information about the intersection and the
   * method returns true. It returns false otherwise and the information in
   * outRecord is not modified.
   *
   * @param outRecord the output IntersectionRecord
   * @param ray the ray to intersect
   * @return true if the surface intersects the ray
   */
  public boolean intersect(IntersectionRecord outRecord, Ray rayIn) {
    Vector3d hyp = center.clone().sub(rayIn.origin);
    Vector3d adj = rayIn.direction.clone().normalize().mul(hyp.dot(rayIn.direction));
    Vector3d opp = adj.clone().sub(hyp);
    if(opp.len() > radius) return false;
    double adjLen = Math.pow(radius, 2) - opp.lenSq();
    Vector3d adjShort = adj.clone().normalize().mul(adj.len() - adjLen);
    Vector3d intersect = rayIn.origin.clone().add(adjShort);
    Vector3d norm = adj.clone().mul(-adjLen).add(opp);
	outRecord.location.set(intersect);
	outRecord.normal.set(norm);
	outRecord.surface = this;
	outRecord.t = adjShort.len() / rayIn.direction.len();
	//TODO#A2 : no textures
	
    return true;
  }
  
  /**
   * @see Object#toString()
   */
  public String toString() {
    return "sphere " + center + " " + radius + " " + shader + " end";
  }

}