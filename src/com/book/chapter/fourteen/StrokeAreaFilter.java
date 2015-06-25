package com.book.chapter.fourteen;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class StrokeAreaFilter extends AbstractBufferedImageOp {
	
	// default value, optional value 30, 15, 10, 5, 2
	private double size = 10; 
	private static double d02 = 150*150;
	public StrokeAreaFilter() {
	}
	
	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}
	
	public StrokeAreaFilter(int strokeSize) {
		this.size = strokeSize;
	}
	
	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
		int height = src.getHeight();
		int width = src.getWidth();
		// System.out.println("create custom white background color image..."); 
		BufferedImage whiteImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] outPixels = new int[width*height];
		int index = 0;
		for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		outPixels[index] = -1; // white color
        	}
		}
		setRGB( whiteImage, 0, 0, width, height, outPixels );
		return whiteImage;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null ) {
            dest = createCompatibleDestImage( src, null );
        }

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0, index2 = 0;
        int semiRow = (int)(size/2); 
    	int semiCol = (int)(size/2);
    	int newX, newY;
    	
    	// initialize the color RGB array with zero...
    	int[] rgb = new int[3];
    	int[] rgb2 = new int[3];
    	for(int i=0; i<rgb.length; i++) {
    		rgb[i] = rgb2[i] = 0;
    	}
    	
    	// start the algorithm process here!!
        for(int row=0; row<height; row++) {
        	int ta = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (inPixels[index] >> 24) & 0xff;
        		rgb[0] = (inPixels[index] >> 16) & 0xff;
        		rgb[1] = (inPixels[index] >> 8) & 0xff;
        		rgb[2] = inPixels[index] & 0xff;
                
                /* adjust region to fit in source image */
        		// color difference and moment Image
                double moment = 0.0d;
                for(int subRow = -semiRow; subRow <= semiRow; subRow++) {
                	for(int subCol = -semiCol; subCol <= semiCol; subCol++) {
                		newY = row + subRow;
                		newX = col + subCol;
                		if(newY < 0) {
                			newY = 0;
                		}
                		if(newX < 0) {
                			newX = 0;
                		}
                		if(newY >= height) {
                			newY = height-1;
                		}
                		if(newX >= width) {
                			newX = width - 1;
                		}
                		index2 = newY * width + newX;
                		rgb2[0] = (inPixels[index2] >> 16) & 0xff; // red
                		rgb2[1] = (inPixels[index2] >> 8) & 0xff; // green
                		rgb2[2] = inPixels[index2] & 0xff; // blue
                		moment += colorDiff(rgb, rgb2);
                	}
                }
                // calculate the output pixel value.
                int outPixelValue = clamp((int) (255.0d * moment / (size*size)));
                outPixels[index] = (ta << 24) | (outPixelValue << 16) | (outPixelValue << 8) | outPixelValue;
        	}
        }

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
//	public static int clamp(int p) {
//		return p > 255 ? 255 : (p < 0 ? 0: p);
//	}
	
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

}
