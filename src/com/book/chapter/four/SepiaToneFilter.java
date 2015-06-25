package com.book.chapter.four;

import java.awt.image.BufferedImage;

public class SepiaToneFilter extends AbstractBufferedImageOp {

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
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

//int fr= (int)(((double)tr * 0.393) + ((double)tg * 0.769) + ((double)tb * 0.189));
//int fg = (int)(((double)tr * 0.349) + ((double)tg * 0.686) + ((double)tb * 0.168)); 
//int fb= (int)(((double)tr * 0.272) + ((double)tg * 0.534) + ((double)tb * 0.131));
                
                int fr = (int)colorBlend(noise(), (tr * 0.393) + (tg * 0.769) + (tb * 0.189), tr);
                int fg = (int)colorBlend(noise(), (tr * 0.349) + (tg * 0.686) + (tb * 0.168), tg);
                int fb = (int)colorBlend(noise(), (tr * 0.272) + (tg * 0.534) + (tb * 0.131), tb);
                
                outPixels[index] = (ta << 24) | (clamp(fr) << 16) | (clamp(fg) << 8) | clamp(fb);
                
        	}
        }
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
	private double noise() {
		return Math.random()*0.5 + 0.5;
	}
	
	private double colorBlend(double scale, double dest, double src) {
	    return (scale * dest + (1.0 - scale) * src);
	}
//	
//	public static int clamp(int c)
//	{
//		return c > 255 ? 255 :( (c < 0) ? 0: c);
//	}
	
	public String toString()
	{
		return "Sepia Tone Effect - Effect from Photoshop App";
	}

}
