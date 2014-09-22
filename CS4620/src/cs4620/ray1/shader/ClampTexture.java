package cs4620.ray1.shader;

import java.awt.image.Raster;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;

/**
 * A Texture class that treats UV-coordinates outside the [0.0, 1.0] range as if they
 * were at the nearest image boundary.
 * @author eschweic
 *
 */
public class ClampTexture extends Texture {

	public Colord getTexColor(Vector2d texCoord) {
		if (image == null) {
			System.err.println("Warning: Texture uninitialized!");
			return new Colord();
		}
		
		double x = texCoord.x;
		double y = texCoord.y;
		
		Raster pixels = image.getRaster();
		int width = pixels.getWidth();
		int height = pixels.getHeight();
		
		int i = (int) (x * width + 0.5);
		if(i < pixels.getMinX()) i = pixels.getMinX();
		if(i >= pixels.getMinX() + pixels.getWidth()) i = pixels.getMinX() + pixels.getWidth() - 1;
		
		int j = (int) (height - y * height + 0.5);
		if(j < pixels.getMinY()) j = pixels.getMinY();
		if(j >= pixels.getMinY() + pixels.getHeight()) j = pixels.getMinY() + pixels.getHeight() - 1;
		
		int rgb = 0;
		try{
			rgb = image.getRGB(i, j);
		} catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Clamp " + i + ", " + j + " failed.");
			e.printStackTrace();
		}
		
		
		Colord outColor = new Colord(Color.fromIntRGB(rgb));
				
		// TODO#A2: Fill in this function.
		// 1) Convert the input texture coordinates to integer pixel coordinates. Adding 0.5
		//    before casting a double to an int gives better nearest-pixel rounding.
		// 2) Clamp the resulting coordinates to the image boundary.
		// 3) Create a Color object based on the pixel coordinate (use Color.fromIntRGB
		//    and the image object from the Texture class), convert it to a Colord, and return it.
		// NOTE: By convention, UV coordinates specify the lower-left corner of the image as the
		//    origin, but the ImageBuffer class specifies the upper-left corner as the origin.

		return outColor;
	}

}
