package com.book.chapter.four;

import java.awt.image.BufferedImage;

public class ContrastFilter extends AbstractBufferedImageOp {

	private float contrast;
	
	public ContrastFilter()
	{
		this(0.0f);
	}
	
	public ContrastFilter(float c)
	{
		this.contrast = c;
	}

	public float getContrast() {
		return contrast;
	}

	public void setContrast(float contrast) {
		this.contrast = contrast;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		src.getRGB(0, 0, width, height, inPixels, 0, width);

		// handle user input parameter-contrast
		if(this.contrast > 100)
		{
			contrast = 100;
		}
		if(this.contrast < -100)
		{
			contrast = -100;
		}
		contrast =  (1 + contrast / 100.0f);
		
		// adjust image contrast pixel by pixel, here
		int index = 0;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;

				// make it more difference
				float cr = ((tr /255.0f) - 0.5f) * contrast;
				float cg = ((tg /255.0f) - 0.5f) * contrast;
				float cb = ((tb /255.0f) - 0.5f) * contrast;
				
				// output RGB value
				tr = (int)((cr + 0.5f) * 255.0f);
				tg = (int)((cg + 0.5f) * 255.0f);
				tb = (int)((cb + 0.5f) * 255.0f);

				// write it back 
				outPixels[index] = (ta << 24) | (clamp(tr) << 16)
						| (clamp(tg) << 8) | clamp(tb);
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	public int clamp(int value) {
		return value > 255 ? 255 :
			(value < 0 ? 0 : value);
	}

}
