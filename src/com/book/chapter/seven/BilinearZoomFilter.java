package com.book.chapter.seven;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BilinearZoomFilter extends AbstractBufferedImageOp {
	private int destH; // zoom height
	private int destW; // zoom width
	public BilinearZoomFilter()
	{
		
	}
	public void setDestHeight(int destH) {
		this.destH = destH;
	}

	public void setDestWidth(int destW) {
		this.destW = destW;
	}
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[destH * destW];
		getRGB(src, 0, 0, width, height, inPixels);
		float rowRatio = ((float)height)/((float)destH);
		float colRatio = ((float)width)/((float)destW);
		int index = 0;
		for(int row=0; row<destH; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			double srcRow = ((float)row)*rowRatio;
			// 获取整数部分坐标 row Index
			double j = Math.floor(srcRow);
			// 获取行的小数部分坐标
			double t = srcRow - j; 
			for(int col=0; col<destW; col++) {
				double srcCol = ((float)col)*colRatio;
				// 获取整数部分坐标 column Index
				double k = Math.floor(srcCol);
				// 获取列的小数部分坐标
				double u = srcCol - k;
				int[] p1 = getPixel(j, k, width, height, inPixels);
				int[] p2 = getPixel(j, k+1, width, height, inPixels);
				int[] p3 = getPixel(j+1, k, width, height, inPixels);
				int[] p4 = getPixel(j+1, k+1, width, height, inPixels);
				double a = (1.0d-t)*(1.0d-u);
				double b = (1.0d-t)*u;
				double c = (t)*(1.0d-u);
				double d = t*u;
				ta = 255;
				tr = (int)(p1[0] * a + p2[0] * b + p3[0] * c + p4[0] * d);
				tg = (int)(p1[1] * a + p2[1] * b + p3[1] * c + p4[1] * d);
				tb = (int)(p1[2] * a + p2[2] * b + p3[2] * c + p4[2] * d);
				index = row * destW + col;
				outPixels[index] = (ta << 24) 
						| (clamp(tr) << 16) | (clamp(tg) << 8) | clamp(tb);
			}
		}
		setRGB(dest, 0, 0, destW, destH, outPixels);
		return dest;
	}
	
    private int[] getPixel(double j, double k, int width, 
    						int height, int[] inPixels) {
    	int row = (int)j;
    	int col = (int)k;
    	if(row >= height)
    	{
    		row = height - 1;
    	}
    	if(row < 0)
    	{
    		row = 0;
    	}
    	if(col < 0)
    	{
    		col = 0;
    	}
    	if(col >= width)
    	{
    		col = width - 1;
    	}
    	int index = row * width + col;
    	int[] rgb = new int[3];
		rgb[0] = (inPixels[index] >> 16) & 0xff;
		rgb[1] = (inPixels[index] >> 8) & 0xff;
		rgb[2] = inPixels[index] & 0xff;
		return rgb;	
	}
    
	public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, 
        		dstCM.createCompatibleWritableRaster(destW, destH), 
        		dstCM.isAlphaPremultiplied(), null);
    }

}
