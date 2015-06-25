package com.book.chapter.eleven;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class GrayDilationFilter extends AbstractBufferedImageOp {

	private int[][] elements;

	public GrayDilationFilter() {
		System.out.println("Gray Image Dilation Filter...");
	}

	public int[][] getElements() {
		return elements;
	}

	public void setElements(int[][] elements) {
		this.elements = elements;
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
		int s = elements[0].length;
		int t = elements.length;
		int max = 0;
		for (int row = 0; row < height; row++) {
			int tg = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				max = 0;
				for(int subRow=0; subRow<t; subRow++)
				{
					int nrow = row - subRow;
					if(nrow < 0 || nrow >= height)
					{
						nrow = 0;
					}
					for(int subCol=0; subCol<s; subCol++)
					{
						int ncol = col - subCol;
						if(ncol < 0 || ncol >= width)
						{
							ncol = 0;
						}
						int index1 = nrow * width + ncol;
						tg = (inPixels[index1] >> 8) & 0xff;
						max = Math.max(tg, max);
					}
				}
				outPixels[index] = (255 << 24) | (max << 16) | (max << 8) | max;
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}

}
