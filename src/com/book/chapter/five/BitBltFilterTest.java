package com.book.chapter.five;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class BitBltFilterTest extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7462704254856439832L;
	private BufferedImage textImg;
	private BufferedImage targetImg;
	private Dimension mySize;
	public BitBltFilterTest(File f) {
		try {
			textImg = ImageIO.read(f);
			targetImg = ImageIO.read(new File("D:\\resource\\yourImage.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		mySize = new Dimension(2*targetImg.getWidth() + 20, targetImg.getHeight()+ 100);
		filterImage();
		final JFrame imageFrame = new JFrame("Emboss Text - gloomyfish");
		imageFrame.getContentPane().setLayout(new BorderLayout());
		imageFrame.getContentPane().add(this, BorderLayout.CENTER);
		imageFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		imageFrame.pack();
		imageFrame.setVisible(true);
	}
	
	private void filterImage() {
		BitBltFilter filter = new BitBltFilter();
		filter.emboss(textImg, targetImg);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(textImg, 0, 0, textImg.getWidth(), textImg.getHeight(), null);
		g2.drawImage(targetImg, textImg.getWidth()+10, 0, targetImg.getWidth(), targetImg.getHeight(), null);
		g2.drawString("text image", textImg.getWidth()/2, textImg.getHeight()+10);
		g2.drawString("sharped text in image", targetImg.getWidth() + 10, targetImg.getHeight()+10);
	}
	public Dimension getPreferredSize() {
		return mySize;
	}
	
	public Dimension getMinimumSize() {
		return mySize;
	}
	
	public Dimension getMaximumSize() {
		return mySize;
	}
	
	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		File f = chooser.getSelectedFile();
		new BitBltFilterTest(f);
	}
}
