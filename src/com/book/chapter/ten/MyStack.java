package com.book.chapter.ten;


public class MyStack {
	private Object[] elementData;
	private int elementCount;
	private int capacityIncrement;
	
	public MyStack(int capacity)
	{
		if(capacity <= 0)
		{
			throw new java.lang.IllegalArgumentException("The value of capacity must be > 0 ");
		}
		this.capacityIncrement = capacity;
		elementData = new Object[capacityIncrement];
	}
	
	public boolean isEmpty()
	{
		return elementCount == 0;
	}
	
	public void clear()
	{
		elementData = null;
		elementCount = 0;
	}
	
	public void push(Object obj)
	{
		if(elementCount == elementData.length)
		{
			Object[] objects = new Object[elementData.length + capacityIncrement];
			System.arraycopy(elementData, 0, objects, 0, elementData.length);
			elementData = objects;
		}
		elementData[elementCount] = obj;
		elementCount++;
	}
	
	public void display()
	{
		for(int i=0; i<elementCount; i++)
		{
			System.out.print("\t" + elementData[i]);
		}
	}
	
	public Object pop()
	{
		Object obj = elementData[elementCount-1];
		elementCount--;
		return obj;
	}
	
	public int size()
	{
		return elementCount;
	}
	
	public static void main(String[] args)
	{
		MyStack ms = new MyStack(10);
		for(int i=0; i<20; i++)
		{
			ms.push(new Integer(i));
		}
		for(int i=0; i<15; i++)
		{
			System.out.println("pop value : " + ms.pop());
		}
		System.out.println(ms.size());
		ms.push(new Integer(99));
		ms.push(new Integer(199));
		ms.push(new Integer(1199));
		System.out.println(ms.size());
		int size = ms.size();
		for(int i=0; i<size; i++)
		{
			System.out.println("pop value : " + ms.pop());
		}
	}

}
