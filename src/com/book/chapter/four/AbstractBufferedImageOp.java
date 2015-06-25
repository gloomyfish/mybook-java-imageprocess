/*
** Copyright 2005 Huxtable.com. All rights reserved.
*/

package com.book.chapter.four;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

import com.book.image.util.ImageMath;

/**
 * A convenience class which implements those methods of BufferedImageOp which are rarely changed.
 */
public abstract class AbstractBufferedImageOp implements BufferedImageOp {
	
    public final static double c1o60  = 1.0 / 60.0;
    public final static double c1o255 = 1.0 / 255.0;

    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }
    
    public Rectangle2D getBounds2D( BufferedImage src ) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }
    
    public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
        if ( dstPt == null )
            dstPt = new Point2D.Double();
        dstPt.setLocation( srcPt.getX(), srcPt.getY() );
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }
    
	public int clamp(int value) {
		return value > 255 ? 255 :
			(value < 0 ? 0 : value);
	}
	
	public double[] rgb2Hsl(int[] rgb)
	{
        double min, max, dif, sum;
        double f1, f2;
        double h, s, l;
        // convert to HSL space
        min = rgb[0];
        if (rgb[1] < min) 
        	min = rgb[1];
        if (rgb[2] < min) 
        	min = rgb[2];
        max = rgb[0];
        f1 = 0.0;
        f2 = rgb[1] - rgb[2];
        if (rgb[1] > max) {
           max = rgb[1];
           f1 = 120.0;
           f2 = rgb[2] - rgb[0];
        }
        if (rgb[2] > max) {
           max = rgb[2];
           f1 = 240.0;
           f2 = rgb[0] - rgb[1];
        }
        dif = max - min;
        sum = max + min;
        l = 0.5 * sum;
        if (dif == 0) {
        	h = 0.0;
        	s = 0.0;
        } 
        else if(l < 127.5) {
        	s = 255.0 * dif / sum;
        }
        else {
        	s = 255.0 * dif / (510.0 - sum);
        }

        h = (f1 + 60.0 * f2 / dif);
        if (h < 0.0) { 
        	h += 360.0;
        }
        if (h >= 360.0) {
        	h -= 360.0;
        }
        
        return new double[]{h, s, l};
	}
	
	public int[] hsl2RGB(double[] hsl)
	{
        // conversion back to RGB space here!!
		int tr = 0, tg = 0, tb = 0;
		double v1, v2, v3, h1;
		double s = hsl[1], l = hsl[2];
		double h = hsl[0];
        if (s == 0) {
           tr = (int)l;
           tg = (int)l;
           tb = (int)l;
        } else {
        
           if (l < 127.5) {
              v2 = c1o255 * l * (255 + s);
           } else {
              v2 = l + s - c1o255 * s * l;
           }
           
           v1 = 2 * l - v2;
           v3 = v2 - v1;
           h1 = h + 120.0;
           if (h1 >= 360.0) 
        	   h1 -= 360.0;
           
           if (h1 < 60.0) {
              tr = (int)(v1 + v3 * h1 * c1o60);
           }
           else if (h1 < 180.0) {
              tr = (int)v2;
           }
           else if (h1 < 240.0) {
              tr = (int)(v1 + v3 * (4 - h1 * c1o60));
           }
           else {
              tr = (int)v1;
           }
           
           h1 = h;
           if (h1 < 60.0) {
              tg = (int)(v1 + v3 * h1 * c1o60);
           }
           else if (h1 < 180.0) {
              tg = (int)v2;
           } 
           else if (h1 < 240.0) {
              tg = (int)(v1 + v3 * (4 - h1 * c1o60));
           }
           else {
              tg = (int)v1;
           }
           
           h1 = h - 120.0;
           if (h1 < 0.0) {
        	   h1 += 360.0;
           }
           if (h1 < 60.0) {
              tb = (int)(v1 + v3 * h1 * c1o60);
           }
           else if (h1 < 180.0) {
              tb = (int)v2;
           }
           else if (h1 < 240.0) {
              tb = (int)(v1 + v3 * (4 - h1 * c1o60));
           }
           else {
              tb = (int)v1;
           }
        }
        return new int[]{tr, tg, tb};
	}

	/**
	 * A convenience method for getting ARGB pixels from an image. This tries to avoid the performance
	 * penalty of BufferedImage.getRGB unmanaging the image.
	 */
	public int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
		return image.getRGB( x, y, width, height, pixels, 0, width );
    }

	/**
	 * A convenience method for setting ARGB pixels in an image. This tries to avoid the performance
	 * penalty of BufferedImage.setRGB unmanaging the image.
	 */
	public void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
		int type = image.getType();
		if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
			image.getRaster().setDataElements( x, y, width, height, pixels );
		else
			image.setRGB( x, y, width, height, pixels, 0, width );
    }
	
    
	public static float[] makeKernel(float radius) {
		int r = (int)Math.ceil(radius);
		int rows = r*2+1;
		float[] matrix = new float[rows];
		float sigma = radius/3;
		float sigma22 = 2*sigma*sigma;
		float sigmaPi2 = 2*ImageMath.PI*sigma;
		float sqrtSigmaPi2 = (float)Math.sqrt(sigmaPi2);
		float radius2 = radius*radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++) {
			float distance = row*row;
			if (distance > radius2)
				matrix[index] = 0;
			else
				matrix[index] = (float)Math.exp(-(distance)/sigma22) / sqrtSigmaPi2;
			total += matrix[index];
			index++;
		}
		for (int i = 0; i < rows; i++)
			matrix[i] /= total;

		return matrix;
	}
}
