package com.book.chapter.six;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class HistogramPanel extends JPanel implements ViewCallBack {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage histogramImage;
	private Dimension size;
	private double linePos;
	private int[] data;
	private int threshold;
	private JButton okBtn;
	public double getLinePos() {
		return linePos;
	}

	public void setLinePos(double linePos) {
		this.linePos = linePos;
	}
	
	public int getThreshold()
	{
		return threshold;
	}

	public BufferedImage getHistogramImage() {
		return histogramImage;
	}

	public void setHistogramImage(BufferedImage histogramImage) {
		this.histogramImage = histogramImage;
	}

	public HistogramPanel(BufferedImage image, int[] histogramData) {
		linePos = 127;
		this.histogramImage = image;
		this.data = histogramData;
		this.size = new Dimension(image.getWidth(), image.getHeight());
		okBtn = new JButton("OK");
		this.setSize(size);
		this.setPreferredSize(size);
		this.setMinimumSize(size);
		this.setMaximumSize(size);		
	}
	
	public void setupActionListener(ActionListener l)
	{
		this.okBtn.addActionListener(l);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
		if (histogramImage != null) {
			renderHistogramWithLine(histogramImage);
			g2d.drawImage(histogramImage, 0, 0, histogramImage.getWidth(),
					histogramImage.getHeight(), null);
		}
	}

	private void renderHistogramWithLine(BufferedImage histogramImage2) {
		int width = (int)size.getWidth();
		int height = (int)size.getHeight();
		double maxFrequency = 0;
		for(int i=0; i<data.length; i++)
		{
			maxFrequency = Math.max(maxFrequency, data[i]);
		}

		// render the histogram graphic 
		Graphics2D g2d = histogramImage2.createGraphics();
		g2d.setPaint(Color.LIGHT_GRAY);
		g2d.fillRect(0, 0, width, height);
		
		// draw XY Axis
		g2d.setPaint(Color.BLACK);
		g2d.drawLine(50, 50, 50, height - 50);
		g2d.drawLine(50, height-50, width-50, height - 50);
		
		// draw XY Title
		g2d.drawString("0", 50, height-30);
		g2d.drawString("255", width-50, height-30);
		g2d.drawString("0", 20, height-50);
		g2d.drawString("" + maxFrequency, 20,50);
		
		// draw histogram bar
		double xunit = (width - 100.0)/256.0d;
		double yunit = (height - 100.0)/maxFrequency;
		for(int i=0; i<data.length; i++)
		{
			 double xp = 50 + xunit * i;
			 double yp = yunit * data[i];
			 Rectangle2D rect2d = new Rectangle2D.
					 Double(xp, height - 50 - yp, xunit, yp);
			 g2d.fill(rect2d);
		}
		
		// render red line
		if((linePos - 50) >= 0 && (width - linePos)>= 50)
		{
			threshold = (int)((linePos - 50) / xunit);
			linePos = 50 + xunit * threshold;
			g2d.setPaint(Color.RED);
			g2d.drawLine((int)linePos, 50, (int)linePos, height - 50);
			g2d.drawString("阈值:"+threshold, (int)linePos-10, 50);
		}
	}
	
	public void openView()
	{
		JDialog ui = new JDialog();
		ui.setTitle("直方图阈值寻找");
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(this, BorderLayout.CENTER);
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(okBtn);
		ui.getContentPane().add(okBtn, BorderLayout.SOUTH);
		ui.setDefaultCloseOperation(JDialog. DISPOSE_ON_CLOSE);
		ui.pack();
		ui.setVisible(true);
	}

	@Override
	public void mooveLine(double position) {
		this.linePos = position;
		this.repaint();
		
	}

}
