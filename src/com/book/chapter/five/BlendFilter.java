package com.book.chapter.five;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BlendFilter extends AbstractBufferedImageOp {

	public final static int MULTIPLY_PIXEL = 1;
	public final static int PLUS_PIXEL = 2;
	public final static int MINUS_PIXEL = 3;
	public final static int INVERSE_PIXEL = 4;
	public final static int INVERSE_PLUS_PIXEL = 5;
	public final static int DIVISION_PIXEL = 6;

	private int MODE;
	private BufferedImage secondImage;

	public BlendFilter() {
		MODE = MULTIPLY_PIXEL;
	}

	public void setBlendMode(int mode) {
		this.MODE = mode;
	}

	public void setSecondImage(BufferedImage image) {
		this.secondImage = image;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		checkImages(src);
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] input1 = new int[width * height];
		int[] input2 = new int[secondImage.getWidth() * secondImage.getHeight()];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, input1);
		getRGB(secondImage, 0, 0, secondImage.getWidth(),
				secondImage.getHeight(), input2);
		int index = 0;
		int ta1 = 0, tr1 = 0, tg1 = 0, tb1 = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta1 = (input1[index] >> 24) & 0xff;
				tr1 = (input1[index] >> 16) & 0xff;
				tg1 = (input1[index] >> 8) & 0xff;
				tb1 = input1[index] & 0xff;
				int[] rgb = getBlendData(tr1, tg1, tb1, input2, row, col);
				outPixels[index] = (ta1 << 24) | (rgb[0] << 16) | (rgb[1] << 8)
						| rgb[2];

			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}

	private int[] getBlendData(int tr1, int tg1, int tb1, int[] input, int row,
			int col) {
		int width = secondImage.getWidth();
		int height = secondImage.getHeight();
		if (col >= width || row >= height) {
			return new int[] { tr1, tg1, tb1 };
		}
		int index = row * width + col;
		int tr = (input[index] >> 16) & 0xff;
		int tg = (input[index] >> 8) & 0xff;
		int tb = input[index] & 0xff;
		int[] rgb = new int[3];
		if (MODE == MULTIPLY_PIXEL) {
			rgb[0] = modeOne(tr1, tr);
			rgb[1] = modeOne(tg1, tg);
			rgb[2] = modeOne(tb1, tb);
		} else if (MODE == PLUS_PIXEL) {
			rgb[0] = modeTwo(tr1, tr);
			rgb[1] = modeTwo(tg1, tg);
			rgb[2] = modeTwo(tb1, tb);
		} else if (MODE == MINUS_PIXEL) {
			rgb[0] = modeThree(tr1, tr);
			rgb[1] = modeThree(tg1, tg);
			rgb[2] = modeThree(tb1, tb);
		} else if (MODE == INVERSE_PIXEL) {
			rgb[0] = modeFour(tr1, tr);
			rgb[1] = modeFour(tg1, tg);
			rgb[2] = modeFour(tb1, tb);
		} else if (MODE == INVERSE_PLUS_PIXEL) {
			rgb[0] = modeFive(tr1, tr);
			rgb[1] = modeFive(tg1, tg);
			rgb[2] = modeFive(tb1, tb);
		} else if (MODE == DIVISION_PIXEL) {
			rgb[0] = modeSix(tr1, tr);
			rgb[1] = modeSix(tg1, tg);
			rgb[2] = modeSix(tb1, tb);
		}
		return rgb;
	}

	private int modeOne(int v1, int v2) {
		return (v1 * v2) / 255;
	}

	private int modeTwo(int v1, int v2) {
		return (v1 + v2) / 2;
	}

	private int modeThree(int v1, int v2) {
		return Math.abs(v1 - v2);
	}

	private int modeFour(double v1, double v2) {
		double p = (int) ((255 - v1) * (255 - v2));
		return (int) (255 - (p / 255));
	}

	private int modeFive(double v1, double v2) {
		int p = (int) (v1 + v2);
		if (p > 255)
			return 0;
		else
			return 255 - p;
	}

	private int modeSix(double v1, double v2) {
		if(v2 == 255)
			return 0;
		double p = (v1 / (255 - v2)) * 255;
		return clamp((int)p);
	}

	private void checkImages(BufferedImage src) {
		int width = src.getWidth();
		int height = src.getHeight();
		if (secondImage == null || secondImage.getWidth() > width
				|| secondImage.getHeight() > height) {
			throw new IllegalArgumentException(
					"the width, height of the input image must be " +
					"great than blend image");
		}
	}

}
