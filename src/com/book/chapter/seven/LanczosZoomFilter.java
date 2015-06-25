package com.book.chapter.seven;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class LanczosZoomFilter extends AbstractBufferedImageOp {
	// lanczos_size
	private float lanczosSize;
	private float destWidth;

	public LanczosZoomFilter() {
		lanczosSize = 3;
		destWidth = 100;
	}

	public LanczosZoomFilter(float lobes, int width) {
		this.lanczosSize = lobes;
		this.destWidth = width;
	}

	public void setLanczosSize(float size) {
		this.lanczosSize = size;
	}

	public void setDestWidth(float destWidth) {
		this.destWidth = destWidth;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();
		float ratio = width / this.destWidth;
		float rcp_ratio = 2.0f / ratio;
		float range2 = (float) Math.ceil(ratio * lanczosSize / 2);

		// destination image
		int dh = (int) (height * (this.destWidth / width));
		int dw = (int) this.destWidth;

		if (dest == null) {
			ColorModel cMD = src.getColorModel();
			dest = new BufferedImage(src.getColorModel(),
					cMD.createCompatibleWritableRaster(dw, dh),
					cMD.isAlphaPremultiplied(), null);
		}

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[dw * dh];

		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		float fcy = 0, icy = 0, fcx = 0, icx = 0;
		for (int row = 0; row < dh; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			fcy = (row + 0.5f) * ratio;
			icy = (float) Math.floor(fcy);
			for (int col = 0; col < dw; col++) {
				fcx = (col + 0.5f) * ratio;
				icx = (float) Math.floor(fcx);

				float sumred = 0, sumgreen = 0, sumblue = 0;
				float totalWeight = 0;
				for (int subcol = (int) (icx - range2); 
						subcol <= icx + range2; subcol++) {
					if (subcol < 0 || subcol >= width)
						continue;
					int ncol = (int) Math.floor(1000 * 
							Math.abs(subcol - fcx));

					for (int subrow = (int) (icy - range2); 
							subrow <= icy + range2; subrow++) {
						if (subrow < 0 || subrow >= height)
							continue;
						int nrow = (int) Math.floor(1000 * 
								Math.abs(subrow - fcy));
						float weight = (float) getLanczosFactor(
								Math.sqrt(Math
								.pow(ncol * rcp_ratio, 2)
								+ Math.pow(nrow * rcp_ratio, 2)) / 1000);
						if (weight > 0) {
							index = (subrow * width + subcol);
							tr = (inPixels[index] >> 16) & 0xff;
							tg = (inPixels[index] >> 8) & 0xff;
							tb = inPixels[index] & 0xff;
							totalWeight += weight;
							sumred += weight * tr;
							sumgreen += weight * tg;
							sumblue += weight * tb;
						}
					}
				}
				index = row * dw + col;
				tr = (int) (sumred / totalWeight);
				tg = (int) (sumgreen / totalWeight);
				tb = (int) (sumblue / totalWeight);
				outPixels[index] = (255 << 24) | (clamp(tr) << 16)
						| (clamp(tg) << 8) | clamp(tb);

				// clear for next pixel
				sumred = 0;
				sumgreen = 0;
				sumblue = 0;
				totalWeight = 0;

			}
		}
		setRGB(dest, 0, 0, dw, dh, outPixels);
		return dest;
	}

	private double getLanczosFactor(double distance) {
		if (distance > lanczosSize)
			return 0;
		distance *= Math.PI;
		if (Math.abs(distance) < 1e-16)
			return 1;
		double xx = distance / lanczosSize;
		return Math.sin(distance) * Math.sin(xx) / distance / xx;
	}

}