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
	Vector3d coeff = (new Vector3d(rayIn.origin)).sub(center);
    double a = rayIn.direction.lenSq();
    double b = 2 * rayIn.direction.dot(coeff);
    double c = coeff.lenSq() - Math.pow(radius, 2);
    
    double t = quad(a, b, c);
    
    if(t == Double.NEGATIVE_INFINITY) return false;
    else if(t < rayIn.start || t > rayIn.end) return false;
    
    Vector3d intersect = (new Vector3d(rayIn.origin)).addMultiple(t, rayIn.direction);
    Vector3d norm = (new Vector3d(intersect)).sub(center).normalize();
    
	outRecord.location.set(intersect);
	outRecord.normal.set(norm);
	outRecord.surface = this;
	outRecord.t = t;
	
	double phi = norm.angle(new Vector3d(0, 1, 0));
	Vector3d normHoriz = (new Vector3d()).set(norm.x, 0, norm.z);
	double theta1 = normHoriz.angle(new Vector3d(0, 0, -1));
	double theta2 = normHoriz.angle(new Vector3d(1, 0, 0));
	double theta;
	
	if(theta2 < Math.PI/2){
		theta = theta1;
	}else{
		theta = theta1 + Math.PI;
	}
	
    outRecord.texCoords.set(phi / Math.PI, theta / (Math.PI * 2));
    
    return true;
  }
  
  //returns smaller positive t-value, if one exists, or
  //negative-infinity if there is no solution
  private double quad(double a, double b, double c){
	  double det = Math.pow(b, 2) - 4 * a * c;
	  if(det < 0) return Double.NEGATIVE_INFINITY;
	  double t1 = ((-b) + Math.sqrt(det))/(2 * a);
	  double t2 = ((-b) - Math.sqrt(det))/(2 * a);
	  double t;
	  if(t1 < 0){
		  if(t2 < 0) return Double.NEGATIVE_INFINITY;
		  return t2;
	  }else{
		  if(t2 < 0) return t1;
		  else{
			  if(t1 < t2) return t1;
			  return t2;
		  }
	  }
  }
  
  /**
   * @see Object#toString()
   */
  public String toString() {
    return "sphere " + center + " " + radius + " " + shader + " end";
  }

}