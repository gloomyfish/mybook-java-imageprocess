package com.book.chapter.eleven;

import java.util.ArrayList;
import java.util.List;

public class PixelPoint implements Comparable<PixelPoint> {
	public final static int INIT = -1;
	public final static int MASK = -2;
	public final static int WSHED = 0;

	private int row;
	private int col;
	private int level;
	private int label;
	private int distance;
	private List<PixelPoint> neighbours;

	public PixelPoint(int row, int col, int gray) {
		this.row = row;
		this.col = col;
		this.level = gray;
		this.label = INIT;
		this.neighbours = new ArrayList<PixelPoint>();
	}

	public PixelPoint() {
		this.level = -100;
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public void setLabelToINIT() {
		label = INIT;
	}

	public void setLabelToMASK() {
		label = MASK;
	}

	public void setLabelToWSHED() {
		label = WSHED;
	}

	public boolean isLabelINIT() {
		return label == INIT;
	}

	public boolean isLabelMASK() {
		return label == MASK;
	}

	public boolean isLabelWSHED() {
		return label == WSHED;
	}
	
	public boolean isFictitious()
	{
		return (level == -100);
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public List<PixelPoint> getNeighbours() {
		return neighbours;
	}

	public void setNeighbours(List<PixelPoint> neighbours) {
		this.neighbours = neighbours;
	}
	
	public boolean allNeighboursAreWSHED()
	{
		boolean result = true;
		for(PixelPoint p :neighbours)
		{
			if(!p.isLabelWSHED())
			{
				result = false;
				break;
			}
		}
		return result;
	}

	@Override
	public int compareTo(PixelPoint pp) {
		if (pp.getLevel() < getLevel())
			return 1;
		else if (pp.getLevel() > getLevel())
			return -1;
		else
			return 0;
	}

}
