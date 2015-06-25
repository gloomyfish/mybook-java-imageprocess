package com.book.chapter.six;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;
import com.book.chapter.six.emd.Feature2D;
import com.book.chapter.six.emd.JFastEMD;
import com.book.chapter.six.emd.Signature;

public class HistogramComparisonFilter extends AbstractBufferedImageOp {
	public final static int EUCLIDEAN_DISTANCE = 1;
	public final static int BHATTACHARYYA_COEFFICIENT = 2;
	public final static int EARTH_MOVERS_DISTANCE = 4;
	
	private BufferedImage srcImage;
	private double[] srcHistogramData;
	private int distanceType = 1;
	
	public HistogramComparisonFilter(BufferedImage srcImage)
	{
		this.srcImage = srcImage;
		srcHistogramData = calculateHistogram(srcImage);
	}
	
	public HistogramComparisonFilter(BufferedImage srcImage, int type)
	{
		this.distanceType = type;
		this.srcImage = srcImage;
		srcHistogramData = calculateHistogram(srcImage);
	}
	
	private double[] calculateHistogram(BufferedImage image) {
		double[] bins = distanceType == EARTH_MOVERS_DISTANCE 
				? new double[4*4*4] : new double[16*16*16];
		int width = image.getWidth();
		int height = image.getHeight();
		
		// 从图像中获取像素数据
		int[] inPixels = new int[width * height];
		getRGB(image, 0, 0, width, height, inPixels);
		int index = 0;
		
		// 初始化直方图数据
		for(int i=0; i<bins.length; i++)
		{
			bins[i] = 0;
		}
		
		// 计算RGB每个分量的16 bin的index
		for (int row = 0; row < height; row++) {
			int tr = 0, tg=0, tb=0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
        		index = row * width + col;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
                int level = 16;
                if(distanceType == EARTH_MOVERS_DISTANCE)
                {
                	level = 64;
                }
                int rbinIndex = tr / level;
                int gbinIndex = tg / level;
                int bbinIndex = tb / level;
                int binIndex = rbinIndex + gbinIndex * (256/level) 
                		+ bbinIndex * (256/level) * (256/level);
                bins[binIndex]++;
			}
		}
		
		// 归一化直方图数据
		float total = width * height;
		for(int i=0; i<bins.length; i++)
		{
			bins[i] = bins[i] / total;
		}
		return bins;
	}

	public BufferedImage getSrcImage() {
		return srcImage;
	}

	public void setSrcImage(BufferedImage srcImage) {
		this.srcImage = srcImage;
	}

	public double[] getSrcHistogramData() {
		return srcHistogramData;
	}

	public void setSrcHistogramData(double[] srcHistogramData) {
		this.srcHistogramData = srcHistogramData;
	}

	public int getDistanceType() {
		return distanceType;
	}

	public void setDistanceType(int distanceType) {
		this.distanceType = distanceType;
	}

	public double compareTo(BufferedImage destImage) {
		int width = destImage.getWidth();
		int height = destImage.getHeight();
		if(width != srcImage.getWidth() || height != srcImage.getHeight())
		{
			throw new IllegalArgumentException("图像宽度与高度与源图像不符合！");
		}
		double[] destBins = calculateHistogram(destImage);
		if(getDistanceType() == EUCLIDEAN_DISTANCE)
		{
			
			return calculateEuclideanDis(getSrcHistogramData(), destBins);
		}
		else if(getDistanceType() == BHATTACHARYYA_COEFFICIENT)
		{
			return calculateBhattacharyya(getSrcHistogramData(), destBins);
		}
		else
		{
			return calculateEmd(getSrcHistogramData(), destBins);
		}
	}

	private double calculateEmd(double[] srcBins, double[] destBins) {
		Signature sig1 = getSignature(srcBins, 4);
		Signature sig2 = getSignature(destBins, 4);
		double dist = JFastEMD.distance(sig1, sig2, -1);
		return dist;

	}
    static double getValue(double[] map, int x, int y, int bins) {
        return map[(y * bins) + x];
    }

    static Signature getSignature(double[] map, int bins)
    {
        // find number of entries in the sparse matrix
        int n = 0;
        for (int x = 0; x < bins; x++) {
            for (int y = 0; y < bins; y++) {
                if (getValue(map, x, y, bins) > 0) {
                    n++;
                }
            }
        }
        

        // compute features and weights
        Feature2D[] features = new Feature2D[n];
        double[] weights = new double[n];
        int i = 0;
        for (int x = 0; x < bins; x++) {
            for (int y = 0; y < bins; y++) {
                double val = getValue(map, x, y, bins);
                if (val > 0) {
                    Feature2D f = new Feature2D(x, y);
                    features[i] = f;
                    weights[i] = val;
                    i++;
                }
            }
        }

        Signature signature = new Signature();
        signature.setNumberOfFeatures(n);
        signature.setFeatures(features);
        signature.setWeights(weights);

        return signature;
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

	private double calculateEuclideanDis(double[] srcBins, double[] destBins) {
		double sum = 0;
		for(int i=0; i<srcBins.length; i++)
		{
			sum += Math.pow((srcBins[i] - destBins[i]), 2);
		}
		return Math.sqrt(sum);
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		// TODO Auto-generated method stub
		return null;
	}

}
