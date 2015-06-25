package com.book.chapter.twelves;

public class MeanPoint {
	public MeanPoint(int row, int col, float[] rgb) {
		this.row = row;
		this.col = col;
		this.rgb = rgb;
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

	public float[] getRgb() {
		return rgb;
	}

	public void setRgb(float[] rgb) {
		this.rgb = rgb;
	}

	private int row;
	private int col;
	private float[] rgb;

}
