package com.book.chapter.thirteen;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class GaussianFilter extends AbstractBufferedImageOp {
	private float a;
	private float[] gaussianKeneral = null;
	
	public GaussianFilter() {
		a = 0.4f;
		generateGaussianKeneral();
	}
	
	public void setCofficience(float a) {
		this.a = a;
	}
	
	public float[][] getHVGaussianKeneral() {
		float[][] hvKeneralData = new float[5][5];
		float sum = 0;
		for(int i=0; i<5; i++)
		{
			for(int j=0; j<5; j++) 
			{
				hvKeneralData[i][j] = gaussianKeneral[i] * gaussianKeneral[j];
				sum += hvKeneralData[i][j] ;
			}
		}
		System.out.println("Sum of Gaussian Keneral Data = " + sum);
		return hvKeneralData;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels);
        blur( inPixels, outPixels, width, height); // H Gaussian
        blur( outPixels, inPixels, height, width); // V Gaussain
        setRGB(dest, 0, 0, width, height, inPixels );
        return dest;
	}

	private void blur(int[] inPixels, int[] outPixels, int width, int height)
	{
		int subCol = 0;
		int index = 0, index2 = 0;
		float redSum=0, greenSum=0, blueSum=0;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		// index = row * width + col;
        		redSum=0;
        		greenSum=0;
        		blueSum=0;
        		for(int m=-2; m<=2; m++) {
        			subCol = col + m;
        			if(subCol < 0 || subCol >= width) {
        				subCol = 0;
        			}
        			index2 = row * width + subCol;
            		ta = (inPixels[index2] >> 24) & 0xff;
                    tr = (inPixels[index2] >> 16) & 0xff;
                    tg = (inPixels[index2] >> 8) & 0xff;
                    tb = inPixels[index2] & 0xff;
                    redSum += (tr * gaussianKeneral[m + 2]);
                    greenSum += (tg * gaussianKeneral[m + 2]);
                    blueSum += (tb * gaussianKeneral[m + 2]);
        		}
        		outPixels[index] = (ta << 24) | (clamp(redSum) << 16) | (clamp(greenSum) << 8) | clamp(blueSum);
        		index += height;
        	}
        }
	}
	
	public static int clamp(float a) {
		return (int)(a < 0 ?  0 : ((a > 255) ? 255 : a));
	}

	private void generateGaussianKeneral() {
		gaussianKeneral = new float[]{0.25f-a/2.0f, 0.25f, a, 0.25f, 0.25f-a/2.0f};
	}
}
