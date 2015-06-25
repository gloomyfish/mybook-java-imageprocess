package com.book.chapter.seven;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class ZoomFilter extends AbstractBufferedImageOp {
	public final static int PIXEL_REPLACE_ZOOM =  1;
	public final static int ZERO_ORDER_ZOOM = 2;
	public final static int K_TIMES_ZOOM = 4;
	private int type = PIXEL_REPLACE_ZOOM;
	private float times = 2;

	public ZoomFilter() {

	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public float getTimes() {
		return times;
	}

	public void setTimes(float times) {
		this.times = times;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int dw = (int)(width * times);
		int dh = (int)(height * times);
		if(this.getType() == K_TIMES_ZOOM)
		{
			int k = (int) times;
			dh = k * (height-1) + 1;
			dw = k * (width-1) + 1;
		}
		int[] outPixels = new int[dw * dh];
		getRGB(src, 0, 0, width, height, inPixels);
		if(getType() == PIXEL_REPLACE_ZOOM)
		{
			int index = 0;
			for (int row = 0; row < dh; row++) { // for each row
				for (int col = 0; col < dw; col++) { // for each column
					int nrow = (int)(row / times);
					int ncol = (int)(col / times);
					int index2 = nrow * width + ncol;
					index = row * dw + col;
					outPixels[index] = inPixels[index2];				
				}
			}
		}
		else if(getType() == ZERO_ORDER_ZOOM)
		{
			outPixels = zeroOrderZoom(inPixels, width, height);
		}
		else if(getType() == K_TIMES_ZOOM)
		{
			outPixels = kTimesZoom(inPixels, width, height);
		}
		setRGB(dest, 0, 0, dw, dh, outPixels);
		return dest;
	}
	
	private int[] kTimesZoom(int[] inPixels, int width, int height) {
		int k = (int) times;
		int dh = k * (height-1) + 1;
		int dw = k * (width-1) + 1;
		int[] outPixels = new int[dw * dh];
		int index = 0;
		int[] rowPixels = new int[dw * height];
		for (int row = 0; row < height; row++) { 
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 1; col < width; col++) { 
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				int pcol = col - 1;
				if(pcol < 0)
				{
					pcol = 0;
				}
				int index2 = row * width + pcol; 
				int ta2 = (inPixels[index2] >> 24) & 0xff;
				int tr2 = (inPixels[index2] >> 16) & 0xff;
				int tg2 = (inPixels[index2] >> 8) & 0xff;
				int tb2 = inPixels[index2] & 0xff;
				int optr = Math.abs(tr - tr2)/k;
				int optg = Math.abs(tg - tg2)/k;
				int optb = Math.abs(tb - tb2)/k;
				for(int t=1; t<k; t++)
				{
					int ncol = col*k - (k-t);
					if(ncol < 0)
					{
						ncol = 0;
					}
					index = row *dw + ncol;
					int tr3 = Math.min(tr, tr2) + t * optr;
					int tg3 = Math.min(tg, tg2) + t * optg;
					int tb3 = Math.min(tb, tb2) + t * optb;
					rowPixels[index] = (ta << 24) 
							| (tr3 << 16) | (tg3 << 8) | tb3;
					
				}
				index = row * dw + col * k;
				rowPixels[index] = (ta << 24) 
						| (tr << 16) | (tg << 8) | tb;
				index = row * dw + (col * k) - k;
				rowPixels[index] = (ta << 24) 
						| (tr2 << 16) | (tg2 << 8) | tb2;
			}
		}
		
		// for each column
		for (int col = 0; col < dw; col++) { 
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int row = 1; row < height; row++) { 
				index = row * dw + col;
				ta = (rowPixels[index] >> 24) & 0xff;
				tr = (rowPixels[index] >> 16) & 0xff;
				tg = (rowPixels[index] >> 8) & 0xff;
				tb = rowPixels[index] & 0xff;
				int prow = (row -1) < 0 ? 0 : (row - 1);
				int index2 = prow * dw + col; 
				int ta2 = (rowPixels[index2] >> 24) & 0xff;
				int tr2 = (rowPixels[index2] >> 16) & 0xff;
				int tg2 = (rowPixels[index2] >> 8) & 0xff;
				int tb2 = rowPixels[index2] & 0xff;
				int optr = Math.abs(tr - tr2)/k;
				int optg = Math.abs(tg - tg2)/k;
				int optb = Math.abs(tb - tb2)/k;
				for(int t=1; t<k; t++)
				{
					int nrow = row*k - (k-t);
					if(nrow < 0)
					{
						nrow = 0;
					}
					index = nrow *dw + col;
					int tr3 = Math.min(tr, tr2) + t * optr;
					int tg3 = Math.min(tg, tg2) + t * optg;
					int tb3 = Math.min(tb, tb2) + t * optb;
					outPixels[index] = (ta << 24) 
							| (tr3 << 16) | (tg3 << 8) | tb3;					
				}
				index = (row * k) * dw + col;
				outPixels[index] = (ta << 24) 
						| (tr << 16) | (tg << 8) | tb;
				index = (row * k - k) * dw + col;
				outPixels[index] = (ta << 24) 
						| (tr2 << 16) | (tg2 << 8) | tb2;
			}
		}
		return outPixels;
	}

	private int[] zeroOrderZoom(int[] inPixels, int width, int height)
	{
		int dw = (int)(width * 2);
		int dh = (int)(height * 2);
		int[] outPixels = new int[dw * dh];
		
		// for each row
		int index = 0;
		int[] rowPixels = new int[dw * height];
		for (int row = 0; row < height; row++) { 
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) { 
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				int pcol = col - 1;
				if(pcol < 0)
				{
					pcol = 0;
				}
				int index2 = row * width + pcol; 
				int ta2 = (inPixels[index2] >> 24) & 0xff;
				int tr2 = (inPixels[index2] >> 16) & 0xff;
				int tg2 = (inPixels[index2] >> 8) & 0xff;
				int tb2 = inPixels[index2] & 0xff;
				int tr3 = (tr + tr2)/2;
				int tg3 = (tg + tg2)/2;
				int tb3 = (tb + tb2)/2;
				int ncol = col*2 - 1;
				if(ncol < 0)
				{
					ncol = 0;
				}
				index = row *dw + ncol;
				rowPixels[index] = (ta << 24) 
						| (tr3 << 16) | (tg3 << 8) | tb3;
				index = row * dw + col * 2;
				rowPixels[index] = (ta << 24) 
						| (tr << 16) | (tg << 8) | tb;
			}
		}
		
		// for each column
		for (int col = 0; col < dw; col++) { 
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int row = 0; row < height; row++) { 
				index = row * dw + col;
				ta = (rowPixels[index] >> 24) & 0xff;
				tr = (rowPixels[index] >> 16) & 0xff;
				tg = (rowPixels[index] >> 8) & 0xff;
				tb = rowPixels[index] & 0xff;
				int prow = (row -1) < 0 ? 0 : (row - 1);
				int index2 = prow * dw + col; 
				int ta2 = (rowPixels[index2] >> 24) & 0xff;
				int tr2 = (rowPixels[index2] >> 16) & 0xff;
				int tg2 = (rowPixels[index2] >> 8) & 0xff;
				int tb2 = rowPixels[index2] & 0xff;
				int tr3 = (tr + tr2)/2;
				int tg3 = (tg + tg2)/2;
				int tb3 = (tb + tb2)/2;
				int nrow = row*2 - 1;
				if(nrow < 0)
				{
					nrow = 0;
				}
				index = nrow *dw + col;
				outPixels[index] = (ta << 24) 
						| (tr3 << 16) | (tg3 << 8) | tb3;
				index = (row * 2) * dw + col;
				outPixels[index] = (ta << 24) 
						| (tr << 16) | (tg << 8) | tb;
			}
		}
		
		return outPixels;
	}
	
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        int width = (int)(src.getWidth() * times);
        int height = (int)(src.getHeight() * times);
		if(this.getType() == K_TIMES_ZOOM)
		{
			int k = (int) times;
			height = k * (src.getHeight()-1) + 1;
			width = k * (src.getWidth()-1) + 1;
		}
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height), dstCM.isAlphaPremultiplied(), null);
    }

}
