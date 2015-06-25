package com.book.chapter.twelves;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.book.chapter.eleven.WatershedTransform;
import com.book.chapter.four.AbstractBufferedImageOp;
import com.book.chapter.ten.DFSAlgorithm;
import com.book.chapter.ten.PixelPoint;

public class WatershedSegmentationAlgo extends AbstractBufferedImageOp {
	public static final int[][] X_SOBEL = new int[][]{{-1,-2,-1},{0,0,0},{1,2,1}};
	public static final int[][] Y_SOBEL = new int[][]{{-1, 0, 1},{-2,0,2},{-1,0,1}};
	private int tolerance;
	
	public WatershedSegmentationAlgo()
	{
		tolerance = 10; // default
		System.out.println("Watershed Image Segmentation...");
	}
	
	public int getTolerance() {
		return tolerance;
	}

	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );
        // 图像灰度化
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta = (inPixels[index] >> 24) & 0xff;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
				int gray= (int)(0.299 *tr + 0.587*tg + 0.114*tb);
				outPixels[index]  = gray;
        	}
        }
		// 梯度计算
        int[] gradientResult = gradient(outPixels, width, height);
        setRGB( dest, 0, 0, width, height, gradientResult );
		// 分水岭变换
        WatershedTransform wt = new WatershedTransform();
        dest = wt.filter(dest, null);
        
		// 区域标记
        getRGB( dest, 0, 0, width, height, outPixels );
        List<PixelPoint> pixelList = new ArrayList<PixelPoint>();
        
        // 初始化每个像素节点状态
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		PixelPoint p = new PixelPoint(row, col, (outPixels[index] >> 16) & 0xff);
        		pixelList.add(p);
        	}
        }
        // 添加每个像素节点的四邻域像素
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		PixelPoint p = pixelList.get(index);
        		
        		// add four neighbors for each pixel
        		if((row - 1) >= 0)
        		{
        			index = (row-1) * width + col;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((row + 1) < height)
        		{
        			index = (row+1) * width + col;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((col - 1) >= 0)
        		{
        			index = row * width + col-1;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((col+1) < width)
        		{
        			index = row * width + col+1;
        			p.addNeighour(pixelList.get(index));
        		}
        	}
        }
        
        // 深度优先搜索算法，连通组件标记
        DFSAlgorithm dfs = new DFSAlgorithm(pixelList);
        dfs.process();
        System.out.println("Total Number of Labels : " + dfs.getTotalOfLabels());
        Map<Integer, ArrayList<PixelPoint>> labelPixelMap = new HashMap<Integer, ArrayList<PixelPoint>>();
        for(PixelPoint pp : pixelList)
        {
        	if(pp.getLabel() == 0)continue;
        	Integer iKey = new Integer(pp.getLabel());
        	if(!labelPixelMap.containsKey(iKey))
        	{
        		ArrayList<PixelPoint> labelList = new ArrayList<PixelPoint>();
        		labelPixelMap.put(iKey, labelList);
        	}
        	labelPixelMap.get(iKey).add(pp);
        }
        
        // 直方图相似度合并
        Integer[] labelKeys = labelPixelMap.keySet().toArray(new Integer[0]);
        double[][] allBins = new double[labelKeys.length][4*4*4];
        for(int k=0; k<labelKeys.length; k++)
        {
        	ArrayList<PixelPoint> labeledPixels = labelPixelMap.get(labelKeys[k]);
        	allBins[k] = calculateHistogram(labeledPixels, width, inPixels);
        }
        Arrays.fill(outPixels, -1);
        for(int k=0; k<labelKeys.length; k++)
        {
        	if(!labelPixelMap.containsKey(labelKeys[k])) continue;
        	ArrayList<PixelPoint> labeledPixels = labelPixelMap.get(labelKeys[k]);
        	double[] srcBins = allBins[k];
        	for(int i=0; i<labelKeys.length; i++)
        	{
        		if(k == i) continue;
        		if(!labelPixelMap.containsKey(labelKeys[i])) continue;
        		double[] destBins = allBins[i];
        		if(calculateBhattacharyya(srcBins, destBins)>0.8)
        		{
            		int megerLabel = labeledPixels.get(0).getLabel();
            		for(PixelPoint mp : labelPixelMap.get(labelKeys[i]))
            		{
            			index = mp.getY() * width + mp.getX();
            			outPixels[index] = megerLabel;
            		}
            		labelPixelMap.remove(labelKeys[i]);
        		}
        	}
    		for(PixelPoint pp : labeledPixels)
    		{
    			index = pp.getY() * width + pp.getX();
    			outPixels[index] = pp.getLabel();
    		}
        }
        // 基于距离的合并
        System.out.println(labelPixelMap.size());
        labelKeys = labelPixelMap.keySet().toArray(new Integer[0]);
        
        double[][] cmeans = calculateMeans(labelKeys, inPixels, labelPixelMap, width);
        for(int i=0; i<outPixels.length; i++)
        {
        	if(outPixels[i] == -1)
        	{
        		outPixels[i] = assignToSegment(cmeans, inPixels[i], labelKeys);
        	}
        }
        Map<Integer, java.awt.Color> colorMap = new HashMap<Integer, java.awt.Color>();
        for(int i=0; i<outPixels.length; i++)
        {
        	 getColor(outPixels[i], colorMap);
        }
        labelKeys = colorMap.keySet().toArray(new Integer[0]);
        cmeans = new double[labelKeys.length][3];
        int[] ccounts = new int[labelKeys.length];
        for(int i=0; i<labelKeys.length; i++)
        {
        	double redSum = 0, greenSum = 0, blueSum = 0;
        	double count = 0;
        	for(int m=0; m<outPixels.length; m++)
        	{
        		if(labelKeys[i] == outPixels[m])
        		{
	                int red = (inPixels[m] >> 16) & 0xff;
	                int green = (inPixels[m] >> 8) & 0xff;
	                int blue = inPixels[m] & 0xff;
	                redSum += red;
	                greenSum += green;
	                blueSum += blue;
	                ccounts[i]++;
	                count++;
        		}
        	}
        	cmeans[i][0] = (redSum / count);
        	cmeans[i][1] = (greenSum / count);
        	cmeans[i][2] = (blueSum / count);
        	System.out.println("count = " + count);
        }
        
        for(int i=0; i<labelKeys.length; i++)
        {
    		double minDis = Double.MAX_VALUE;
    		int size = ccounts[i];
    		if(size > 3000) continue;
    		int tag = -1;
    		int foundIndex = -1;
    		for(int j=0; j<labelKeys.length; j++)
    		{
    			if(j == i) continue;
    			if(ccounts[j] < 3000) continue;
    			double dis = calculateEuclideanDistance(cmeans[j], cmeans[i]);
    			if(dis < minDis)
    			{
    				minDis = dis;
    				foundIndex = j;
    			}
    		}
    		if(foundIndex > 0)
    		{
    			for(int f=0; f<outPixels.length; f++)
        		{
        			if(outPixels[f] == labelKeys[i]){
        				outPixels[f] = labelKeys[foundIndex];
        			}
        		}
    		}
        }
        colorMap.clear();
        for(int i=0; i<outPixels.length; i++)
        {
        	 Color c = getColor(outPixels[i], colorMap);
        	 outPixels[i] = (0xff << 24) | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
        }
        System.out.println("final components = " + colorMap.size());
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
	public double[][] calculateMeans(Integer[] labelKeys, int[] inPixels, Map<Integer, ArrayList<PixelPoint>> labelPixelMap, int width)
	{
        double[][] cmeans = new double[labelKeys.length][3];
        for(int i=0; i<labelKeys.length; i++)
        {
        	double redSum = 0, greenSum = 0, blueSum = 0;
        	double count = 0;
        	for(PixelPoint pp : labelPixelMap.get(labelKeys[i]))
        	{
        		int index = pp.getY() * width + pp.getX();
                int red = (inPixels[index] >> 16) & 0xff;
                int green = (inPixels[index] >> 8) & 0xff;
                int blue = inPixels[index] & 0xff;
                redSum += red;
                greenSum += green;
                blueSum += blue;
                count++;
        	}
        	cmeans[i][0] = (redSum / count);
        	cmeans[i][1] = (greenSum / count);
        	cmeans[i][2] = (blueSum / count);
        	// System.out.println("count = " + count);
        }
        return cmeans;
	}
	
	private int assignToSegment(double[][] cmeans, int pixel, Integer[] labelKeys) {
		int label = -1;
		int[] rgb = new int[3];
		rgb[0] = (pixel >> 16) & 0xff;
		rgb[1] = (pixel >> 8) & 0xff;
		rgb[2] = pixel & 0xff;
		double minDis = Double.MAX_VALUE;
		for(int i=0; i<labelKeys.length; i++)
		{
			if(cmeans[i][0] < 0) continue;
			double dis = calculateEuclideanDistance(rgb, cmeans[i]);
			if(dis < minDis)
			{
				label = labelKeys[i];
				minDis = dis;
			}
		}
		return label;
	}
	
	private double calculateEuclideanDistance(int[] rgb, double[] rgb2) 
	{
	    // cluster center
	    int cr = (int)rgb2[0];
	    int cg = (int)rgb2[1];
	    int cb = (int)rgb2[2];
	    
	    return Math.sqrt(Math.pow((rgb[0] - cr), 2.0) + Math.pow((rgb[1] - cg), 2.0) + Math.pow((rgb[2] - cb), 2.0));
	}

	private Color getColor(Integer label, Map<Integer, java.awt.Color> colorMap)
	{
		Color c = colorMap.get(label);
		if(c == null)
		{
			int red = (int)(Math.random() * 255);
			int green = (int)(Math.random() * 255);
			int blue = (int)(Math.random() * 255);
			c = new Color(red, green, blue);
			colorMap.put(label, c);			
		}
		return c;
	}
	
	private double calculateBhattacharyya(double[] srcBins, double[] destBins) {
		double[] mixedData = new double[srcBins.length];
		for(int i=0; i<srcBins.length; i++ ) {
			mixedData[i] = Math.sqrt(srcBins[i] * destBins[i]);
		}
		
		// The values of Bhattacharyya Coefficient 
		// ranges from 0 to 1,
		double similarity = 0;
		for(int i=0; i<mixedData.length; i++ ) {
			similarity += mixedData[i];
		}
		
		// The degree of similarity
		return similarity;
	}
	
	private double[] calculateHistogram(ArrayList<PixelPoint> pixels, int width, int[] inPixels) {
		double[] bins = new double[4*4*4];
		
		// 从图像中获取像素数据
		int index = 0;
		
		// 初始化直方图数据
		for(int i=0; i<bins.length; i++)
		{
			bins[i] = 0;
		}
		
		// 计算RGB每个分量的16 bin的index
		for (PixelPoint pp : pixels) {
			int tr = 0, tg=0, tb=0;
    		index = pp.getY() * width + pp.getX();
            tr = (inPixels[index] >> 16) & 0xff;
            tg = (inPixels[index] >> 8) & 0xff;
            tb = inPixels[index] & 0xff;
            int level = 64;
            int rbinIndex = tr / level;
            int gbinIndex = tg / level;
            int bbinIndex = tb / level;
            int binIndex = rbinIndex + gbinIndex * (256/level) 
            		+ bbinIndex * (256/level) * (256/level);
            bins[binIndex]++;
			
		}
		
		// 归一化直方图数据
		float total = pixels.size();
		for(int i=0; i<bins.length; i++)
		{
			bins[i] = bins[i] / total;
		}
		return bins;
	}
	
	private double calculateEuclideanDistance(double[] p1, double[] p2) 
	{
		// each pixel
	    double pr = p1[0];
	    double pg = p1[1];
	    double pb = p1[2];
	    // cluster center
	    double cr = p2[0];
	    double cg = p2[1];
	    double cb = p2[2];
	    
	    return Math.sqrt(Math.pow((pr - cr), 2.0) + Math.pow((pg - cg), 2.0) + Math.pow((pb - cb), 2.0));
	}

	private int[] gradient(int[] inPixels, int width, int height)
	{
        int[] outPixels = new int[width*height];
        int[] x_gradient = new int[width*height];
        int[] y_gradient = new int[width*height];
        int index = 0;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		int xg = 0, yg = 0;
        		for(int sr=-1; sr<=1; sr++)
        		{
        			for(int sc=-1; sc<=1; sc++)
        			{
        				int nrow = row + sr;
        				int ncol = col + sc;
        				if(nrow < 0 || nrow >= height)
        				{
        					nrow = 0;
        				}
        				if(ncol < 0 || ncol >= width)
        				{
        					ncol = 0;
        				}
        				index = nrow * width + ncol;
                        int gray = inPixels[index];
                        xg += X_SOBEL[sr+1][sc+1] * gray;
                        yg += Y_SOBEL[sr+1][sc+1] * gray;
        			}
        		}
        		index = row * width + col;
                x_gradient[index] = xg;
                y_gradient[index] = yg;
                outPixels[index] = (int)Math.sqrt(x_gradient[index] * x_gradient[index]+y_gradient[index]*y_gradient[index]);
        	}
        }
        
        // normalization the MAX
        int max = 0;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		if(max<outPixels[index])
					max=outPixels[index];
        	}
        }
		
        float ratio=(float)max/255;
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		int sum = (int)(outPixels[index]/ratio);
        		if(sum < tolerance)
        		{
        			sum = 0;
        		}
        		outPixels[index] = (0xff << 24) | (sum << 16) | (sum << 8) | sum;
        	}
        }
        return outPixels;
	}

}
