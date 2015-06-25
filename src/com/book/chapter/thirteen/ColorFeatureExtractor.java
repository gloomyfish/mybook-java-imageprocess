package com.book.chapter.thirteen;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class ColorFeatureExtractor extends AbstractBufferedImageOp {

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();
		if (dest == null)
			dest = createCompatibleDestImage(src, null);
		int[] inPixels = new int[width*height];
		src.getRGB( 0, 0, width, height, inPixels, 0, width );		
        int index = 0;
        
        // 提取像素
    	float[][] R=new float[height][width];
    	float[][] G=new float[height][width];
    	float[][] B=new float[height][width];
    	float[][] H=new float[height][width];
    	float[][] S=new float[height][width];
    	float[][] V=new float[height][width];
    	float hsv[]=new float[3];
    	for (int row=0;row<height;row++){
    		for (int col=0;col<width;col++){
    			index = row * width + col;
    			int argb = inPixels[index];
    			int r = (argb >> 16) & 0xff;
    			int g = (argb >>  8) & 0xff;
    			int b = (argb) & 0xff;
    			R[row][col]=r;
    			G[row][col]=g;
    			B[row][col]=b;
    			hsv=Color.RGBtoHSB(r, g, b, hsv);
    			H[row][col]=hsv[0];
    			S[row][col]=hsv[1];
    			V[row][col]=hsv[2];
    		}
    	}
    	// 提取颜色特征值
    	double[] redMSS = meanStdSkew(R, height, width);
    	double[] greenMSS = meanStdSkew(G, height, width);
    	double[] blueMSS = meanStdSkew(B, height, width);
    	double[] hMSS = meanStdSkew(H, height, width);
    	double[] sMSS = meanStdSkew(S, height, width);
    	double[] vMSS = meanStdSkew(V, height, width);
    	
    	// 返回结果
    	System.out.println("h = " + hMSS[0]);
    	System.out.println("s = " + sMSS[0]);
    	System.out.println("v = " + vMSS[0]);
    	for (int row=0;row<height;row++){
    		for (int col=0;col<width;col++){
    			index = row * width + col;
    			inPixels[index] = (0xff << 24) | (((int)redMSS[0]) << 16) | (((int)greenMSS[0]) << 8) | ((int)blueMSS[0]);
    		}
    	}
    	setRGB(dest, 0, 0, width, height, inPixels);
        return dest;
	}
	
    public static double[] meanStdSkew( float[][] data, int height, int width )
    {
	    double mean = 0;
	    double[] out=new double[3];
	    
	    for (int row=0;row<height;row++){
        	for (int col=0;col<width;col++){
        		mean += data[row][col];
        	}
	    }
	    mean /= (height*width);
	    out[0]=mean;
	    double sum = 0;
	    for (int row=0;row<height;row++){
        	for (int col=0;col<width;col++){
        		final double v = data[row][col] - mean;
        		sum += v * v;
        	}
	    }
	    out[1]=Math.sqrt( sum / ( height*width - 1 ) );
	    
	    sum = 0;
	    for (int row=0;row<height;row++){
        	for (int col=0;col<width;col++){
        		final double v = (data[row][col] - mean)/out[1];
        		sum += v * v * v;        		
        	}
	    }
	    
	    out[2]=Math.pow(1+sum/(height*width-1),1./3);
	    return out;
    }
	

}
