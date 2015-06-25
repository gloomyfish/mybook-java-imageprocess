package com.book.chapter.five;

import java.awt.image.BufferedImage;
import java.util.Random;

import com.book.chapter.four.AbstractBufferedImageOp;
/**
 * plus/minus/multiplication/division
 * @author fish
 *
 */
public class PMMDFilter extends AbstractBufferedImageOp {
	public final static int PLUS = 1;
	public final static int MINUS = 2;
	public final static int MULTIPLE = 4;
	
	private Random rnd;
	private double range;
	private int type;
	
	public PMMDFilter()
	{
		type = MULTIPLE;
		rnd = new Random();
		range = 25.0;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
        int centerX = width/2;
        int centerY = height/2;
        double maxDistance = Math.sqrt(centerX * centerX + centerY * centerY);
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				int[] rgb = new int[]{tr, tg, tb};
				// plus
				if(type == PLUS)
				{
					rgb = plus(rgb);
				}
				// minus
				if(type == MINUS)
				{
					int pcol = col - 1;
					if(pcol < 0 || pcol >= width)
					{
						pcol = 0;
					}
					int index2 = row * width + pcol;
					rgb = minus(rgb, inPixels[index2]);
				}
				if(type == MULTIPLE)
				{
					rgb = multiple(rgb, maxDistance, 
							centerX, centerY, row, col);
				}
				tr = rgb[0];
				tg = rgb[1];
				tb = rgb[2];
				outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;

			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	private int[] multiple(int[] rgb, double maxDistance, 
			int cx, int cy, int row, int col) {
        double scale = 1.0 - 
        		getDistance(cx, cy, col, row)/maxDistance;
        scale = scale * scale;
        rgb[0] = (int)(scale * rgb[0]);
        rgb[1] = (int)(scale * rgb[1]);
        rgb[2] = (int)(scale * rgb[2]);
        return rgb;
	}

	private int[] minus(int[] rgb, int p) {
		int tr = (p >> 16) & 0xff;
		int tg = (p >> 8) & 0xff;
		int tb = p & 0xff;
		rgb[0] = clamp(rgb[0] - tr);
		rgb[1] = clamp(rgb[1] - tg);
		rgb[2] = clamp(rgb[2] - tb);
		return rgb;
	}

	private int[] plus(int[] rgb)
	{
		rgb[0] = addNoise(rgb[0]);
		rgb[1] = addNoise(rgb[1]);
		rgb[2] = addNoise(rgb[2]);
		return rgb;
	}
	
	private int addNoise(int p)
	{
		boolean valid = false;
		do {
			int ran = (int)Math.round(
					rnd.nextGaussian()*range);
			int v = p + ran;// pixel add noise
			valid = v>=0 && v<=255;
			if (valid) p = v;
		} while (!valid);
		
		return p;
	}
	
	private double getDistance(int cx, int cy, 
			int px, int py) {
		double xx = (cx - px)*(cx - px);
		double yy = (cy - py)*(cy - py);
		return (int)Math.sqrt(xx + yy);
	}

}
