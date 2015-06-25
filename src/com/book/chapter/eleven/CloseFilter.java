package com.book.chapter.eleven;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class CloseFilter extends DilationFilter {

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        // 膨胀操作
        src = super.filter(src, null);
        // 获取腐蚀操作之后像素集合与原像素集合
        int[] setA = new int[width*height];
        int[] output = new int[width*height];
        getRGB( src, 0, 0, width, height, setA );
        int index = 0;
        // 结构元素宽与高
        int seh = (int)(this.getStructureElements().length/2.0f + 0.5f);
        int sew = (int)(this.getStructureElements()[0].length/2.0f + 0.5f);
        Arrays.fill(output, -16777216);// black
        // 腐蚀操作
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				boolean found = false;
				for(int er=-seh; er<(this.getStructureElements().length-seh); er++)
				{
					int nrow = row + er;
					for(int ec=-sew; ec<(this.getStructureElements()[0].length - sew); ec++)
					{
						int ncol = col + ec;
						int g1 = getPixel(setA, width, height, ncol, nrow);
						if(g1<127)
						{
							found = true;
							break;
						}
					}
					if(found)break;
				}
				
				if(!found) {// B与A有交集, 中心像素设为白色
					output[index] = -1; // make it white
				}
			}
		}
		setRGB(dest, 0, 0, width, height, output);
		return dest;
	}
}
