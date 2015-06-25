package com.book.chapter.ten;

public class MyQueue {
	private Object[] objectArray;
	private int header = 0;
	private int last = -1;
	private int numberOfItems = 0;
	private int capticy;
	
	public MyQueue(int capticy)
	{
		this.capticy = capticy;
		objectArray = new Object[capticy];
		header = -1;
	}
	
	public Object dequeue()
	{
		Object item = objectArray[header];
		for(int i=1; i<numberOfItems; i++)
		{
			objectArray[i-1] = objectArray[i];
		}
		numberOfItems--;
		last--;
		return item;
	}
	
	public Object getHeader()
	{
		return objectArray[header];
	}
	
	public void enqueue(Object item)
	{
		if(this.isEmpty())
		{
			header = 0;
			last = 0;
		}
		numberOfItems++;
		if(numberOfItems > this.capticy)
		{
			throw new IllegalArgumentException("reach the max size of this queue ");
		}
		objectArray[last] = item;
		last++;
	}
	
	public boolean isEmpty()
	{
		return (numberOfItems == 0);
	}
	
	public int size()
	{
		return this.numberOfItems;
	}
	
	/**
	 * First In First Out - Queue
	 * @param args
	 */
	public static void main(String[] args)
	{
		MyQueue mq = new MyQueue(10);
		mq.enqueue("aa");
		System.out.println(mq.dequeue().toString());
		mq.enqueue("bb");
		mq.enqueue("cc");
		mq.enqueue("dd");
		System.out.println(mq.dequeue().toString());
		System.out.println(mq.dequeue().toString());
		System.out.println(mq.dequeue().toString());
		
		
	}

}
