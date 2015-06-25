package com.book.chapter.fourteen;
/**
 * @author gloomyfish
 * @date 2012-09-23
 * @BLOGPAGE:http://blog.csdn.net/jia20003
 */

public class ScaleFilter {
	
	/**
	 * default will zoom in 2.0 * input size of original image.
	 */
	private float hscale = 1.0f;
	private float vscale = 1.0f;
	public ScaleFilter() {
		
	}
	
	public void setHscale(float hscale) {
		this.hscale = hscale;
	}

	public void setVscale(float vscale) {
		this.vscale = vscale;
	}

	public int[] filter(int[] inPixels, int width, int height) {

        // initialization pixel data
        int outwidth = (int)(hscale * (float)width);
        int outheight = (int)(vscale * (float)height);
        int[] outhPixels = new int[outwidth*height];
        int[] outPixels = new int[outwidth*outheight];

        // start to zoom in/out here
        hscale(inPixels, outhPixels, width, height);
        vscale(outhPixels, outPixels, outwidth, height);
        return outPixels;
	}
	
	private void hscale(int[] input, int[] output, int width, int height) {
		int ta1 = 0, tr1 = 0, tg1 = 0, tb1 = 0;
		int ta2 = 0, tr2 = 0, tg2 = 0, tb2 = 0;
		int sumred = 0, sumgreen = 0, sumblue = 0;
		double accred = 0, accgreen = 0, accblue = 0;
		int p, q;
		int outwidth = (int)(this.hscale * width);
		double area = (outwidth * width);
		int inCol = 0, outCol = 0;
		int inIndex1 = 0, inIndex2 = 0, outIndex = 0;
		for (int row = 0; row < height; row++) {
			q = width;
			p = outwidth;
			accred = accgreen = accblue = 0;
			inCol = outCol = 0;
			while (outCol < outwidth) {
				if ((inCol + 1) < 2) {
					inIndex1 = row * width + inCol;
					inIndex2 = row * width + (inCol + 1);
	        		ta1 = (input[inIndex1] >> 24) & 0xff;
	                tr1 = (input[inIndex1] >> 16) & 0xff;
	                tg1 = (input[inIndex1] >> 8) & 0xff;
	                tb1 = input[inIndex1] & 0xff;
	                
	        		ta2 = (input[inIndex2] >> 24) & 0xff;
	                tr2 = (input[inIndex2] >> 16) & 0xff;
	                tg2 = (input[inIndex2] >> 8) & 0xff;
	                tb2 = input[inIndex2] & 0xff;
	                sumred = p * tr1 + (outwidth - p) * tr2;
	                sumgreen = p * tg1 + (outwidth - p) * tg2;
	                sumblue = p * tb1 + (outwidth - p) * tb2;
				}
				else 
				{
					inIndex1 = row * width + inCol;
	        		ta1 = (input[inIndex1] >> 24) & 0xff;
	                tr1 = (input[inIndex1] >> 16) & 0xff;
	                tg1 = (input[inIndex1] >> 8) & 0xff;
	                tb1 = input[inIndex1] & 0xff;
	                sumred = outwidth * tr1;
	                sumgreen = outwidth * tg1;
	                sumblue = outwidth * tb1;
				}
				if (p < q) {
					accred += sumred * p;
					accgreen += sumgreen * p;
					accblue += sumblue * p;
					q -= p;
					p = outwidth;
					inCol++;
				} else {
					accred += sumred * q;
					accgreen += sumgreen * q;
					accblue += sumblue * q;
					outIndex = row * outwidth + outCol;
					output[outIndex] = ta1 << 24 | ((int)(accred / area) << 16) | ((int)(accgreen / area) << 8) | (int)(accblue / area);
					accred = accgreen = accblue = 0;
					p -= q;
					q = width;
					outCol++;
				}
			}
		}
	}

	private void vscale(int[] input, int[] output, int width, int height) {
		int ta1 = 0, tr1 = 0, tg1 = 0, tb1 = 0;
		int ta2 = 0, tr2 = 0, tg2 = 0, tb2 = 0;
		int sumred = 0, sumgreen = 0, sumblue = 0;
		double accred = 0, accgreen = 0, accblue = 0;
		int inRow = 0, outRow = 0;
		int inIndex1 = 0, inIndex2 = 0, outIndex = 0;
		int p, q;
		int ih = height;
		int oh = (int)(height * vscale);
		int area = (ih * oh);
		for (int col = 0; col < width; col++) {
			q = ih;
			p = oh;
			accred = accgreen = accblue = 0;
			inRow = outRow = 0;
			while (outRow < oh) {
				if (inRow+1 < ih) {
					inIndex1 = inRow * width + col;
					inIndex2 = (inRow+1) * width + col;
	        		ta1 = (input[inIndex1] >> 24) & 0xff;
	                tr1 = (input[inIndex1] >> 16) & 0xff;
	                tg1 = (input[inIndex1] >> 8) & 0xff;
	                tb1 = input[inIndex1] & 0xff;
	                
	        		ta2 = (input[inIndex2] >> 24) & 0xff;
	                tr2 = (input[inIndex2] >> 16) & 0xff;
	                tg2 = (input[inIndex2] >> 8) & 0xff;
	                tb2 = input[inIndex2] & 0xff;
	                sumred = p * tr1 + (oh - p) * tr2;
	                sumgreen = p * tg1 + (oh - p) * tg2;
	                sumblue = p * tb1 + (oh - p) * tb2;
				}
				else
				{
					inIndex1 = inRow * width + col;
	        		ta1 = (input[inIndex1] >> 24) & 0xff;
	                tr1 = (input[inIndex1] >> 16) & 0xff;
	                tg1 = (input[inIndex1] >> 8) & 0xff;
	                tb1 = input[inIndex1] & 0xff;
	                sumred = oh * tr1;
	                sumgreen = oh * tg1;
	                sumblue = oh * tb1;
				}
				if (p < q) {
					accred += sumred * p;
					accgreen += sumgreen * p;
					accblue += sumblue * p;
					q -= p;
					p = oh;
					inRow++;
				} else {
					accred += sumred * q;
					accgreen += sumgreen * q;
					accblue += sumblue * q;
					outIndex = outRow * width + col;
					
					output[outIndex] = ta1 << 24 | ((int)(accred / area) << 16) | ((int)(accgreen / area) << 8) | (int)(accblue / area);
					accred = accgreen = accblue = 0;
					p -= q;
					q = ih;
					outRow++;
				}
			}
		}		
	}
}
