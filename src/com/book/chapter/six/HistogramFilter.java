package com.book.chapter.six;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class HistogramFilter extends AbstractBufferedImageOp {

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
		int height = src.getHeight();

		if (dest == null)
			dest = createCompatibleDestImage(src, null);

		int[] inPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		// get histogram data
		int[] histogram = new int[256];
		for(int i=0; i<histogram.length; i++)
		{
			histogram[i] = 0;
		}
		for (int row = 0; row < height; row++) {
			int tr = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				tr = (inPixels[index] >> 16) & 0xff;
				histogram[tr]++;
			}
		}
		double maxFrequency = 0;
		for(int i=0; i<histogram.length; i++)
		{
			maxFrequency = Math.max(maxFrequency, histogram[i]);
		}

		// render the histogram graphic 
		Graphics2D g2d = dest.createGraphics();
		g2d.setPaint(Color.LIGHT_GRAY);
		g2d.fillRect(0, 0, width, height);
		
		// draw XY Axis
		g2d.setPaint(Color.BLACK);
		g2d.drawLine(50, 50, 50, height - 50);
		g2d.drawLine(50, height-50, width-50, height - 50);
		// draw XY Title
		g2d.drawString("0", 50, height-30);
		g2d.drawString("255", width-50, height-30);
		g2d.drawString("0", 20, height-50);
		g2d.drawString("" + maxFrequency, 20,50);
		// draw histogram bar
		double xunit = (width - 100.0)/256.0d;
		double yunit = (height - 100.0)/maxFrequency;
		for(int i=0; i<histogram.length; i++)
		{
			 double xp = 50 + xunit * i;
			 double yp = yunit * histogram[i];
			 Rectangle2D rect2d = new Rectangle2D.
					 Double(xp, height - 50 - yp, xunit, yp);
			 g2d.fill(rect2d);
		}
		return dest;
	}

}
