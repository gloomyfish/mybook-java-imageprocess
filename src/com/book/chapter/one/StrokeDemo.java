package com.book.chapter.one;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class StrokeDemo extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage image = null;
	public StrokeDemo()
	{
		super();
		java.net.URL imageURL = this.getClass().getResource("lena.jpg");
		try {
			image = ImageIO.read(imageURL);
		} catch (IOException e) {
			System.err.println("An error occured when loading the image icon...");
		}
	}
	
	protected void paintComponent(Graphics g) {  
	    Graphics2D g2 = (Graphics2D)g;  
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
	    		RenderingHints.VALUE_ANTIALIAS_ON);  
	    
	    // 创建Stroke对象实例
	    float[] dash = {10.0f, 5.0f, 3.0f};
	    Stroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE,
	            BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		    
	    // 设置Graphics2D的Stroke对象引用
	    g2.setStroke(dashed);
	    
	    // 创建形状
	    Shape rect2D = new RoundRectangle2D.Double(50, 50, 300, 100, 10, 10);
	    g2.draw(rect2D);
	    
		// Texture Fill
		Rectangle2D rect = new Rectangle2D.Double(10,10,200,200);
		TexturePaint tp = new TexturePaint(image, rect);
		g2.setPaint(tp);
		g2.fill(rect2D);

	}  
	
	public static void main(String[] args)
	{
		JFrame ui = new JFrame("Stroke Demo");
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(new StrokeDemo(), BorderLayout.CENTER);
		ui.setPreferredSize(new Dimension(380, 380));
		ui.pack();
		ui.setVisible(true);
	}
}
