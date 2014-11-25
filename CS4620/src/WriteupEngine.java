import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Quat;
import egl.math.Vector4;


public class WriteupEngine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Matrix4 rot0 = Matrix4.createRotationX(0);
		Quat q0 = new Quat(rot0);
		System.out.println("Rot0 quat: " + q0);
		
		Matrix4 rot180 = Matrix4.createRotationX((float) Math.PI);
		Quat q1 = (new Quat(rot180.getAxes()));
		System.out.println("Rot180 quat: " + q1);
		System.out.println(q1.toAxisAngle(new Vector4()));
		
		//Quat help = (q0.clone()).mul(q1);
		double theta = Math.acos(q0.x * q1.x + q0.y * q1.y + q0.z * q1.z + q0.w * q1.w);
		System.out.println(theta);
		
		float coff1 = (float) (Math.sin(0.25 * theta) / Math.sin(theta));
		float coff2 = (float) (Math.sin(0.75 * theta) / Math.sin(theta));
		
		
		Quat qInterp = (q0.clone()).mul(coff1, coff1, coff1, coff1).add((q1.clone()).mul(coff2, coff2, coff2, coff2));
		
		System.out.println("Their interpolation is : " + qInterp);
		System.out.println(qInterp.toAxisAngle(new Vector4()));
		
		Matrix3 m3 = (qInterp).toRotationMatrix(new Matrix3());
		
		//System.out.println(m3);
		
		///
		///
		
		Matrix4 x90 = Matrix4.createRotationX((float) (Math.PI / 2)); 
		Matrix4 y90 = Matrix4.createRotationY((float) (Math.PI / 2));
		Matrix4 z90 = Matrix4.createRotationZ((float) (Math.PI / 2)); 
		
		Matrix4 rot4 = (y90.clone()).mulBefore(x90);
		Matrix4 rot5 = (z90.clone()).mulBefore(y90);
		
		Quat q4 = new Quat(rot4);
		Quat q5 = new Quat(rot5);
		
		System.out.println(rot4);
		System.out.println(q4);
		System.out.println(rot5);
		System.out.println(q5);
		
		double theta45 = Math.acos(q4.x * q5.x + q4.y * q5.y + q4.z * q5.z + q4.w * q5.w);
		
		float coff4 = (float) (Math.sin(theta45 / 4) / Math.sin(theta45));
		float coff5 = (float) (Math.sin(theta45 * 0.75) / Math.sin(theta45));
		
		Quat q40 = (new Quat(q4)).setScaled(coff4, new Quat(q4));
		Quat q50 = (new Quat(q5)).setScaled(coff5, new Quat(q5));
		System.out.println("q4: " + q4 + ", q5: " + q5);
		Quat q6 = (q40).add(q50);
		System.out.println(q6);
		System.out.println(coff4 + ", " + coff5);
		System.out.println(q6.toAxisAngle(new Vector4()) + ", " + q6.toAxisAngle(new Vector4()).w * 180 / Math.PI);
		
		Matrix3 rot6 = q6.toRotationMatrix(new Matrix3());
		Matrix3 rot4i = rot4.getAxes().invert();
		
		Matrix3 rot7 = (new Matrix3(rot6)).mulBefore(rot4i);
		Quat q7 = new Quat(rot7);
		System.out.println(q7 + ", axis-angle: " + q7.toAxisAngle(new Vector4()));
	}

}
