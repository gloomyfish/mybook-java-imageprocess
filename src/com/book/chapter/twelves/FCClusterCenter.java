package com.book.chapter.twelves;

public class FCClusterCenter {
	
	public FCClusterCenter(int row, int col)
	{
		this.row = row;
		this.col = col;
	}

	public double getRow() {
		return row;
	}
	public void setRow(double row) {
		this.row = row;
	}
	public double getCol() {
		return col;
	}
	public void setCol(double col) {
		this.col = col;
	}
	public int getPvalue() {
		return pvalue;
	}
	public void setPvalue(int pvalue) {
		this.pvalue = pvalue;
	}
	public double getRedSum() {
		return redSum;
	}
	public void setRedSum(double redSum) {
		this.redSum = redSum;
	}
	public double getGreenSum() {
		return greenSum;
	}
	public void setGreenSum(double greenSum) {
		this.greenSum = greenSum;
	}
	public double getBlueSum() {
		return blueSum;
	}
	public void setBlueSum(double blueSum) {
		this.blueSum = blueSum;
	}
	public double getMemberShipSum() {
		return memberShipSum;
	}
	public void setMemberShipSum(double memberShipSum) {
		this.memberShipSum = memberShipSum;
	}
	public int getOriginalPvalue() {
		return originalPvalue;
	}
	public void setOriginalPvalue(int originalPvalue) {
		this.originalPvalue = originalPvalue;
	}
	private double row;
	private double col;
	private int pvalue;
	private double redSum;
	private double greenSum;
	private double blueSum;
	private double memberShipSum;
	private int originalPvalue;
}
