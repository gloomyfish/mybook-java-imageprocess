package com.book.chapter.seven;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import com.book.chapter.four.AbstractBufferedImageOp;

public class NearestZoomFilter extends AbstractBufferedImageOp {
	
	private int destH; // zoom height
	private int destW; // zoom width
	
	public NearestZoomFilter()
	{
		System.out.println("Nearest Pixel Interpolation Algo");
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
		for (int row = 0; row < destH; row++) {
			int srcRow = Math.round(((float)row)*rowRatio);
			if(srcRow >=height) {
				srcRow = height - 1;
			}
			for (int col = 0; col < destW; col++) {
				int srcCol = Math.round(((float)col)*colRatio);
				if(srcCol >= width) {
					srcCol = width - 1;
				}
				int index2 = row * destW + col;
				index = srcRow * width + srcCol;
				outPixels[index2] = inPixels[index];				
			}
		}
		setRGB(dest, 0, 0, destW, destH, outPixels);
		return dest;
	}
	
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(destW, destH), dstCM.isAlphaPremultiplied(), null);
    }

}
