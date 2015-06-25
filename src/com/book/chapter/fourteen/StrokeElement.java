package com.book.chapter.fourteen;

public class StrokeElement /*implements Comparable<StrokeElement>*/ {
	public int getXc() {
		return xc;
	}
	public void setXc(int xc) {
		this.xc = xc;
	}
	public int getYc() {
		return yc;
	}
	public void setYc(int yc) {
		this.yc = yc;
	}
	public float getW() {
		return w;
	}
	public void setW(float w) {
		this.w = w;
	}
	public float getL() {
		return l;
	}
	public void setL(float l) {
		this.l = l;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public float getTheta() {
		return theta;
	}
	public void setTheta(float theta) {
		this.theta = theta;
	}
	public int[] getRgb() {
		return rgb;
	}
	public void setRgb(int[] rgb) {
		this.rgb = rgb;
	}
	
	public StrokeElement(int xc, int yc, float w, float l, int level, float theta, int[] rgb)
	{
		this.xc = xc;
		this.yc = yc;
		this.w = w;
		this.l = l;
		this.level = level;
		this.theta = theta;
		this.rgb = rgb;
	}
	
	private int xc;
	private int yc;
	private float w;
	private float l;
	private int level;
	private float theta;
	private int[] rgb;
	
	public float getD() {
		float d1 = l * w;
		return d1;
	}
}
