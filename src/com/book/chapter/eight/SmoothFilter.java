package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;


public class SmoothFilter extends AbstractBufferedImageOp {
	public final static int ARITHMETIC_TYPE = 1;
	public final static int GEOMETRIC_TYPE = 2;
	public final static int HARMONIC_TYPE = 4;
	
	private int repeats = 3; // default 1
	private int kernel_size = 3; // default 3
	private int type = 1; // default mean type
	
	public int getRepeat() {
		return repeats;
	}
	
	public void setRepeat(int repeat) {
		this.repeats = repeat;
	}
	
	public int getKernelSize() {
		return kernel_size;
	}
	
	public void setKernelSize(int kernelSize) {
		this.kernel_size = kernelSize;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );
        
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
		int rows2 = kernel_size/2;
		int cols2 = kernel_size/2;
		int index = 0;
		int index2 = 0;
		int[][] windowsPixels = new int[kernel_size][kernel_size];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (int row = -rows2; row <= rows2; row++) {
					int rowoffset = y + row;
					if(rowoffset < 0 || rowoffset >=height) {
						rowoffset = y;
					}
					for(int col = -cols2; col <= cols2; col++) {
						int coloffset = col + x;
						if(coloffset < 0 || coloffset >= width) {
							coloffset = x;
						}
						index2 = rowoffset * width + coloffset;
						windowsPixels[row+ rows2][col + cols2] = inPixels[index2];
					}
				}
		        // ¼ÆËã¾ùÖµ
				int[] op = calculateMeans(windowsPixels);

				int ia = 0xff;
				outPixels[index++] = (ia << 24) | (clamp(op[0]) << 16) | (clamp(op[1]) << 8) | clamp(op[2]);
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	private int[] calculateMeans(int[][] windowsPixels) 
	{
		int rows = windowsPixels.length;
		int cols = windowsPixels[0].length;
		int[] rgb = new int[3];
		double total = rows * cols;
		double redSum = 0, greenSum = 0, blueSum = 0;
		if(this.type == GEOMETRIC_TYPE) 
		{
			redSum = 1;
			greenSum = 1; 
			blueSum = 1;
		}
			
		for(int row=0; row<rows; row++)
		{
			for(int col=0; col<cols; col++)
			{
				double r = (windowsPixels[row][col] >> 16) & 0xff;
				double g = (windowsPixels[row][col] >> 8) & 0xff;
				double b = windowsPixels[row][col] & 0xff;
				if(this.type == ARITHMETIC_TYPE) 
				{
					redSum += r;
					greenSum += g;
					blueSum += b;
				} 
				else if(this.type == GEOMETRIC_TYPE) 
				{
					redSum = r * redSum;
					greenSum = g * greenSum;
					blueSum = b * blueSum;
				} 
				else if(this.type == HARMONIC_TYPE) 
				{
					redSum += 1.0d/r;
					greenSum += 1.0d/g;
					blueSum += 1.0d/b;
				}				
			}
		}
		
		if(this.type == ARITHMETIC_TYPE) 
		{
			rgb[0] = (int)(redSum/total);
			rgb[1] = (int)(greenSum/total);
			rgb[2] = (int)(blueSum/total);
		} 
		else if(this.type == GEOMETRIC_TYPE) 
		{
			rgb[0] = (int)Math.pow(redSum, 1.0d/total);
			rgb[1] = (int)Math.pow(greenSum, 1.0d/total);
			rgb[2] = (int)Math.pow(blueSum, 1.0d/total);
		} 
		else if(this.type == HARMONIC_TYPE) 
		{
			rgb[0] = (int)(total/redSum);
			rgb[1] = (int)(total/greenSum);
			rgb[2] = (int)(total/blueSum);
		}
		return rgb;
	}
}
