import egl.math.Matrix3;
import egl.math.Matrix4;
import egl.math.Vector2;
import egl.math.Vector3;


public class WrittenQ {

	public WrittenQ() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Vector2[] square = new Vector2[4];
		Matrix3 a = Matrix3.createRotation((float) (Math.PI / 6));
		Matrix3 b = Matrix3.createTranslation(new Vector2(2, 3));
		Matrix3 c = Matrix3.createScale(new Vector2(1, 5));
		for(int i = 0; i < square.length; i++){
			square[i] = new Vector2(i / 2, i % 2);
			System.out.println("orig: " + square[i]);
		}
		System.out.println("By ABC: ");
		Vector2[] abc = new Vector2[square.length];
		for(int i = 0; i < square.length; i++){
			abc[i] = c.mul(new Vector2(square[i]));
			abc[i] = b.mul(abc[i]);
			abc[i] = a.mul(abc[i]);
			System.out.println(abc[i]);
		}
		System.out.println("By CBA: ");
		Vector2[] cba = new Vector2[square.length];
		for(int i = 0; i < square.length; i++){
			cba[i] = a.mul(new Vector2(square[i]));
			cba[i] = b.mul(abc[i]);
			cba[i] = c.mul(abc[i]);
			System.out.println(cba[i]);
		}
		
		Vector3 axis = new Vector3(-1, -1, -1);
		System.out.println(axis.lenSq());
		axis.normalize();
		Vector3 iNorm = new Vector3(0, 0, 1);
		Vector3 w = (new Vector3(axis)).cross(iNorm);
		System.out.println("Basis: " + axis + ", " + w);
		
		Matrix3 affine1 = new Matrix3(axis, w, new Vector3((float) -1.5, -1, 3));
		affine1.invert();
		Vector3 coeff = new Vector3(-1, -1, 6);
		affine1.mul(coeff);
		System.out.println("Result: " + coeff);
		Vector3 point1 = (new Vector3(axis)).mul(coeff.x).add((new Vector3(w)).mul(coeff.y)).add(1, 1, -6);
		System.out.println("World intersection 1: " + point1);
		
		Matrix3 affine2 = new Matrix3(axis, w, new Vector3((float) .5, 0, 3));
		affine2.invert();
		Vector3 result = new Vector3(-1, -1, 6);
		affine2.mul(result);
		System.out.println("Result: " + result);
		Vector3 point2 = (new Vector3(axis)).mul(result.x).add((new Vector3(w)).mul(result.y)).add(1, 1, -6);
		System.out.println("World intersection 1: " + point2);
		
		System.out.println("Translation:" + -(coeff.x - result.x));
		
		Vector3 x = new Vector3(1, 1, 0).cross(new Vector3(0, 1, -1));
		System.out.println("2: x = " + x);
		Vector3 y = new Vector3(0, 1, -1).cross(x);
		System.out.println("y = " + y +  y.lenSq());
		
		Matrix4 cam = Matrix4.createView(new Vector3(1, 1, 0), new Vector3(0, -1, 1), new Vector3(1, 1, 0));
		Vector3 p = new Vector3(5, -20, 15);
		cam.mulPos(p);
		System.out.println(p);
		Matrix4 proj = Matrix4.createPerspective(10, 10, -5, -30);
		proj.mulPos(p);
		System.out.println(p);
	}
	
	

}
