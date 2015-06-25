package com.book.chapter.thirteen;

import java.awt.image.BufferedImage;

import com.book.chapter.four.AbstractBufferedImageOp;

public class HoughFilter extends AbstractBufferedImageOp {
	public static final int LINE_TYPE = 1;
	public static final int CIRCLE_TYPE = 2;
	private int type;
	
	public HoughFilter(int type)
	{
		this.type = type;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        
        if(type == LINE_TYPE)
        {
        	LineHough lh = new LineHough();
        	lh.init(inPixels, width, height);
        	outPixels = lh.process();
        }
        else if(type == CIRCLE_TYPE)
        {
        	CircleHough ch = new CircleHough();
        	ch.init(inPixels, width, height, 40);
        	outPixels = ch.process();
        }
        else 
        {
        	throw new IllegalArgumentException("Warning: not supported type...");
        }
        
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
	public BufferedImage getHoughSpaceImage(BufferedImage src, BufferedImage dest)
	{
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        
        if(type == LINE_TYPE)
        {
        	LineHough lh = new LineHough();
        	lh.init(inPixels, width, height);
        	lh.process();
        	int rmax = (int)Math.sqrt(width*width + height*height);
        	BufferedImage houghImage = new BufferedImage(180, rmax, BufferedImage.TYPE_INT_ARGB);
        	setRGB( houghImage, 0, 0, 180, rmax, lh.getAcc() );
        	return dest;
        }
        else if(type == CIRCLE_TYPE)
        {
        	CircleHough ch = new CircleHough();
        	ch.init(inPixels, width, height, 40);
        	ch.process();  	
        	setRGB( dest, 0, 0, width, height, ch.getAcc() );
        	return dest;
        }
        else 
        {
        	throw new IllegalArgumentException("Warning: not supported type...");
        }
	}

}
