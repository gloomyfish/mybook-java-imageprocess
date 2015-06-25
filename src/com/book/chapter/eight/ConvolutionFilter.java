package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class ConvolutionFilter extends AbstractBufferedImageOp {
	public final static int ZERO_EXTEND = 1;
	public final static int WRAP = 2;
	public final static int CROP = 4;
	
	private int edgeAction;
	private float[] kernels;
	
	public ConvolutionFilter()
	{
		edgeAction = CROP;
	}
	
	public int getEdgeAction() {
		return edgeAction;
	}

	public void setEdgeAction(int edgeAction) {
		this.edgeAction = edgeAction;
	}

	public float[] getKernels() {
		return kernels;
	}

	public void setKernels(float[] kernels) {
		this.kernels = kernels;
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

        // normalization kernels
        float sum = 0.0f;
        for(int i=0; i<kernels.length; i++)
        {
        	sum += kernels[i];
        }
        for(int i=0; i<kernels.length; i++)
        {
        	kernels[i] = kernels[i]/sum;
        }
        
        // execute convolve 
        // X Direction
		convolve1D(kernels, inPixels, outPixels, width, height, edgeAction);
		System.arraycopy(outPixels, 0, inPixels, 0, inPixels.length);
		// Y Direction
		convolve1D(kernels, inPixels, outPixels, height, width, edgeAction);
		
		// return image with output pixels
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}

	private void convolve1D(float[] kernels2, int[] inPixels, 
			int[] outPixels,
			int width, int height, 
			int edgeAction2) {
		int cr = kernels2.length/2;
		int index = 0;
		for(int row=0; row<height; row++)
		{
			for(int col=0; col<width; col++)
			{
				float sumr=0, sumg=0, sumb=0;
				for(int nr=-cr; nr<=cr; nr++)
				{
					int offsetCol = col + nr;
					if(offsetCol >=0 && offsetCol < width)
					{
						index = row * width + offsetCol;
					}
					// handle edge pixels
					else if(edgeAction == ZERO_EXTEND)
					{
						continue;
					}
					else if(edgeAction == WRAP)
					{
						int ncol = offsetCol / width;
						offsetCol = offsetCol-(ncol * width); // wrap col
						if(offsetCol < 0)
							offsetCol += width;
					}
					else if(edgeAction == CROP)
					{
						if(offsetCol < 0)
							offsetCol = 0;
						else if(offsetCol >= width)
							offsetCol = width - 1;
					}
					index = row * width + offsetCol;
					int rgb = inPixels[index];
					sumr += kernels2[cr+nr] * ((rgb >> 16) & 0xff);
					sumg += kernels2[cr+nr] * ((rgb >> 8) & 0xff);
					sumb += kernels2[cr+nr] * (rgb & 0xff);
				}
				index = row * width + col;
				outPixels[index] = (255 << 24) 
						| (clamp(sumr) << 16) 
						| (clamp(sumg) << 8) 
						| clamp(sumb);
			}
		}
		
	}
	
	public int clamp(float f) {
		int value = (int)f;
		return value > 255 ? 255 :
			(value < 0 ? 0 : value);
	}

}
