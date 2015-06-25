package com.book.chapter.four;

import java.awt.image.BufferedImage;

public class BCSAdjustFilter extends AbstractBufferedImageOp {
	private double contrast;
	private double brightness;
	private double saturation;

	public double getContrast() {
		return contrast;
	}

	public void setContrast(double contrast) {
		this.contrast = contrast;
	}

	public double getBrightness() {
		return brightness;
	}

	public void setBrightness(double brightness) {
		this.brightness = brightness;
	}

	public double getSaturation() {
		return saturation;
	}

	public void setSaturation(double saturation) {
		this.saturation = saturation;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		handleParameters();
		int width = src.getWidth();
        int height = src.getHeight();
        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

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
                double[] hsl = rgb2Hsl(new int[]{tr, tg, tb});

                
                // adjust saturation.
                hsl[1] = hsl[1] * saturation;
                if( hsl[1] < 0.0) {
                	hsl[1] = 0.0;
                }
                if( hsl[1] > 255.0) {
                	hsl[1] = 255.0;
                }
                
                // adjust brightness
                hsl[2] = hsl[2] * brightness;
                if( hsl[2] < 0.0) {
                	hsl[2] = 0.0;
                }
                if( hsl[2] > 255.0) {
                	hsl[2] = 255.0;
                }
                
                // back to RGB space
                int[] rgb = hsl2RGB(hsl);
                tr = clamp(rgb[0]);
                tg = clamp(rgb[1]);
                tb = clamp(rgb[2]);
                
                // adjust contrast
				double cr = ((tr /255.0d) - 0.5d) * contrast;
				double cg = ((tg /255.0d) - 0.5d) * contrast;
				double cb = ((tb /255.0d) - 0.5d) * contrast;
				
				// output RGB value
				tr = (int)((cr + 0.5f) * 255.0f);
				tg = (int)((cg + 0.5f) * 255.0f);
				tb = (int)((cb + 0.5f) * 255.0f);

				// write it back 
				outPixels[index] = (ta << 24) | (clamp(tr) << 16)
						| (clamp(tg) << 8) | clamp(tb);

        	}
        }

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}

	private void handleParameters() {
		contrast = (1.0 + contrast/100.0);
		brightness = (1.0 + brightness/100.0);
		saturation = (1.0 + saturation/100.0);
		
	}

}
