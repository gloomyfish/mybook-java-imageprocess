package com.book.chapter.ten;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class FloodFillAlgorithm extends AbstractBufferedImageOp {

	private BufferedImage inputImage;
	private int[] inPixels;
	private int width;
	private int height;
	
	// 	stack data structure
	private int maxStackSize = 500; // will be increased as needed
	private int[] xstack = new int[maxStackSize];
	private int[] ystack = new int[maxStackSize];
	private int stackSize;

	public FloodFillAlgorithm(BufferedImage rawImage) {
		this.inputImage = rawImage;
		width = rawImage.getWidth();
        height = rawImage.getHeight();
        inPixels = new int[width*height];
        getRGB(rawImage, 0, 0, width, height, inPixels );
	}

	public BufferedImage getInputImage() {
		return inputImage;
	}

	public void setInputImage(BufferedImage inputImage) {
		this.inputImage = inputImage;
	}
	
	public int getColor(int x, int y)
	{
		int index = y * width + x;
		return inPixels[index];
	}
	
	public void setColor(int x, int y, int newColor)
	{
		int index = y * width + x;
		inPixels[index] = newColor;
	}
	
	public void updateResult()
	{
		setRGB( inputImage, 0, 0, width, height, inPixels );
	}
	
	/**
	 * it is very low calculation speed and cause the stack overflow issue when fill 
	 * some big area and irregular shape. performance is very bad.
	 * 
	 * @param x
	 * @param y
	 * @param newColor
	 * @param oldColor
	 */
	public void floodFill4(int x, int y, int newColor, int oldColor)
	{
	    if(x >= 0 && x < width && y >= 0 && y < height 
	    		&& getColor(x, y) == oldColor && getColor(x, y) != newColor) 
	    { 
	    	setColor(x, y, newColor); //set color before starting recursion
	        floodFill4(x + 1, y,     newColor, oldColor);
	        floodFill4(x - 1, y,     newColor, oldColor);
	        floodFill4(x,     y + 1, newColor, oldColor);
	        floodFill4(x,     y - 1, newColor, oldColor);
	    }   
	}
	/**
	 * 
	 * @param x
	 * @param y
	 * @param newColor
	 * @param oldColor
	 */
	public void floodFill8(int x, int y, int newColor, int oldColor)
	{
	    if(x >= 0 && x < width && y >= 0 && y < height && 
	    		getColor(x, y) == oldColor && getColor(x, y) != newColor) 
	    { 
	    	setColor(x, y, newColor); //set color before starting recursion
	        floodFill8(x + 1, y,     newColor, oldColor);
	        floodFill8(x - 1, y,     newColor, oldColor);
	        floodFill8(x,     y + 1, newColor, oldColor);
	        floodFill8(x,     y - 1, newColor, oldColor);
	        floodFill8(x + 1, y + 1, newColor, oldColor);
	        floodFill8(x - 1, y - 1, newColor, oldColor);
	        floodFill8(x - 1, y + 1, newColor, oldColor);
	        floodFill8(x + 1, y - 1, newColor, oldColor);
	    }   
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param newColor
	 * @param oldColor
	 */
	public void floodFillScanLine(int x, int y, int newColor, int oldColor)
	{
		if(oldColor == newColor) return;
	    if(getColor(x, y) != oldColor) return;
	      
	    int y1;
	    
	    //draw current scanline from start position to the top
	    y1 = y;
	    while(y1 < height && getColor(x, y1) == oldColor)
	    {
	    	setColor(x, y1, newColor);
	        y1++;
	    }    
	    
	    //draw current scanline from start position to the bottom
	    y1 = y - 1;
	    while(y1 >= 0 && getColor(x, y1) == oldColor)
	    {
	    	setColor(x, y1, newColor);
	        y1--;
	    }
	    
	    //test for new scanlines to the left
	    y1 = y;
	    while(y1 < height && getColor(x, y1) == newColor)
	    {
	        if(x > 0 && getColor(x - 1, y1) == oldColor) 
	        {
	        	floodFillScanLine(x - 1, y1, newColor, oldColor);
	        } 
	        y1++;
	    }
	    y1 = y - 1;
	    while(y1 >= 0 && getColor(x, y1) == newColor)
	    {
	        if(x > 0 && getColor(x - 1, y1) == oldColor) 
	        {
	        	floodFillScanLine(x - 1, y1, newColor, oldColor);
	        }
	        y1--;
	    } 
	    
	    //test for new scanlines to the right 
	    y1 = y;
	    while(y1 < height && getColor(x, y1) == newColor)
	    {
	        if(x < width - 1 && getColor(x + 1, y1) == oldColor) 
	        {           
	        	floodFillScanLine(x + 1, y1, newColor, oldColor);
	        } 
	        y1++;
	    }
	    y1 = y - 1;
	    while(y1 >= 0 && getColor(x, y1) == newColor)
	    {
	        if(x < width - 1 && getColor(x + 1, y1) == oldColor) 
	        {
	        	floodFillScanLine(x + 1, y1, newColor, oldColor);
	        }
	        y1--;
	    }
	}
	
	public void floodFillScanLineWithStack(int x, int y, int newColor, int oldColor)
	{
		if(oldColor == newColor) {
			System.out.println("do nothing !!!, filled area!!");
			return;
		}
	    emptyStack();
	    
	    int y1; 
	    boolean spanLeft, spanRight;
	    push(x, y);
	    
	    while(true)
	    {    
	    	x = popx();
	    	if(x == -1) return;
	    	y = popy();
	        y1 = y;
	        while(y1 >= 0 && getColor(x, y1) == oldColor) y1--; // go to line top/bottom
	        y1++; // start from line starting point pixel
	        spanLeft = spanRight = false;
	        while(y1 < height && getColor(x, y1) == oldColor)
	        {
	        	setColor(x, y1, newColor);
	            if(!spanLeft && x > 0 && getColor(x - 1, y1) == oldColor)// just keep left line once in the stack
	            {
	                push(x - 1, y1);
	                spanLeft = true;
	            }
	            else if(spanLeft && x > 0 && getColor(x - 1, y1) != oldColor)
	            {
	                spanLeft = false;
	            }
	            if(!spanRight && x < width - 1 && getColor(x + 1, y1) == oldColor) // just keep right line once in the stack
	            {
	                push(x + 1, y1);
	                spanRight = true;
	            }
	            else if(spanRight && x < width - 1 && getColor(x + 1, y1) != oldColor)
	            {
	                spanRight = false;
	            } 
	            y1++;
	        }
	    }
		
	}
	
	private void emptyStack() {
		while(popx() != - 1) {
			popy();
		}
		stackSize = 0;
	}

	final void push(int x, int y) {
		stackSize++;
		if (stackSize==maxStackSize) {
			int[] newXStack = new int[maxStackSize*2];
			int[] newYStack = new int[maxStackSize*2];
			System.arraycopy(xstack, 0, newXStack, 0, maxStackSize);
			System.arraycopy(ystack, 0, newYStack, 0, maxStackSize);
			xstack = newXStack;
			ystack = newYStack;
			maxStackSize *= 2;
		}
		xstack[stackSize-1] = x;
		ystack[stackSize-1] = y;
	}
	
	final int popx() {
		if (stackSize==0)
			return -1;
		else
            return xstack[stackSize-1];
	}

	final int popy() {
        int value = ystack[stackSize-1];
        stackSize--;
        return value;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		// TODO Auto-generated method stub
		return null;
	}

}
