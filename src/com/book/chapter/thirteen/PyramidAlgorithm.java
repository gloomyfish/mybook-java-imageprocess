package com.book.chapter.thirteen;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

public class PyramidAlgorithm extends GaussianFilter {
	private float a;
	private int level = 3;
	private int[][] whData;

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public PyramidAlgorithm() {
		a = 0.4f;
		level = 3;
	}
	
	public void setParameter(float p) {
		this.a = p;
	}

	public BufferedImage[] pyramidDown(BufferedImage src) {
		BufferedImage[] imagePyramids = new BufferedImage[level + 1];
		imagePyramids[0] = src;
		whData = new int[level][2];
		whData[0][0] = src.getWidth();
		whData[0][1] = src.getHeight();					
		for(int i=1; i<imagePyramids.length; i++) {
			imagePyramids[i] = pyramidReduce(imagePyramids[i-1]);
			if(i < level) {
				whData[i][0] = imagePyramids[i].getWidth();
				whData[i][1] = imagePyramids[i].getHeight();	
			}
		}
		return imagePyramids;
	}
	
	public BufferedImage[] pyramidUp(BufferedImage[] srcImage) {
		BufferedImage[] imagePyramids = new BufferedImage[srcImage.length-1];
		for(int i=1; i<srcImage.length; i++) {
			imagePyramids[i-1] = pyramidExpand(srcImage[i], i-1);
		}
		return imagePyramids;
	}
	
	/***
	 * l1 = g1 - expand(g2)
	 * l2 = g2 - expand(g3)
	 * l0 = g0 - expand(g1)
	 * @param reduceImages
	 * @param expandImages
	 * @return
	 */
	public BufferedImage[] getLaplacianPyramid(BufferedImage[] reduceImages) {
		BufferedImage[] laplaciImages = new BufferedImage[reduceImages.length -1];
		for(int i=1; i<reduceImages.length; i++) {
			BufferedImage expandImage = pyramidExpand(reduceImages[i], i-1);
			laplaciImages[i-1] = createCompatibleDestImage(expandImage, null);
			int width = reduceImages[i-1].getWidth();
	        int height = reduceImages[i-1].getHeight();
	        
	        int ewidth = expandImage.getWidth();
	        width = (width > ewidth) ? ewidth : width;
	        height = (height > expandImage.getHeight()) ? expandImage.getHeight():height;
	        System.out.println(" width = " + width + " expand width = " + ewidth);
	        
	        int[] reducePixels = new int[width*height];
	        int[] expandPixels = new int[width*height];
	        int[] laPixels = new int[width*height];
	        getRGB( reduceImages[i-1], 0, 0, width, height, reducePixels);
	        getRGB( expandImage, 0, 0, width, height, expandPixels );
	        int index = 0;
	        int er = 0, eg = 0, eb = 0;
	        for(int row=0; row<height; row++) {
	        	int ta = 0, tr = 0, tg = 0, tb = 0;
	        	for(int col=0; col<width; col++) {
	        		index = row * width + col;
	        		ta = (reducePixels[index] >> 24) & 0xff;
	                tr = (reducePixels[index] >> 16) & 0xff;
	                tg = (reducePixels[index] >> 8) & 0xff;
	                tb = reducePixels[index] & 0xff;
	                
	        		ta = (expandPixels[index] >> 24) & 0xff;
	                er = (expandPixels[index] >> 16) & 0xff;
	                eg = (expandPixels[index] >> 8) & 0xff;
	                eb = expandPixels[index] & 0xff;
	                
	                tr = (tr - er);
	                tg = (tg - eg);
	                tb = (tb - eb);
	                
	                laPixels[index] = (ta << 24) | (clamp(tr) << 16) | (clamp(tg) << 8) | clamp(tb);
	        	}
	        }
	        setRGB( laplaciImages[i-1], 0, 0, width, height, laPixels );
		}

        return laplaciImages;
	}
	
	public BufferedImage collapse(BufferedImage image1, BufferedImage image2)
	{
		int width = image1.getWidth();
        int height = image1.getHeight();
        BufferedImage blendedImage = createCompatibleDestImage( image1, null );

        int[] image1Pixels = new int[width*height];
        int[] image2Pixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( image1, 0, 0, width, height, image1Pixels );
        getRGB( image2, 0, 0, width, height, image2Pixels );
        int index = 0;
        for(int row=0; row<height; row++) {
        	int ta1 = 0, tr1 = 0, tg1 = 0, tb1 = 0;
        	int ta2 = 0, tr2 = 0, tg2 = 0, tb2 = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta1 = (image1Pixels[index] >> 24) & 0xff;
                tr1 = (image1Pixels[index] >> 16) & 0xff;
                tg1 = (image1Pixels[index] >> 8) & 0xff;
                tb1 = image1Pixels[index] & 0xff;
                
        		ta2 = (image2Pixels[index] >> 24) & 0xff;
                tr2 = (image2Pixels[index] >> 16) & 0xff;
                tg2 = (image2Pixels[index] >> 8) & 0xff;
                tb2 = image2Pixels[index] & 0xff;
                
                int br = (int)clamp(tr2  +  tr1);
                int bg = (int)clamp(tg2  +  tg1);
                int bb = (int)clamp(tb2  +  tb1);
                
                outPixels[index] = (ta1 << 24) | (br << 16) | (bg << 8) | bb;
        	}
        }
        setRGB( blendedImage, 0, 0, width, height, outPixels );
        return blendedImage;
	}

	public BufferedImage blendOneImage(BufferedImage image1, BufferedImage image2, BufferedImage maskImage, BufferedImage blendedImage) {
		int width = image1.getWidth();
        int height = image1.getHeight();

        if ( blendedImage == null )
        	blendedImage = createCompatibleDestImage( maskImage, null );

        int[] image1Pixels = new int[width*height];
        int[] image2Pixels = new int[width*height];
        int[] maskPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( image1, 0, 0, width, height, image1Pixels );
        getRGB( image2, 0, 0, width, height, image2Pixels );
        getRGB( maskImage, 0, 0, width, height, maskPixels );
        int index = 0;
        float mr = 0, mg = 0, mb = 0;
        for(int row=0; row<height; row++) {
        	int ta1 = 0, tr1 = 0, tg1 = 0, tb1 = 0;
        	int ta2 = 0, tr2 = 0, tg2 = 0, tb2 = 0;
        	int ta3 = 0, tr3 = 0, tg3 = 0, tb3 = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		ta1 = (image1Pixels[index] >> 24) & 0xff;
                tr1 = (image1Pixels[index] >> 16) & 0xff;
                tg1 = (image1Pixels[index] >> 8) & 0xff;
                tb1 = image1Pixels[index] & 0xff;
                
        		ta2 = (image2Pixels[index] >> 24) & 0xff;
                tr2 = (image2Pixels[index] >> 16) & 0xff;
                tg2 = (image2Pixels[index] >> 8) & 0xff;
                tb2 = image2Pixels[index] & 0xff;
                
        		ta3 = (maskPixels[index] >> 24) & 0xff;
                tr3 = (maskPixels[index] >> 16) & 0xff;
                tg3 = (maskPixels[index] >> 8) & 0xff;
                tb3 = maskPixels[index] & 0xff;
                
                mr = tr3 / 255.0f;
                mg = tg3 / 255.0f;
                mb = tb3 / 255.0f;
                int br = (int)(mr * tr2  +  (1.0f - mr) * tr1);
                int bg = (int)(mg * tg2  +  (1.0f - mr) * tg1);
                int bb = (int)(mb * tb2  +  (1.0f - mr) * tb1);
                outPixels[index] = (ta1 << 24) | (clamp(br) << 16) | (clamp(bg) << 8) | clamp(bb);
        	}
        }
        setRGB( blendedImage, 0, 0, width, height, outPixels );
        return blendedImage;
	}
	
	private BufferedImage pyramidReduce(BufferedImage src) {
		int width = src.getWidth();
        int height = src.getHeight();
        BufferedImage dest = createSubCompatibleDestImage(src, null);
        int[] inPixels = new int[width*height];
        int ow = width/2;
        int oh = height/2;
        int[] outPixels = new int[ow*oh];
        getRGB(src, 0, 0, width, height, inPixels );
        int inRow=0, inCol = 0, index = 0, oudex =0, ta = 0;
        float[][] keneralData = this.getHVGaussianKeneral();
        for(int row=0; row<oh; row++) {
        	for(int col=0; col<ow; col++) {
        		inRow = 2* row;
        		inCol = 2* col;
        		if(inRow >= height) {
        			inRow = 0;
        		}
        		if(inCol >= width) {
        			inCol = 0;
        		}
        		float sumRed = 0, sumGreen = 0, sumBlue = 0;
        		for(int subRow = -2; subRow <= 2; subRow++) {
        			int inRowOff = inRow + subRow;
        			if(inRowOff >= height || inRowOff < 0) {
        				inRowOff = 0;
        			}
        			for(int subCol = -2; subCol <= 2; subCol++) {
        				int inColOff = inCol + subCol;
        				if(inColOff >= width || inColOff < 0) {
        					inColOff = 0;
        				}
        				index = inRowOff * width + inColOff;
        				ta = (inPixels[index] >> 24) & 0xff;
        				int red = (inPixels[index] >> 16) & 0xff;
        				int green = (inPixels[index] >> 8) & 0xff;
        				int blue = inPixels[index] & 0xff;
        				sumRed += keneralData[subRow + 2][subCol + 2] * red;
        				sumGreen += keneralData[subRow + 2][subCol + 2] * green;
        				sumBlue += keneralData[subRow + 2][subCol + 2] * blue;
        			}
        		}
        		
        		oudex = row * ow + col;
        		outPixels[oudex] = (ta << 24) | (clamp(sumRed) << 16) | (clamp(sumGreen) << 8) | clamp(sumBlue);
        	}
        }
        setRGB( dest, 0, 0, ow, oh, outPixels );
        return dest;
	}
	
    public BufferedImage createSubCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth()/2, src.getHeight()/2), dstCM.isAlphaPremultiplied(), null);
    }
    
    public BufferedImage createTwiceCompatibleDestImage(BufferedImage src, ColorModel dstCM, int index) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(whData[index][0], whData[index][1]), dstCM.isAlphaPremultiplied(), null);
    }
    
    public BufferedImage createTwiceCompatibleDestImage(BufferedImage src, ColorModel dstCM, int width, int height) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height), dstCM.isAlphaPremultiplied(), null);
    }
    
    public BufferedImage pyramidExpand(BufferedImage src, int ewidth, int eheight) {
		int width = src.getWidth();
		int height = src.getHeight();
		System.out.println("expand src.width = " + width + " , src.height = " + height);
		int[] inPixels = new int[width*height];
		getRGB(src, 0, 0, width, height, inPixels );
		int ow = ewidth;
		int oh = eheight;
		System.out.println("expand exp.width = " + ow + " , exp.height = " + oh);
		int[] outPixels = new int[ow * oh];
		int index = 0, outdex = 0, ta = 0;
		float[][] keneralData = this.getHVGaussianKeneral();
		BufferedImage dest = createTwiceCompatibleDestImage(src, null, ewidth, eheight);
		for(int row=0; row<oh; row++) {
			for(int col=0; col<ow; col++) {
	    		float sumRed = 0, sumGreen = 0, sumBlue = 0;
	    		for(int subRow = -2; subRow <= 2; subRow++) {
	    			double srcRow = (row + subRow)/2.0;
	    			double j = Math.floor(srcRow);
	    			double t = srcRow - j; 
	    			if(t > 0) {
	    				continue;
	    			}
	    			if(srcRow >= height || srcRow < 0) {
	    				srcRow = 0;
	    			}
	    			for(int subCol = -2; subCol <= 2; subCol++) {
	    				double srcColOff = (col + subCol)/2.0;
	    				j = Math.floor(srcColOff);
	    				t = srcColOff - j;
	    				if(t > 0) {
	    					continue;
	    				}
	    				if(srcColOff >= width || srcColOff < 0) {
	    					srcColOff = 0;
	    				}
	    				index = (int)(srcRow * width + srcColOff);
	    				ta = (inPixels[index] >> 24) & 0xff;
	    				int red = (inPixels[index] >> 16) & 0xff;
	    				int green = (inPixels[index] >> 8) & 0xff;
	    				int blue = inPixels[index] & 0xff;
	    				sumRed += keneralData[subRow + 2][subCol + 2] * red;
	    				sumGreen += keneralData[subRow + 2][subCol + 2] * green;
	    				sumBlue += keneralData[subRow + 2][subCol + 2] * blue;
	    			}
	    		}
				outdex = row * ow + col;
	    		outPixels[outdex] = (ta << 24) | (clamp(4.0f * sumRed) << 16) | (clamp(4.0f * sumGreen) << 8) | clamp(4.0f * sumBlue);
	    		// outPixels[outdex] = (ta << 24) | (clamp(sumRed) << 16) | (clamp(sumGreen) << 8) | clamp(sumBlue);
			}
		}
		setRGB( dest, 0, 0, ow, oh, outPixels );
		return dest;
    }
	
	public BufferedImage pyramidExpand(BufferedImage src, int levelIndex) {
		int width = src.getWidth();
		int height = src.getHeight();
		System.out.println("expand src.width = " + width + " , src.height = " + height);
		int[] inPixels = new int[width*height];
		getRGB(src, 0, 0, width, height, inPixels );
		int ow = whData[levelIndex][0];
		int oh = whData[levelIndex][1];
		System.out.println("expand exp.width = " + ow + " , exp.height = " + oh);
		int[] outPixels = new int[ow * oh];
		int index = 0, outdex = 0, ta = 0;
		float[][] keneralData = this.getHVGaussianKeneral();
		BufferedImage dest = createTwiceCompatibleDestImage(src, null, levelIndex);
		for(int row=0; row<oh; row++) {
			for(int col=0; col<ow; col++) {
	    		float sumRed = 0, sumGreen = 0, sumBlue = 0;
	    		for(int subRow = -2; subRow <= 2; subRow++) {
	    			double srcRow = (row + subRow)/2.0;
	    			double j = Math.floor(srcRow);
	    			double t = srcRow - j; 
	    			if(t > 0) {
	    				continue;
	    			}
	    			if(srcRow >= height || srcRow < 0) {
	    				srcRow = 0;
	    			}
	    			for(int subCol = -2; subCol <= 2; subCol++) {
	    				double srcColOff = (col + subCol)/2.0;
	    				j = Math.floor(srcColOff);
	    				t = srcColOff - j;
	    				if(t > 0) {
	    					continue;
	    				}
	    				if(srcColOff >= width || srcColOff < 0) {
	    					srcColOff = 0;
	    				}
	    				index = (int)(srcRow * width + srcColOff);
	    				ta = (inPixels[index] >> 24) & 0xff;
	    				int red = (inPixels[index] >> 16) & 0xff;
	    				int green = (inPixels[index] >> 8) & 0xff;
	    				int blue = inPixels[index] & 0xff;
	    				sumRed += keneralData[subRow + 2][subCol + 2] * red;
	    				sumGreen += keneralData[subRow + 2][subCol + 2] * green;
	    				sumBlue += keneralData[subRow + 2][subCol + 2] * blue;
	    			}
	    		}
				outdex = row * ow + col;
	    		outPixels[outdex] = (ta << 24) | (clamp(4.0f * sumRed) << 16) | (clamp(4.0f * sumGreen) << 8) | clamp(4.0f * sumBlue);
	    		// outPixels[outdex] = (ta << 24) | (clamp(sumRed) << 16) | (clamp(sumGreen) << 8) | clamp(sumBlue);
			}
		}
		setRGB( dest, 0, 0, ow, oh, outPixels );
		return dest;
	}

}
