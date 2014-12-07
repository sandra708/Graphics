package cs4620.ray2;

import cs4620.ray2.camera.PerspectiveCamera;
import cs4620.ray2.shader.Glazed;
import cs4620.ray2.shader.Lambertian;
import cs4620.ray2.surface.Cylinder;
import egl.math.Colord;
import egl.math.Matrix4d;
import egl.math.Vector3d;

public class ReadmeEngine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		prob1();
	}
	
	private static void prob1(){
		Vector3d cameraLoc = new Vector3d(0, 0, 20);
		Vector3d rightLoc = new Vector3d(5, 0, 0);
		Vector3d normal = new Vector3d(1, 0, 0);
		Vector3d view = (new Vector3d(cameraLoc)).sub(rightLoc);
		Vector3d ref = (new Vector3d(normal)).mul(2 * normal.dot(view)).sub(view);
		double phi = Math.acos(ref.y) * 180 / Math.PI;
		double theta = Math.atan(ref.z / ref.x);
		if(ref.z < 0) theta += Math.PI;
		if(theta < 0) theta += Math.PI * 2;
		theta *= 180 / Math.PI;
		System.out.println("View direction: " + view);
		System.out.println("Angle: " + (normal.angle(view) * 180 / Math.PI));
		System.out.println("Reflection: " + ref);
		System.out.println("Theta: " + theta + ", phi: " + phi);
	}
	
	private void simulate(){
		Scene scene = new Scene();
		
		Cylinder mug = new Cylinder();
		mug.setCenter(new Vector3d());
		mug.setHeight(10);
		mug.setRadius(5);
		Matrix4d trans = Matrix4d.createRotationX(Math.PI / 2.0);
		mug.setTransformation(trans, trans.clone().invert(), trans.clone().transpose().invert());
		
		Lambertian graySubShade = new Lambertian();
		graySubShade.setDiffuseColor(new Colord(0.2, 0.2, 0.2));
		Glazed gray = new Glazed();
		gray.setRefractiveIndex(1.5);
		gray.setSubstrate(graySubShade);
		scene.addShader(gray);
		
		Lambertian whiteSubShade = new Lambertian();
		whiteSubShade.setDiffuseColor(new Colord(0.85, 0.85, 0.85));
		Glazed white = new Glazed();
		white.setSubstrate(whiteSubShade);
		white.setRefractiveIndex(1.5);
		scene.addShader(white);
		
		//we don't know how to model steel
		
		PerspectiveCamera camera = new PerspectiveCamera();
		camera.setViewPoint(new Vector3d(0, 0, 20));
		camera.setViewDir(new Vector3d(0, 0, -1));
		camera.setViewUp(new Vector3d(0, 1, 0));
	}

}
