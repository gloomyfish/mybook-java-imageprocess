package com.book.chapter.fourteen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

public class StrokeGenerator {
	public StrokeGenerator()
	{
		
	}
	
	public List<StrokeElement> getStrokes(int[] inPixels, int[] strokeArea, int S, int factor, int level, int width, int height)
	{
		List<StrokeElement> strokes = new ArrayList<StrokeElement>();
		float scaleFactor = 1 / ((float)factor);
		int scaledWidth = (int)(scaleFactor * (float)width);
		int scaledHeight = (int)(scaleFactor * (float)height);
		ScaleFilter sf = new ScaleFilter();
		sf.setHscale(scaleFactor);
		sf.setVscale(scaleFactor);
		int[] scaledArea = sf.filter(strokeArea, width, height);
		int[] strokePosition = strokePosition(scaledArea, scaledWidth, scaledHeight, 4*S/Math.sqrt(level), 2.0f/level);
		// -- just for debug the stroke position ----
		// BufferedImage bi = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		// setRGB( bi, 0, 0, scaledWidth, scaledHeight, strokePosition);
		// saveImage(bi, "temp_iamge" + level);
		
		// make stroke now!!!
		int index = 0;
		int count = 0;
		int[] scaledInput = sf.filter(inPixels, width, height);
		for (int y = 0; y < scaledHeight; y++) {
			for (int x = 0; x < scaledWidth; x++) {
				index = y * scaledWidth + x;
				if (getGrayPixels(strokePosition, index) == 0) {
					strokes.add(makeStroke(scaledInput, x, y, S, scaledWidth, scaledHeight, factor, level));
					count++;
				}
			}
		}
		return strokes;
	}
	
	public void saveImage(BufferedImage bi, String title) {
    	File outputfile = new File("D:\\" + title + ".png");
    	System.out.println(outputfile.getAbsolutePath());
		try {
			ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private StrokeElement makeStroke(int[] scaledInput, int x, int y, int s, int sw, int sh, int factor, int level/*, BufferedImage wbi*/)
	{
		// declare variables
		double m00, m01, m10, m11, m02, m20;
		double a, b, c;
		double tempval;
		double dw, dxc, dyc;
		
		// calculate moments
		int[] wh = new int[2]; // width, height
		int[] roiArea = MomentsUtil.getRoi(scaledInput, x, y, s, s, sw, sh, wh);
		int[] xyrgb = getColorPixels(scaledInput, (y * sw + x));
		double[] mms = new double[6];
		MomentsUtil.calculateMoments(roiArea, xyrgb, mms, wh[0], wh[1]);
		m00 = mms[0];
		m01 = mms[1];
		m10 = mms[2];
		m11 = mms[3];
		m02 = mms[4];
		m20 = mms[5];
		
		// calculate parameters
 		dxc = m10 / m00;
		dyc = m01 / m00;
		a = (m20 / m00) - (double)((dxc)*(dxc));
		b = 2 * (m11 / m00 - (double)((dxc)*(dyc)));
		c = (m02 / m00) - (double)((dyc)*(dyc));
		double theta = Math.atan2(b, (a-c)) / 2;
		tempval = Math.sqrt(b*b + (a-c)*(a-c));
		dw = Math.sqrt(6 * (a+c - tempval));
		float w = (float)(Math.sqrt(6 * (a+c - tempval)));
		float l = (float)(Math.sqrt(6 * (a+c + tempval)));
		int xc = (int)(x + Math.floor(dxc - s/2));
		int yc = (int)(y + Math.floor(dyc - s/2));
		// factor*xc, factor*yc, factor*w, factor*l, (float) theta, rgb, level
		StrokeElement element = new StrokeElement(factor*xc, factor*yc, factor*w, factor*l, level, (float)theta, xyrgb);
		return element;
	}
	
	public static int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
		return image.getRGB( x, y, width, height, pixels, 0, width );
    }
	
	public static void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			image.getRaster().setDataElements( x, y, width, height, pixels );
		else
			image.setRGB( x, y, width, height, pixels, 0, width );
    }
	
	public int[] strokePosition(int[] inpixels, int width, int height, double s, double p)
	{
		int x, y;
		float error, value;
		float a = (float) ((s-1)/Math.pow(255.0, p));
		Random rand = new Random();
		// initialization the buffer rows
		float[] currow = new float[width];
		float[] nxtrow = new float[width];
		int[] posArea = new int[width * height];
		for (x = 0; x < width; x++)
		{
			nxtrow[x] = Math.max(1.0f/(a*(float)(Math.pow(getGrayPixels(inpixels, x),p))+1), 0.5f);
		}
		// start to dither algorithm
		for(int row=1; row<height-1; row++)
		{
			/* next line becomes current line */
			swap(currow, nxtrow);
			/* copies next line to local buffer */
			nxtrow[0] = Math.max(1.0f/(a*(float)(Math.pow(getGrayPixels(inpixels, row * width), p))+1), 0.5f);
			for (x = 1; x < width; x++) {
				nxtrow[x] = 1.0f/(a*(float)(Math.pow(getGrayPixels(inpixels, (row * width + x)), p))+1);
			}
			/* spread error */
			for (x = 1; x < width-1; x++) {
				value = currow[x] > 1.0f ? 1.0f : 0.0f;
				error = currow[x] - value;
				int gray = value > 0.0 ? 0 : 255;
				posArea[row * width + x] = (255 << 24) | (gray << 16) | (gray << 8) | gray;
				switch (rand.nextInt(100) % 4) {
					case 0:
						nxtrow[x + 1] += error/16;
						nxtrow[x - 1] += 3*error/16;
						nxtrow[x]     += 5*error/16;
						currow[x + 1] += 7*error/16;
						break;
					case 1:
						nxtrow[x + 1] += 7*error/16;
						nxtrow[x - 1] += error/16;
						nxtrow[x]     += 3*error/16;
						currow[x + 1] += 5*error/16;
						break;
					case 2:
						nxtrow[x + 1] += 5*error/16;
						nxtrow[x - 1] += 7*error/16;
						nxtrow[x]     += error/16;
						currow[x + 1] += 3*error/16;
						break;
					case 3:
						nxtrow[x + 1] += 3*error/16;
						nxtrow[x - 1] += 5*error/16;
						nxtrow[x]     += 7*error/16;
						currow[x + 1] += error/16;
						break;
				}
				
			}
		}
		
		// post process for last row pixels
		for (x = 0; x < width; x++)
		{
			int gray = nxtrow[x] > 1.0f ? 0 : 255;
			posArea[(height-1) * width + x] = (255 << 24) | (gray << 16) | (gray << 8) | gray;
		}
		return posArea;
	}
	
	private void swap(float[] currow, float[] nxtrow) {
		for(int i=0; i<currow.length; i++)
		{
			float temp = currow[i];
			currow[i] = nxtrow[i];
			nxtrow[i] = temp;
		}
	}

	private int getGrayPixels(int[] pixels, int index)
	{
		int gray = (pixels[index] >> 16) & 0xff;
		return gray;
	}
	
	public static int[] getColorPixels(int[] pixels, int index)
	{
		int[] rgb = new int[3];
		rgb[0] = (pixels[index] >> 16) & 0xff;
		rgb[1] = (pixels[index] >> 8) & 0xff;
		rgb[2] = pixels[index] & 0xff;
		return rgb;
	}

}
