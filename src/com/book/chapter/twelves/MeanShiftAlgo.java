package com.book.chapter.twelves;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.book.chapter.four.AbstractBufferedImageOp;


public class MeanShiftAlgo extends AbstractBufferedImageOp {
	
	private int radius;
	private float colorDistance;
	private int numOfCenters;
	private Map<MeanPoint, List<PixelPoint>> allPoints;
	
	public MeanShiftAlgo()
	{
		// default
		this.radius = 3;
		this.colorDistance = 35;
		numOfCenters = 25;
		allPoints = new HashMap<MeanPoint, List<PixelPoint>>();
	}
	
	public MeanShiftAlgo(int radius, float colorDistance)
	{
		this.radius = radius;
		this.colorDistance = colorDistance;
	}
	
	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public float getColorDistance() {
		return colorDistance;
	}

	public void setColorDistance(float colorDistance) {
		this.colorDistance = colorDistance;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels);
        
        // convert RGB color space to YIQ color space
        float[][] pixelsf = new float[width*height][4];
        for(int i=0; i<inPixels.length; i++) {
			int argb = inPixels[i];
			int r = (argb >> 16) & 0xff;
			int g = (argb >>  8) & 0xff;
			int b = (argb) & 0xff;
			pixelsf[i][0] = 0.299f  *r + 0.587f *g + 0.114f  *b; // Y
			pixelsf[i][1] = 0.5957f *r - 0.2744f*g - 0.3212f *b; // I
			pixelsf[i][2] = 0.2114f *r - 0.5226f*g + 0.3111f *b; // Q
			pixelsf[i][3] = 0.0f; // flag
        }

        // initialization the center
        int index = 0;
        MeanPoint[] meanpoints = new MeanPoint[25];
        Random random = new Random();
        
        // start to mean shift algorithm now!!
        int repeat = 0;
        do{
        	// initialize the centers
        	for(int i=0; i<numOfCenters; i++)
        	{
        		int px = random.nextInt(width);
        		int py = random.nextInt(height);
        		int pIndex = py * width + px;
        		meanpoints[i] = new MeanPoint(py, px, new float[]{pixelsf[pIndex][0], pixelsf[pIndex][1],pixelsf[pIndex][2]});
        	}
        	allPoints.clear();
	        for(int i=0; i<numOfCenters; i++)
	        {
	        	meanShfit(meanpoints[i], allPoints, width, height, pixelsf);
	        }
	        
	        // now assign remaining pixels to feature space
	        for(int row=0; row<height; row++) {
	        	for(int col=0; col<width; col++) {
	        		index = row * width + col;
	        		if(pixelsf[index][3] == 0.0f)
	        		{
	        			MeanPoint smp = findSimilarMeans(pixelsf[index], allPoints.keySet());
	        			PixelPoint f = new PixelPoint(row, col);
	        			f.setRGB(new float[]{pixelsf[index][0],pixelsf[index][1],pixelsf[index][2]});
	        			allPoints.get(smp).add(f);
	        		}
	        		else
	        		{
	        			pixelsf[index][3] = 0.0f;// reset , otherwise, big bug!!! fuck!!
	        		}
	        	}
	        }
	        
	        // update with means centers
	        for(MeanPoint mpKey : allPoints.keySet())
	        {   
	        	for(PixelPoint ft : allPoints.get(mpKey))
	        	{
	        		index = ft.getRow() * width + ft.getCol();
	        		pixelsf[index][0] = mpKey.getRgb()[0];
	        		pixelsf[index][1] = mpKey.getRgb()[1];
	        		pixelsf[index][2] = mpKey.getRgb()[2];
	        	}
	        }
	        repeat ++;
        }
        while(repeat < 60);
        
        // merge the result, remove number of pixels is less than 100
        System.out.println("total : " + repeat);
        List<PixelPoint> needToAssigns = new ArrayList<PixelPoint>();
        Map<MeanPoint, List<PixelPoint>> clusterPoints = new HashMap<MeanPoint, List<PixelPoint>>();
        for(MeanPoint mpKey : allPoints.keySet())
        {
        	if(allPoints.get(mpKey).size() > 1000)
        	{
        		clusterPoints.put(mpKey, allPoints.get(mpKey));
        	}
        	else
        	{
        		needToAssigns.addAll(allPoints.get(mpKey));
        	}
        }
        
        for(PixelPoint feature : needToAssigns)
        {
        	MeanPoint nearestMp = findSimilarMeans(feature.getRGB(), clusterPoints.keySet());
        	clusterPoints.get(nearestMp).add(feature);
        }
        
        // display the result use last loop
        System.out.println("Number Of clusters in target image : " + clusterPoints.keySet().size());
        for(MeanPoint mpKey : clusterPoints.keySet())
        {
        	int tr = (int)(mpKey.getRgb()[0] + 0.9563f*mpKey.getRgb()[1] + 0.6210f*mpKey.getRgb()[2]);
        	int tg = (int)(mpKey.getRgb()[0] - 0.2721f*mpKey.getRgb()[2] - 0.6473f*mpKey.getRgb()[2]);
        	int tb = (int)(mpKey.getRgb()[0] - 1.1070f*mpKey.getRgb()[2] + 1.7046f*mpKey.getRgb()[2]);
        	System.out.println("RGB value :" + tr + ", " + tg + ", " + tb);
        	System.out.println("Number of pixels :" + clusterPoints.get(mpKey).size());
        	for(PixelPoint ft : clusterPoints.get(mpKey))
        	{
        		index = ft.getRow() * width + ft.getCol();
        		outPixels[index] = (0xff << 24) | (tr << 16) | (tg << 8) | tb;
        	}
        }
        
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}

	private MeanPoint findSimilarMeans(float[] onePixel, Set<MeanPoint> keySet) {
		MeanPoint[] mps = keySet.toArray(new MeanPoint[0]);
		float deltaY = onePixel[0] - mps[0].getRgb()[0];
		float deltaI = onePixel[1] - mps[0].getRgb()[1];
		float deltaQ = onePixel[2] - mps[0].getRgb()[2];
		float minDis = deltaY * deltaY + deltaI * deltaI + deltaQ * deltaQ;
		MeanPoint mp = mps[0];
		for(int i=1; i<mps.length; i++)
		{
			deltaY = onePixel[0] - mps[i].getRgb()[0];
			deltaI = onePixel[1] - mps[i].getRgb()[1];
			deltaQ = onePixel[2] - mps[i].getRgb()[2];
			float dis = deltaY * deltaY + deltaI * deltaI + deltaQ * deltaQ;
			if(dis < minDis)
			{
				mp = mps[i];
				minDis = dis;
			}
		}
		return mp;
	}

	private void meanShfit(MeanPoint meanPoint, Map<MeanPoint, List<PixelPoint>> result2, int width, int height, float[][] pixelsf) {
		double shift = 0.0;
		// space distance and color distance
        float radius2 = radius * radius;
        float dis2 = colorDistance * colorDistance;
		// current shift center
		int xc = meanPoint.getCol();
		int yc = meanPoint.getRow();
		float Yc = meanPoint.getRgb()[0];
		float Ic = meanPoint.getRgb()[1];
		float Qc = meanPoint.getRgb()[2];
		// save previous shift center
		int xcOld = meanPoint.getCol();
		int ycOld = meanPoint.getRow();
		float YcOld = meanPoint.getRgb()[0];
		float IcOld = meanPoint.getRgb()[1];
		float QcOld = meanPoint.getRgb()[2];
		// calculate 5D, x, y, YIQ
		float[] yiq = new float[]{Yc, Ic, Qc};
		List<PixelPoint> results = new ArrayList<PixelPoint>();
		do
		{
			xcOld = xc;
			ycOld = yc;
			YcOld = Yc;
			IcOld = Ic;
			QcOld = Qc;

			float mx = 0;
			float my = 0;
			float mY = 0;
			float mI = 0;
			float mQ = 0;
			int num=0;
			// calculate the sum based on generated pixel
			for (int ry=-radius; ry <= radius; ry++) {
				int y2 = yc + ry; 
				if (y2 >= 0 && y2 < height) {
					for (int rx=-radius; rx <= radius; rx++) {
						int x2 = xc + rx; 
						if (x2 >= 0 && x2 < width) {
							if (ry*ry + rx*rx <= radius2) {
								yiq = pixelsf[y2*width + x2];
								float Y2 = yiq[0];
								float I2 = yiq[1];
								float Q2 = yiq[2];

								float dY = Yc - Y2;
								float dI = Ic - I2;
								float dQ = Qc - Q2;

								if (dY*dY+dI*dI+dQ*dQ <= dis2) {
									PixelPoint f = new PixelPoint(y2, x2);
									f.setRGB(yiq);
									results.add(f);
									yiq[3] = 100;// flag it 
									mx += x2;
									my += y2;
									mY += Y2;
									mI += I2;
									mQ += Q2;
									num++;
								}
							}
						}
					}
				}
			}
			
			// calculate means
			float num_ = 1f/num;
			Yc = mY*num_; // 得到平均值
			Ic = mI*num_;
			Qc = mQ*num_;
			xc = (int) (mx*num_+0.5);
			yc = (int) (my*num_+0.5);
			// calculate offset
			int dx = xc-xcOld;
			int dy = yc-ycOld;
			float dY = Yc-YcOld;
			float dI = Ic-IcOld;
			float dQ = Qc-QcOld;
			// shift
			shift = dx*dx+dy*dy+dY*dY+dI*dI+dQ*dQ;
			// update center location and YIQ value
			meanPoint.getRgb()[0] = Yc;
			meanPoint.getRgb()[1] = Ic;
			meanPoint.getRgb()[2] = Qc;
			meanPoint.setCol(xc);
			meanPoint.setRow(yc);
		}
		while(shift>0.1);
		
		// start to merge the features, find the local maximum
		boolean flag = false;
		for(MeanPoint mpKey : result2.keySet())
		{
			int deltaY = meanPoint.getRow() - mpKey.getRow();
			int deltaX = meanPoint.getCol() - mpKey.getCol();
			float deltaYc = mpKey.getRgb()[0] - meanPoint.getRgb()[0];
			float deltaIc = mpKey.getRgb()[1] - meanPoint.getRgb()[1];
			float deltaQc = mpKey.getRgb()[2] - meanPoint.getRgb()[2];
			float twoSpaceDis = deltaY * deltaY + deltaX * deltaX;
			float twoColorDis = deltaYc * deltaYc + deltaIc * deltaIc + deltaQc * deltaQc;
			if(twoSpaceDis <= radius2 && twoColorDis <= dis2)
			{
				List<PixelPoint> pList = result2.get(mpKey);
				MergeTwo(pList, results);
				flag = true;
				break;
			}
		}
		// new density center
		if(!flag)  
		{
			result2.put(meanPoint, results);
		}
	}

	private void MergeTwo(List<PixelPoint> pList, List<PixelPoint> results) {
		for(PixelPoint f : results)
		{
			if(!foundIt(f, pList))
			{
				pList.add(f);
			}
		}
		
	}

	private boolean foundIt(PixelPoint f, List<PixelPoint> pList) {
		for(PixelPoint ff : pList)
		{
			if(ff.getRow() == f.getRow() && ff.getCol() == f.getCol())
			{
				return true;
			}
		}
		return false;
	}

}
