package com.book.chapter.twelves;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class OtsuBinaryFilter extends AbstractBufferedImageOp {
	
	public OtsuBinaryFilter()
	{
		System.out.println("Otsu Threshold Binary Filter...");
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );
        // 图像灰度化
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
				int gray= (int)(0.299 *tr + 0.587*tg + 0.114*tb);
				inPixels[index]  = (ta << 24) | (gray << 16) | (gray << 8) | gray;
        	}
        }
        // 获取直方图
        int[] histogram = new int[256];
        for(int row=0; row<height; row++) {
        	int tr = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
                tr = (inPixels[index] >> 16) & 0xff;
                histogram[tr]++;
        	}
        }
        // 图像二值化 - OTSU 阈值化方法
        double total = width * height;
        double[] variances = new double[256];
        for(int i=0; i<variances.length; i++)
        {
        	double bw = 0;
        	double bmeans = 0;
        	double bvariance = 0;
        	double count = 0;
        	for(int t=0; t<i; t++)
        	{
        		count += histogram[t];
        		bmeans += histogram[t] * t;
        	}
        	bw = count / total;
        	bmeans = (count == 0) ? 0 :(bmeans / count);
        	for(int t=0; t<i; t++)
        	{
        		bvariance += (Math.pow((t-bmeans),2) * histogram[t]);
        	}
        	bvariance = (count == 0) ? 0 : (bvariance / count);
        	double fw = 0;
        	double fmeans = 0;
        	double fvariance = 0;
        	count = 0;
        	for(int t=i; t<histogram.length; t++)
        	{
        		count += histogram[t];
        		fmeans += histogram[t] * t;
        	}
        	fw = count / total;
        	fmeans = (count == 0) ? 0 : (fmeans / count);
        	for(int t=i; t<histogram.length; t++)
        	{
        		fvariance += (Math.pow((t-fmeans),2) * histogram[t]);
        	}
        	fvariance = (count == 0) ? 0 : (fvariance / count);
        	variances[i] = bw * bvariance + fw * fvariance;
        }

        // find the minimum within class variance
        double min = variances[0];
        int threshold = 0;
        for(int m=1; m<variances.length; m++)
        {
        	if(min > variances[m]){
        		threshold = m;
        		min = variances[m];
        	}
        }
        // 二值化
        System.out.println("final threshold value : " + threshold);
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
                int gray = (inPixels[index] >> 8) & 0xff;
                if(gray > threshold)
                {
                	gray = 255;
                	outPixels[index]  = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
                }
                else
                {
                	gray = 0;
                	outPixels[index]  = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
                }
				
        	}
        }
        setRGB(dest, 0, 0, width, height, outPixels );
        return dest;
	}

}
