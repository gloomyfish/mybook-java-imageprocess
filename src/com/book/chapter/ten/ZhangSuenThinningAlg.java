package com.book.chapter.ten;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.book.chapter.four.AbstractBufferedImageOp;

public class ZhangSuenThinningAlg extends AbstractBufferedImageOp {
	private List<ThinPixel> pixelList;
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );
        pixelList = new ArrayList<ThinPixel>();
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        boolean changed = true;
        while(changed)
        {
        	changed = false;
        	initPixels(inPixels, width, height);
            for(ThinPixel tp : pixelList)
            {
            	if(tp.getValue() == 0) continue;
            	int p246 = tp.getP2() * tp.getP4() * tp.getP6();
            	int p468 = tp.getP8() * tp.getP4() * tp.getP6();
            	if((tp.getNumOfBlack() >= 2 && tp.getNumOfBlack() <= 6) && 
            			(p468 == 0) && (p246 == 0) &&
    					(tp.getNumOfConnectivity() == 1)) { 
            		setPixel(inPixels, width, height, tp.getCol(), tp.getRow(), 255);
            		changed = true;
            	}
            }
            initPixels(inPixels, width, height);
            for(ThinPixel tp : pixelList)
            {
            	if(tp.getValue() == 0) continue;
            	int p248 = tp.getP2() * tp.getP4() * tp.getP8();
            	int p268 = tp.getP2() * tp.getP6() * tp.getP8();
            	if((tp.getNumOfBlack() >= 2 && tp.getNumOfBlack() <= 6) && 
            			(p248 == 0) &&
            			(p268 == 0) && 
    					(tp.getNumOfConnectivity() == 1)) { 
            		changed = true;
            		setPixel(inPixels, width, height, tp.getCol(), tp.getRow(), 255);
            	}
            }
        }

        Arrays.fill(outPixels, -1);
        for(ThinPixel tp : pixelList)
        {
        	int row = tp.getRow();
        	int col = tp.getCol();
        	int p = tp.getValue();
        	if(p==0)
    		{
        		p = 255;
    		}
        	else
        	{
        		p = 0;
        	}
        	setPixel(outPixels, width, height, col, row, p);
        }
        setRGB(dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
	private void initPixels(int[] inPixels, int width, int height)
	{

		int index = 0;
		pixelList.clear();
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		int value = (inPixels[index] >> 16) & 0xff;
        		if(value == 255) 
    			{
        			ThinPixel p = new ThinPixel(row, col, 0); // white;
        			pixelList.add(p);     
    			}
        		else
        		{
        			ThinPixel p = new ThinPixel(row, col, 1); // black;
        			pixelList.add(p);        			
        		}
        	}
        }

        for(ThinPixel tp : pixelList)
        {
        	int row = tp.getRow();
        	int col = tp.getCol();
        	
        	if(tp.getValue() == 0) continue;
        	// 获取八个邻居
        	int p2 = getPixel(inPixels, width, height, col, row-1);
        	int p3 = getPixel(inPixels, width, height, col+1, row-1);
        	int p4 = getPixel(inPixels, width, height, col+1, row);
        	int p5 = getPixel(inPixels, width, height, col+1, row+1);
        	int p6 = getPixel(inPixels, width, height, col, row+1);
        	int p7 = getPixel(inPixels, width, height, col-1, row+1);
        	int p8 = getPixel(inPixels, width, height, col-1, row);
        	int p9 = getPixel(inPixels, width, height, col-1, row-1);
        	p2 = p2 == 0 ? 1 : 0;
        	p3 = p3 == 0 ? 1 : 0;
        	p4 = p4 == 0 ? 1 : 0;
        	p5 = p5 == 0 ? 1 : 0;
        	p6 = p6 == 0 ? 1 : 0;
        	p7 = p7 == 0 ? 1 : 0;
        	p8 = p8 == 0 ? 1 : 0;
        	p9 = p9 == 0 ? 1 : 0;
        	
        	int sum = p2+p3+p4+p5+p6+p7+p8+p9;
        	int times = 0;
        	if(p2==0 && p3 == 1)
        	{
        		times++;
        	}
        	if(p3==0 && p4== 1)
        	{
        		times++;
        	}
        	if(p4==0 && p5 == 1)
        	{
        		times++;
        	}
        	if(p5== 0 && p6==1)
        	{
        		times++;
        	}
        	if(p6== 0 && p7==1)
        	{
        		times++;
        	}
        	if(p7== 0 && p8==1)
        	{
        		times++;
        	}
        	if(p8== 0 && p9==1)
        	{
        		times++;
        	}
        	if(p9== 0 && p2==1)
        	{
        		times++;
        	}
        	tp.setNumOfConnectivity(times);
        	tp.setNumOfBlack(sum);
        	tp.setP2(p2);
        	tp.setP4(p4);
        	tp.setP6(p6);
        	tp.setP8(p8);
        }
	}
	
	private void setPixel(int[] input, int width, int height, int col, int row, int p)
	{
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		input[index] = (0xff << 24) | (p << 16) | (p << 8) | p;
	}
	
	private int getPixel(int[] input, int width, int height, int col,
			int row) {
		if(col < 0 || col >= width)
			col = 0;
		if(row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		int tr = (input[index] >> 16) & 0xff;
		return tr;
	}

}
