package com.book.chapter.eleven;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.book.chapter.four.AbstractBufferedImageOp;
// 二值图像中的集合操作演示
public class SetOperatorFilter extends AbstractBufferedImageOp {
	public final static int UNION = 1;
	public final static int INTERSECTION = 2;
	public final static int COMPLEMETNT = 3;
	public final static int DIFFERENCE = 4;
	private int operatorType;
	private BufferedImage bImage;
	
	public SetOperatorFilter(int operatorType)
	{
		this.operatorType = operatorType;
	}
	
	public BufferedImage getbImage() {
		return bImage;
	}

	public void setbImage(BufferedImage bImage) {
		this.bImage = bImage;
	}

	public int getOperatorType() {
		return operatorType;
	}

	public void setOperatorType(int operatorType) {
		this.operatorType = operatorType;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        if(bImage == null || width != bImage.getWidth() || height != bImage.getHeight())
        {
        	throw new java.lang.IllegalArgumentException("width and height " +
        			"must be same between image A and B");
        }

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        int[] setA = new int[width*height];
        int[] setB = new int[width*height];
        int[] output = new int[width*height];
        getRGB( src, 0, 0, width, height, setA );
        getRGB( bImage, 0, 0, width, height, setB );
        int index = 0;
        Arrays.fill(output, -16777216);// black
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				int g1 = getPixel(setA, width, height, col, row);
				int g2 = getPixel(setB, width, height, col, row);
				// 集合并操作
				if(getOperatorType() == UNION)
				{
					if(g1 <127  && g2 < 127)
						continue;
					output[index] = -1; // make it white
				}
				// 集合交操作
				else if(getOperatorType() == INTERSECTION)
				{
					if(g1 > 127 && g2 > 127)
						output[index] = -1; // make it white
					else
						continue;
				}
				// 集合补操作
				else if(getOperatorType() == COMPLEMETNT)
				{
					if(g1 > 127)
					{
						output[index] = -16777216;
					}
					else
					{
						output[index] = (0xff << 24) | (200 << 16) | (200 << 8) | 200;
					}
				}
				// 集合补操作
				else if(getOperatorType() == DIFFERENCE)
				{
					// 求补
					if(g2 > 127)
					{
						g2 = 0;
					}
					else
					{
						g2 = 255;
					}
					// 求交
					if(g1 > 127 && g2 > 127)
						output[index] = -1; // make it white
					else
						continue;
				}

			}
		}
		setRGB(dest, 0, 0, width, height, output);
		return dest;
	}
	
	private int getPixel(int[] input, int width, int height, int col,
			int row) {
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		int tr = (input[index] >> 16) & 0xff;
		return tr;
	}
}
