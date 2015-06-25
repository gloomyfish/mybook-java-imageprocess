package com.book.chapter.nine;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class SobolPrewittEdgeDetector extends AbstractBufferedImageOp {
	public final static int SOBOL_TYPE = 1;
	public final static int PREWITT_TYPE = 2;
	public final static int X_DIRECTION = 4;
	public final static int Y_DIRECTION = 8;
	
	private int type;
	private int direction;
	public SobolPrewittEdgeDetector()
	{
		type = PREWITT_TYPE;
		direction = X_DIRECTION;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
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
		// 每一行、每一列的循环每个像素
		int index = 0;
		// 确定是否为Sobol算子
		int coefficient = (getType() == SOBOL_TYPE) ? 2 : 1;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				// X方向边缘检测
				if(getDirection() == X_DIRECTION)
				{
					int[] a2 = getPixel(inPixels, width, height, col+1, row-1);
					int[] a3 = getPixel(inPixels, width, height, col+1, row);
					int[] a4 = getPixel(inPixels, width, height, col+1, row+1);
					
					int[] a0 = getPixel(inPixels, width, height, col-1, row-1);
					int[] a7 = getPixel(inPixels, width, height, col-1, row);
					int[] a6 = getPixel(inPixels, width, height, col-1, row+1);
					
					tr = (a2[0] + coefficient * a3[0] + a4[0]) - (a0[0] + coefficient * a7[0] + a6[0]);
					tg = (a2[1] + coefficient * a3[1] + a4[1]) - (a0[1] + coefficient * a7[1] + a6[1]);
					tb = (a2[2] + coefficient * a3[2] + a4[2]) - (a0[2] + coefficient * a7[2] + a6[2]);
				}
				else
				{ 	// Y方向边缘检测
					int[] a6 = getPixel(inPixels, width, height, col-1, row+1);			
					int[] a5 = getPixel(inPixels, width, height, col, row+1);
					int[] a4 = getPixel(inPixels, width, height, col+1, row+1);
					
					int[] a0 = getPixel(inPixels, width, height, col-1, row-1);			
					int[] a1 = getPixel(inPixels, width, height, col, row-1);
					int[] a2 = getPixel(inPixels, width, height, col+1, row-1);
					
					tr = (a6[0] + coefficient*a5[0] + a4[0]) - (a0[0]+coefficient*a1[0]+a2[0]);
					tg = (a6[1] + coefficient*a5[1] + a4[1]) - (a0[1]+coefficient*a1[1]+a2[1]);
					tb = (a6[2] + coefficient*a5[2] + a4[2]) - (a0[2]+coefficient*a1[2]+a2[2]);
				}
				// clamp来处理计算后结果
				outPixels[index] = (ta << 24) | (clamp(tr) << 16) | (clamp(tg) << 8) | clamp(tb);
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	private int[] getPixel(int[] inPixels, int width, int height, int col,
			int row) {
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		int tr = (inPixels[index] >> 16) & 0xff;
		int tg = (inPixels[index] >> 8) & 0xff;
		int tb = inPixels[index] & 0xff;
		return new int[]{tr, tg, tb};
	}

}
