package com.book.chapter.twelves;

public class ClusterCenter {
	
	public ClusterCenter(int row, int col)
	{
		this.row = row;
		this.col = col;
		this.index = -1;
		this.numOfPixels = 0;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}
	public int[] getRGB() {
		return rgb;
	}
	public void setRGB(int[] cValue) {
		this.rgb = cValue;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getNumOfPixels() {
		return numOfPixels;
	}
	public void setNumOfPixels(int numOfPixels) {
		this.numOfPixels = numOfPixels;
	}
	
	public void addNumOfPixel()
	{
		numOfPixels++;
	}
	
	private int row;
	private int col;
	private int[] rgb;
	private int index;
	private int numOfPixels;

}
