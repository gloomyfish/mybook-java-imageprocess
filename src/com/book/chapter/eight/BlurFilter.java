package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BlurFilter extends AbstractBufferedImageOp {

	private int[][] kernels= new int[][]{{1,1,1}, {1,1,1}, {1,1,1}};
	
	public BlurFilter()
	{
		System.out.println("goldfish-filter");
	}
	
	public void setKernels(int[][] kernels) {
		this.kernels = kernels;
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
		int kwRaduis = kernels[0].length/2;
		int khRaduis = kernels.length/2;
		double total = kernels.length * kernels.length;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				for(int subRow=-khRaduis; subRow<=khRaduis; subRow++)
				{
					int nrow = row + subRow;
					if(nrow < 0 || nrow >= height)
					{
						nrow = 0;
					}
					for(int subCol=-kwRaduis; subCol<=kwRaduis; subCol++)
					{
						int ncol = col + subCol;
						if(ncol < 0 || ncol >= width)
						{
							ncol = 0;
						}
						int index1 = nrow * width + ncol;
						ta = (inPixels[index1] >> 24) & 0xff;
						tr += (inPixels[index1] >> 16) & 0xff;
						tg += (inPixels[index1] >> 8) & 0xff;
						tb += inPixels[index1] & 0xff;
					}
				}
				tr = (int)(tr / total);
				tg = (int)(tg / total);
				tb = (int)(tb / total);
				outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
				
				// clean up for next pixel
				tr = 0;
				tg = 0;
				tb = 0;
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}

}
