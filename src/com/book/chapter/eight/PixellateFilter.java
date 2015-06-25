package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class PixellateFilter extends AbstractBufferedImageOp {
	private int size;
	
	public PixellateFilter() {
		// default block size=10x10
		size = 10; 
	}
	
	public PixellateFilter(int size) {
		this.size = size;
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
        int index = 0;
        
        int offsetX = 0, offsetY = 0;
        int newX = 0, newY = 0;
        double total = size*size;
        double sumred = 0, sumgreen = 0, sumblue = 0;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		// 寻找当前像素格
        		newY = (row/size) * size;
        		newX = (col/size) * size;
        		offsetX = newX + size;
        		offsetY = newY + size;
        		// 计算像素格内像素值之和
        		for(int subRow =newY; subRow < offsetY; subRow++) {
        			for(int subCol =newX; subCol < offsetX; subCol++) {
        				if(subRow <0 || subRow >= height) {
        					continue;
        				}
        				if(subCol < 0 || subCol >=width) {
        					continue;
        				}
        				index = subRow * width + subCol;
                		ta = (inPixels[index] >> 24) & 0xff;
                		sumred += (inPixels[index] >> 16) & 0xff;
                		sumgreen += (inPixels[index] >> 8) & 0xff;
                		sumblue += inPixels[index] & 0xff;
        			}
        		}
        		// 计算平均值
        		index = row * width + col;
        		tr = (int)(sumred/total);
        		tg = (int)(sumgreen/total);
        		tb = (int)(sumblue/total);
        		outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
        		// 清空计算下一个像素
        		sumred = sumgreen = sumblue = 0; 
        	}
        }

        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}

}
