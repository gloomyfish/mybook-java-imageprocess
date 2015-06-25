package com.book.chapter.three;

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
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainUI extends JFrame 
		implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String IMAGE_CMD = "选择图像...";
	public static final String PROCESS_CMD = "处理";
	
	private JButton imgBtn;
	private JButton processBtn;
	private ImagePanel imagePanel;
	
	// image
	private BufferedImage srcImage;
	
	public MainUI()
	{
		setTitle("JFrame UI - 演示");
		imgBtn = new JButton(IMAGE_CMD);
		processBtn = new JButton(PROCESS_CMD);
		
		// buttons
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(imgBtn);
		btnPanel.add(processBtn);
		
		// filters
		imagePanel = new ImagePanel();
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(imagePanel, BorderLayout.CENTER);
		getContentPane().add(btnPanel, BorderLayout.SOUTH);
		
		// setup listener
		setupActionListener();
		
	}

	private void setupActionListener() {
		imgBtn.addActionListener(this);
		processBtn.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(SwingUtilities.isEventDispatchThread())
		{
			System.out.println("Event Dispath Thread!!");
		}
		
		if(srcImage == null)
		{
			JOptionPane.showMessageDialog(this, "请先选择图像源文件");
			try {
				JFileChooser chooser = new JFileChooser();
				setFileTypeFilter(chooser);
				chooser.showOpenDialog(null);
				File f = chooser.getSelectedFile();
				if(f != null)
				{
					srcImage = ImageIO.read(f);
					imagePanel.setSourceImage(srcImage);
					imagePanel.repaint();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		if(IMAGE_CMD.equals(e.getActionCommand()))
		{
			try {
				JFileChooser chooser = new JFileChooser();
				setFileTypeFilter(chooser);
				chooser.showOpenDialog(null);
				File f = chooser.getSelectedFile();
				if(f != null)
				{
					srcImage = ImageIO.read(f);
					imagePanel.setSourceImage(srcImage);
					imagePanel.repaint();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			imagePanel.repaint();
		}
		else if(PROCESS_CMD.equals(e.getActionCommand()))
		{
			imagePanel.process();
			imagePanel.repaint();
			
/*			final BrightContrastSatUI bcsUI = new BrightContrastSatUI(this);
			bcsUI.setupActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					bcsUI.setVisible(false);
					bcsUI.dispose();
					double s = bcsUI.getSaturation();
					double b = bcsUI.getBright();
					double c = bcsUI.getContrast();
					imagePanel.process(new double[]{s, b, c});
					imagePanel.repaint();
					
				}
				
			});
			bcsUI.showUI();*/
		}
	}
	
	public void setFileTypeFilter(JFileChooser chooser)
	{
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "JPG & PNG Images", "jpg", "png");
		    chooser.setFileFilter(filter);
	}
	
	public void openView()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));
		pack();
		setVisible(true);
	}

	public static void main(String[] args) {
		MainUI ui = new MainUI();
		ui.openView();
	}
	
}
