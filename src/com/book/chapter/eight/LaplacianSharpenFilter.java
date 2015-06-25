package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class LaplacianSharpenFilter extends AbstractBufferedImageOp {

	private int[][] kernels= new int[][]{{-1,-1,-1}, {-1,8,-1}, {-1,-1,-1}};
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[][] outPixels = new int[width * height][3];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		int kwRaduis = kernels[0].length/2;
		int khRaduis = kernels.length/2;
		double total = kernels.length * kernels.length;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				float sumRed = 0, sumGreen = 0, sumBlue = 0;
				index = row * width + col;
				// 拉普拉斯卷积操作
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
						tr = (inPixels[index1] >> 16) & 0xff;
						tg = (inPixels[index1] >> 8) & 0xff;
						tb = inPixels[index1] & 0xff;
						sumRed = sumRed + kernels[subRow+khRaduis][subCol+kwRaduis] * tr;
						sumGreen = sumGreen + kernels[subRow+khRaduis][subCol+kwRaduis] * tg;
						sumBlue = sumBlue + kernels[subRow+khRaduis][subCol+kwRaduis] * tb;
					}
				}
				// 得到卷积之后的像素值
				tr = (int)(sumRed / total);
				tg = (int)(sumGreen / total);
				tb = (int)(sumBlue / total);
				outPixels[index][0] = tr;
				outPixels[index][1] = tg;
				outPixels[index][2] = tb;
				
				// clean up for next pixel
				sumRed = 0;
				sumGreen = 0;
				sumBlue = 0;
			}
		}
		
		// 放缩像素值0～255之间
		scalePixels(outPixels);
		// 将拉普拉斯结果与原来像素值叠加
		
		for(int i=0; i<inPixels.length; i++)
		{
			outPixels[i][0] = outPixels[i][0] + ((inPixels[i] >> 16) & 0xff);
			outPixels[i][1] = outPixels[i][1] + ((inPixels[i] >> 8) & 0xff);
			outPixels[i][2] = outPixels[i][2] + (inPixels[i] & 0xff);
		}
		scalePixels(outPixels);
		
		// 像素的直方图拉伸
		int min = 55;
		int max = 200;
		float dynamic = max - min;
		for(int i=0; i<inPixels.length; i++)
		{
			if(outPixels[i][0] >= 200)
			{
				outPixels[i][0] = 255;
			}
			else
			{
				outPixels[i][0] = (outPixels[i][0] <= min) ? 0 : 
					(int)(((outPixels[i][0]-min)/dynamic) * 255.0f);
			}
			if(outPixels[i][1] >= 200)
			{
				outPixels[i][1] = 255;
			}
			else
			{
				outPixels[i][1] = (outPixels[i][1] <= min) ? 0 : 
					(int)(((outPixels[i][1]-min)/dynamic) * 255.0f);
			}
			if(outPixels[i][2] >= 200)
			{
				outPixels[i][2] = 255;
			}
			else
			{
				outPixels[i][2] = (outPixels[i][2] <= min) ? 0 : 
					(int)(((outPixels[i][2]-min)/dynamic) * 255.0f);
			}
		}
		
		// 转换到32位的输出像素
		int[] output = new int[outPixels.length];
		for(int i=0; i<inPixels.length; i++)
		{
			output[i] = (0xff << 24) | (outPixels[i][0] << 16) 
					| (outPixels[i][1] << 8) | outPixels[i][2];
		}
		setRGB(dest, 0, 0, width, height, output);
		return dest;
	}
	
	private void scalePixels(int[][] rgbPxiels)
	{
		// scale to 0~255
		float minRed = 255, minGreen = 255, minBlue = 255;
		float maxRed = 0, maxGreen = 0, maxBlue = 0;
		for(int i=0; i<rgbPxiels.length; i++)
		{
			minRed = Math.min(minRed, rgbPxiels[i][0]);
			minGreen = Math.min(minGreen, rgbPxiels[i][1]);
			minBlue = Math.min(minBlue, rgbPxiels[i][2]);
		}
		System.out.println("Min red = " + minRed + ", min green = " + minGreen + ", min blue = " + minBlue);
		for(int i=0; i<rgbPxiels.length; i++)
		{
			rgbPxiels[i][0] = (int)(rgbPxiels[i][0] - minRed);
			rgbPxiels[i][1] = (int)(rgbPxiels[i][1] - minGreen);
			rgbPxiels[i][2] = (int)(rgbPxiels[i][2] - minBlue);
		}
		// filter max value
		for(int i=0; i<rgbPxiels.length; i++)
		{
			maxRed = Math.max(maxRed, rgbPxiels[i][0]);
			maxGreen = Math.max(maxGreen, rgbPxiels[i][1]);
			maxBlue = Math.max(maxBlue, rgbPxiels[i][2]);
		}
		System.out.println("Max red = " + maxRed + ", Max green = " + maxGreen + ", Max blue = " + maxBlue);
		for(int i=0; i<rgbPxiels.length; i++)
		{
			rgbPxiels[i][0] = (int)(rgbPxiels[i][0] * (255/maxRed));
			rgbPxiels[i][1] = (int)(rgbPxiels[i][1] * (255/maxGreen));
			rgbPxiels[i][2] = (int)(rgbPxiels[i][2] * (255/maxBlue));
		}
	}

}
