package com.book.chapter.one;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class JAVA2DDemo extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public JAVA2DDemo()
	{
		super();
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, //反锯齿
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setPaint(Color.BLUE); // 设置画笔颜色
		g2d.drawRect(10, 10, 50, 50); // 绘制矩形
		g2d.dispose(); // 释放资源
	}
	
	public static void main(String[] args)
	{
		JFrame ui = new JFrame("Demo Graphics");
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(new JAVA2DDemo(), BorderLayout.CENTER);
		ui.pack();
		ui.setVisible(true);
	}
}
