package com.book.chapter.twelves;

import java.util.ArrayList;
import java.util.List;


public class PixelPoint implements Comparable<PixelPoint> {
	public final static int UNMARKED = -1;
	public final static int VISITED = 1;
	public final static int MARKED = 2;
	public PixelPoint(int row, int col, int pixel) {
		this.status = UNMARKED;

		this.value = pixel;
		this.col = col;
		this.row = row;
		this.dist = 0;
	}
	
	public PixelPoint(int row, int col)
	{
		this.row = row;
		this.col = col;
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

	public float[] getRGB() {
		return rgb;
	}

	public void setRGB(float[] value) {
		this.rgb = value;
	}

	public int getLable() {
		return lable;
	}

	public void setLable(int lable) {
		this.lable = lable;
	}
	
	public double possible;

	public double getPossible() {
		return possible;
	}
	public void setPossible(double possible) {
		this.possible = possible;
	}

	private int row;
	private int col;
	private float[] rgb;
	private int lable;
	private int value;
	private int status;
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getDist() {
		return dist;
	}

	public void setDist(int dist) {
		this.dist = dist;
	}

	public List<PixelPoint> getNeighbours() {
		return neighbours;
	}

	public void setNeighbours(List<PixelPoint> neighbours) {
		this.neighbours = neighbours;
	}

	private int dist;
	private List<PixelPoint> neighbours = null;

	@Override
	public int compareTo(PixelPoint object) {
		if (object.getValue() < getValue()) {
			return 1;
		}
		if (object.getValue() > getValue()) {
			return -1;
		}
		return 0;
	}

	public void addNeighour(PixelPoint pixelPoint) {
		if(this.neighbours == null)
		{
			this.neighbours = new ArrayList<PixelPoint>();
		}
		this.neighbours.add(pixelPoint);
		
	}

}
