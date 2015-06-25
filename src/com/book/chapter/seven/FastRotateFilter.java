package com.book.chapter.seven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class FastRotateFilter extends AbstractBufferedImageOp {
	
	private int outw;
	private int outh;
	private double angle;
	private Color backgroundColor;
	
	public FastRotateFilter()
	{
		backgroundColor = Color.BLACK;
		angle = 45;// 45 degree
	}
	public int getOutw() {
		return outw;
	}

	public void setOutw(int outw) {
		this.outw = outw;
	}

	public int getOuth() {
		return outh;
	}

	public void setOuth(int outh) {
		this.outh = outh;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();
		int srcHeight = src.getHeight();
		
		// 第一次错切 - X方向错切
        double angleValue = ((angle/2.0d)/180.0d) * Math.PI;
        outh = height;
        outw = (int)(width + height * Math.tan(angleValue));        	
        System.out.println("after shear, new width : " + outw);
        System.out.println("after shear, new height: " + outh);
        
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[outh*outw];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
        for(int row=0; row<outh; row++) {
        	int ta = 255;
        	for(int col=0; col<outw; col++) {
        		double prow = row;
        		double pcol = col + Math.tan(angleValue) * (row - height);
        		int[] rgb = getNearestPixels(inPixels, width, height, prow, pcol, false);
        		index = row * outw + col;
        		outPixels[index] = (ta << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];  
        	}
        }
        
		// 第二次错切 - Y方向
        int srcWidth = width;
        width = outw;
        height = outh;
        angleValue = ((angle)/180.0d) * Math.PI;
        outw = width;        // big trick!!!!
        outh = (int)(srcWidth * Math.sin(angleValue) 
        		+ height * Math.cos(angleValue));
        int outhh = (int)(srcWidth * Math.sin(angleValue) + height);
        int offsetY = outhh - outh;
        System.out.println("after shear, new width : " + outw);
        System.out.println("after shear, new height: " + outh);
        System.out.println("delta delta Y = " + offsetY);
        int[] yshearOutPixels = new int[outh*outw];
        index = 0;
        for(int row=0; row<outhh; row++) {
        	int ta = 255;
        	for(int col=0; col<width; col++) {
        		double pcol = col;
        		double prow = row - ((col) * Math.sin(angleValue));
        		int[] rgb = getNearestPixels(outPixels, width, height, prow, pcol, true);
        		if((row - offsetY) < 0) continue;
        		index = (row - offsetY) * outw + col;
        		yshearOutPixels[index] = (ta << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];  
        	}
        }
        
		// 第三次错切 - X方向
        width = outw;
        height = outh;
        angleValue = ((angle/2.0d)/180.0d) * Math.PI;
        double fullAngleValue = ((angle)/180.0d) * Math.PI;
        outh = height; // big trick 
        outw = (int)(srcWidth * Math.cos(fullAngleValue) 
        		+ srcHeight * Math.sin(fullAngleValue));
        int outww = (int)(width + height * Math.tan(angleValue)); 
        double offsetX = Math.floor((outww - outw)/2.0d + 0.5d);
        System.out.println("after shear, new width : " + outw);
        System.out.println("after shear, new height: " + outh);
        int[] resultPixels = new int[outh*outw];
        index = 0;
        for(int row=0; row<outh; row++) {
        	int ta = 255;
        	for(int col=0; col<outww; col++) {
        		double prow = row;
        		double pcol = col + Math.tan(angleValue) * (row - height);
        		int[] rgb = getNearestPixels(yshearOutPixels, width, height, prow, pcol, false);
        		if(col - offsetX < 0 || col - outw >= offsetX) continue;
        		index = row * outw + (int)(col - offsetX);
        		resultPixels[index] = (ta << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];  
        	}
        }

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );
        setRGB( dest, 0, 0, outw, outh, resultPixels );
        return dest;
	}
	
	private int[] getNearestPixels(int[] input, int width, int height, 
			double prow, double pcol, boolean yshear) {
		double row = Math.floor(prow);
		double col = Math.floor(pcol);
		if(row < 0 || row >= height) {
			return new int[]{backgroundColor.getRed(), 
					backgroundColor.getGreen(), 
					backgroundColor.getBlue()};
		}
		if(col < 0 || col >= width) {
			return new int[]{backgroundColor.getRed(), 
					backgroundColor.getGreen(), 
					backgroundColor.getBlue()};
		}
		double u = yshear ? prow - row : pcol - col;
		int nextRow = (int)(row + 1);
		int nextCol = (int)(col + 1);
		if((col + 1) >= width) {
			nextCol = (int)col;
		}
		if((row + 1) >= height) {
			nextRow = (int)row;
		}
		int index1 = yshear?(int)(row * width + col) :
			(int)(row * width + col);
		int index2 = yshear?(int)(nextRow * width + col):
			(int)(row * width + nextCol);

		int tr1, tr2;
		int tg1, tg2;
		int tb1, tb2;
		
        tr1 = (input[index1] >> 16) & 0xff;
        tg1 = (input[index1] >> 8) & 0xff;
        tb1 = input[index1] & 0xff;
        
        tr2 = (input[index2] >> 16) & 0xff;
        tg2 = (input[index2] >> 8) & 0xff;
        tb2 = input[index2] & 0xff;
        
        int tr = (int)(tr1 * (1-u) + tr2 * u);
        int tg = (int)(tg1 * (1-u) + tg2 * u);
        int tb = (int)(tb1 * (1-u) + tb2 * u);
        
		return new int[]{tr, tg, tb};
	}
	
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(outw, outh), dstCM.isAlphaPremultiplied(), null);
    }

}
