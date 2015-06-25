package com.book.chapter.ten;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DFSAlgorithm {
	
	private List<PixelPoint> pixelList = null;
	private int grayLevel = 1;
	public int getGrayLevel() {
		return grayLevel;
	}
	public void setGrayLevel(int grayLevel) {
		this.grayLevel = grayLevel;
	}
	public DFSAlgorithm(List<PixelPoint> pixelList)
	{
		this.pixelList = pixelList;
		grayLevel = 0; // front color - target pixel
	}
	
	public void process()
	{
		if(this.pixelList == null) return;
		int label = 1;
		for(PixelPoint pp : pixelList)
		{
			if(pp.getValue() == grayLevel)
			{
				if(pp.getStatus() == PixelPoint.UNMARKED)
				{
					// initialization stack
					pp.setStatus(PixelPoint.MARKED);
					pp.setLabel(label);
					MyStack ms = new MyStack(4);
					MyStack markedPoint = new MyStack(4000);
					ms.push(pp);

					// Depth First Search
					while(!ms.isEmpty())
					{
						PixelPoint obj = (PixelPoint)ms.pop();
						markedPoint.push(obj);
						if(obj.getStatus() == PixelPoint.MARKED)
						{
							for(PixelPoint nnpp : obj.getNeighbours())
							{
								if(nnpp.getStatus() == PixelPoint.UNMARKED && nnpp.getValue() == grayLevel)
								{
									nnpp.setStatus(PixelPoint.MARKED);
									ms.push(nnpp);
								}
							}
						}
					}
					
					// tag label now!!
					while(!markedPoint.isEmpty())
					{
						PixelPoint obj = (PixelPoint)markedPoint.pop();
						obj.setLabel(label);
						obj.setStatus(PixelPoint.VISITED);
					}
					label++;
				}
			}
		}
	}
	
	public int getTotalOfLabels()
	{
		Map<Integer, Integer> labelMap = new HashMap<Integer, Integer>();
		for(PixelPoint p : pixelList)
		{
			if(p.getValue() == grayLevel)
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
	
	

}
