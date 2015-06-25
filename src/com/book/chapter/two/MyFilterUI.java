package com.book.chapter.two;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MyFilterUI extends JFrame 
		implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String GRAY_CMD = "灰度";
	public static final String BINARY_CMD = "黑白";
	public static final String BLUR_CMD = "模糊";
	public static final String ZOOM_CMD = "放缩";
	public static final String BROWSER_CMD = "选择...";
	
	private JButton grayBtn;
	private JButton binaryBtn;
	private JButton blurBtn;
	private JButton zoomBtn;
	private JButton browserBtn;
	private MyFilters filters;
	
	// image
	private BufferedImage srcImage;
	
	public MyFilterUI()
	{
		this.setTitle("JAVA 2D BufferedImageOp - 滤镜演示");
		grayBtn = new JButton(GRAY_CMD);
		binaryBtn = new JButton(BINARY_CMD);
		blurBtn = new JButton(BLUR_CMD);
		zoomBtn = new JButton(ZOOM_CMD);
		browserBtn = new JButton(BROWSER_CMD);
		
		// buttons
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(grayBtn);
		btnPanel.add(binaryBtn);
		btnPanel.add(blurBtn);
		btnPanel.add(zoomBtn);
		btnPanel.add(browserBtn);
		
		// filters
		filters = new MyFilters();
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(filters, BorderLayout.CENTER);
		getContentPane().add(btnPanel, BorderLayout.SOUTH);
		
		// setup listener
		setupActionListener();
		
	}

	private void setupActionListener() {
		grayBtn.addActionListener(this);
		binaryBtn.addActionListener(this);
		blurBtn.addActionListener(this);
		zoomBtn.addActionListener(this);
		browserBtn.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(srcImage == null)
		{
			JOptionPane.showMessageDialog(this, "请先选择图像源文件");
			try {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				File f = chooser.getSelectedFile();
				srcImage = ImageIO.read(f);
				filters.setImage(srcImage);
				filters.repaint();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		if(GRAY_CMD.equals(e.getActionCommand()))
		{
			filters.doColorGray(srcImage);
			filters.repaint();
		}
		else if(BINARY_CMD.equals(e.getActionCommand()))
		{
			filters.doBinaryImage(srcImage);
			filters.repaint();
		}
		else if(BLUR_CMD.equals(e.getActionCommand()))
		{
			filters.doBlur(srcImage);
			filters.repaint();
		}
		else if(ZOOM_CMD.equals(e.getActionCommand()))
		{
			filters.doScale(srcImage, 1.5, 1.5);
			filters.repaint();
		}
		else if(BROWSER_CMD.equals(e.getActionCommand()))
		{
			try {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				File f = chooser.getSelectedFile();
				srcImage = ImageIO.read(f);
				filters.setImage(srcImage);
				filters.repaint();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		MyFilterUI ui = new MyFilterUI();
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setPreferredSize(new Dimension(800, 600));
		ui.pack();
		ui.setVisible(true);
	}
	
}
