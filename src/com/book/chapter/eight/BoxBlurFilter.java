package com.book.chapter.eight;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;
import com.book.image.util.ImageMath;

public class BoxBlurFilter extends AbstractBufferedImageOp {

	private int hRadius;
	private int vRadius;
	private int iterations = 1;
	
    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );

        for (int i = 0; i < iterations; i++ ) {
            blur( inPixels, outPixels, width, height, hRadius );
            blur( outPixels, inPixels, height, width, vRadius );
        }

        setRGB( dst, 0, 0, width, height, inPixels );
        return dst;
    }

    public void blur( int[] in, int[] out, int width, int height, int radius ) {
        int widthMinus1 = width-1;
        int tableSize = 2*radius+1;
        int lookupTable[] = new int[256*tableSize];

        // 建立查找表
        for ( int i = 0; i < 256*tableSize; i++ )
            lookupTable[i] = i/tableSize; 

        int inIndex = 0;
        
        // 每一行
        for ( int y = 0; y < height; y++ ) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;
            
            // 初始化盒子里面的像素和
            for ( int i = -radius; i <= radius; i++ ) {
                int rgb = in[inIndex + ImageMath.clamp(i, 0, width-1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }
            
            // 每一列，每一个像素
            for ( int x = 0; x < width; x++ ) {
            	// 赋值到输出像素
                out[ outIndex ] = (lookupTable[ta] << 24) 
                		| (lookupTable[tr] << 16) 
                		| (lookupTable[tg] << 8) 
                		| lookupTable[tb];
                // 移动盒子一个像素距离
                int i1 = x+radius+1;
                // 检测是否达到边缘
                if ( i1 > widthMinus1 )
                    i1 = widthMinus1;
                // 将要移出的一个像素
                int i2 = x-radius;
                if ( i2 < 0 )
                    i2 = 0;
                int rgb1 = in[inIndex+i1];
                int rgb2 = in[inIndex+i2];
                // 计算移除与移进像素之间的差值，更新像素和
                ta += ((rgb1 >> 24) & 0xff)-((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000)-(rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00)-(rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff)-(rgb2 & 0xff);
                // 继续到下一行
                outIndex += height;
            }
            // 继续到下一行
            inIndex += width;
        }
    }
        
	public void setHRadius(int hRadius) {
		this.hRadius = hRadius;
	}
	
	public int getHRadius() {
		return hRadius;
	}
	
	public void setVRadius(int vRadius) {
		this.vRadius = vRadius;
	}
	
	public int getVRadius() {
		return vRadius;
	}
	
	public void setRadius(int radius) {
		this.hRadius = this.vRadius = radius;
	}
	
	public int getRadius() {
		return hRadius;
	}
	
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	public String toString() {
		return "Blur/Box Blur...";
	}
}
