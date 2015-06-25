package com.book.chapter.ten;

public class GeometricMomentsAlg {
	private int BACKGROUND = 0; // background color
	private int labelIndex = 1;

	public GeometricMomentsAlg()
	{
		System.out.println("Geometric Moments Algorithm Initialziation...");
	}
	
	public int getLabelIndex() {
		return labelIndex;
	}
	
	public void setLabelIndex(int labelIndex) {
		this.labelIndex = labelIndex;
	}
	
	public int getBACKGROUND() {
		return BACKGROUND;
	}

	public void setBACKGROUND(int bACKGROUND) {
		BACKGROUND = bACKGROUND;
	}
	
	public double[] getGeometricCenterCoordinate(int[] pixels, int width, int height)
	{
		double m00 = moments(pixels, width, height, 0, 0);
		double xCr = moments(pixels, width, height, 1, 0) / m00; // row
		double yCr = moments(pixels, width, height, 0, 1) / m00; // column
		return new double[]{xCr, yCr};
	}

	public double moments(int[] pixels, int width, int height, int p, int q)
	{
		double mpq = 0.0;
		int index = 0;
		for(int row=0; row<height; row++)
		{
			for(int col=0; col<width; col++)
			{
				index = row * width + col;
				if(pixels[index] == BACKGROUND) continue;
				mpq += Math.pow(row, p) * Math.pow(col, q);
			}
		}
		return mpq;
	}
	
	public double centralMoments(int[] pixel, int width, int height, int p, int q)
	{
		double m00 = moments(pixel, width, height, 0, 0);
		double xCr = moments(pixel, width, height, 1, 0) / m00;
		double yCr = moments(pixel, width, height, 0, 1) / m00;
		double cMpq = 0.0;
		int index = 0;
		for(int row=0; row<height; row++)
		{
			for(int col=0; col<width; col++)
			{
				index = row * width + col;
				if(pixel[index] == BACKGROUND) continue;
				cMpq += Math.pow(row - xCr, p) * Math.pow(col - yCr, q);
			}
		}
		return cMpq;
	}
	
	public double normalCentralMoments(int[] pixel, int width, int height, int p, int q)
	{
		double m00 = moments(pixel, width, height, 0, 0);
		double normal = Math.pow(m00, ((double)(p+q+2))/2.0d);
		return centralMoments(pixel, width, height, p, q)/normal;
	}
}
