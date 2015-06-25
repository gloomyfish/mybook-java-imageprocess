package com.book.chapter.nine;

import java.awt.image.BufferedImage;

public class GradientEdgeFilter extends SobolPrewittEdgeDetector {
	
	private int threshold = 127;
	public GradientEdgeFilter()
	{
		System.out.println("图像梯度边缘提取法...");
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		BufferedImage xImage = super.filter(src, null);
		this.setDirection(Y_DIRECTION);
		BufferedImage yImage = super.filter(src, null);
		
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);
		int[] dxPixels = new int[width * height];
		int[] dyPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(xImage, 0, 0, width, height, dxPixels);
		getRGB(yImage, 0, 0, width, height, dyPixels);
		int index = 0;
		double mred, mgreen, mblue;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
                int xred = (dxPixels[index] >> 16) & 0xff;
                int xgreen = (dxPixels[index] >> 8) & 0xff;
                int xblue = dxPixels[index] & 0xff;

                int yred = (dyPixels[index] >> 16) & 0xff;
                int ygreen = (dyPixels[index] >> 8) & 0xff;
                int yblue = dyPixels[index] & 0xff;
                
                mred = Math.sqrt(xred * xred + yred * yred);
                mgreen = Math.sqrt(xgreen * xgreen + ygreen * ygreen);
                mblue = Math.sqrt(xblue * xblue + yblue * yblue);
                
                int tr = clamp((int)mred);
                int tg = clamp((int)mgreen);
                int tb = clamp((int)mblue);
                outPixels[index] = (0xff << 24) | (tr << 16) | (tg << 8) | tb;           
			}
		}
		
		// 根据阈值细化边缘
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				int tr = (outPixels[index] >> 16) & 0xff;
				int tg = (outPixels[index] >> 8) & 0xff;
				int tb = outPixels[index] & 0xff;
				tr = tg = tb = (int)(0.299 * (double)tr + 0.587 * (double)tg + 0.114 * (double)tb);
				if(tr < threshold || tg < threshold || tb < threshold)
				{
					tr = tg = tb = 0;
				}
				else
				{
					tr = tg = tb = 255;
				}
                outPixels[index] = (0xff << 24) | (tr << 16) | (tg << 8) | tb;           
			}
		}
        
		setRGB(dest, 0, 0, width, height, outPixels );
        return dest;
	}
}
