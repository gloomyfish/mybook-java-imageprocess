package com.book.chapter.one;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class CustomJPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage image = null;

	public CustomJPanel()
	{
		super();
		java.net.URL imageURL = this.getClass().getResource("lena.jpg");
		try {
			image = ImageIO.read(imageURL);
		} catch (IOException e) {
			System.err.println("An error occured when loading the image icon...");
		}
		
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
	    Graphics2D g2 = (Graphics2D)g;  
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
	    		RenderingHints.VALUE_ANTIALIAS_ON);
	    /*
	    // 单一颜色背景填充
	    g2.setPaint(Color.BLUE);
	    
	    // 水平方向线性渐变颜色填充
	    Color sencondColor = new Color(99, 153, 255);
	    GradientPaint hLinePaint = new GradientPaint(0, 0, Color.BLACK, 
	    		this.getWidth(), 0,sencondColor);
	    g2.setPaint(hLinePaint);
	    
	    // 竖直方向线性渐变颜色填充
	    Color controlColor = new Color(99, 153, 255);
	    GradientPaint vLinePaint = new GradientPaint(0, 0, Color.BLACK, 
	    		0, getHeight(),controlColor);
	    g2.setPaint(vLinePaint);
	    
	    // 圆周径向渐变颜色填充
	    float cx = this.getWidth() / 2;
	    float cy = this.getHeight() / 2;
	    float radius = Math.min(cx, cy);
	    float[] fractions = new float[]{0.1f, 0.5f, 1.0f};
	    Color[] colors = new Color[]{Color.RED, Color.GREEN, Color.BLUE};
	    RadialGradientPaint rgp = new RadialGradientPaint(cx, cy, radius,
				fractions, colors, CycleMethod.NO_CYCLE);
	    g2.setPaint(rgp);
	    */
	    // 填充背景
	    g2.fillRect(0, 0, getWidth(), getHeight());
	    
	    // 图片做为背景填充
	    if(image != null)
	    {
	    	g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	    }
	    
	    
	}


	public static void main(String[] args)
	{
		JFrame ui = new JFrame("Demo Graphics");
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(new CustomJPanel(), BorderLayout.CENTER);
		ui.setPreferredSize(new Dimension(380, 380));
		ui.pack();
		ui.setVisible(true);
	}
}
