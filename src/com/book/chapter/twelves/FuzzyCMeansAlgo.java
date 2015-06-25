package com.book.chapter.twelves;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.book.chapter.four.AbstractBufferedImageOp;

public class FuzzyCMeansAlgo extends AbstractBufferedImageOp {
	private double Eps = Math.pow(10, -5);
	private int numOfCluster;
	private int maxIteration;
	private double accuracy;
	private double fuzzy = 2;
    private double[][] fuzzyForPixels;
    
	private List<FCClusterCenter> clusters;
	private List<PixelPoint> points;

	public FuzzyCMeansAlgo() {
		numOfCluster = 2;
		maxIteration = 20;
		accuracy = 0.00001;
	}

	public FuzzyCMeansAlgo(int numberOfCluster) {
		numOfCluster = numberOfCluster;
		maxIteration = 20;
		accuracy = 0.00001;
		fuzzy = numberOfCluster;
	}

	public int getNumOfCluster() {
		return numOfCluster;
	}

	public void setNumOfCluster(int numOfCluster) {
		this.numOfCluster = numOfCluster;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] inPixels = new int[width*height];
        src.getRGB( 0, 0, width, height, inPixels, 0, width );
        int index = 0;
        // initialization the pixel data
        points = new ArrayList<PixelPoint>();
        for (int row = 0; row < src.getHeight(); ++row)
        {
            for (int col = 0; col < src.getWidth(); ++col)
            {
            	index = row * width + col;
            	int color = inPixels[index];
    			PixelPoint pp = new PixelPoint(row, col);
    			int r = (color >> 16) & 0xff;
    			int g = (color >>  8) & 0xff;
    			int b = (color) & 0xff;
    			pp.setRGB(new float[]{r, g, b});
                points.add(pp);
            }
        }

        //Create random points to use a the cluster centroids
        Random random = new Random();
        clusters = new ArrayList<FCClusterCenter>();
        for (int i = 0; i < numOfCluster; i++)
        {
            int randomNumber1 = random.nextInt(width);
            int randomNumber2 = random.nextInt(height);
            index = randomNumber2 * width + randomNumber1;
            FCClusterCenter fccc = new FCClusterCenter(randomNumber2, randomNumber1);
            fccc.setOriginalPvalue(inPixels[index]);
            fccc.setPvalue(inPixels[index]);
            clusters.add(fccc); 
        }
        
        // Iterate through all points to create initial U matrix
        double diff;
        fuzzyForPixels = new double[this.points.size()][this.clusters.size()];
        for (int i = 0; i < this.points.size(); i++)
        {
        	PixelPoint p = points.get(i);
        	for (int j = 0; j < this.clusters.size(); j++)
            {
            	FCClusterCenter c = this.clusters.get(j);
                diff = Math.sqrt(Math.pow(calculateEuclideanDistance(p, c), 2.0));
                fuzzyForPixels[i][j] = (diff == 0) ? Eps : diff;
            }
         }
        
        // re-calculate the membership value for one point of all clusters, and make suer it's sum of value is 1
        recalculateClusterMembershipValues();
        int k = 0;
        double oldJm = calculateObjectiveFunction();
        do
        {
            k++;
            calculateClusterCentroids();
            stepFuzzy();
            double Jnew = calculateObjectiveFunction();
            System.out.println("Run method accuracy of delta value = " + Math.abs(oldJm - Jnew));
            if (Math.abs(oldJm - Jnew) < accuracy) break;
            oldJm = Jnew;
        }
        while (maxIteration > k);

        //update the original image
        dest = createCompatibleDestImage(src, null );
        index = 0;
        int[] outPixels = new int[width*height];       
        for (int j = 0; j < this.points.size(); j++)
        {
            for (int i = 0; i < this.clusters.size(); i++)
            {
            	PixelPoint p = this.points.get(j);
                if (fuzzyForPixels[j][i] == p.getPossible())
                {
                	int row = (int)p.getRow(); // row
                	int col = (int)p.getCol(); // column
                	index = row * width + col;
                	outPixels[index] = this.clusters.get(i).getPvalue();
                }
            }
        }
        
        // fill the pixel data
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
    public void stepFuzzy()
    {
        for (int c = 0; c < this.clusters.size(); c++)
        {
            for (int h = 0; h < this.points.size(); h++)
            {

                double top;
                top = calculateEuclideanDistance(this.points.get(h), this.clusters.get(c));
                if (top < 1.0) top = Eps;

                // sumTerms is the sum of distances from this data point to all clusters.
                double sumTerms = 0.0;

                for (int ck = 0; ck < this.clusters.size(); ck++)
                {
                    sumTerms += top / calculateEuclideanDistance(this.points.get(h), this.clusters.get(ck));

                }
                // Then the membership value can be calculated as...
                fuzzyForPixels[h][c] = (double)(1.0 / Math.pow(sumTerms, (2 / (this.fuzzy - 1)))); 
            }
        }
        this.recalculateClusterMembershipValues();
    }
	
    public void calculateClusterCentroids()
    {
        for (int j = 0; j < this.clusters.size(); j++)
        {
        	FCClusterCenter clusterCentroid = this.clusters.get(j);
            
            double l = 0.0;
            clusterCentroid.setRedSum(0);
            clusterCentroid.setBlueSum(0);
            clusterCentroid.setGreenSum(0);
            clusterCentroid.setMemberShipSum(0);
            double redSum = 0;
            double greenSum = 0;
            double blueSum = 0;
            double memebershipSum = 0;
            // double pixelCount = 1;

            for (int i = 0; i < this.points.size(); i++)
            {
            
            	PixelPoint p = this.points.get(i);
                l = Math.pow(fuzzyForPixels[i][j], this.fuzzy);
                int tr = (int)p.getRGB()[0];
                int tg = (int)p.getRGB()[1];
                int tb = (int)p.getRGB()[2];
                redSum += l * tr;
                greenSum += l * tg;
                blueSum += l * tb;
                memebershipSum += l;
            }
            
            int clusterColor = (255 << 24) | ((int)(redSum / memebershipSum) << 16) | ((int)(greenSum / memebershipSum) << 8) | (int)(blueSum / memebershipSum);
            clusterCentroid.setPvalue(clusterColor);
         }
    }
	
    public double calculateObjectiveFunction()
    {
        double Jk = 0.0;

        for (int i = 0; i < this.points.size();i++)
        {
            for (int j = 0; j < this.clusters.size(); j++)
            {
                Jk += Math.pow(fuzzyForPixels[i][j], this.fuzzy) * Math.pow(this.calculateEuclideanDistance(points.get(i), clusters.get(j)), 2);
            }
        }
        return Jk;
    }
	
	private void recalculateClusterMembershipValues() 
	{
	
	    for (int i = 0; i < this.points.size(); i++)
	   {
	       double max = 0.0;
	       double min = 0.0;
	       double sum = 0.0;
	       double newmax = 0;
	       PixelPoint p = this.points.get(i);
	       //Normalize the entries
	       for (int j = 0; j < this.clusters.size(); j++)
	       {
	           max = fuzzyForPixels[i][j] > max ? fuzzyForPixels[i][j] : max;
	           min = fuzzyForPixels[i][j] < min ? fuzzyForPixels[i][j] : min;
	       }
	       //Sets the values to the normalized values between 0 and 1
	       for (int j = 0; j < this.clusters.size(); j++)
	       {
	    	   fuzzyForPixels[i][j] = (fuzzyForPixels[i][j] - min) / (max - min);
	           sum += fuzzyForPixels[i][j];
	       }
	       //Makes it so that the sum of all values is 1 
	       for (int j = 0; j < this.clusters.size(); j++)
	       {
	    	   fuzzyForPixels[i][j] = fuzzyForPixels[i][j] / sum;
	           if (Double.isNaN(fuzzyForPixels[i][j]))
	           {
	        	   fuzzyForPixels[i][j] = 0.0;
	           }
	           newmax = fuzzyForPixels[i][j] > newmax ? fuzzyForPixels[i][j] : newmax;
	       }
	       // ClusterIndex is used to store the strongest membership value to a cluster, used for defuzzification
	        p.setPossible(newmax);
	     }
	}
	
	private double calculateEuclideanDistance(PixelPoint p, FCClusterCenter c) 
	{
		// one pixel point
	    int pr = (int)p.getRGB()[0];
	    int pg = (int)p.getRGB()[1];
	    int pb =(int)p.getRGB()[2];
	    // cluster center pixel value
	    int cr = (c.getPvalue() >> 16) & 0xff;
	    int cg = (c.getPvalue() >> 8) & 0xff;
	    int cb = c.getPvalue() & 0xff;
	    // distance
	    return Math.sqrt(Math.pow((pr - cr), 2.0) + 
	    		Math.pow((pg - cg), 2.0) + 
	    		Math.pow((pb - cb), 2.0));
	}

}
