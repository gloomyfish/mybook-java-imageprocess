package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class BilateralFilter extends AbstractBufferedImageOp {
	public final static double factor = -0.5d;
	private double sigmas; // space 
	private double sigmar; // range
	private int radius;	
	private double[][] sWeightTable;
	private double[] rWeightTable;
	
	public BilateralFilter()
	{
		radius = 2;
		sigmas = 3;
		sigmar = 30;
	}
	
	public double getSigmas() {
		return sigmas;
	}

	public void setSigmas(double sigmas) {
		this.sigmas = sigmas;
	}

	public double getSigmar() {
		return sigmar;
	}

	public void setSigmar(double sigmar) {
		this.sigmar = sigmar;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	private void buildSpaceWeightTable() {
		int size = 2 * radius + 1;
		sWeightTable = new double[size][size];
		for(int sr = -radius; sr <= radius; sr++) {
			for(int sc = - radius; sc <= radius; sc++) {
				// 计算欧几里德距离
				double delta = Math.sqrt(sr * sr + sc * sc)/sigmas;
				// 根据一维高斯公式，计算高斯权重系数
				double deltaDelta = delta * delta;
				int row = sr + radius;
				int col = sc + radius;
				sWeightTable[row][col] = Math.exp(deltaDelta * factor);
			}
		}
	}

	private void buildRangeWeightTable() {
		// 像素值范围 是[0,255]
		rWeightTable = new double[256];
		// 计算像素值的高斯权重
		for(int i=0; i<256; i++) {
			double delta = Math.sqrt(i * i ) / sigmar;
			double deltaDelta = delta * delta;
			rWeightTable[i] = Math.exp(deltaDelta * factor);
		}
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        //int sigmaMax = (int)Math.max(ds, rs);
        //radius = (int)Math.ceil(2 * sigmaMax);
        radius = (int)Math.max(sigmas, sigmar);
        buildSpaceWeightTable();
        buildRangeWeightTable();
        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
		double redSum = 0, greenSum = 0, blueSum = 0;
		double csRedWeight = 0, csGreenWeight = 0, csBlueWeight = 0;
		double csSumRedWeight = 0, csSumGreenWeight = 0, csSumBlueWeight = 0;
		// 由上向下，从左到右循环每个像素，
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
                int rowOffset = 0, colOffset = 0;
                int index2 = 0;
                int ta2 = 0, tr2 = 0, tg2 = 0, tb2 = 0;
        		for(int semirow = -radius; semirow <= radius; semirow++) {
        			for(int semicol = - radius; semicol <= radius; semicol++) {
        				if((row + semirow) >= 0 && (row + semirow) < height) {
        					rowOffset = row + semirow;
        				} else {
        					rowOffset = 0;
        				}
        				
        				if((semicol + col) >= 0 && (semicol + col) < width) {
        					colOffset = col + semicol;
        				} else {
        					colOffset = 0;
        				}
        				index2 = rowOffset * width + colOffset;
        				ta2 = (inPixels[index2] >> 24) & 0xff;
        		        tr2 = (inPixels[index2] >> 16) & 0xff;
        		        tg2 = (inPixels[index2] >> 8) & 0xff;
        		        tb2 = inPixels[index2] & 0xff;
        		        // 在查找表中获取对应权重
        		        csRedWeight = sWeightTable[semirow+radius][semicol+radius]  * rWeightTable[(Math.abs(tr2 - tr))];
        		        csGreenWeight = sWeightTable[semirow+radius][semicol+radius]  * rWeightTable[(Math.abs(tg2 - tg))];
        		        csBlueWeight = sWeightTable[semirow+radius][semicol+radius]  * rWeightTable[(Math.abs(tb2 - tb))];
        		        // 累加权重之和
        		        csSumRedWeight += csRedWeight;
        		        csSumGreenWeight += csGreenWeight;
        		        csSumBlueWeight += csBlueWeight;
        		        // 累加权重像素之和
        		        redSum += (csRedWeight * (double)tr2);
        		        greenSum += (csGreenWeight * (double)tg2);
        		        blueSum += (csBlueWeight * (double)tb2);
        			}
        		}
        		// 归一化获取双边滤波之后的像素值
        		tr = (int)Math.floor(redSum / csSumRedWeight);
        		tg = (int)Math.floor(greenSum / csSumGreenWeight);
        		tb = (int)Math.floor(blueSum / csSumBlueWeight);
                outPixels[index] = (ta << 24) | (clamp(tr) << 16) | (clamp(tg) << 8) | clamp(tb);
                
                // 清空变量，为下一个像素计算做好初始化
                redSum = greenSum = blueSum = 0;
                csRedWeight = csGreenWeight = csBlueWeight = 0;
                csSumRedWeight = csSumGreenWeight = csSumBlueWeight = 0;
                
        	}
        }
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	

}
