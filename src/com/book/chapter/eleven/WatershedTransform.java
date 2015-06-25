package com.book.chapter.eleven;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.book.chapter.four.AbstractBufferedImageOp;

public class WatershedTransform extends AbstractBufferedImageOp {
	
	public WatershedTransform()
	{
		System.out.println("watershed immersion algorithm");
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();
        if ( dest == null )
            dest = createCompatibleDestImage( src, null );
        int[] input = new int[width*height];
        getRGB( src, 0, 0, width, height, input );
        int index = 0;
        // 初始化每个像素值
        Map<String, PixelPoint> pixelMap = new HashMap<String, PixelPoint>();
        // 直方图高度
        Map<Integer, List<PixelPoint>> heightMap = new HashMap<Integer, List<PixelPoint>>();
		for (int row = 0; row < height; row++) {
			int tr=0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				tr = (input[index] >> 16) & 0xff;
				PixelPoint pp = new PixelPoint(row, col, tr);
				pixelMap.put((row + "," + col) , pp);
				if(heightMap.get(Integer.valueOf(tr)) == null)
				{
					heightMap.put(Integer.valueOf(tr), new ArrayList<PixelPoint>());
				}
				heightMap.get(Integer.valueOf(tr)).add(pp);
			}
		}
		// 八邻域链接像素寻找
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				PixelPoint cpp = pixelMap.get(row + "," + col);
				for(int nr=-1; nr<2; nr++)
				{
					if((nr+row) <0 || (row+nr) >= height)
						continue;
					for(int nc=-1; nc<2; nc++)
					{
						
						if((nc+col) < 0 || (nc+col) >=width)
							continue;
						int index2 = (row+nr) * width + nc + col;
						if(index == index2) continue; // skip it
						cpp.getNeighbours().add(pixelMap.get((row+nr) + "," + (nc + col)));
					}
				}
			}
		}
		// 初始化浸泡算法
		FIFOQueue myQueue = new FIFOQueue();
		int curlab = 0;
		int curdist = 0;
		int _watershedPixelCount = 0;
		// 开始浸泡
		for(int h=0; h<256; h++) 
		{
			if(heightMap.get(Integer.valueOf(h)) == null) continue;
			for(PixelPoint pp : heightMap.get(Integer.valueOf(h)))
			{
				pp.setLabelToMASK();
				for(PixelPoint neighbourPixel : pp.getNeighbours())
				{
					if(neighbourPixel.getLabel() >= 0) {// water-shed or tag label
						pp.setDistance(1);
						myQueue.fifo_add(pp);
						break;
					}						
				}
			}
			curdist = 1;
			myQueue.fifo_add_FICTITIOUS();
			// 扩展盆地
			while(true)
			{
				PixelPoint p = myQueue.fifo_remove();
				if(p.isFictitious())
				{
					if(myQueue.fifo_empty())
					{
						break;
					}
					else
					{
						myQueue.fifo_add_FICTITIOUS();
						curdist++;
						p = myQueue.fifo_remove();
					}
				}
				
				for(PixelPoint q : p.getNeighbours())
				{
					if(q.getDistance() <= curdist && (q.getLabel() > 0 || q.isLabelWSHED()))
					// if(q.getDistance() <= curdist && (q.getLabel() >= 0))
					{
						if(q.getLabel() > 0)
						{
							if(p.isLabelMASK())
							{
								p.setLabel(q.getLabel());
							}
							else if(p.getLabel() != q.getLabel())
							{
								p.setLabelToWSHED();
								_watershedPixelCount++;
							}
						}
						else if(p.isLabelMASK())
						{
							p.setLabelToWSHED();
							_watershedPixelCount++;
						}
					}
					else if(q.isLabelMASK() && q.getDistance() == 0)
					{
						q.setDistance(curdist+1);
						myQueue.fifo_add(q);
					}
				}
			}
			
			// DFS - tag label for all mask pixel point
			if(heightMap.get(Integer.valueOf(h)) == null) continue;
			for(PixelPoint maskPxielPoint : heightMap.get(Integer.valueOf(h)))
			{
				// reset distance to zero
				maskPxielPoint.setDistance(0); 
				if(maskPxielPoint.isLabelMASK()) // new minimum region
				{
					curlab++;
					myQueue.fifo_add(maskPxielPoint);
					maskPxielPoint.setLabel(curlab);
					// 组件标记算法
					while(!myQueue.fifo_empty())
					{
						PixelPoint q = myQueue.fifo_remove();
						for(PixelPoint qn : q.getNeighbours())
						{
							if(qn.isLabelMASK())
							{
								myQueue.fifo_add(qn);
								qn.setLabel(curlab);
							}
						}
					}
				}
				
			}
		}
		// 输出统计
		if(_watershedPixelCount > 0)
		{
			System.out.println(" total watershed pixel count = " + _watershedPixelCount);
		}
		
		// 显示分水岭线
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				PixelPoint pp = pixelMap.get(row + "," + col);
				if(pp.isLabelWSHED() && !pp.allNeighboursAreWSHED())
				{
					input[index] = (255 << 24) | (255 << 16) | (255 << 8) | 255;
				}
			}
		}
		setRGB(dest, 0, 0, width, height, input);
		return dest;
	}

}
