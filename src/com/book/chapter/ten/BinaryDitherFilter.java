package com.book.chapter.ten;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BinaryDitherFilter extends AbstractBufferedImageOp {
	public final static int[][] COLOR_PALETTE = new int[][] {{0, 0, 0}, {255,255,255}};
	public final static int FLOYD_STEINBERG_DITHER = 1;
	public final static int ATKINSON_DITHER = 2;
	private int method;
	
	public BinaryDitherFilter()
	{
		method = FLOYD_STEINBERG_DITHER;
	}
	
	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

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
        		index = row * width + col;
                int r1 = (inPixels[index] >> 16) & 0xff;
                int g1 = (inPixels[index] >> 8) & 0xff;
                int b1 = inPixels[index] & 0xff;
                int cIndex = getCloseColor(r1, g1, b1);
                outPixels[index] = (255 << 24) | (COLOR_PALETTE[cIndex][0] << 16) | 
                		(COLOR_PALETTE[cIndex][1] << 8) | COLOR_PALETTE[cIndex][2];
                // 获取错误
                int[] ergb = new int[3];
                ergb[0] = r1 - COLOR_PALETTE[cIndex][0];
                ergb[1] = g1 - COLOR_PALETTE[cIndex][1];
                ergb[2] = b1 -  COLOR_PALETTE[cIndex][2];
                
                // 错误扩散功能
                if(method == FLOYD_STEINBERG_DITHER)
                {
					float e1=7f/16f;
					float e2=5f/16f;
					float e3=3f/16f;
					float e4=1f/16f;
					int[] rgb1 = getPixel(inPixels, width, height, col+1, row, e1, ergb);
					int[] rgb2 = getPixel(inPixels, width, height, col, row+1, e2, ergb);
					int[] rgb3 = getPixel(inPixels, width, height, col-1, row+1, e3, ergb);
					int[] rgb4 = getPixel(inPixels, width, height, col+1, row+1, e4, ergb);
					setPixel(inPixels, width, height, col+1, row, rgb1);
					setPixel(inPixels, width, height, col, row+1, rgb2);
					setPixel(inPixels, width, height, col-1, row+1, rgb3);
					setPixel(inPixels, width, height, col+1, row+1, rgb4);
                }
                else if(method == ATKINSON_DITHER)
                {
					float e1=0.125f;
					int[] rgb1 = getPixel(inPixels, width, height, col+1, row, e1, ergb);
					int[] rgb2 = getPixel(inPixels, width, height, col+2, row, e1, ergb);
					int[] rgb3 = getPixel(inPixels, width, height, col-1, row+1, e1, ergb);
					int[] rgb4 = getPixel(inPixels, width, height, col, row+1, e1, ergb);
					int[] rgb5 = getPixel(inPixels, width, height, col+1, row+1, e1, ergb);
					int[] rgb6 = getPixel(inPixels, width, height, col, row+2, e1, ergb);
					setPixel(inPixels, width, height, col+1, row, rgb1);
					setPixel(inPixels, width, height, col+2, row, rgb2);
					setPixel(inPixels, width, height, col-1, row+1, rgb3);
					setPixel(inPixels, width, height, col, row+1, rgb4);
					setPixel(inPixels, width, height, col+1, row+1, rgb5);
					setPixel(inPixels, width, height, col, row+2, rgb6);
                }
                else
                {
                	throw new java.lang.IllegalArgumentException("Not Supported Dither Mothed!!"); 
                }
                
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	private int getCloseColor(int tr, int tg, int tb) {
		int minDistanceSquared = 255*255 + 255*255 + 255*255 + 1;
		int bestIndex = 0;
		for(int i=0; i<COLOR_PALETTE.length; i++) {
			int rdiff = tr - COLOR_PALETTE[i][0];
			int gdiff = tg - COLOR_PALETTE[i][1];
			int bdiff = tb - COLOR_PALETTE[i][2];
			int distanceSquared = rdiff*rdiff + gdiff*gdiff + bdiff*bdiff;
			if(distanceSquared < minDistanceSquared) {
				minDistanceSquared = distanceSquared;
				bestIndex = i;
			}
		}

		return bestIndex;
	}
	
	private void setPixel(int[] input, int width, int height, int col, int row, int[] p)
	{
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		input[index] = (0xff << 24) | (clamp(p[0]) << 16) | (clamp(p[1]) << 8) | clamp(p[2]);
	}
	
	private int[] getPixel(int[] input, int width, int height, int col,
			int row, float error, int[] ergb) {
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
        int tr = (input[index] >> 16) & 0xff;
        int tg = (input[index] >> 8) & 0xff;
        int tb = input[index] & 0xff;
        tr = (int)(tr + error * ergb[0]);
        tg = (int)(tg + error * ergb[1]);
        tb = (int)(tb + error * ergb[2]);
		return new int[]{tr, tg, tb};
	}

}
