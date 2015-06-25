package com.book.chapter.five;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BitBltFilter extends AbstractBufferedImageOp {
	// raster operation - bit block transfer.
	// 1975 for the Smalltalk-72 system, For the Smalltalk-74 system
	private boolean isTop = true;

	/**
	 * left - top skeleton or right - bottom.
	 * 
	 * @param isTop
	 */
	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}

	/**
	 * blend the pixels and get the final output image
	 * 
	 * @param textImage
	 * @param targetImage
	 */
	public void emboss(BufferedImage textImage, BufferedImage targetImage) {
		// BitBltFilter filter = new BitBltFilter();
		BufferedImage topImage = filter(textImage, null);
		setTop(false);
		BufferedImage buttomImage = filter(textImage, null);

		int width = textImage.getWidth();
		int height = textImage.getHeight();

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(textImage, 0, 0, width, height, inPixels);
		getRGB(topImage, 0, 0, width, height, outPixels);
		processonePixelWidth(width, height, inPixels, outPixels, topImage);
		getRGB(buttomImage, 0, 0, width, height, outPixels);
		processonePixelWidth(width, height, inPixels, outPixels, buttomImage);

		// emboss now
		embossImage(topImage, targetImage, true);
		embossImage(buttomImage, targetImage, false);
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
		int index2 = 0;
		// initialization outPixels
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				outPixels[index] = (255 << 24) | (255 << 16) | (255 << 8) | 255;
			}
		}

		// one pixel transfer
		for (int row = 1; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 1; col < width; col++) {
				index = row * width + col;
				index2 = (row - 1) * width + (col - 1);
				ta = (inPixels[isTop ? index : index2] >> 24) & 0xff;
				tr = (inPixels[isTop ? index : index2] >> 16) & 0xff;
				tg = (inPixels[isTop ? index : index2] >> 8) & 0xff;
				tb = inPixels[isTop ? index : index2] & 0xff;
				outPixels[isTop ? index2 : index] = (ta << 24) | (tr << 16)
						| (tg << 8) | tb;
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}

	/**
	 * 
	 * @param width
	 * @param height
	 * @param inPixels
	 * @param outPixels
	 * @param destImage
	 */
	private void processonePixelWidth(int width, int height, int[] inPixels,
			int[] outPixels, BufferedImage destImage) {
		// now get one pixel data
		int index = 0;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			int ta2 = 0, tr2 = 0, tg2 = 0, tb2 = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;

				ta2 = (outPixels[index] >> 24) & 0xff;
				tr2 = (outPixels[index] >> 16) & 0xff;
				tg2 = (outPixels[index] >> 8) & 0xff;
				tb2 = outPixels[index] & 0xff;

				if (tr2 == tr && tg == tg2 && tb == tb2) {
					outPixels[index] = (255 << 24) | (255 << 16) | (255 << 8)
							| 255;
				} else {
					if (tr2 < 5 && tg2 < 5 && tb2 < 5) {
						outPixels[index] = (ta2 << 24) | (tr2 << 16)
								| (tg2 << 8) | tb2;
					} else {
						outPixels[index] = (255 << 24) | (255 << 16)
								| (255 << 8) | 255;
					}
				}
			}
		}
		setRGB(destImage, 0, 0, width, height, outPixels);
	}

	/**
	 * 
	 * @param src
	 * @param dest
	 * @param colorInverse
	 *            - must be setted here!!!
	 */
	private void embossImage(BufferedImage src, BufferedImage dest,
			boolean colorInverse) {
		int width = src.getWidth();
		int height = src.getHeight();
		int dw = dest.getWidth();
		int dh = dest.getHeight();

		int[] sinPixels = new int[width * height];
		int[] dinPixels = new int[dw * dh];
		src.getRGB(0, 0, width, height, sinPixels, 0, width);
		dest.getRGB(0, 0, dw, dh, dinPixels, 0, dw);
		int index = 0;
		int index2 = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				index = y * width + x;
				int srgb = sinPixels[index];
				int r1 = (srgb >> 16) & 0xff;
				int g1 = (srgb >> 8) & 0xff;
				int b1 = srgb & 0xff;
				if (r1 > 200 || g1 >= 200 || b1 >= 200) {
					continue;
				}
				index2 = y * dw + x;
				if (colorInverse) {
					r1 = 255 - r1;
					g1 = 255 - g1;
					b1 = 255 - b1;
				}
				dinPixels[index2] = (255 << 24) | (r1 << 16) | (g1 << 8) | b1;
			}
		}
		dest.setRGB(0, 0, dw, dh, dinPixels, 0, dw);
	}
}
