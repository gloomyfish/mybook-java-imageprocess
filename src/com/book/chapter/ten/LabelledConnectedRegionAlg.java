package com.book.chapter.ten;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.book.chapter.four.AbstractBufferedImageOp;
/**
 * work for binary image
 * @author fish
 *
 */
public class LabelledConnectedRegionAlg extends AbstractBufferedImageOp {

	private Map<Integer, java.awt.Color> colorMap = null;
	public LabelledConnectedRegionAlg()
	{
		colorMap = new HashMap<Integer, java.awt.Color>();
	}
	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
            dest = createCompatibleDestImage( src, null );
        List<PixelPoint> pixelList = new ArrayList<PixelPoint>();
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
        // 初始化每个像素节点状态
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		PixelPoint p = new PixelPoint(row, col, (inPixels[index] >> 16) & 0xff);
        		pixelList.add(p);
        	}
        }
        // 添加每个像素节点的四邻域像素
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		PixelPoint p = pixelList.get(index);
        		
        		// add four neighbors for each pixel
        		if((row - 1) >= 0)
        		{
        			index = (row-1) * width + col;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((row + 1) < height)
        		{
        			index = (row+1) * width + col;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((col - 1) >= 0)
        		{
        			index = row * width + col-1;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((col+1) < width)
        		{
        			index = row * width + col+1;
        			p.addNeighour(pixelList.get(index));
        		}
        	}
        }
        
        // 深度优先搜索算法，连通组件标记
        DFSAlgorithm dfs = new DFSAlgorithm(pixelList);
        dfs.process();
        System.out.println("Total Number of Labels : " + dfs.getTotalOfLabels());
        
        // post process - 区域连通组件着色
        for(int row=0; row<height; row++) {
        	int ta = 0, tr = 0, tg = 0, tb = 0;
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		PixelPoint p = pixelList.get(index);
        		ta = 255;
        		if(p.getLabel() > 0)
        		{
        			Color c = getColor(p.getLabel());
	        		tr = c.getRed(); 
	                tg = c.getGreen();
	                tb = c.getBlue();
        		}
        		else
        		{
            		tr = p.getValue();
                    tg = p.getValue();
                    tb = p.getValue();
        		}
                outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;
        	}
        }
        
        setRGB( dest, 0, 0, width, height, outPixels );
        return dest;
	}
	
	private Color getColor(Integer label)
	{
		Color c = colorMap.get(label);
		if(c == null)
		{
			int red = (int)(Math.random() * 255);
			int green = (int)(Math.random() * 255);
			int blue = (int)(Math.random() * 255);
			c = new Color(red, green, blue);
			colorMap.put(label, c);			
		}
		return c;
	}

}
