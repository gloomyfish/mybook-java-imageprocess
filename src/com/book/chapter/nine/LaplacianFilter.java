package com.book.chapter.nine;

import java.awt.image.BufferedImage;

import com.book.chapter.eight.GaussianBlurFilter;

public class LaplacianFilter extends GaussianBlurFilter {
	public final static int[][] LAPLACIAN_OPERATOR = new int[][]{{0,1,0},{1,-4,1},{0,1,0}};
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);
		
		// 5x5 高斯模糊窗口
		this.setN(2);
		this.setSigma(2);
		BufferedImage smoothedImage = super.filter(src, null);
		
		// 拉普拉斯算子提取边缘
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(smoothedImage, 0, 0, width, height, inPixels);
		int index = 0;
		int index1 = 0;
		int sumRed = 0, sumGreen = 0, sumBlue = 0;
		int subSize = LAPLACIAN_OPERATOR.length/2;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				for(int subRow=-subSize; subRow<=subSize; subRow++)
				{
					int nrow = row + subRow;
					if(nrow < 0 || nrow >= height )
					{
						nrow = 0;
					}
					for(int subCol=-subSize; subCol<=subSize; subCol++)
					{
						int ncol = col + subCol;
						if(ncol < 0 || ncol >= width)
						{
							ncol = 0;
						}
						index1 = nrow * width + ncol;
						int tr = (inPixels[index1] >> 16) & 0xff;
						int tg = (inPixels[index1] >> 8) & 0xff;
						int tb = inPixels[index1] & 0xff;
						// 提取边缘
						sumRed = sumRed + (tr * LAPLACIAN_OPERATOR[subRow + subSize][subCol + subSize]);
						sumGreen = sumGreen + (tg * LAPLACIAN_OPERATOR[subRow + subSize][subCol + subSize]);
						sumBlue = sumBlue + (tb * LAPLACIAN_OPERATOR[subRow + subSize][subCol + subSize]);
					}
				}
				// clamp来处理计算后结果
				outPixels[index] = (0xff << 24) | (clamp(sumRed) << 16) | (clamp(sumGreen) << 8) | clamp(sumBlue);
				// 重置为下个像素计算结果
				sumRed = 0;
				sumGreen = 0;
				sumBlue = 0;				
			}
		}
		
		// 图像灰度化，寻找最大最小灰度值
		float min = 255, max = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				int tr = (outPixels[index] >> 16) & 0xff;
				int tg = (outPixels[index] >> 8) & 0xff;
				int tb = outPixels[index] & 0xff;
				tr = tg = tb = (int)(0.299 * (double)tr + 0.587 * (double)tg + 0.114 * (double)tb);
				min = Math.min(min, tr);
				max = Math.max(max, tr);
                outPixels[index] = (0xff << 24) | (tr << 16) | (tg << 8) | tb;           
			}
		}
		
		// 灰度拉伸
		float scale = max - min;
		double sum = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				int gray = (outPixels[index] >> 16) & 0xff;
				if(gray >= max)
				{
					gray = 255;
				}
				else if(gray <= (min+4))// want to remove some noise
				{
					gray = 0;
				}
				else
				{
					gray = (int)((gray-min)*(255.0f/scale));					
				}
				sum += gray;
                outPixels[index] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;           
			}
		}
		
		// 简单的二值化
		int means = (int)(sum / outPixels.length);
		for(int i=0; i<outPixels.length; i++)
		{
			int gray = (outPixels[i] >> 16) & 0xff;
			if(gray <= means)
			{
				gray = 0;
			}
			else
			{
				gray = 255;
			}
			outPixels[i] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;        
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}

}
