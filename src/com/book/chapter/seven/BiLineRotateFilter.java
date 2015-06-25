package com.book.chapter.seven;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BiLineRotateFilter extends AbstractBufferedImageOp {
	private double angle;
	private Color background;
	private double specialAngle;
	private int outw;
	private int outh;
	
	public BiLineRotateFilter() {
		this.angle = (45.0d/180.0d) * Math.PI;
		background = Color.BLACK;
		outw = -1;
		outh = -1;
		specialAngle = -1;
	}
	
	public void setDegree(double angle) {
		this.angle = (angle/180.0d) * Math.PI;
		if(angle == 90)
		{
			specialAngle = 90;
		}
		else if(angle == 180)
		{
			specialAngle = 180;
		}
		else if(angle == 270)
		{
			specialAngle = 270;
		}
	}
	
	public void setBackgroud(Color background) {
		this.background = background;
	}
	
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(outw, outh), dstCM.isAlphaPremultiplied(), null);
    }

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        int[] inPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        if(specialAngle > 0)
        {
        	return specialAngleRotate(src, inPixels, width, height);
        }
        outw = (int)(width*Math.cos(angle)+height*Math.sin(angle)); 
        outh = (int)(height*Math.cos(angle)+width*Math.sin(angle));
        System.out.println("after rotate, new width : " + outw);
        System.out.println("after rotate, new height: " + outh);
        
        int[] outPixels = new int[outw*outh];
        int index = 0;
        int centerPixel = inPixels[height/2 * width + width/2];
        
        // calculate new center coordinate
        float centerX = outw / 2.0f + 0.5f;
        float centerY = outh /2.0f + 0.5f;
        
        // calculate the original center coordinate
        float ocenterX = width / 2.0f + 0.5f;
        float ocenterY = height /2.0f + 0.5f;
        
        float rx =0, ry = 0; //after rotated coordinate
        float px = 0, py = 0; // original coordinate
        float prow = 0, pcol = 0;
        for(int row=0; row<outh; row++) {
        	for(int col=0; col<outw; col++) {
        		rx = col - centerX;
        		ry = centerY - row;
        		float fDistance = (float)Math.sqrt(rx * rx + ry * ry);
        		float fPolarAngle = 0; //;
        		if(rx != 0) {
        			fPolarAngle = (float)Math.atan2((double)ry, (double)rx);
        		} else {
        			if(rx == 0) {
        				if(ry == 0) {
        					outPixels[index] = centerPixel;
        					continue; 
        				} 
        				else if(ry < 0) {
            				fPolarAngle = 1.5f * (float)Math.PI;
            			} else {
            				fPolarAngle = 0.5f * (float)Math.PI;
            			}
        			}
        		}
        		
        		// "reverse" rotate, so minus instead of plus
                fPolarAngle -= angle;
                px = fDistance * (float)Math.cos(fPolarAngle);
                py = fDistance * (float)Math.sin(fPolarAngle);

                // get original pixel float point
                prow = ((float)ocenterY) - py;
                pcol = ((float)ocenterX) + px;

                // now start the biline-interpolation algorithm here!!!
                int[] rgb = bilineInterpolation(inPixels, width, height, prow, pcol);
                
                index = row * outw + col;
                outPixels[index] = (255 << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                
        	}
        }

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );
        setRGB( dest, 0, 0, outw, outh, outPixels );
        return dest;
	}

	private BufferedImage specialAngleRotate(BufferedImage src, 
			int[] inPixels, int width, int height) {
        int index = 0;
        int index2 = 0;
		if(specialAngle == 90)
		{
			outw = height;
			outh = width;
		}
		else if(specialAngle == 180)
		{
			outw = width;
			outh = height;
		}
		else if(specialAngle == 270)
		{
			outw = height;
			outh = width;
		}
        int[] outPixels = new int[width * height];
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
        		if(specialAngle == 90)
        		{
        			index2 = outw * col + (height - 1- row);
        		}
        		else if(specialAngle == 180)
        		{
        			index2 = outw * (height - 1 - row) + 
        					(width - 1 - col);
        		}
        		else if(specialAngle == 270)
        		{
        			index2 = outw * (width - 1 - col) + row;
        		}
        		outPixels[index2] = (ta << 24) | 
						(tr << 16) | (tg << 8) | tb;
        	}
        }
        
        BufferedImage dest = createCompatibleDestImage( src, null );
        setRGB( dest, 0, 0, outw, outh, outPixels );
        return dest;
	}

	private int[] bilineInterpolation(int[] input, int width, int height, float prow, float pcol) {
		double row = Math.floor(prow);
		double col = Math.floor(pcol);
		if(row < 0 || row >= height) {
			return new int[]{background.getRed(), background.getGreen(), background.getBlue()};
		}
		if(col < 0 || col >= width) {
			return new int[]{background.getRed(), background.getGreen(), background.getBlue()};
		}
		
		int rowNext = (int)row + 1, colNext = (int)col + 1;
		if((row + 1) >= height) {
			rowNext = (int)row;
		}
		
		if((col + 1) >= width) {
			colNext = (int)col;
		}
		double t = prow - row;
		double u = pcol - col;
		double coffiecent1 = (1.0d-t)*(1.0d-u);
		double coffiecent2 = (t)*(1.0d-u);
		double coffiecent3 = t*u;
		double coffiecent4 = (1.0d-t)*u;
		
		int index1 = (int)(row * width + col);
		int index2 = (int)(row * width + colNext);
		
		int index3 = (int)(rowNext * width + col);
		int index4 = (int)(rowNext * width + colNext);
		int tr1, tr2, tr3, tr4;
		int tg1, tg2, tg3, tg4;
		int tb1, tb2, tb3, tb4;
		
        tr1 = (input[index1] >> 16) & 0xff;
        tg1 = (input[index1] >> 8) & 0xff;
        tb1 = input[index1] & 0xff;
        
        tr2 = (input[index2] >> 16) & 0xff;
        tg2 = (input[index2] >> 8) & 0xff;
        tb2 = input[index2] & 0xff;
        
        tr3 = (input[index3] >> 16) & 0xff;
        tg3 = (input[index3] >> 8) & 0xff;
        tb3 = input[index3] & 0xff;
        
        tr4 = (input[index4] >> 16) & 0xff;
        tg4 = (input[index4] >> 8) & 0xff;
        tb4 = input[index4] & 0xff;

        int tr = (int)(tr1 * coffiecent1 + tr2 * coffiecent4 + tr3 * coffiecent2 + tr4 * coffiecent3);
        int tg = (int)(tg1 * coffiecent1 + tg2 * coffiecent4 + tg3 * coffiecent2 + tg4 * coffiecent3);
        int tb = (int)(tb1 * coffiecent1 + tb2 * coffiecent4 + tb3 * coffiecent2 + tb4 * coffiecent3);

		return new int[]{tr, tg, tb};
	}

}
