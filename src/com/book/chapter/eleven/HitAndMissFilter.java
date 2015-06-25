package com.book.chapter.eleven;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.book.chapter.six.BinaryFilter;

public class HitAndMissFilter extends BinaryFilter {
	// 结构元素, 1 - white, 0 - black, 2 - don't care
	// left-down corner
	// int[][] template = new int[][]{{2,1,2},{0,1,1},{0,0,2}}; 
	// right-down corner
	// int[][] template = new int[][]{{2,1,2},{1,1,0},{2,0,0}};
	private int[][] template; 
	
	public HitAndMissFilter()
	{
		
	}
	
	public int[][] getTemplate() {
		return template;
	}

	public void setTemplate(int[][] structureElements) {
		this.template = structureElements;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        if ( dest == null )
            dest = createCompatibleDestImage( src, null );

        // 二值化
        src = super.filter(src, null);
        int[] setA = new int[width*height];
        int[] output = new int[width*height];
        
        // 像素归一化
        getRGB( src, 0, 0, width, height, setA );
        for(int i=0; i<setA.length; i++)
        {
        	int tr = (setA[i]  >> 16) & 0xff;
        	setA[i] = tr / 255;
        }
        
        //获取中心元素 颜色- 白色为1， 黑色为0
        int index = 0;
        int total = countZeroAndOne();
        Arrays.fill(output, -16777216);
        
        // 结构元素宽与高
		int rr = template.length/2;
		int rc = template[0].length/2;
        
        // 腐蚀操作, 初始化
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				int count = 0;
				for(int trow=-rr; trow<=rr; trow++)
				{
					if((row + trow) < 0 || (row + trow) >= height)
					{
						continue;
					}
					for(int tcol=-rc; tcol<=rc; tcol++)
					{
						if((col + tcol) < 0 || (col + tcol) >= width)
						{
							continue;
						}
						if(template[trow+rr][tcol+rc] == 2)
						{
							continue;
						}
						int index2 = (col+tcol) + (row+trow) * width;
						if(setA[index2] == template[trow+rr][tcol+rc])
						{
							count++;
						}
					}
				}
				if(count == total)
				{
					output[index] = -1;
				}
			}
		}
		setRGB(dest, 0, 0, width, height, output);
		return dest;
	}
	
	private int countZeroAndOne()
	{
		int count = 0;
		for(int i=0; i<template.length; i++)
		{
			for(int j=0; j<template[i].length; j++)
			{
				if(template[i][j] == 1 || template[i][j] == 0)
				{
					count++;
				}
			}
		}
		return count;
	}
	
	public static void main(String[] args)
	{
		int[][] data = new int[][]{
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0},
			{0,0,1,1,1,1,0,0,1,1,1,1,1,1,0,0},
			{0,0,1,1,1,1,0,0,1,1,1,1,1,0,0,0},
			{0,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0},
			{0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
			{0,0,1,1,0,0,0,0,0,1,1,1,1,0,0,0},
			{0,0,1,1,0,0,0,0,0,1,1,1,1,0,0,0},
			{0,0,0,1,0,0,0,0,0,1,1,1,1,0,0,0},
			{0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,1,1,0,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0},
			{0,0,1,1,1,1,1,1,1,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
		};
		// left-down corner
		// int[][] template = new int[][]{{2,1,2},{0,1,1},{0,0,2}}; 
		// right-down corner
		int[][] template = new int[][]{{2,1,2},{1,1,0},{2,0,0}};
		int rr = template.length/2;
		int rc = template[0].length/2;
		int size = data.length;
		int[][] result = new int[size][size];
		for(int row=0; row<size; row++)
		{
			for(int col=0; col<size; col++)
			{
				int count = 0;
				for(int trow=-rr; trow<=rr; trow++)
				{
					if((row + trow) < 0 || (row + trow) >= size)
					{
						continue;
					}
					for(int tcol=-rc; tcol<=rc; tcol++)
					{
						if((col + tcol) < 0 || (col + tcol) >= size)
						{
							continue;
						}
						if(template[trow+rr][tcol+rc] == 2)
						{
							continue;
						}
						if(data[row+trow][col+tcol] == template[trow+rr][tcol+rc])
						{
							count++;
						}
					}
				}
				if(count == 6)
				{
					result[row][col] = 1;
				}
			}
		}
		
		for(int i=0; i<size; i++)
		{
			for(int j=0; j<size; j++)
			{
				if(result[i][j] == 1)
				{
					System.out.println(" row: " + i + " column: " + j + " = " + result[i][j]);
				}
			}
		}
		
	}
}
