package com.book.chapter.thirteen;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class CoOccurrenceMatrixFilter extends AbstractBufferedImageOp {
	public final static int ZERO_DEGREES = 0;
	public final static int ANGLE_45_DEGREES = 45;
	public final static int ANGLE_90_DEGREES = 90;
	public final static int ANGLE_135_DEGREES = 135;
	// 角度与步长参数
	private int degrees;
	private int distance;
	public CoOccurrenceMatrixFilter()
	{
		degrees = 0; // angle
		distance = 1; // step
		System.out.println("Co-Occurrence Matrix Extractor");
	}
	
	public int getDegrees() {
		return degrees;
	}

	public void setDegrees(int degrees) {
		this.degrees = degrees;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );
        // 图像灰度化
        int[] pixels = new int[width*height];
        getRGB( src, 0, 0, width, height, pixels );
        int index = 0;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (pixels[index] >> 24) & 0xff;
                tr = (pixels[index] >> 16) & 0xff;
                tg = (pixels[index] >> 8) & 0xff;
                tb = pixels[index] & 0xff;
				int gray= (int)(0.299 *tr + 0.587*tg + 0.114*tb);
				pixels[index]  = (ta << 24) | (gray << 16) | (gray << 8) | gray;
        	}
        }
        // 计算共生矩阵
        int offset = 0;
        double totalPixels = 0;
        double[][] coMatrix = new double[256][256];        
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;// i
        		int igray = (pixels[index] >> 16) & 0xff;
        		if(degrees == ZERO_DEGREES)
        		{
        			offset = col + distance;
        			if(offset >= width)
        			{
        				continue;
        			}
        			index = row * width + offset;
        			int jgray =  (pixels[index] >> 16) & 0xff;
        			coMatrix[igray][jgray] += 1;
        			coMatrix[jgray][igray] += 1;
        			totalPixels += 2;
        		}
        		else if(degrees == ANGLE_45_DEGREES)
        		{
        			offset = col + distance;
        			if(offset >= width)
        			{
        				continue;
        			}
        			if((row - distance) < 0)
        			{
        				continue;
        			}
        			index = (row-distance) * width + offset;
        			int jgray =  (pixels[index] >> 16) & 0xff;
        			coMatrix[igray][jgray] += 1;
        			coMatrix[jgray][igray] += 1;
        			totalPixels += 2;
        		}
        		else if(degrees == ANGLE_90_DEGREES)
        		{
        			offset = row - distance;
        			if(offset < 0)
        			{
        				continue;
        			}
        			index = (offset) * width + col;
        			int jgray =  (pixels[index] >> 16) & 0xff;
        			coMatrix[igray][jgray] += 1;
        			coMatrix[jgray][igray] += 1;
        			totalPixels += 2;
        		}
        		else if(degrees == ANGLE_135_DEGREES)
        		{
        			offset = col - distance;
        			if(offset < 0)
        			{
        				continue;
        			}
        			if((row - distance) < 0)
        			{
        				continue;
        			}
        			index = (row-distance) * width + offset;
        			int jgray =  (pixels[index] >> 16) & 0xff;
        			coMatrix[igray][jgray] += 1;
        			coMatrix[jgray][igray] += 1;
        			totalPixels += 2;
        		}
        	}
        }
        
        // 归一化处理
        for(int i=0; i<256; i++)
        {
        	for(int j=0; j<256; j++)
        	{
        		coMatrix[i][j] = (coMatrix[i][j] / totalPixels);
        	}
        }
        
        // calculates the angular second moment - ASM
    	double asm=0.0;
    	for (int i=0;  i<256; i++)  {
    		for (int j=0; j<256;j++) {
    			asm=asm+ (coMatrix[i][j]*coMatrix[i][j]);
    		}
    	}
    	System.out.println("ASM = " + asm);
    	
    	// calculates the contrast
    	double contrast=0.0;
    	for (int i=0; i<256; i++)  {
    		for (int j=0; j<256; j++) {
    			contrast=contrast+ (i-j)*(i-j)*(coMatrix[i][j]);
    		}
    	}
    	System.out.println("contrast = " + contrast);
    	
    	// 计算和
    	double pi = 0;
    	double pj = 0;
    	for (int i=0; i<256; i++){
            for (int j=0; j<256; j++){
               pi=pi+i*coMatrix [i][j];  
               pj=pj+j*coMatrix [i][j];
			} 
		}
    	// 计算方差
    	double stdevi = 0;
    	double stdevj = 0;
    	for (int i=0; i<256; i++){
    		for (int j=0; j<256; j++){
    			stdevi=stdevi+(i-pi)*(i-pi)*coMatrix [i][j];
    			stdevj=stdevj+(j-pj)*(j-pj)*coMatrix [i][j];
    		}
    	}
    	// 计算相关性
    	double correlation = 0;
    	for (int i=0; i<256; i++)  {
			for (int j=0; j<256; j++) {
				correlation=correlation+( (i-pi)*(j-pj)*coMatrix [i][j]/(stdevi*stdevj)) ;
			}
		}
    	System.out.println("correlation = " + correlation);

    	// result
    	BufferedImage bi = new BufferedImage(256,256, BufferedImage.TYPE_INT_ARGB);
    	int[] outPixels = new int[256 *256];
        for(int i=0; i<256; i++)
        {
        	for(int j=0; j<256; j++)
        	{
        		int pv = (int)(coMatrix[i][j] * 256);
        		pv = clamp(pv);
        		outPixels[index] = (0xff << 24) | (pv << 16) | (pv << 8) | pv;
        	}
        }
        setRGB( bi, 0, 0, 256, 256, outPixels );
        return bi;
	}

}
