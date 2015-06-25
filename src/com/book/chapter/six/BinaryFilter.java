package com.book.chapter.six;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BinaryFilter extends AbstractBufferedImageOp {
	public final static int MEAN_THRESHOLD = 2;
	public final static int SHIFT_THRESHOLD = 4;

	private int thresholdType;

	public BinaryFilter() {
		thresholdType = SHIFT_THRESHOLD;
	}

	public int getThresholdType() {
		return thresholdType;
	}

	public void setThresholdType(int thresholdType) {
		this.thresholdType = thresholdType;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		int means = (int) getThreshold(inPixels, height, width);
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				if (tr > means) {
					tr = tg = tb = 255; // white
				} else {
					tr = tg = tb = 0; // black
				}
				outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}

	private double getThreshold(int[] pixels, int width, int height) {
		int index = 0;
		double mean = 0;
		if (thresholdType == MEAN_THRESHOLD) {
			// calculate mean, MAX, MIN
			int max = 0;
			int min = 255;
			double sum = 0.0;
			for (int row = 0; row < height; row++) {
				int tr = 0;
				for (int col = 0; col < width; col++) {
					index = row * width + col;
					tr = (pixels[index] >> 16) & 0xff;
					min = Math.min(min, tr);
					max = Math.max(max, tr);
					sum += tr;
				}
			}
			mean = sum / (width * height);
		} else if (thresholdType == SHIFT_THRESHOLD) {
			mean = getMeanShiftThreshold(pixels, height, width);
		}
		return mean;
	}

	private static int getMeans(List<Integer> data) {
		int result = 0;
		int size = data.size();
		for (Integer i : data) {
			result += i;
		}
		return (result / size);
	}

	private int getMeanShiftThreshold(int[] inPixels, int height, int width) {
		// maybe this value can reduce the calculation consume;
		int inithreshold = 127;
		int finalthreshold = 0;
		int temp[] = new int[inPixels.length];
		for (int index = 0; index < inPixels.length; index++) {
			temp[index] = (inPixels[index] >> 16) & 0xff;
		}
		List<Integer> sub1 = new ArrayList<Integer>();
		List<Integer> sub2 = new ArrayList<Integer>();
		int means1 = 0, means2 = 0;
		while (finalthreshold != inithreshold) {
			finalthreshold = inithreshold;
			for (int i = 0; i < temp.length; i++) {
				if (temp[i] <= inithreshold) {
					sub1.add(temp[i]);
				} else {
					sub2.add(temp[i]);
				}
			}
			means1 = getMeans(sub1);
			means2 = getMeans(sub2);
			sub1.clear();
			sub2.clear();
			inithreshold = (means1 + means2) / 2;
		}
		long start = System.currentTimeMillis();
		System.out.println("Final threshold  = " + finalthreshold);
		long endTime = System.currentTimeMillis() - start;
		System.out.println("Time consumes : " + endTime);
		return finalthreshold;
	}

}
