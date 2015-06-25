package com.book.chapter.nine;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class RobotFilter extends AbstractBufferedImageOp {
	private boolean useA = true;
	
	public RobotFilter()
	{
		System.out.println("Robot Filter");
	}
	
	public boolean isUseA() {
		return useA;
	}

	public void setUseA(boolean useA) {
		this.useA = useA;
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
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				// 计算Robot算子， 使用A模板
				if(isUseA())
				{
					int[] rgb1 = getPixel(inPixels, width, height, col, row);			
					int[] rgb2 = getPixel(inPixels, width, height, col+1, row+1);	
					tr = rgb1[0] - rgb2[0];
					tg = rgb1[1] - rgb2[1];
					tb = rgb1[2] - rgb2[2];
				}
				else
				{ // 使用Robot算子B模板
					int[] rgb1 = getPixel(inPixels, width, height, col+1, row);			
					int[] rgb2 = getPixel(inPixels, width, height, col, row+1);
					tr = rgb1[0] - rgb2[0];
					tg = rgb1[1] - rgb2[1];
					tb = rgb1[2] - rgb2[2];
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
