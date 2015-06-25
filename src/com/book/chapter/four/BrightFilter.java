package com.book.chapter.four;

import java.awt.image.BufferedImage;

public class BrightFilter extends AbstractBufferedImageOp {
	private float brightness;

	public BrightFilter() {
		this(1.2f);
	}

	public BrightFilter(float bright) {
		this.brightness = bright;
	}
	
	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		src.getRGB(0, 0, width, height, inPixels, 0, width);

		// calculate RED, GREEN, BLUE means of pixel
		int index = 0;
		int[] rgbmeans = new int[3];
		double redSum = 0, greenSum = 0, blueSum = 0;
		double total = height * width;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				redSum += tr;
				greenSum += tg;
				blueSum += tb;
			}
		}

		// get means
		rgbmeans[0] = (int) (redSum / total);
		rgbmeans[1] = (int) (greenSum / total);
		rgbmeans[2] = (int) (blueSum / total);

		// adjust brightness algorithm, here
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;

				// remove means
				tr -= rgbmeans[0];
				tg -= rgbmeans[1];
				tb -= rgbmeans[2];

				// adjust brightness
				tr += (int) (rgbmeans[0] * getBrightness());
				tg += (int) (rgbmeans[1] * getBrightness());
				tb += (int) (rgbmeans[2] * getBrightness());
				outPixels[index] = (ta << 24) | (clamp(tr) << 16)
						| (clamp(tg) << 8) | clamp(tb);
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	public int clamp(int value) {
		return value > 255 ? 255 :
			(value < 0 ? 0 : value);
	}
}
