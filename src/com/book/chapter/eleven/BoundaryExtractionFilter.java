package com.book.chapter.eleven;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class BoundaryExtractionFilter extends ErosionFilter {
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        // 腐蚀操作
        BufferedImage erosionImage = super.filter(src, null);
        // 获取腐蚀操作之后像素集合与原像素集合
        int[] setA = new int[width*height];
        int[] setB = new int[width*height];
        int[] output = new int[width*height];
        getRGB( src, 0, 0, width, height, setA );
        getRGB( erosionImage, 0, 0, width, height, setB );
        int index = 0;
        // black
        Arrays.fill(output, -16777216);
        // 提取边缘
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				int pa = getPixel(setA, width, height, col, row);
				int pb = getPixel(setB, width, height, col, row);
				if(pa < 127) continue;
				// 对B求补
				if(pb > 127)
				{
					pb = 0;
				}
				else
				{
					pb = 255;
				}
				
				// 设置边缘像素为白色
				if(pb == 255)
				{
					output[index] = -1; 
				}
			}
		}
		setRGB(dest, 0, 0, width, height, output);
		return dest;
	}

}
