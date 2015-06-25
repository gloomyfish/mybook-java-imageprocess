package com.book.chapter.ten;

import java.util.ArrayList;
import java.util.List;

public class PixelPoint implements Comparable<PixelPoint> {
	public final static int UNMARKED = -1;
	public final static int VISITED = 1;
	public final static int MARKED = 2;
	private int value;
	private int x; // col
	private int y; // row
	private int status;
	private int label;
	private int dist;
	private List<PixelPoint> neighbours = null;

	public PixelPoint(int row, int col, int pixel) {
		this.status = UNMARKED;

		this.value = pixel;
		this.x = col;
		this.y = row;
		this.dist = 0;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}
	
	public int getDistance()
	{
		return this.dist;
	}
	
	public void setDistance(int dist)
	{
		this.dist = dist;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public void addNeighour(PixelPoint p) {
		if (this.neighbours == null) {
			this.neighbours = new ArrayList<PixelPoint>();
		}
		this.neighbours.add(p);
	}

	public List<PixelPoint> getNeighbours() {
		return this.neighbours;
	}

	public int compareTo(PixelPoint object) {

		if (object.getValue() < getValue()) {
			return 1;
		}
		if (object.getValue() > getValue()) {
			return -1;
		}
		return 0;
	}
}
