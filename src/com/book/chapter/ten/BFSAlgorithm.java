package com.book.chapter.ten;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Breath First Search for graphics
 * @author gloomyfish
 *
 */
public class BFSAlgorithm {
	private List<PixelPoint> pixelList = null;
	private int grayLevel = 1;
	public int getGrayLevel() {
		return grayLevel;
	}
	
	public int getTotalOfLabels()
	{
		Map<Integer, Integer> labelMap = new HashMap<Integer, Integer>();
		for(PixelPoint p : pixelList)
		{
			if(p.getValue() >= grayLevel)
			{
				if(labelMap.containsKey(p.getLabel()))
				{
					Integer count = labelMap.get(p.getLabel());
					count += 1;
					labelMap.put(p.getLabel(), count);
				}
				else
				{
					labelMap.put(p.getLabel(), new Integer(1));
				}
			}
		}
		Integer[] keys = labelMap.keySet().toArray(new Integer[0]);
		for(Integer key : keys)
		{
			System.out.println("Label index : " + key);
		}
		System.out.println("total labels : " + labelMap.size());
		return labelMap.size();
	}

	public void setGrayLevel(int grayLevel) {
		this.grayLevel = grayLevel;
	}

	public BFSAlgorithm(List<PixelPoint> pixelList)
	{
		this.pixelList = pixelList;
		grayLevel = 1; // front color - target pixel
	}
	
	public void process()
	{
		if(this.pixelList == null) return;
		int label = 1;
		for(PixelPoint pp : pixelList)
		{
			if(pp.getValue() >= grayLevel)
			{
				if(pp.getStatus() == PixelPoint.UNMARKED)
				{
					pp.setStatus(PixelPoint.VISITED);
					pp.setLabel(label);
					MyQueue mq = new MyQueue(10000);
					for(PixelPoint npp : pp.getNeighbours())
					{
						if(npp.getStatus() == PixelPoint.UNMARKED && npp.getValue() >= grayLevel)
						{
							npp.setStatus(PixelPoint.MARKED);
							mq.enqueue(npp);
						}
					}
					while(!mq.isEmpty())
					{
						PixelPoint obj = (PixelPoint)mq.dequeue();
						if(obj.getStatus() == PixelPoint.MARKED)
						{
							obj.setLabel(label);
							obj.setStatus(PixelPoint.VISITED);
						}
						for(PixelPoint nnpp : obj.getNeighbours())
						{
							if(nnpp.getStatus() == PixelPoint.UNMARKED && nnpp.getValue() >= grayLevel)
							{
								nnpp.setStatus(PixelPoint.MARKED);
								mq.enqueue(nnpp);
							}
						}
					}
					label++;
				}
			}
		}
	}

}
