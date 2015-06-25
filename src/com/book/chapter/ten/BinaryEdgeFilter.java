package com.book.chapter.ten;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;
/**
 * 基于拉普拉斯算子实现
 * @author fish
 *
 */
public class BinaryEdgeFilter extends AbstractBufferedImageOp {
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);
		//初始化，获取输入图像像素数组
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				int g1 = getPixel(inPixels, width, height, col-1, row-1);
				int g2 = getPixel(inPixels, width, height, col, row-1);
				int g3 = getPixel(inPixels, width, height, col+1, row-1);
				int g4 = getPixel(inPixels, width, height, col-1, row+1);
				int g5 = getPixel(inPixels, width, height, col, row+1);
				int g6 = getPixel(inPixels, width, height, col+1, row+1);
				int g7 = getPixel(inPixels, width, height, col-1, row);
				int g8 = getPixel(inPixels, width, height, col+1, row);
				int g0 = getPixel(inPixels, width, height, col, row);
				int sum = g0*8-g1-g2-g3-g4-g5-g6-g7-g8;
				setPixel(outPixels, width, height, col, row, sum);
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	private void setPixel(int[] input, int width, int height, int col, int row, int p)
	{
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		input[index] = (0xff << 24) | (clamp(p) << 16) | (clamp(p) << 8) | clamp(p);
	}
	
	private int getPixel(int[] input, int width, int height, int col,
			int row) {
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		int tr = (input[index] >> 16) & 0xff;
		return tr;
	}

}
