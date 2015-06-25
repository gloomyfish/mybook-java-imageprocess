package com.book.chapter.ten;

public class ThinPixel {

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

	public int getNumOfBlack() {
		return numOfBlack;
	}

	public void setNumOfBlack(int numOfBlack) {
		this.numOfBlack = numOfBlack;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getP2() {
		return P2;
	}

	public void setP2(int p2) {
		P2 = p2;
	}

	public int getP4() {
		return P4;
	}

	public void setP4(int p4) {
		P4 = p4;
	}

	public int getP6() {
		return P6;
	}

	public void setP6(int p6) {
		P6 = p6;
	}

	public int getP8() {
		return P8;
	}

	public void setP8(int p8) {
		P8 = p8;
	}

	private int row;
	private int col;
	private int numOfBlack;
	private int numOfConnectivity;
	public int getNumOfConnectivity() {
		return numOfConnectivity;
	}

	public void setNumOfConnectivity(int numOfConnectivity) {
		this.numOfConnectivity = numOfConnectivity;
	}

	private int value;
	private int P2;
	private int P4;
	private int P6;
	private int P8;
	
	public ThinPixel(int row, int col, int value) {
		this.row = row;
		this.col = col;
		this.value = value;
	}

}
