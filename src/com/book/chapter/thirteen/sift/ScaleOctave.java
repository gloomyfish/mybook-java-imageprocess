package com.book.chapter.thirteen.sift;



public class ScaleOctave {
	public final static int min_size = 64;
	public final static int max_size = 1024;
	public int STEPS = 1;
	public int width = 0;
	public int height = 0;
	
	private float K = 2.0f; 
	private float K_MIN1_INV = 1.0f / ( K - 1.0f );
	protected float[] sigma;
	private float[] sigmaDiff;
	private float[][] kenDiff;
	private Float2DArray[] images;
	private Float2DArray[] DoG;
	
	public ScaleOctave(Float2DArray src, float[] sigma, float[] sigmaDiff, float[][] kenDiff)
	{
		width = src.width;
		height = src.height;
		if(width == 0 || height == 0)
		{
			throw new java.lang.IllegalArgumentException("width or height of image is not correct");
		}
		if((width <min_size || width > max_size) || (height <min_size || height > max_size))
		{
			throw new java.lang.IllegalArgumentException("width or height of image is not correct");
		}
		images = new Float2DArray[1];
		STEPS = sigma.length - 3;
		images[0] = src;
		K = ( float )Math.pow( 2.0, 1.0 / ( float )STEPS );
		K_MIN1_INV = 1.0f / ( K - 1.0f );
		width = src.width;
		height = src.height;
		this.sigma = sigma;
		this.sigmaDiff = sigmaDiff;
		this.kenDiff = kenDiff;
	}
	
	public void buildStub()
	{
		Float2DArray img = images[ 0 ];
		images = new Float2DArray[ 2 ];
		images[ 0 ] = img;
		images[ 1 ] = GaussianUtil.convolveSeparable( images[ 0 ], kenDiff[STEPS], kenDiff[STEPS] );
	}
	
	public Float2DArray getLevel(int index)
	{
		return images[index];
	}
	
	public Float2DArray[] getDoGs()
	{
		return DoG;
	}
	
	
	
	public void build()
	{
		// build image pyramid
		Float2DArray img = images[0];
		Float2DArray img2 = images[1];
		// build S + 3 images, must have according to paper 2004 
		images = new Float2DArray[STEPS + 3]; 
		images[0] = img;
		images[STEPS] = img2;
		for ( int i = 1; i < kenDiff.length; ++i )
		{
			if (i == STEPS ) continue;
			images[ i ] = GaussianUtil.convolveSeparable( images[0], kenDiff[i], kenDiff[i] );
		}
		// build DOG images
		DoG = new Float2DArray[ STEPS + 2 ];
		for ( int i = 0; i < DoG.length; ++i )
		{
			DoG[ i ] = new Float2DArray(images[i].width, images[i].height );
			int j = i + 1;
			for ( int k = 0; k < images[ i ].data.length; ++k )
			{
				DoG[ i ].data[ k ] = ( images[ j ].data[ k ] - images[ i ].data[ k ] ) * K_MIN1_INV;
			}
		}
	}

}
