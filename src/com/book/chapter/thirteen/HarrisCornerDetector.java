package com.book.chapter.thirteen;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class HarrisCornerDetector extends GrayFilter {
	private GaussianDerivativeFilter filter;
	private List<HarrisMatrix> harrisMatrixList;
	private double lambda = 0.04; // scope : 0.04 ~ 0.06
	
	// i hard code the window size just keep it' size is same as 
	// first order derivation Gaussian window size
	private double sigma = 1; // always
	private double window_radius = 1; // always
	public HarrisCornerDetector() {
		filter = new GaussianDerivativeFilter();
		harrisMatrixList = new ArrayList<HarrisMatrix>();
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        initSettings(height, width);
        if ( dest == null )
            dest = createCompatibleDestImage( src, null );
        
        BufferedImage grayImage = super.filter(src, null);
        int[] inPixels = new int[width*height];
        
		// first step  - Gaussian first-order Derivatives (3 × 3) - X - gradient, (3 × 3) - Y - gradient
		filter.setDirectionType(GaussianDerivativeFilter.X_DIRECTION);
		BufferedImage xImage = filter.filter(grayImage, null);
		getRGB( xImage, 0, 0, width, height, inPixels );
		extractPixelData(inPixels, GaussianDerivativeFilter.X_DIRECTION, height, width);
		
		filter.setDirectionType(GaussianDerivativeFilter.Y_DIRECTION);
		BufferedImage yImage = filter.filter(grayImage, null);
		getRGB( yImage, 0, 0, width, height, inPixels );
		extractPixelData(inPixels, GaussianDerivativeFilter.Y_DIRECTION, height, width);
				
		// second step - calculate the Ix^2, Iy^2 and Ix^Iy
		for(HarrisMatrix hm : harrisMatrixList)
		{
			double Ix = hm.getXGradient();
			double Iy = hm.getYGradient();
			hm.setIxIy(Ix * Iy);
			hm.setXGradient(Ix*Ix);
			hm.setYGradient(Iy*Iy);
		}
		
		// 基于高斯方法，中心点化窗口计算一阶导数和，关键一步 SumIx2, SumIy2 and SumIxIy, 高斯模糊
		calculateGaussianBlur(width, height);

		// 求取Harris Matrix 特征值 
		// 计算角度相应值R R= Det(H) - lambda * (Trace(H))^2
		harrisResponse(width, height);
		
		// based on R, compute non-max suppression
		nonMaxValueSuppression(width, height);
		
		// match result to original image and highlight the key points
		int[] outPixels = matchToImage(width, height, src);
		
		// return result image
		setRGB( dest, 0, 0, width, height, outPixels );
		return dest;
	}
	
	
	private int[] matchToImage(int width, int height, BufferedImage src) {
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
                HarrisMatrix hm = harrisMatrixList.get(index);
                if(hm.getMax() > 0)
                {
                	tr = 0;
                	tg = 255; // make it as green for corner key pointers
                	tb = 0;
                	outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
                }
                else
                {
                	outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;                	
                }
                
        	}
        }
		return outPixels;
	}
	/***
	 * we still use the 3*3 windows to complete the non-max response value suppression
	 */
	private void nonMaxValueSuppression(int width, int height) {
        int index = 0;
        int radius = (int)window_radius;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		HarrisMatrix hm = harrisMatrixList.get(index);
        		double maxR = hm.getR();
        		boolean isMaxR = true;
        		for(int subrow =-radius; subrow<=radius; subrow++)
        		{
        			for(int subcol=-radius; subcol<=radius; subcol++)
        			{
        				int nrow = row + subrow;
        				int ncol = col + subcol;
        				if(nrow >= height || nrow < 0)
        				{
        					nrow = 0;
        				}
        				if(ncol >= width || ncol < 0)
        				{
        					ncol = 0;
        				}
        				int index2 = nrow * width + ncol;
        				HarrisMatrix hmr = harrisMatrixList.get(index2);
        				if(hmr.getR() > maxR)
        				{
        					isMaxR = false;
        				}
        			}       			
        		}
        		if(isMaxR)
        		{
        			hm.setMax(maxR);
        		}
        	}
        }
		
	}
	
	/***
	 * 计算两个特征值，然后得到R，公式如下，可以自己推导，关于怎么计算矩阵特征值，请看这里：
	 * http://www.sosmath.com/matrix/eigen1/eigen1.html
	 * 
	 * 	A = Sxx;
	 *	B = Syy;
	 *  C = Sxy*Sxy*4;
	 *	lambda = 0.04;
	 *	H = (A*B - C) - lambda*(A+B)^2;
     *
	 * @param width
	 * @param height
	 */
	private void harrisResponse(int width, int height) {
        int index = 0;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		HarrisMatrix hm = harrisMatrixList.get(index);
        		double c =  hm.getIxIy() * hm.getIxIy();
        		double ab = hm.getXGradient() * hm.getYGradient();
        		double aplusb = hm.getXGradient() + hm.getYGradient();
        		double response = (ab -c) - lambda * Math.pow(aplusb, 2);
        		hm.setR(response);
        	}
        }		
	}

	private void calculateGaussianBlur(int width, int height) {
        int index = 0;
        int radius = (int)window_radius;
        double[][] gw = get2DKernalData(radius, sigma);
        double sumxx = 0, sumyy = 0, sumxy = 0;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {        		
        		for(int subrow =-radius; subrow<=radius; subrow++)
        		{
        			for(int subcol=-radius; subcol<=radius; subcol++)
        			{
        				int nrow = row + subrow;
        				int ncol = col + subcol;
        				if(nrow >= height || nrow < 0)
        				{
        					nrow = 0;
        				}
        				if(ncol >= width || ncol < 0)
        				{
        					ncol = 0;
        				}
        				int index2 = nrow * width + ncol;
        				HarrisMatrix whm = harrisMatrixList.get(index2);
        				sumxx += (gw[subrow + radius][subcol + radius] * whm.getXGradient());
        				sumyy += (gw[subrow + radius][subcol + radius] * whm.getYGradient());
        				sumxy += (gw[subrow + radius][subcol + radius] * whm.getIxIy());
        			}
        		}
        		index = row * width + col;
        		HarrisMatrix hm = harrisMatrixList.get(index);
        		hm.setXGradient(sumxx);
        		hm.setYGradient(sumyy);
        		hm.setIxIy(sumxy);
        		
        		// clean up for next loop
        		sumxx = 0;
        		sumyy = 0;
        		sumxy = 0;
        	}
        }		
	}
	
	public double[][] get2DKernalData(int n, double sigma) {
		int size = 2*n +1;
		double sigma22 = 2*sigma*sigma;
		double sigma22PI = Math.PI * sigma22;
		double[][] kernalData = new double[size][size];
		int row = 0;
		for(int i=-n; i<=n; i++) {
			int column = 0;
			for(int j=-n; j<=n; j++) {
				double xDistance = i*i;
				double yDistance = j*j;
				kernalData[row][column] = Math.exp(-(xDistance + yDistance)/sigma22)/sigma22PI;
				column++;
			}
			row++;
		}
		
//		for(int i=0; i<size; i++) {
//			for(int j=0; j<size; j++) {
//				System.out.print("\t" + kernalData[i][j]);
//			}
//			System.out.println();
//			System.out.println("\t ---------------------------");
//		}
		return kernalData;
	}

	private void extractPixelData(int[] pixels, int type, int height, int width)
	{
        int index = 0;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (pixels[index] >> 24) & 0xff;
                tr = (pixels[index] >> 16) & 0xff;
                tg = (pixels[index] >> 8) & 0xff;
                tb = pixels[index] & 0xff;
                HarrisMatrix matrix = harrisMatrixList.get(index);
                if(type == GaussianDerivativeFilter.X_DIRECTION)
                {
                	matrix.setXGradient(tr);
                }
                if(type == GaussianDerivativeFilter.Y_DIRECTION)
                {
                	matrix.setYGradient(tr);
                }
        	}
        }
	}
	
	private void initSettings(int height, int width)
	{
        int index = 0;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		HarrisMatrix matrix = new HarrisMatrix();
                harrisMatrixList.add(index, matrix);
        	}
        }
	}

}
