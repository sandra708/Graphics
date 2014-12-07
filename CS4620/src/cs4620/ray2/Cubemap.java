package cs4620.ray2;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import egl.math.Vector2d;
import egl.math.Vector3d;
import egl.math.Colord;

public class Cubemap {

	// Parameters
	String filename;
	double scaleFactor = 1.0;

	int width, height, blockSz;
	int mapBits; // 2^(mapBits-1) < width*height <= 2^mapBits
	float[] imageData;
	float[] cumProb;

	Vector2d faceUV = new Vector2d();

	public Cubemap() { }

	public void setFilename(String filename) {
		this.filename = filename;

		PNMHeaderInfo hdr = new PNMHeaderInfo();
		imageData = readPFM(new File(filename), hdr);

		width = hdr.width;
		height = hdr.height;
		blockSz = width / 3;

		for (mapBits = 0; (1 << mapBits) < width*height; mapBits++);

		cumProb = new float[width*height+1];
		cumProb[0] = 0;

		for (int k = 1; k <= width*height; k++)
			cumProb[k] = cumProb[k-1] + calcPixelProb(k-1);

		for (int k = 1; k <= width*height; k++)
			cumProb[k] /= cumProb[width*height];
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}


	public void evaluate(Vector3d dir, Colord outRadiance) {
		Vector3d uv = (new Vector3d(dir)).normalize();
		
		Vector3d abs = new Vector3d(uv);
		abs.abs();
		
		//the face is determined by which component has the 
		//greatest absolute value, and the sign of that component
		int face; //-z, -x, -y, z, x, y map to 0..5
		double angle; //angle that the incoming vector makes with the center of that face
		
		if(abs.x > abs.y){
			if(abs.x > abs.z){
				if(uv.x > 0){
					face = 4;
					angle = uv.angle(new Vector3d(1, 0, 0));
				}else{
					face = 1;
					angle = uv.angle(new Vector3d(-1, 0, 0));
				}
			}else{
				if(uv.z > 0){
					face = 3;
					angle = uv.angle(new Vector3d(0, 0, 1));
				}else{
					face = 0;
					angle = uv.angle(new Vector3d(0, 0, -1));
				}
			}
		}else{
			if(abs.y > abs.z){
				if(uv.y > 0){
					face = 5;
					angle = uv.angle(new Vector3d(0, 1, 0));
				}else{
					face = 2;
					angle = uv.angle(new Vector3d(0, -1, 0));
				}
			}else{
				if(uv.z > 0){
					face = 3;
					angle = uv.angle(new Vector3d(0, 0, -1));
				}else{
					face = 0;
					angle = uv.angle(new Vector3d(0, 0, 1));
				}
			}
		}
		//adjust so the non-face numbers come out in texture space
		//uv.mul(Math.sin(angle));
		uv.mul(Math.sqrt(3));
		uv.mul(0.5);
		uv.add(0.5);
		Colord uvC = (new Colord(uv));
		uvC.clamp(0.0, 1.0);
		uv = new Vector3d(uvC);
		
		Vector2d texUV; //image coordinates 
		
		switch(face){
		case 0: //-z
		case 3: //+z
			texUV = new Vector2d(uv.x, uv.y); break;
		case 1: //-x
		case 4: //+x
			texUV = new Vector2d(uv.y, uv.z); break;
		case 2: //-y
		case 5: //+y
			texUV = new Vector2d(uv.x, uv.z); break;
		default:
			texUV = new Vector2d();
		}
		//convert to pixels
		texUV.mul(blockSz);
		//convert to whole image
		switch(face){
		case 0: //-z
			texUV.add(blockSz, 2 * blockSz); break;
		case 3: //+z
			texUV.add(blockSz, 0); break;
		case 1: //-x
			texUV.add(0, 2 * blockSz); break;
		case 4: //+x
			texUV.add(2 * blockSz, 2 * blockSz); break;
		case 2: //-y
			texUV.add(blockSz, blockSz); break;
		case 5: //+y
			texUV.add(blockSz, 3 * blockSz); break;
		}
		
		int index = (int) Math.round(3 * (texUV.x + width * texUV.y));
		if(index > imageData.length - 3) index = imageData.length - 3;
		if(index < 0) index = 0;
		
		float R = imageData[index];
		float G = imageData[index + 1];
		float B = imageData[index + 2];
		
		outRadiance.set(R, G, B);
		outRadiance.mul(scaleFactor);
		
		//TODO#A7 Look up for the radiance of the environment mapping in a given direction
		//don't forget to multiply the outRadiance by scaleFactor
		
	}
	
	public void generate(Vector2d seed, Vector3d outDirection) {

		// choose a pixel
		double searchProb = seed.x;
		int k = 0;

		for (int p = mapBits-1; p >= 0; p--)
			if (searchProb > cumProb[k + (1 << p)])
				k += (1 << p);

		double pixelProb = cumProb[k + 1] - cumProb[k];
		seed.x = (searchProb - cumProb[k]) / pixelProb;

		// choose u and v randomly in that pixel.  faceUV is the pixel center.
		int iFace = indexToFace(k, faceUV);
		faceUV.x += (2 * seed.x - 1) / blockSz;
		faceUV.y += (2 * seed.y - 1) / blockSz;

		// choose the direction based on face index and (u,v)
		faceToDir(iFace, faceUV, outDirection);
	}

	protected int indexToFace(int index, Vector2d outFaceUV) {
		
		// Table of which face is at each position in the 3x4 grid of the map
		final int[][] locFace = { {-1, 4, -1}, { -1, 3, -1}, {1, 5, 0}, {-1, 2, -1} };

		// (ix, iy) are the pixel coords in the whole map
		int ix = index % width;
		int iy = index / width;
		int iFace = locFace[iy / blockSz][ix / blockSz];

		// (iu, iv) are the pixel coords within a face
		int iu = ix % blockSz;
		int iv = iy % blockSz;

		outFaceUV.set(2 * (iu + 0.5) / (double) blockSz - 1, 2 * (iv + 0.5) / (double) blockSz - 1);

		return iFace;
	}

	protected void faceToDir(int iFace, Vector2d faceUV, Vector3d outDir) {
		double u = faceUV.x;
		double v = faceUV.y;

		switch (iFace) {
		case 0:
			outDir.set(1, v, u);
			break;
		case 1:
			outDir.set(-1, v, -u);
			break;
		case 2:
			outDir.set(u, 1, v);
			break;
		case 3:
			outDir.set(u, -1, -v);
			break;
		case 4:
			outDir.set(u, -v, 1);
			break;
		case 5:
			outDir.set(u, v, -1);
			break;
		}

		outDir.normalize();
	}

	protected float calcPixelProb(int k) {
		if (indexToFace(k, faceUV) == -1) return 0;

		float r = imageData[0 + 3*k];
		float g = imageData[1 + 3*k];
		float b = imageData[2 + 3*k];

		double u = faceUV.x;
		double v = faceUV.y;

		return Math.max(Math.max(r, g), b) / (float) Math.pow(1 + u*u + v*v, 1.5);
	}

	public static class PNMHeaderInfo { 
		int width, height, bands;
		float maxval; 
	}

	public float[] readPFM(File pfmFile, PNMHeaderInfo hdr) {
		
		try {
			FileInputStream inf = new FileInputStream(pfmFile);
			DataInputStream inSt = new DataInputStream(inf);
			FileChannel inCh = inf.getChannel();

			int imageSize = readPPMHeader(inSt, hdr);

			if (imageSize == -1) return null;

			//System.err.println("reading FP image: " + hdr.width + "x" + hdr.height + "x" + hdr.bands);

			ByteBuffer imageBuffer = ByteBuffer.allocate(imageSize * 4);
			imageBuffer.order(ByteOrder.LITTLE_ENDIAN);
			imageBuffer.clear();
			inCh.read(imageBuffer);

			float[] imageData = new float[imageSize];
			imageBuffer.flip();
			imageBuffer.asFloatBuffer().get(imageData);

			return imageData;
		} catch (FileNotFoundException e) {
			System.err.println("readPFM: file not found: " + pfmFile.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static int readPPMHeader(DataInputStream in, PNMHeaderInfo info) throws IOException {
		
		// Read PNM header of the form 'P[F]\n<width> <height>\n<maxval>\n'
		if (in.readByte() != 'P') {
			System.err.println("readPFM: not a PNM file");
			return -1;
		}

		byte magic = in.readByte();
		int bands;

		if (magic == 'F') bands = 3;
		else {
			System.err.println("readPFM: Unsupported PNM variant 'P" + magic + "'");
			return -1;
		}

		int width = Integer.parseInt(readWord(in));
		int height = Integer.parseInt(readWord(in));
		int imageSize = width * height * bands;
		float maxval = Float.parseFloat(readWord(in));

		if (info != null) {
			info.width = width;
			info.height = height;
			info.bands = bands;
			info.maxval = maxval;
		}

		return imageSize;
	}

	static String readWord(DataInputStream in) throws IOException {
		char c;
		String s = "";

		while (Character.isWhitespace(c = (char) in.readByte()))
			;
		s += c;
		while (!Character.isWhitespace(c = (char) in.readByte()))
			s += c;

		return s;
	}
}
