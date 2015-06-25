package com.book.chapter.seven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class XShearFilter extends AbstractBufferedImageOp {
	private int outw;
	private int outh;
	private double angle;
	private Color backgroundColor;
	
	public XShearFilter()
	{
		backgroundColor = Color.BLACK;
		this.angle = 45;
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
	public double getAngle() {
		return angle;
	}
	public void setAngle(double angle) {
		this.angle = angle;
	}
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        double angleValue = ((angle)/180.0d) * Math.PI;
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
        		int[] rgb = getPixel(inPixels, width, height, prow, pcol);
        		index = row * outw + col;
        		outPixels[index] = (ta << 24) | (clamp(rgb[0]) << 16) | (clamp(rgb[1]) << 8) | clamp(rgb[2]);  
        	}
        }

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );
        setRGB( dest, 0, 0, outw, outh, outPixels );
        return dest;
	}

	private int[] getPixel(int[] input, int width, int height, 
			double prow, double pcol) {
		double row = Math.floor(prow);
		double col = Math.floor(pcol);
		if(row < 0 || row >= height) {
			return new int[]{backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()};
		}
		if(col < 0 || col >= width) {
			return new int[]{backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue()};
		}
		double u = pcol - col;
		int nextCol = (int)(col + 1);
		if((col + 1) >= width) {
			nextCol = (int)col;
		}
		int index1 = (int)(row * width + col);
		int index2 = (int)(row * width + nextCol);
		
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
