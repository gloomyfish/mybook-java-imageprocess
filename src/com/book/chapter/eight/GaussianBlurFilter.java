package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class GaussianBlurFilter extends AbstractBufferedImageOp {
	
	private int n=1;
	private float sigma=1;
	
	public GaussianBlurFilter()
	{
		System.out.println("Gaussian Blur Filter...");
	}
	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public float getSigma() {
		return sigma;
	}

	public void setSigma(float sigma) {
		this.sigma = sigma;
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
		float[][] kernels = get2DKernalData(n, sigma);
		int kwRaduis = kernels[0].length/2;
		int khRaduis = kernels.length/2;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				double weightSum = 0.0;
				double redSum = 0, greenSum = 0, blurSum = 0;
				for(int subRow=-khRaduis; subRow<=khRaduis; subRow++)
				{
					int nrow = row + subRow;
					if(nrow < 0 || nrow >= height)
					{
						nrow = 0;
					}
					for(int subCol=-kwRaduis; subCol<=kwRaduis; subCol++)
					{
						int ncol = col + subCol;
						if(ncol < 0 || ncol >= width)
						{
							ncol = 0;
						}
						int index1 = nrow * width + ncol;
						int ta1 = (inPixels[index1] >> 24) & 0xff;
						int tr1 = (inPixels[index1] >> 16) & 0xff;
						int tg1 = (inPixels[index1] >> 8) & 0xff;
						int tb1 = inPixels[index1] & 0xff;
						redSum += tr1 * kernels[subRow + khRaduis][subCol + kwRaduis];
						greenSum += tg1 * kernels[subRow + khRaduis][subCol + kwRaduis];
						blurSum += tb1 * kernels[subRow + khRaduis][subCol + kwRaduis];
						weightSum += kernels[subRow + khRaduis][subCol + kwRaduis];
					}
				}
				tr = (int)(redSum / weightSum);
				tg = (int)(greenSum / weightSum);
				tb = (int)(blurSum / weightSum);
				outPixels[index] = (255 << 24) | (tr << 16) | (tg << 8) | tb;
				
				// clean up for next pixel
				tr = 0;
				tg = 0;
				tb = 0;
			}
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		return dest;
	}
	
	public float[][] get2DKernalData(int n, float sigma) {
		int size = 2*n +1;
		float sigma22 = 2*sigma*sigma;
		float sigma22PI = (float)Math.PI * sigma22;
		float[][] kernalData = new float[size][size];
		int row = 0;
		for(int i=-n; i<=n; i++) {
			int column = 0;
			for(int j=-n; j<=n; j++) {
				float xDistance = i*i;
				float yDistance = j*j;
				kernalData[row][column] = (float)Math.exp(-(xDistance 
						+ yDistance)/sigma22)/sigma22PI;
				column++;
			}
			row++;
		}
		return kernalData;
	}
	
	private float[] get1DKernelData(int n, float sigma) {
		float sigma22 = 2*sigma*sigma;
		float Pi2 = 2*(float)Math.PI;
		float sqrtSigmaPi2 = (float)Math.sqrt(Pi2) * sigma ;
		int size = 2*n + 1;
		int index = 0;
		float[] kernalData = new float[size];
		for(int i=-n; i<=n; i++) {
			float distance = i*i;
			kernalData[index] = (float)Math.exp((-distance)
					/sigma22)/sqrtSigmaPi2;
			index++;
		}
		return kernalData;
	}


}
