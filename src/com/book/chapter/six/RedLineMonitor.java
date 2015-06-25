package com.book.chapter.six;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RedLineMonitor extends MouseAdapter {
	private double startX;
	private double startY;
	private double endX;
	private double endY;
	private ViewCallBack callBack;
	public RedLineMonitor(ViewCallBack callBack)
	{
		this.callBack = callBack;
		System.out.println("install mouse monitor");
	}

	@Override 
	public void mousePressed(MouseEvent event) {
		startX = event.getPoint().getX();
		startY = event.getPoint().getY();
	}
	
	public void mouseMoved(MouseEvent event) {
		// do nothing
	}
	
	public void mouseReleased(MouseEvent event) {
		endX = event.getPoint().getX();
		endY = event.getPoint().getY();
		startX = endX;
		startY = endY;
	}
	
	public void mouseDragged(MouseEvent event) {
		endX = (int)event.getPoint().getX();
		endY = (int)event.getPoint().getY();
		if(Math.abs(startX-endX) >= 0)
		{
			callBack.mooveLine(endX);
			
		}
	}
}
