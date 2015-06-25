package com.book.chapter.twelves;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

/***
 * based on gray level image
 * 
 */
public class SobelEdgeFilter extends AbstractBufferedImageOp {
	public static final int[][] X_SOBEL = new int[][]{{-1,-2,-1},{0,0,0},{1,2,1}};
	public static final int[][] Y_SOBEL = new int[][]{{-1, 0, 1},{-2,0,2},{-1,0,1}};
	
	public SobelEdgeFilter()
	{
		System.out.println("Sobel Edge Detection!");
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        int[] x_gradient = new int[width*height];
        int[] y_gradient = new int[width*height];
        getRGB(src, 0, 0, width, height, inPixels );
        int index = 0;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		int xg = 0, yg = 0;
        		for(int sr=-1; sr<=1; sr++)
        		{
        			for(int sc=-1; sc<=1; sc++)
        			{
        				int nrow = row + sr;
        				int ncol = col + sc;
        				if(nrow < 0 || nrow >= height)
        				{
        					nrow = 0;
        				}
        				if(ncol < 0 || ncol >= width)
        				{
        					ncol = 0;
        				}
        				index = nrow * width + ncol;
                        tr = (inPixels[index] >> 16) & 0xff;
                        tg = (inPixels[index] >> 8) & 0xff;
                        tb = inPixels[index] & 0xff;
                        xg += X_SOBEL[sr+1][sc+1] * tr;
                        yg += Y_SOBEL[sr+1][sc+1] * tr;
        			}
        		}
        		index = row * width + col;
                x_gradient[index] = xg;
                y_gradient[index] = yg;
                outPixels[index] = (int)Math.sqrt(x_gradient[index] * x_gradient[index]+y_gradient[index]*y_gradient[index]);
        	}
        }
        
        // normalization the MAX
        int max = 0;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		if(max<outPixels[index])
					max=outPixels[index];
        	}
        }
		
        float ratio=(float)max/255;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		int sum = (int)(outPixels[index]/ratio);
        		if(sum < 50)
        		{
        			sum = 0;
        		}
        		outPixels[index] = (255 << 24) | (sum << 16) | (sum << 8) | sum;
        	}
        }
        setRGB( dest, 0, 0, width, height, outPixels );
		return dest;
	}

}
