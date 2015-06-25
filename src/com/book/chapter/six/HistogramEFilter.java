package com.book.chapter.six;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class HistogramEFilter extends AbstractBufferedImageOp{

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        double[][] hsiPixels = new double[3][width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int[] iDataBins = new int[256]; // RGB
        int[] newiBins = new int[256]; // after HE
    	for(int j=0; j<256; j++) {
    		iDataBins[j] = 0;
    		newiBins[j] = 0;
    	}
        
        int index = 0;
        int totalPixelNumber = height * width;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;

                double[] hsi = rgb2HSI(new int[]{tr, tg, tb});
                iDataBins[(int)hsi[2]]++;
                hsiPixels[0][index] = hsi[0];
                hsiPixels[1][index] = hsi[1];
                hsiPixels[2][index] = hsi[2];
        	}
        }
        
        // generate original source image RGB histogram
        generateHEData(newiBins, iDataBins, totalPixelNumber, 256);
        for(int row=0; row<height; row++) {
        	int ta = 255, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;

                // get output pixel now...
        		double h = hsiPixels[0][index];
        		double s = hsiPixels[1][index];
        		double i =  newiBins[(int)hsiPixels[2][index]];
                int[] rgb = hsi2RGB(new double[]{h, s, i});
                tr = clamp(rgb[0]);
                tg = clamp(rgb[1]);
                tb = clamp(rgb[2]);
                outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
        	}
        }
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
	double[] rgb2HSI(int[] rgb)
	{
		double sum = rgb[0] + rgb[1] + rgb[2];
		double r = rgb[0] / sum;
		double g = rgb[1] / sum;
		double b = rgb[2] / sum;
		double s1 = ((r-g) + (r-b))/2.0f;
		double s2 = Math.pow((r-g), 2) + (r-b)*(g-b);
		double s3 = s1/Math.sqrt(s2);
		double h = 0.0f;
		if(b<=g)
		{
			h = Math.acos(s3);			
		}
		else if(b>g)
		{
			h = 2*Math.PI - Math.acos(s3);
		}
		double s = 1 - 3*Math.min(r, Math.min(g, b));
		double i = sum / (255.0 * 3.0);
		
		// 得到输出值
		double H = ((h*180.0)/Math.PI);
		double S = s * 100.0;
		double I = i * 255.0;
		
		return new double[]{H, S, I};		
	}
	
	int[] hsi2RGB(double[] hsi)
	{
		double h = (hsi[0] * Math.PI)/180.0;
		double s = hsi[1] / 100.0;
		double i = hsi[2] /255.0;
		double x = i*(1-s);
		double y = i*(1 + (s*Math.cos(h))/Math.cos(Math.PI/3.0 - h));
		double z = 3*i - (x + y);
		double r=0, g=0, b=0;
		if(h < ((2*Math.PI)/3))
		{
			b = x;
			r = y;
			g = z;
		}
		else if(h >= ((2*Math.PI)/3) && h < ((4*Math.PI)/3))
		{
			h = h - ((2*Math.PI)/3.0);
			x = i*(1-s);
			y = i*(1 + (s*Math.cos(h))/Math.cos(Math.PI/3.0 - h));
			z = 3*i - (x + y);
			r = x;
			g = y;
			b = z;
		}
		else if(h >= ((4*Math.PI)/3) && h < ((2*Math.PI)))
		{
			h = h - ((4*Math.PI)/3.0);
			x = i*(1-s);
			y = i*(1 + (s*Math.cos(h))/Math.cos(Math.PI/3.0 - h));
			z = 3*i - (x + y);
			g = x;
			b = y;
			r = z;
		}
		
		int red = (int)(r * 255);
		int green = (int)(g * 255);
		int blue = (int)(b * 255);
		
		return new int[]{red, green, blue};
	}
	
	
	/**
	 * 
	 * @param newrgbhis
	 * @param rgbhis
	 * @param totalPixelNumber
	 * @param grayLevel [0 ~ 255]
	 */
	private void generateHEData(int[] newrgbhis, int[] rgbhis, int totalPixelNumber, int grayLevel) {
		for(int i=0; i<grayLevel; i++) {
			newrgbhis[i] = getNewintensityRate(rgbhis, totalPixelNumber, i);
		}
	}
	
	private int getNewintensityRate(int[] grayHis, double totalPixelNumber, int index) {
		double sum = 0;
		for(int i=0; i<=index; i++) {
			sum += ((double)grayHis[i])/totalPixelNumber;
		}
		return (int)(sum * 255.0);
	}

}
