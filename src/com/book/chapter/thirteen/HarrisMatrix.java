package com.book.chapter.thirteen;

public class HarrisMatrix {
	private double Ix;
	private double Iy;
	private double IxIy;
	private double r;
	private double max;

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public HarrisMatrix()
	{
		max = 0; // always
	}
	
	public double getXGradient() {
		return Ix;
	}
	public void setXGradient(double ix) {
		Ix = ix;
	}
	public double getYGradient() {
		return Iy;
	}
	public void setYGradient(double iy) {
		Iy = iy;
	}
	
	public double getIxIy() {
		return IxIy;
	}

	public void setIxIy(double ixIy) {
		IxIy = ixIy;
	}
	
	public double getR() {
		return r;
	}
	
	public void setR(double r) {
		this.r = r;
	}

}
