package com.book.chapter.eleven;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;

import com.book.chapter.four.AbstractBufferedImageOp;

public class DistanceTransform extends AbstractBufferedImageOp {
	
	public static final int background = 255;
	public static final int foreground = 0;

	private float scaleValue;
	private float offsetValue;
	private int width;
	private int height;
	private int[][] pixels2D;
	private HashSet<Point> foregroundEdgePixels = new HashSet<Point>();
	private HashSet<Point> backgroundEdgePixels = new HashSet<Point>();
	private BufferedImage inputImage;
	private int[][] greyLevel;

	// main algorithm code implementation classes
	// 1.binary image, gray level initialization 
	// 2.calculate the foreground and background edge
	// 3.Distance each by corrosion operation using 3*3 matrix
	
	public DistanceTransform(float scaleValue, float offsetValue, BufferedImage src)
	{
		this.scaleValue = scaleValue;
		this.offsetValue = offsetValue;
		this.inputImage = src;
		this.width = src.getWidth();
		this.height = src.getHeight();
        int[] inPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
        pixels2D = new int[height][width]; // row, column
        greyLevel = new int[height][width];
        for(int row=0; row < height; row++)
        {
        	for(int col=0; col<width; col++) 
        	{
        		index = row * width + col;
        		int grayValue = (inPixels[index] >> 16) & 0xff;
        		pixels2D[row][col] = grayValue;
        		greyLevel[row][col] = 0;
        	}
        }
        
        generateForegroundEdge();
        generateBackgroundEdgeFromForegroundEdge();
        
	}
	
	  private void generateForegroundEdge()
	  {
	    foregroundEdgePixels.clear();
	
	    for (int row = 0; row < height; row++)
	      for (int col = 0; col < width; col++)
	        if (this.pixels2D[row][col] == foreground) {
	          Point localPoint = new Point(col, row);
	          for (int k = -1; k < 2; ++k) // 3*3 matrix
	        	  for (int l = -1; l < 2; ++l) { // ¸¯Ê´²Ù×÷
	              if ((localPoint.x + l < 0) || (localPoint.x + l >= this.width) || (localPoint.y + k < 0) || (localPoint.y + k >= this.height) || 
	                (this.pixels2D[(localPoint.y + k)][(localPoint.x + l)] != background) || (this.foregroundEdgePixels.contains(localPoint)))
	                continue;
	              this.foregroundEdgePixels.add(localPoint);
	            }
	        }
	  }
	  
	  private void generateBackgroundEdgeFromForegroundEdge()
	  {
	    this.backgroundEdgePixels.clear();
	
	    Iterator<Point> localIterator = this.foregroundEdgePixels.iterator();
	    while (localIterator.hasNext()) {
	      Point localPoint1 = new Point((Point)localIterator.next());
	      for (int i = -1; i < 2; ++i)
	        for (int j = -1; j < 2; ++j)
	          if ((localPoint1.x + j >= 0) && (localPoint1.x + j < this.width) && (localPoint1.y + i >= 0) && (localPoint1.y + i < this.height)) {
	            Point localPoint2 = new Point(localPoint1.x + j, localPoint1.y + i);
	            if (this.pixels2D[localPoint2.y][localPoint2.x] == background)
	              this.backgroundEdgePixels.add(localPoint2);
	          }
	    }
	  }
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		
		// calculate the distance here!!
		int index = 1;
	    while (foregroundEdgePixels.size() > 0) {
	    	distanceSingleIteration(index);
	        ++index;
	    }
	    
	    // loop the each pixel and assign the color value according to distance value
		for (int row = 0; row < inputImage.getHeight(); row++) {
		      for (int col = 0; col < inputImage.getWidth(); col++) {
		    	  if(greyLevel[row][col] > 0) {
			    	  int colorValue = (int)Math.round(greyLevel[row][col] * scaleValue + offsetValue);
			    	  colorValue = colorValue > 255 ? 255 : ((colorValue < 0) ? 0 : colorValue);
			    	  this.pixels2D[row][col] = colorValue;
		    	  }
		    	  
		      }
		}
		
		// build the result pixel data at here !!!
	    if ( dest == null )
	        dest = createCompatibleDestImage(inputImage, null );
	    
	    index = 0;
	    int[] outPixels = new int[width*height];
	    for(int row=0; row<height; row++) {
	    	int ta = 0, tr = 0, tg = 0, tb = 0;
	    	for(int col=0; col<width; col++) {
	    		index = row * width + col;
	    		tr = tg = tb = this.pixels2D[row][col];
	    		ta = 255;
	    		outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
	    	}
	    }
	    setRGB( dest, 0, 0, width, height, outPixels );
		return dest;
	}
	
	  private void distanceSingleIteration(int paramInt)
	  {
	    Iterator<Point> localIterator = foregroundEdgePixels.iterator();
	    while (localIterator.hasNext()) {
	      Point localPoint = new Point((Point)localIterator.next());
	      backgroundEdgePixels.add(localPoint);
	      removePixel(localPoint);
	      greyLevel[localPoint.y][localPoint.x] = paramInt;
	    }
	    generateForegroundEdgeFromBackgroundEdge();
	  }
	  
	  private void generateForegroundEdgeFromBackgroundEdge()
	  {
	    this.foregroundEdgePixels.clear();

	    Iterator<Point> localIterator = this.backgroundEdgePixels.iterator();
	    while (localIterator.hasNext()) {
	      Point localPoint1 = new Point((Point)localIterator.next());
	      for (int i = -1; i < 2; ++i)
	        for (int j = -1; j < 2; ++j)
	          if ((localPoint1.x + j >= 0) && (localPoint1.x + j < this.width) && (localPoint1.y + i >= 0) && (localPoint1.y + i < this.height)) {
	            Point localPoint2 = new Point(localPoint1.x + j, localPoint1.y + i);
	            if (this.pixels2D[localPoint2.y][localPoint2.x] == foreground)
	              this.foregroundEdgePixels.add(localPoint2);
	          }
	    }
	  }
	  
   private void removePixel(Point paramPoint)
   {
	    this.pixels2D[paramPoint.y][paramPoint.x]= background;
   }

    public float getScaleValue() {
		return scaleValue;
	}

	public void setScaleValue(float scaleValue) {
		this.scaleValue = scaleValue;
	}

	public float getOffsetValue() {
		return offsetValue;
	}

	public void setOffsetValue(float offsetValue) {
		this.offsetValue = offsetValue;
	}

}
