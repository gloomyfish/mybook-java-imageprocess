package com.book.chapter.one;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class TaijiDemo extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public TaijiDemo()
	{
		super();
	}
	
	protected void paintComponent(Graphics g) {  
	    Graphics2D g2 = (Graphics2D)g;  
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
	    		RenderingHints.VALUE_ANTIALIAS_ON);  
	    Shape lefthalfCirle = new Ellipse2D.Double(10,10, 300,300); // R = 150  
	    Shape righthalfCircle = new Ellipse2D.Double(10,10, 300,300); // R = 150  
	    Shape innerCircle1 = new Ellipse2D.Double(85,10, 150,150); // R/2 = 75  
	    Shape innerCircle2 = new Ellipse2D.Double(85,160, 150,150); // R = 150  
	  
	    Shape rectangel1 = new Rectangle2D.Double(160, 10, 150, 300);  
	    Shape rectangel2 = new Rectangle2D.Double(10, 10, 150, 300);  
	      
	    Area left = new Area(lefthalfCirle);  
	    Area right = new Area(righthalfCircle);  
	      
	    Area area11 = new Area(rectangel1);  
	    Area area22 = new Area(rectangel2);  
	      
	    left.subtract(area11);  
	    right.subtract(area22);  
	      
	    Area inner1 = new Area(innerCircle1);  
	    Area inner2 = new Area(innerCircle2);  
	      
	    left.add(inner1);  
	    right.add(inner2);
	    
	    // trick is here !!! 
	    right.subtract(inner1);  
	      
	      
	    // create minor circle here!!!  
	    Shape minorWhiteCircle = new Ellipse2D.Double(150,70, 20,20); // ++ 60  
	    Shape innerBlackCircle = new Ellipse2D.Double(150,230, 20,20); // R = 150  
	      
	    // draw two big frame shape here...  
	    g2.setPaint(Color.WHITE);  
	    g2.fill(left);  
	    g2.setPaint(Color.BLACK);  
	    g2.fill(right);  
	      
	    // draw minor circle here!!!  
	    g2.fill(minorWhiteCircle);  
	    g2.setPaint(Color.WHITE);  
	    g2.fill(innerBlackCircle);  
	}  
	
	public static void main(String[] args)
	{
		JFrame ui = new JFrame("Demo Graphics");
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(new TaijiDemo(), BorderLayout.CENTER);
		ui.setPreferredSize(new Dimension(380, 380));
		ui.pack();
		ui.setVisible(true);
	}
}
