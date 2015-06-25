package com.book.chapter.six;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class PixelStatisticFilter extends AbstractBufferedImageOp {
	private double threshold;
	private boolean blankImage;
	
	public PixelStatisticFilter() {
		blankImage = false;
		threshold = 1.0;
	}
	
	public boolean isBlankImage() {
		return blankImage;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public BufferedImage filter(BufferedImage src, 
			BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();
		blankImage = false;
		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		
		// calculate mean, MAX, MIN
		int max = 0;
		int min = 255;
		double sum = 0.0;
		for (int row = 0; row < height; row++) {
			int tr = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				tr = (inPixels[index] >> 16) & 0xff;
				min = Math.min(min, tr);
				max = Math.max(max, tr);
				sum += tr;
			}
		}
		double mean = sum / (width * height);
		
		// calculate standard deviation
		double stdev = 0.0;
		double total = width * height;
		sum = 0.0;
		for (int row = 0; row < height; row++) {
			int tr = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				tr = (inPixels[index] >> 16) & 0xff;
				sum += tr * tr;
				outPixels[index] = (255 << 24) 
						| (tr << 16) | (tr << 8) | tr;
			}
		}
		stdev = (sum / total) - Math.pow(mean, 2);
		if(stdev <= threshold)
		{
			blankImage = true;
		}
		System.out.println("均值 = " + mean + " 标准方差 = " + stdev);
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}

}
