package cs4620.ray1.shader;

import java.awt.image.Raster;

import egl.math.Color;
import egl.math.Colord;
import egl.math.Vector2d;

/**
 * A Texture class that repeats the texture image as necessary for UV-coordinates
 * outside the [0.0, 1.0] range.
 * 
 * @author eschweic
 *
 */
public class RepeatTexture extends Texture {

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
		
		int i = (int) ((x * width + 0.5) % width);
		if(i < 0) i += width;
		i += pixels.getMinX();
		int j = (int) ((height - y * height + 0.5) % height);
		if(j < 0) j += height;
		j += pixels.getMinY();
		if(i < 0 || i < pixels.getMinX()){
			i = pixels.getMinX();
		} if (i > width + pixels.getMinX()){
			i = width - 1;
		} if (j < 0 || j < pixels.getMinY()){
			j = pixels.getMinY();
		} if(j > height + pixels.getMinY()){
			j = height - 1;
		}
		int rgb = 0;
		try{
			rgb = image.getRGB(i, j); //ArrayIndexOutOfBounds - teapot, 28%
		} catch(ArrayIndexOutOfBoundsException e){
			System.out.println("Index " + i + ", " + j + " failed.");
			e.printStackTrace();
		}
		Colord outColor = new Colord(Color.fromIntRGB(rgb));
				
		// TODO#A2: Fill in this function.
		// 1) Convert the input texture coordinates to integer pixel coordinates. Adding 0.5
		//    before casting a double to an int gives better nearest-pixel rounding.
		// 2) If these coordinates are outside the image boundaries, modify them to read from
		//    the correct pixel on the image to give a repeating-tile effect.
		// 3) Create a Color object based on the pixel coordinate (use Color.fromIntRGB
		//    and the image object from the Texture class), convert it to a Colord, and return it.
		// NOTE: By convention, UV coordinates specify the lower-left corner of the image as the
		//    origin, but the ImageBuffer class specifies the upper-left corner as the origin.
			
		return outColor;
	}

}
