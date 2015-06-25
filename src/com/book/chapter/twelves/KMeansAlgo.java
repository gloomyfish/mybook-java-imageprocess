package com.book.chapter.twelves;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.book.chapter.four.AbstractBufferedImageOp;

public class KMeansAlgo extends AbstractBufferedImageOp {
	private List<ClusterCenter> clusterCenterList;
	private List<PixelPoint> pointList;

	private int numOfCluster;
	
	public KMeansAlgo(int clusters) {
		clusterCenterList = new ArrayList<ClusterCenter>();
		pointList = new ArrayList<PixelPoint>();
		numOfCluster = clusters;
	}

	public int getNumOfCluster() {
		return numOfCluster;
	}

	public void setNumOfCluster(int numOfCluster) {
		this.numOfCluster = numOfCluster;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		// initialization the pixel data
        int width = src.getWidth();
        int height = src.getHeight();
        int[] inPixels = new int[width*height];
        src.getRGB( 0, 0, width, height, inPixels, 0, width );
        int index = 0;
        
        //Create random points to use a the cluster center
		Random random = new Random();
		for (int i = 0; i < numOfCluster; i++)
		{
		    int randomNumber1 = random.nextInt(width);
		    int randomNumber2 = random.nextInt(height);
		    index = randomNumber2 * width + randomNumber1;
		    ClusterCenter cp = new ClusterCenter(randomNumber1, randomNumber2);
			int argb = inPixels[i];
			int r = (argb >> 16) & 0xff;
			int g = (argb >>  8) & 0xff;
			int b = (argb) & 0xff;
			cp.setRGB(new int[]{r, g, b});
			cp.setIndex(i);
		    clusterCenterList.add(cp); 
		}
        
        // create all cluster point
        for (int row = 0; row < height; ++row)
        {
            for (int col = 0; col < width; ++col)
            {
            	index = row * width + col;
            	int color = inPixels[index];
    			PixelPoint pp = new PixelPoint(row, col);
    			int r = (color >> 16) & 0xff;
    			int g = (color >>  8) & 0xff;
    			int b = (color) & 0xff;
    			pp.setRGB(new float[]{r, g, b});
    			pp.setLable(-1);
            	pointList.add(pp);
            }
        }
        
        // initialize the clusters for each point
        double[] clusterDisValues = new double[clusterCenterList.size()];
        for(int i=0; i<pointList.size(); i++)
        {
        	for(int j=0; j<clusterCenterList.size(); j++)
        	{
        		clusterDisValues[j] = calculateEuclideanDistance(clusterCenterList.get(j), pointList.get(i));
        	}
        	pointList.get(i).setLable(getCloserCluster(clusterDisValues));
        }
        
        // calculate the old summary
        // assign the points to cluster center
        // calculate the new cluster center
        // computation the delta value
        // stop condition--
        double[] oldClusterCenterColors = reCalculateClusterCenters();
        while(true)
        {
        	stepClusters();
        	double[] newClusterCenterColors = reCalculateClusterCenters();
        	if(isStop(oldClusterCenterColors, newClusterCenterColors))
        	{        		
        		break;
        	} 
        	else
        	{
        		oldClusterCenterColors = newClusterCenterColors;
        	}
        }
        
        //update the result image
        dest = createCompatibleDestImage(src, null );
        index = 0;
        int[] outPixels = new int[width*height];       
        for (int j = 0; j < pointList.size(); j++)
        {
            for (int i = 0; i < clusterCenterList.size(); i++)
            {
            	PixelPoint p = this.pointList.get(j);
                if (clusterCenterList.get(i).getIndex() == p.getLable())
                {
                	int row = p.getRow(); // row
                	int col = p.getCol(); // column
                	index = row * width + col;
                	int[] rgb = clusterCenterList.get(i).getRGB();
                	outPixels[index] = (0xff << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                }
            }
        }
        
        // fill the pixel data
        setRGB( dest, 0, 0, width, height, outPixels );
		return dest;
	}
	
	private boolean isStop(double[] oldClusterCenterColors, double[] newClusterCenterColors) {
		for(int i=0; i<oldClusterCenterColors.length; i++)
		{
			System.out.println("cluster " + i + " old : " + oldClusterCenterColors[i] + ", new : " + newClusterCenterColors[i]);
			if(oldClusterCenterColors[i]  != newClusterCenterColors[i]) 
			{
				return false;
			}
		}
		System.out.println();
		return true;
	}

	/**
	 * update the cluster index by distance value
	 */
	private void stepClusters() 
	{
        // initialize the clusters for each point
        double[] clusterDisValues = new double[clusterCenterList.size()];
        for(int i=0; i<pointList.size(); i++)
        {
        	for(int j=0; j<clusterCenterList.size(); j++)
        	{
        		clusterDisValues[j] = calculateEuclideanDistance(clusterCenterList.get(j),  pointList.get(i));
        	}
        	pointList.get(i).setLable(getCloserCluster(clusterDisValues));
        }
		
	}

	/**
	 * using cluster color of each point to update cluster center color
	 * 
	 * @return
	 */
	private double[] reCalculateClusterCenters() {
		
		// clear the points now
		for(int i=0; i<clusterCenterList.size(); i++)
		{
			 clusterCenterList.get(i).setNumOfPixels(0);
		}
		
		// recalculate the sum and total of points for each cluster
		double[] redSums = new double[3];
		double[] greenSum = new double[3];
		double[] blueSum = new double[3];
		for(int i=0; i<pointList.size(); i++)
		{
			int cIndex = (int)pointList.get(i).getLable();
			clusterCenterList.get(cIndex).addNumOfPixel();
            int tr = (int)pointList.get(i).getRGB()[0];
            int tg = (int)pointList.get(i).getRGB()[1];
            int tb = (int)pointList.get(i).getRGB()[2];
			redSums[cIndex] += tr;
			greenSum[cIndex] += tg;
			blueSum[cIndex] += tb;
		}
		
		double[] oldClusterCentersColors = new double[clusterCenterList.size()];
		for(int i=0; i<clusterCenterList.size(); i++)
		{
			double sum  = clusterCenterList.get(i).getNumOfPixels();
			int cIndex = clusterCenterList.get(i).getIndex();
			int red = (int)(greenSum[cIndex]/sum);
			int green = (int)(greenSum[cIndex]/sum);
			int blue = (int)(blueSum[cIndex]/sum);
			System.out.println("red = " + red + " green = " + green + " blue = " + blue);
			int clusterColor = (255 << 24) | (red << 16) | (green << 8) | blue;
			clusterCenterList.get(i).setRGB(new int[]{red, green, blue});
			oldClusterCentersColors[i] = clusterColor;
		}
		
		return oldClusterCentersColors;
	}
	
	

	/**
	 * 
	 * @param clusterDisValues
	 * @return
	 */
	private int getCloserCluster(double[] clusterDisValues)
	{
		double min = clusterDisValues[0];
		int clusterIndex = 0;
		for(int i=0; i<clusterDisValues.length; i++)
		{
			if(min > clusterDisValues[i])
			{
				min = clusterDisValues[i];
				clusterIndex = i;
			}
		}
		return clusterIndex;
	}

	/**
	 * 
	 * @param point
	 * @param cluster
	 * @return distance value
	 */
	private double calculateEuclideanDistance(ClusterCenter p, PixelPoint c) 
	{
		// each pixel
	    int pr = (int)p.getRGB()[0];
	    int pg = (int)p.getRGB()[1];
	    int pb = (int)p.getRGB()[2];
	    // cluster center
	    int cr = (int)c.getRGB()[0];
	    int cg = (int)c.getRGB()[1];
	    int cb = (int)c.getRGB()[2];
	    
	    return Math.sqrt(Math.pow((pr - cr), 2.0) + Math.pow((pg - cg), 2.0) + Math.pow((pb - cb), 2.0));
	}

}
