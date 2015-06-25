package com.book.chapter.eleven;

import java.util.LinkedList;

public class FIFOQueue {
	private LinkedList<PixelPoint> watershedFIFO;

	public FIFOQueue() {
		watershedFIFO = new LinkedList<PixelPoint>();
	}

	public void fifo_add(PixelPoint p) {
		watershedFIFO.addFirst(p);
	}

	public PixelPoint fifo_remove() {
		return (PixelPoint) watershedFIFO.removeLast();
	}

	public boolean fifo_empty() {
		return watershedFIFO.isEmpty();
	}

	public void fifo_add_FICTITIOUS() {
		watershedFIFO.addFirst(new PixelPoint());
	}

}
