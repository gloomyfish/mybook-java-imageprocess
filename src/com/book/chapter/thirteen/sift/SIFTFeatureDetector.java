package com.book.chapter.thirteen.sift;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.Vector;

import com.book.chapter.four.AbstractBufferedImageOp;

public class SIFTFeatureDetector extends AbstractBufferedImageOp {
	private int FEATURE_DESCRIPTOR_SIZE = 4;
	private int FEATURE_DESCRIPTOR_WIDTH = 16;
	private int FEATURE_DESCRIPTOR_ORIENTATION_BINS = 8;
	private float FEATURE_DESCRIPTOR_ORIENTATION_BIN_SIZE = (2.0f * ( float )Math.PI / ( float )8);
	private static float initial_sigma = 1.6f;
	private static int steps = 2; // means s , final images = s + 3
	private static int min_size = 64;
	private static int max_size = 1024;
	private OctaveKeyPointersDetector doGDetector;
	private float[][] descriptorMask;
	
	public SIFTFeatureDetector()
	{
		initParameters();
	}
	private void initParameters() {
		
		descriptorMask = new float[ FEATURE_DESCRIPTOR_SIZE * 4 ][ FEATURE_DESCRIPTOR_SIZE * 4 ];

		float two_sq_sigma = FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_SIZE * 8;
		for ( int y = FEATURE_DESCRIPTOR_SIZE * 2 - 1; y >= 0; --y )
		{
			float fy = ( float )y + 0.5f;
			for ( int x = FEATURE_DESCRIPTOR_SIZE * 2 - 1; x >= 0; --x )
			{
				float fx = ( float )x + 0.5f;
				float val = ( float )Math.exp( -( fy * fy + fx * fx ) / two_sq_sigma );
				descriptorMask[ 2 * FEATURE_DESCRIPTOR_SIZE - 1 - y ][ 2 * FEATURE_DESCRIPTOR_SIZE - 1 - x ] = val;
				descriptorMask[ 2 * FEATURE_DESCRIPTOR_SIZE + y ][ 2 * FEATURE_DESCRIPTOR_SIZE - 1 - x ] = val;
				descriptorMask[ 2 * FEATURE_DESCRIPTOR_SIZE - 1 - y ][ 2 * FEATURE_DESCRIPTOR_SIZE + x ] = val;
				descriptorMask[ 2 * FEATURE_DESCRIPTOR_SIZE + y ][ 2 * FEATURE_DESCRIPTOR_SIZE + x ] = val;
			}
		}
		
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        // 预处理 1- 图像灰度化 - convert to gray image
        int[] inPixels = new int[width*height];
        Float2DArray fp = new Float2DArray(width, height);
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
        for(int row=0; row<height; row++) {
        	int tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
                tr = (inPixels[index] >> 16) & 0xff;
                tg = (inPixels[index] >> 8) & 0xff;
                tb = inPixels[index] & 0xff;
				int gray= (int)(0.299 *tr + 0.587*tg + 0.114*tb);
				inPixels[index]  = gray;
        	}
        }
        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );
        
		// 预处理 2 - 归一化灰度值到[0,1]之间，这对SIFT算法运算非常重要
        // normalization value to [0,1]
    	float min = Float.MAX_VALUE;
    	float max = Float.MIN_VALUE;
    	for ( int i = 0; i < inPixels.length; ++i )
    	{
    		if ( inPixels[ i ] < min ) 
    			min = inPixels[ i ];
    		else if (inPixels[ i ] > max ) 
    			max = inPixels[ i ];
    	}
    	float scale = 1.0f;
    	scale /= ( max - min );
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		float value = scale * ( inPixels[ index ] - min );
        		fp.setValue(col, row, value);
        	}
        }
        // 预处理 3 blur it by Gaussian
        Float2DArray blurredData = GaussianUtil.blurWith1DGaussian( fp, ( float )Math.sqrt( initial_sigma * initial_sigma - 0.25 ) );
        // display result for testing, very well, i test it
        setRGB( dest, 0, 0, width, height, getTestPixels(blurredData) );
		float w = ( float )width;
		float h = ( float )height;
		int numOctaves = 0;
		while ( w >= ( float )min_size && h >= ( float )min_size )
		{
			w /= 2.0f;
			h /= 2.0f;
			++numOctaves;
		}
		ScaleOctave[] octaves = new ScaleOctave[numOctaves];
		// prepare to build scale space of octave
		float[] sigma = new float[steps+3];
		sigma[ 0 ] = initial_sigma;
		float[] sigma_diff = new float[steps+3];
		sigma_diff[ 0 ] = 0.0f;
		float[][] kernel_diff = new float[steps+3][];
		// initialization parameters
		for ( int i = 1; i <steps+3; ++i )
		{
			sigma[ i ] = initial_sigma * ( float )Math.pow( 2.0f, ( float )i / ( float )steps );// k = Math.pow(2, 1/s);
			sigma_diff[ i ] = ( float )Math.sqrt( sigma[ i ] * sigma[ i ] - initial_sigma * initial_sigma );
			kernel_diff[ i ] = GaussianUtil.createGaussianKernel1D( sigma_diff[ i ], true );
		}
        
        // 步骤 1 -> Detection of scale-space extrema
		// build scale space of octave
		Float2DArray next;
		for ( int i = 0; i < octaves.length; ++i )
		{
			octaves[ i ] = new ScaleOctave(
					blurredData,
					sigma,
					sigma_diff,
					kernel_diff );
			octaves[ i ].buildStub();
			next = new Float2DArray(
					blurredData.width / 2 + blurredData.width % 2,
					blurredData.height / 2 + blurredData.height % 2 );
			GaussianUtil.downsample( octaves[ i ].getLevel( 1 ), next );
			if ( blurredData.width > max_size || blurredData.height > max_size )
				octaves[ i ] = null;
			blurredData = next;
		}
		
		// find extreme value and filter out-lier values
		doGDetector = new OctaveKeyPointersDetector();
		Vector< Feature > features = new Vector< Feature >();
		for ( int o = 0; o < octaves.length; ++o )
		{
			if ( octaves[ o ].width <= max_size && octaves[ o ].height <= max_size )
			{
				Vector< Feature > more = runOctave( octaves[ o ], o );
				features.addAll( more );
			}
		}
		
		// draw result rectangles by description and pointers
		System.out.println("final total features = " + features.size());
		Graphics2D g2d = dest.createGraphics();
		g2d.setPaint(Color.RED);
		for(Feature f : features)
		{
			System.out.println("key point coordinate: x =" + f.location[ 0 ] + ", y = " + f.location[ 1 ]);
			drawSquare( g2d, new double[]{ f.location[ 0 ], f.location[ 1 ] }, 4 * ( double )f.scale, ( double )f.orientation );
		}
		return dest;
	}
	
	public static void drawSquare( Graphics2D g2d, double[] o, double scale, double orient )
	{
		scale /= 2;
		
	    double sin = Math.sin( orient );
	    double cos = Math.cos( orient );
	    
	    int[] x = new int[ 5 ];
	    int[] y = new int[ 5 ];
	    // generate the pointers
	    x[ 0 ] = ( int )( o[ 0 ] + ( sin - cos ) * scale );
	    y[ 0 ] = ( int )( o[ 1 ] - ( sin + cos ) * scale );
	    x[ 1 ] = ( int )( o[ 0 ] + ( sin + cos ) * scale );
	    y[ 1 ] = ( int )( o[ 1 ] + ( sin - cos ) * scale );
	    x[ 2 ] = ( int )( o[ 0 ] - ( sin - cos ) * scale );
	    y[ 2 ] = ( int )( o[ 1 ] + ( sin + cos ) * scale );
	    x[ 3 ] = ( int )( o[ 0 ] - ( sin + cos ) * scale );
	    y[ 3 ] = ( int )( o[ 1 ] - ( sin - cos ) * scale );
	    x[ 4 ] = x[ 0 ];
	    y[ 4 ] = y[ 0 ];
	    // draw red rectangle
	    g2d.draw(new Polygon( x, y, x.length ) );
	}
	
	private Vector<Feature> runOctave(ScaleOctave scaleOctave, int octaveIndex) 
	{
		Vector< Feature > features = new Vector< Feature >();
		scaleOctave.build(); // build DOG
		doGDetector.run( scaleOctave );
		Vector< float[] > candidates = doGDetector.getCandidates();
		System.out.println("total candidates = " + candidates.size());
		for ( float[] c : candidates )
		{
			this.processCandidate( c, scaleOctave, octaveIndex, features );
		}
		System.out.println( features.size() + " candidates processed in octave " + octaveIndex );
		return features;
	}
	/***
	 * handle the candidate key points, invariant of rotate
	 * 
	 * @param c
	 * @param scaleOctave
	 * @param octaveIndex
	 * @param features
	 */
	private void processCandidate(float[] c, ScaleOctave scaleOctave, int octaveIndex, Vector<Feature> features)
	{
		final int ORIENTATION_BINS = 36;
		final float ORIENTATION_BIN_SIZE = 2.0f * ( float )Math.PI / ( float )ORIENTATION_BINS;
		float[] histogram_bins = new float[ ORIENTATION_BINS ];
		int scale = ( int )Math.pow( 2, octaveIndex );
		
		// get current layer sigma of the Octave
		float octave_sigma = scaleOctave.sigma[ 0 ] * ( float )Math.pow( 2.0f, c[ 2 ]);
				
		// create a circular gaussian window with sigma 1.5 times that of the key point
		Float2DArray gaussianMask =
				GaussianUtil.create_gaussian_kernel_2D_offset(
					octave_sigma * 1.5f,
					c[ 0 ] - ( float )Math.floor( c[ 0 ] ), // x
					c[ 1 ] - ( float )Math.floor( c[ 1 ] ), // y
					false );
		
		// get the gradients in a region around the key points location
		Float2DArray[] src = GaussianUtil.createGradients(scaleOctave.getLevel(Math.round(c[ 2 ] )));
		Float2DArray[] gradientROI = new Float2DArray[ 2 ];
		gradientROI[ 0 ] = new Float2DArray( gaussianMask.width, gaussianMask.width );
		gradientROI[ 1 ] = new Float2DArray( gaussianMask.width, gaussianMask.width );
		
		int half_size = gaussianMask.width / 2;
		int p = gaussianMask.width * gaussianMask.width - 1;
		for ( int yi = gaussianMask.width - 1; yi >= 0; --yi )
		{
			int ra_y = src[ 0 ].width * Math.max( 0, Math.min( src[ 0 ].height - 1, ( int )c[ 1 ] + yi - half_size ) );
			int ra_x = ra_y + Math.min( ( int )c[ 0 ], src[ 0 ].width - 1 );

			for ( int xi = gaussianMask.width - 1; xi >= 0; --xi )
			{
				int pt = Math.max( ra_y, Math.min( ra_y + src[ 0 ].width - 2, ra_x + xi - half_size ) );
				gradientROI[ 0 ].data[ p ] = src[ 0 ].data[ pt ];
				gradientROI[ 1 ].data[ p ] = src[ 1 ].data[ pt ];
				--p;
			}
		}
		
		// calculate weighted gradient of each pixels around key points
		for ( int i = 0; i < gradientROI[ 0 ].data.length; ++i )
		{
			gradientROI[ 0 ].data[ i ] *= gaussianMask.data[ i ];
		}

		// build an orientation histogram of the sub region
		for ( int i = 0; i < gradientROI[ 0 ].data.length; ++i )
		{
			int bin = Math.max( 0, ( int )( ( gradientROI[ 1 ].data[ i ] + Math.PI ) / ORIENTATION_BIN_SIZE ) );
			histogram_bins[ bin ] += gradientROI[ 0 ].data[ i ];
		}

		// find the max value
		int max_i = 0;
		for ( int i = 0; i < ORIENTATION_BINS; ++i )
		{
			if ( histogram_bins[ i ] > histogram_bins[ max_i ] ) max_i = i;
		}
		
		/**
		 * interpolate orientation estimate the offset from center of the
		 * parabolic extremum of the taylor series through env[1], derivatives
		 * via central difference and laplace
		 */
		float e0 = histogram_bins[ ( max_i + ORIENTATION_BINS - 1 ) % ORIENTATION_BINS ];
		float e1 = histogram_bins[ max_i ];
		float e2 = histogram_bins[ ( max_i + 1 ) % ORIENTATION_BINS ];
		float offset = ( e0 - e2 ) / 2.0f / ( e0 - 2.0f * e1 + e2 );
		float orientation = ( ( float )max_i + offset ) * ORIENTATION_BIN_SIZE - ( float )Math.PI;
		// assign descriptor and add the Feature instance to the collection
		features.addElement(
				new Feature(
						octave_sigma * scale,
						orientation, // angle
						new float[]{ c[ 0 ] * scale, c[ 1 ] * scale }, //{x, y}
						//new float[]{ ( c[ 0 ] + 0.5f ) * scale - 0.5f, ( c[ 1 ] + 0.5f ) * scale - 0.5f },
						createDescriptor( c, scaleOctave, octaveIndex, octave_sigma, orientation ) ) );
	}
	
	private float[] createDescriptor(float[] c, ScaleOctave scaleOctave, int octaveIndex, float octave_sigma, float orientation) {
		Float2DArray[] gradients = GaussianUtil.createGradients(scaleOctave.getLevel(Math.round(c[ 2 ] )));
		Float2DArray[] region = new Float2DArray[ 2 ];
		
		region[ 0 ] = new Float2DArray(
				FEATURE_DESCRIPTOR_WIDTH,
				FEATURE_DESCRIPTOR_WIDTH );
		region[ 1 ] = new Float2DArray(
				FEATURE_DESCRIPTOR_WIDTH,
				FEATURE_DESCRIPTOR_WIDTH );
		float cos_o = ( float )Math.cos( orientation );
		float sin_o = ( float )Math.sin( orientation );
		
		// sample the region around the key points location
		for ( int y = FEATURE_DESCRIPTOR_WIDTH - 1; y >= 0; --y )
		{
			float ys =
				( ( float )y - 2.0f * ( float )FEATURE_DESCRIPTOR_SIZE + 0.5f ) * octave_sigma; //!< scale y around 0,0
			for ( int x = FEATURE_DESCRIPTOR_WIDTH - 1; x >= 0; --x )
			{
				float xs =
					( ( float )x - 2.0f * ( float )FEATURE_DESCRIPTOR_SIZE + 0.5f ) * octave_sigma; //!< scale x around 0,0
				float yr = cos_o * ys + sin_o * xs; //!< rotate y around 0,0
				float xr = cos_o * xs - sin_o * ys; //!< rotate x around 0,0

				// translate ys to sample y position in the gradient image
				int yg = GaussianUtil.flipInRange(
						( int )( Math.round( yr + c[ 1 ] ) ),
						gradients[ 0 ].height );

				// translate xs to sample x position in the gradient image
				int xg = GaussianUtil.flipInRange(
						( int )( Math.round( xr + c[ 0 ] ) ),
						gradients[ 0 ].width );

				// get the samples
				int region_p = FEATURE_DESCRIPTOR_WIDTH * y + x;
				int gradient_p = gradients[ 0 ].width * yg + xg;

				// weigh the gradients
				region[ 0 ].data[ region_p ] = gradients[ 0 ].data[ gradient_p ] * descriptorMask[ y ][ x ];

				// rotate the gradients orientation it with respect to the features orientation
				region[ 1 ].data[ region_p ] = gradients[ 1 ].data[ gradient_p ] - orientation;
			}
		}
		
		// 建立4x4的16个直方图，每个直方图有8个方向
		float[][][] hist = new float[ FEATURE_DESCRIPTOR_SIZE ][ FEATURE_DESCRIPTOR_SIZE ][ FEATURE_DESCRIPTOR_ORIENTATION_BINS ];

		// build the orientation histograms of 4x4 sub regions
		for ( int y = FEATURE_DESCRIPTOR_SIZE - 1; y >= 0; --y )
		{
			int yp = FEATURE_DESCRIPTOR_SIZE * 16 * y;
			for ( int x = FEATURE_DESCRIPTOR_SIZE - 1; x >= 0; --x )
			{
				int xp = 4 * x;
				for ( int ysr = 3; ysr >= 0; --ysr )
				{
					int ysrp = 4 * FEATURE_DESCRIPTOR_SIZE * ysr;
					for ( int xsr = 3; xsr >= 0; --xsr )
					{
						// make it scope in 0 ~ 2PI
						float bin_location = ( region[ 1 ].data[ yp + xp + ysrp + xsr ] + ( float )Math.PI ) / ( float )FEATURE_DESCRIPTOR_ORIENTATION_BIN_SIZE;
						// calculate rate for line nearest interpolation
						int bin_b = ( int )( bin_location );
						int bin_t = bin_b + 1;
						float d = bin_location - ( float )bin_b;
						// make it value in scope[0, 7],bad way!!, 
						// can check use if(bin_t > 7) bin_b = 6, bin_t = 7, ugly code!!
						bin_b = ( bin_b + 2 * FEATURE_DESCRIPTOR_ORIENTATION_BINS ) % FEATURE_DESCRIPTOR_ORIENTATION_BINS;
						bin_t = ( bin_t + 2 * FEATURE_DESCRIPTOR_ORIENTATION_BINS ) % FEATURE_DESCRIPTOR_ORIENTATION_BINS;
						// 高斯权重梯度
						float t = region[ 0 ].data[ yp + xp + ysrp + xsr ];
						// 基于权重精准累加
						hist[ y ][ x ][ bin_b ] += t * ( 1 - d );
						hist[ y ][ x ][ bin_t ] += t * d;
					}
				}
			}
		}
		
		// define 128 descriptor
		float[] desc = new float[ FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_ORIENTATION_BINS ];
		
		// build the descriptor array, and find max value
		float max_bin_val = 0;
		int i = 0;
		for ( int y = FEATURE_DESCRIPTOR_SIZE - 1; y >= 0; --y )
		{
			for ( int x = FEATURE_DESCRIPTOR_SIZE - 1; x >= 0; --x )
			{
				for ( int b = FEATURE_DESCRIPTOR_ORIENTATION_BINS - 1; b >= 0; --b )
				{
					desc[ i ] = hist[ y ][ x ][ b ];
					if ( desc[ i ] > max_bin_val ) max_bin_val = desc[ i ];
					++i;
				}
			}
		}
		// normalization result by cut off 0.2 of max value
		// can improve the match rate
		max_bin_val /= 0.2;
		for ( i = 0; i < desc.length; ++i )
		{
			desc[ i ] = ( float )Math.min( 1.0, desc[ i ] / max_bin_val );
		}
		return desc;
	}

	/***
	 * just for testing...
	 * @param fp
	 * @return
	 */
	private int[] getTestPixels(Float2DArray fp)
	{
		int[] out = new int[fp.data.length];
		int index = 0;
		for(int row=0; row<fp.getHeight(); row++)
		{
			for(int col=0; col<fp.getWidth(); col++)
			{
				int gray = (int)(fp.getValue(col, row) * 255);
				index = row * fp.getWidth() + col;
				out[index]  = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
			}
		}
		return out;
	}

}
