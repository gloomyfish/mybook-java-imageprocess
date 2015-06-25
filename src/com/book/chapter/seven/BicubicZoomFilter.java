package com.book.chapter.seven;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BicubicZoomFilter extends AbstractBufferedImageOp {
	private int destH; // zoom height
	private int destW; // zoom width
	public BicubicZoomFilter()
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
		float rowRatio = ((float) height) / ((float) destH);
		float colRatio = ((float) width) / ((float) destW);
		int index = 0;
		for (int row = 0; row < destH; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			double srcRow = ((float) row) * rowRatio;
			// 获取整数部分坐标 row Index
			double j = Math.floor(srcRow);
			// 获取行的小数部分坐标
			double t = srcRow - j;
			for (int col = 0; col < destW; col++) {
				double srcCol = ((float) col) * colRatio;
				// 获取整数部分坐标 column Index
				double k = Math.floor(srcCol);
				// 获取列的小数部分坐标
				double u = srcCol - k;
				double[][] bc1 = new double[4][3];
				for(int n=0; n<4; n++)
				{
					int[][] c1 = new int[4][3]; 
					for(int m=0; m<4; m++)
					{
						c1[m] = getPixel(j+n-1, k+m-1, width, height, inPixels);
					}
					for(int d=0; d<3; d++) // for RGB
					{
						bc1[n][d] = get1DCubicValue(new double[]
								{c1[0][d],c1[1][d],c1[2][d],c1[3][d]}, u);						
					}
				}
				double[] dRGB = new double[3];
				for(int dd=0; dd<3; dd++)
				{
					dRGB[dd] = get1DCubicValue(new double[]
							{bc1[0][dd],bc1[1][dd],bc1[2][dd],bc1[3][dd]}, t);
				}
				ta = 255;
				tr = (int) (dRGB[0]);
				tg = (int) (dRGB[1]);
				tb = (int) (dRGB[2]);
				index = row * destW + col;
				outPixels[index] = (ta << 24) | (clamp(tr) << 16)
						| (clamp(tg) << 8) | clamp(tb);
			}
		}
		setRGB(dest, 0, 0, destW, destH, outPixels);
		return dest;
	}

	// extract (1/2*delta) will get below formula
	public double get1DCubicValue(double[] p, double delta) {
		return p[1]
				+ 0.5
				* delta
				* (p[2] - p[0] 
						+ delta * (2.0 * p[0] - 5.0 * p[1] + 4.0 * p[2] - p[3] 
						+ delta * (3.0 * (p[1] - p[2]) + p[3] - p[0])));
	}

	private int[] getPixel(double j, double k, int width, int height,
			int[] inPixels) {
		int row = (int) j;
		int col = (int) k;
		if (row >= height) {
			row = height - 1;
		}
		if (row < 0) {
			row = 0;
		}
		if (col < 0) {
			col = 0;
		}
		if (col >= width) {
			col = width - 1;
		}
		int index = row * width + col;
		int[] rgb = new int[3];
		rgb[0] = (inPixels[index] >> 16) & 0xff;
		rgb[1] = (inPixels[index] >> 8) & 0xff;
		rgb[2] = inPixels[index] & 0xff;
		return rgb;
	}
	public BufferedImage createCompatibleDestImage(
			BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, 
        		dstCM.createCompatibleWritableRaster(destW, destH), 
        		dstCM.isAlphaPremultiplied(), null);
    }
}
