package com.book.chapter.fourteen;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class MomentsUtil {
	private static final double d02 = 250*250;
	public static int[] getRoi(int[] scaledInput, int xc, int yc, int width, int height, int sw, int sh, int[] wh) {
		int x0 = xc - width/2;
		int y0 = yc - height/2;
		/* adjust region to fit in source image */
		if (x0 < 0) {
			width += x0;
			x0 = 0;
		}
		if (x0 > width) x0 = width;
		if (y0 < 0) {
			height += y0;
			y0 = 0;
		}
		if (y0 > height) y0 = height;
		if ((x0 + width) > sw) {
			width = sw - x0; 
		}
		if ((y0 + height) > sh){
			height = sh - y0; 
		}
		wh[0] = width;
		wh[1] = height;
		int[] roi = new int[width * height];
		int rr = 0;
		int cc = 0;
		for(int row=y0; row<(y0 + height); row++)
		{
			cc = 0;
			for(int col= x0; col < (x0+width); col++)
			{
				int index = row * sw + col;
				roi[rr*width + cc] = scaledInput[index];
				cc++;
			}
			rr++;
		}
		return roi;
	}
	
	public static void calculateMoments(int[] roiArea, int[] xyrgb, double[] mms, int width, int height)
	{
		int y, x;
		int[] outPixels = colorDiff(roiArea, xyrgb, width, height);
	 	Arrays.fill(mms, 0);
	 	int index = 0;
//		*m00 = *m01 = *m10 = *m11 = *m02 = *m20 = 0;

		for (y = 0; y <height; y++) {
			for (x = 0; x <width; x++) {
				index = y*width + x;
				mms[0] += outPixels[index]; // m00
				mms[1] += y * (outPixels[index]); // m01;
				mms[2] += x * (outPixels[index]); // m10;
				mms[3] += y * x * (outPixels[index]); // m11;
				mms[4] += y * y * (outPixels[index]); // m02;
				mms[5] += x * x * (outPixels[index]); // m20;
			}
		}
		
	}
	
	public static int[] colorDiff(int[] in, int[] rgb, int width, int height)
	{
		int i, j;
		double m;
		int[] outPixels = new int[width * height];
		//getRGB(wbi, 0, 0, wbi.getWidth(), wbi.getHeight(), outPixels);
		int index = 0;
		for (i = 0; i < height; i++) {
			for (j = 0; j < width; j++) {
				index = (i* width + j);
				int[] rgb2 = getColorPixels(in, index);
				m = colorDiff(rgb2, rgb);
				outPixels[index] = clamp((int)(m *255.0d));
			}
		}
		return outPixels;
	}
	
	public static int[] getColorPixels(int[] pixels, int index)
	{
		int[] rgb = new int[3];
		rgb[0] = (pixels[index] >> 16) & 0xff;
		rgb[1] = (pixels[index] >> 8) & 0xff;
		rgb[2] = pixels[index] & 0xff;
		return rgb;
	}
	//mom_I
	public static double colorDiff(int[] rgb1, int[] rgb2)
	{
	   // (1-(d/d0)^2)^2
	   double d2, r2;
	   d2 = colorDistance (rgb1, rgb2);

	   if (d2 >= d02)
	      return 0.0;

	   r2 = d2 / d02;

	   return ((1.0d - r2) * (1.0d - r2));
	}
	
	public static double colorDistance(int[] rgb1, int[] rgb2)
	{
	   int dr, dg, db;
	   dr = rgb1[0] - rgb2[0];
	   dg = rgb1[1] - rgb2[1];
	   db = rgb1[2] - rgb2[2];
	   return dr * dr + dg * dg + db * db;
	}
	
	/**
	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
	 * penalty of BufferedImage.getRGB unmanaging the image.
	 */
	public static int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
		return image.getRGB( x, y, width, height, pixels, 0, width );
    }

	/**
	 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
	 * penalty of BufferedImage.setRGB unmanaging the image.
	 */
	public static void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			image.getRaster().setDataElements( x, y, width, height, pixels );
		else
			image.setRGB( x, y, width, height, pixels, 0, width );
    }
	public static int clamp(int p) {
		return p > 255 ? 255 : (p < 0 ? 0: p);
	}
}
