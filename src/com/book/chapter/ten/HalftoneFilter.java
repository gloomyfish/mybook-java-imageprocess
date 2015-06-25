package com.book.chapter.ten;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class HalftoneFilter extends AbstractBufferedImageOp {
	private float[][] error_dist = new float[][]{{0,0.2f,0},{0.6f,0.1f,0.1f}};
	private float threshold;
	
	public HalftoneFilter()
	{
		threshold = 128;
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
        int drow = error_dist.length;
        int dcol = error_dist[0].length;
		int index = 0;
		float eg = 0; // 总的错误 
		float ep = 0; // 转移到下个像素点的错误分散
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
                int r1 = (inPixels[index] >> 16) & 0xff;
                float tp = r1 + ep;
                if(tp > threshold)
                {
                	outPixels[index] = -1; // 白色
                	eg = tp - 2*threshold;
                }
                else
                {
                	outPixels[index] = 0xff000000; //黑色
                	eg = threshold;
                }
                
                // 错误扩散功能
                for(int sr=0; sr<drow; sr++)
                {
                	int nrow = sr + row;
                	if(nrow >= height)
                	{
                		nrow = 0;
                	}
                	for(int sc=0; sc<dcol; sc++)
                	{
                		int ncol = sc + col;
                		if(ncol >= width)
                		{
                			ncol = 0;
                		}
                		int p = getPixel(inPixels, width, height, ncol, nrow);
                		p = (int)(p + eg * error_dist[sr][sc]);
                		setPixel(inPixels, width, height, ncol, nrow, p);
                	}
                }
                
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
