package cs4620.splines;

import egl.math.Matrix4;
import egl.math.Vector4;

public class WriteupHelper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Vector4 col0 = new Vector4(1, 0, 0, 0);
		Vector4 col1 = new Vector4(4, 4, 2, 1);
		Vector4 col2 = new Vector4(1, 2, 4, 4);
		Vector4 col3 = new Vector4(0, 0, 0, 1);
		Matrix4 converter = new Matrix4(col0.mul(1/6.0f), col1.mul(1/6.0f), col2.mul(1/6.0f), col3.mul(1/6.0f));
		converter.transpose();
		
		Vector4 xs = new Vector4(0, 1, 2, 6);
		Vector4 ys = new Vector4(0, 1, 3, -2);
		
		System.out.println(converter.mul(xs));
		System.out.println(converter.mul(ys));
		System.out.println(ys.pow(-1));
	}

}
