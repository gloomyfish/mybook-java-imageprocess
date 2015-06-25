package com.book.chapter.eight;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.book.chapter.four.AbstractBufferedImageOp;

public class StatisticsFilter extends AbstractBufferedImageOp {
	public final static int MAX_FILTER = 1;
	public final static int MIN_FILTER = 2;
	public final static int MIN_MAX_FILTER = 4;
	public final static int MEADIAN_FILTER = 8;
	public final static int MID_POINT_FILTER = 16;
	
	private int kernel_size = 3; // default 3
	private int type = 8; // default mean type
	
	public StatisticsFilter()
	{
		System.out.println("Statistics Filter");
	}
	
	public int getKernelSize() {
		return kernel_size;
	}
	
	public void setKernelSize(int kernelSize) {
		this.kernel_size = kernelSize;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
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
        
		int rows2 = kernel_size/2;
		int cols2 = kernel_size/2;
		int index = 0;
		int index2 = 0;
		float total = kernel_size * kernel_size;
		int[][] matrix = new int[3][(int)total];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int count = 0;
				for (int row = -rows2; row <= rows2; row++) {
					int rowoffset = y + row;
					if(rowoffset < 0 || rowoffset >=height) {
						rowoffset = y;
					}

					for(int col = -cols2; col <= cols2; col++) {
						int coloffset = col + x;
						if(coloffset < 0 || coloffset >= width) {
							coloffset = x;
						}
						index2 = rowoffset * width + coloffset;
						matrix[0][count] = (inPixels[index2] >> 16) & 0xff;
						matrix[1][count] = (inPixels[index2] >> 8) & 0xff;
						matrix[2][count] = inPixels[index2] & 0xff;
						count++; 
					}
				}
				// 统计滤波
				int[] rgb = performFilter(matrix);
				int ia = 0xff;
				int ir = rgb[0];
				int ig = rgb[1];
				int ib = rgb[2];
				outPixels[index++] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
			}
		}
        

        
        // return result
        setRGB( dest, 0, 0, width, height, outPixels );
		return dest;
	}

	private int[] performFilter(int[][] matrix) {
        // pick up one filter from here!!!
		int[] rgb = new int[3];
		int[] trs = matrix[0];
		int[] tgs = matrix[1];
		int[] tbs = matrix[2];
		// 默认升序排序
		Arrays.sort(trs);
		Arrays.sort(tgs);
		Arrays.sort(tbs);
		int count = kernel_size * kernel_size;
		//中值滤波
        if(this.type == MEADIAN_FILTER) 
        {
        	rgb[0] = trs[count/2];
        	rgb[1] = tgs[count/2];
        	rgb[2] = tbs[count/2];
        	
        }
        // 最大-最小值滤波
        else if(this.type == MIN_MAX_FILTER) 
        {
        	rgb[0] = trs[count-1] - trs[0];
        	rgb[1] = tgs[count-1] - tgs[0];
        	rgb[2] = tbs[count-1] - tbs[0];
        }
        // 最大值滤波
        else if(this.type == MAX_FILTER) 
        {
        	rgb[0] = trs[count-1];
        	rgb[1] = tgs[count-1];
        	rgb[2] = tbs[count-1];
        }
        // 最小最滤波
        else if(this.type == MIN_FILTER) 
        {
        	rgb[0] = trs[0];
        	rgb[1] = tgs[0];
        	rgb[2] = tbs[0];
        }
        // 中间点滤波
        else if(this.type == MID_POINT_FILTER) 
        {
        	rgb[0] = (trs[0] + trs[count-1])/2;
        	rgb[1] = (tgs[0] + tgs[count-1])/2;
        	rgb[2] = (tbs[0] + tbs[count-1])/2;
        }
		return rgb;
	}
}
