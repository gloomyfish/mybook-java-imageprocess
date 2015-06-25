package com.book.chapter.ten;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.book.chapter.four.AbstractBufferedImageOp;

public class MooreNeighbourTraceAlgorithm extends AbstractBufferedImageOp {
	public final static int P1 = 1;
	public final static int P2 = 2;
	public final static int P3 = 3;
	public final static int P4 = 4;
	public final static int P5 = 5;
	public final static int P6 = 6;
	public final static int P7 = 7;
	public final static int P8 = 8;
	
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
		MyQueue mq = new MyQueue(100000);
		PixelPoint startP = null;
		boolean foundStartP = false;
		// 从左到右
		for (int col = 0; col < width; col++) {
			// 至底向上
			for (int row = height-1; row >= 0; row--) {
				// 获取像素值
				int g1 = getPixel(inPixels, width, height, col, row);
				if(g1 == 0 && startP == null)
				{
					startP = new PixelPoint(row, col, g1);
					mq.enqueue(startP);
					foundStartP = true;
					break;
				}
			}
			if(foundStartP)
			{
				break;
			}
		}
		PixelPoint currentP = null;
		while(!samePixel(currentP, startP))
		{
			if(currentP == null)
			{
				// 初始化开始像素，回退到上一个像素
				int xp = startP.getX();
				int yp = startP.getY();
				yp = yp + 1;
				int gr = getPixel(inPixels, width, height, xp, yp);
				currentP = new PixelPoint(yp, xp, gr);
				currentP.setLabel(P6);
			}
			else
			{
				int position = currentP.getLabel();
				int xp = currentP.getX();
				int yp = currentP.getY();
				// 发现边缘像素
				if(currentP.getValue() == 0)
				{	// 回退
					mq.enqueue(currentP);
					if(position == P1)
					{
						yp = yp + 1;
						position = P6;
					}
					else if(position == P2)
					{
						xp = xp - 1;
						position = P8;
					}
					else if(position == P3)
					{
						xp = xp - 1;
						position = P8;
					}
					else if(position == P4)
					{
						yp = yp - 1;
						position = P2;
					}
					else if(position == P5)
					{
						yp = yp - 1;
						position = P2;
					}
					else if(position == P6)
					{
						xp = xp + 1;
						position = P4;
					}
					else if(position == P7)
					{
						xp = xp + 1;
						position = P4;
					}
					else //  P8
					{
						yp = yp + 1;
						position = P6;
					}
				}
				else
				{	// 非边缘像素，顺时针方向，
					if(position == P1)
					{
						xp = xp + 1;
						position = P2;
					}
					else if(position == P2)
					{
						xp = xp + 1;
						position = P3;
					}
					else if(position == P3)
					{
						yp = yp +1;
						position = P4;
					}
					else if(position == P4)
					{
						yp = yp +1;
						position = P5;
					}
					else if(position == P5)
					{
						xp = xp - 1;
						position = P6;
					}
					else if(position == P6)
					{
						xp = xp - 1;
						position = P7;
					}
					else if(position == P7)
					{
						yp = yp - 1;
						position = P8;
					}
					else
					{
						yp = yp - 1;
						position = P1;
					}
				}
				// 设定当前像素值
				int gr = getPixel(inPixels, width, height, xp, yp);
				currentP = new PixelPoint(yp, xp, gr);
				currentP.setLabel(position);
			}			
		}
		
		// 白色背景
		Arrays.fill(outPixels, -1);
		while(!mq.isEmpty())
		{
			PixelPoint edgePixel = (PixelPoint)mq.dequeue();
			int col = edgePixel.getX();
			int row = edgePixel.getY();
			setPixel(outPixels, width, height, col, row, 0);
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	
	private boolean samePixel(PixelPoint currentP, PixelPoint startP) {
		if(currentP == null)
			return false;
		else
			return (currentP.getX() == startP.getX() && currentP.getY() == startP.getY());
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
