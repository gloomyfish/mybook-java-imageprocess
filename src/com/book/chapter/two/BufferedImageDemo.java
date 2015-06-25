package com.book.chapter.two;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class BufferedImageDemo extends JPanel 
			implements MouseMotionListener {

	private BufferedImage image = null;
	private int width = 350;
	private int height = 350;
	
	public BufferedImageDemo() {
		image = createImage();
		addMouseMotionListener(this);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// 创建新的图片，基于新的颜色模型索引
		image = new BufferedImage(createColorModel(e.getX()),
				image.getRaster(), false, null);
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if(image  != null) {
			g2d.drawImage(image, 2, 2, width, height, null);
		}
	}

	private BufferedImage createImage() {
		byte[] pixels = new byte[width * height];
		DataBuffer dataBuffer = new DataBufferByte(pixels, width*height, 0);
		SampleModel sampleModel = new SinglePixelPackedSampleModel(
        	DataBuffer.TYPE_BYTE, width, height, new int[] {(byte)0xf});
		WritableRaster raster = Raster.createWritableRaster(
			sampleModel, dataBuffer, null);
		return new BufferedImage(createColorModel(0), raster, false, null);
	}

	private static ColorModel createColorModel(int n) {
		byte[] r = new byte[16];
		byte[] g = new byte[16];
		byte[] b = new byte[16];

		for (int i = 0; i < r.length; i++) {
			r[i] = (byte) n;
			g[i] = (byte) n;
			b[i] = (byte) n;
		}
		return new IndexColorModel(4, 16, r, g, b);
	}

	public static void main(String[] args) {
		
		JFrame ui = new JFrame("BufferedImage Demo");
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(new BufferedImageDemo(), 
							BorderLayout.CENTER);
		ui.setPreferredSize(new Dimension(380, 380));
		ui.pack();
		ui.setVisible(true);
	}
}