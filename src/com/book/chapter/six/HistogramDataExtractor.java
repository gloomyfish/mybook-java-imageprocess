package com.book.chapter.six;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class HistogramDataExtractor extends AbstractBufferedImageOp {
	private int threshold = -1;
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	private int[] histogram;
	
	public int[] getHistogram() {
		return histogram;
	}

	public void setHistogram(int[] histogram) {
		this.histogram = histogram;
	}

	@Override
	public BufferedImage filter(BufferedImage src, 
								BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		
		// get histogram data
		histogram = new int[256];
		for(int i=0; i<histogram.length; i++)
		{
			histogram[i] = 0;
		}
		for (int row = 0; row < height; row++) {
			int tr = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				tr = (inPixels[index] >> 16) & 0xff;
				histogram[tr]++;
			}
		}
		
		if(threshold > 0)
		{
	        // binary image
			int[] outPixels = new int[width*height];
	        for(int row=0; row<height; row++) {
	        	int ta = 0, tr = 0, tg = 0, tb = 0;
	        	for(int col=0; col<width; col++) {
	        		index = row * width + col;
	        		ta = (inPixels[index] >> 24) & 0xff;
	                tr = (inPixels[index] >> 16) & 0xff;
	                tg = (inPixels[index] >> 8) & 0xff;
	                tb = inPixels[index] & 0xff;
	                if(tr >=threshold) {
	                	tr = tg = tb = 255;
	                } else {
	                	tr = tg = tb = 0;
	                }
	                outPixels[index] = (ta << 24) | (tr << 16) 
	                					| (tg << 8) | tb;
	        	}
	        }
	        setRGB( dest, 0, 0, width, height, outPixels );
		}
		return dest;
	}

}
