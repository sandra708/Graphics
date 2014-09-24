import cs4620.ray1.IntersectionRecord;
import cs4620.ray1.Light;
import cs4620.ray1.Ray;
import cs4620.ray1.Scene;
import cs4620.ray1.shader.Lambertian;
import cs4620.ray1.surface.Sphere;
import egl.math.Colord;
import egl.math.Vector3d;


public class Writeup {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Sphere earth = new Sphere();
		earth.setCenter(new Vector3d(0, 0, 0));
		earth.setRadius(1);
		IntersectionRecord data = new IntersectionRecord();
		Ray camera = new Ray();
		Vector3d dir = new Vector3d(-9.0/13.0, -2.0/13.0, -6.0/13.0);
		//dir.negate();
		camera.set(new Vector3d(450, 100.5, 300), dir);
		camera.makeOffsetSegment(Double.POSITIVE_INFINITY);
		boolean intersect = earth.intersect(data, camera);
		Light sun = new Light();
		sun.setIntensity(new Colord(1e8, 1e8, 1e8));
		sun.setPosition(new Vector3d(0, 0, 23679));
		Sphere moon = new Sphere();
		moon.setCenter(new Vector3d(0, 1, 56.54));
		moon.setRadius(0.27);
		Scene scene = new Scene();
		scene.addLight(sun);
		scene.addSurface(moon);
		scene.addSurface(earth);
		boolean shadow = (new Lambertian()).checkShadow(scene, sun, data, camera);
		
		System.out.println("Center of image:");
		System.out.println("Is on earth : " + intersect);
		System.out.println("x : " + data.location.x);
		System.out.println("y : " + data.location.y);
		System.out.println("z : " + data.location.z);
		
		System.out.println("lat : " + (data.texCoords.x * 180 - 90) + " degrees.");
		System.out.println("long : " + (data.texCoords.y * 360 - 180) + " degrees.");
		System.out.println("This point is shadowed by the moon : " + shadow);
		
		Vector3d toSun = (new Vector3d(sun.position)).sub(data.location);
		double angle = toSun.angle(data.normal);
		//P = (exposure) * Lambertian light
		Vector3d cameraToSun = (new Vector3d(sun.position)).sub(camera.origin);
		double exposure = cameraToSun.lenSq() / 1e8;
		
		Vector3d earthToSun = new Vector3d(sun.position).sub(data.location);
		double rSq = earthToSun.lenSq();
		earthToSun.normalize();
		double lambertian = 0.3 * Math.max(earthToSun.dot(data.normal.normalize()), 0) * 1e8 / rSq;
		double pixel = lambertian * exposure;
		
		System.out.println();
		System.out.println("The sun is " + (angle * 180 / Math.PI) + " degrees below vertical");
		System.out.println("The center pixel has the value " + pixel);
		
	}

}
