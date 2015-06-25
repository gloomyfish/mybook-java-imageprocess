package com.book.chapter.four;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class BrightContrastSatUI extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton okBtn;
	private JLabel bLabel;
	private JLabel cLabel;
	private JLabel sLabel;
	private JSlider bSlider;
	private JSlider cSlider;
	private JSlider sSlider;
	
	public BrightContrastSatUI(JFrame parent)
	{
		super(parent, "调整图像亮度、对比度、饱和度");
		initComponent();
	}
	
	private void initComponent() {
		okBtn = new JButton("确定");
		bLabel = new JLabel("亮度");
		cLabel = new JLabel("对比度");
		sLabel = new JLabel("饱和度");
		bSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
		bSlider.setMajorTickSpacing(40);
		bSlider.setMinorTickSpacing(10);
		bSlider.setPaintLabels(true);
		bSlider.setPaintTicks(true);
		bSlider.setPaintTrack(true);
        
		cSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
		cSlider.setMajorTickSpacing(40);
		cSlider.setMinorTickSpacing(10);
		cSlider.setPaintLabels(true);
		cSlider.setPaintTicks(true);
		cSlider.setPaintTrack(true);
		
		sSlider = new JSlider(JSlider.HORIZONTAL, -100, 100, 0);
		sSlider.setMajorTickSpacing(40);
		sSlider.setMinorTickSpacing(10);
		sSlider.setPaintLabels(true);
		sSlider.setPaintTicks(true);
		sSlider.setPaintTrack(true);
		
		this.getContentPane().setLayout(new BorderLayout());
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		bPanel.add(bLabel);
		bPanel.add(bSlider);
		
		JPanel cPanel = new JPanel();
		cPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		cPanel.add(cLabel);
		cPanel.add(cSlider);
		
		JPanel sPanel = new JPanel();
		sPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		sPanel.add(sLabel);
		sPanel.add(sSlider);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayout(3,1));
		contentPanel.add(bPanel);
		contentPanel.add(cPanel);
		contentPanel.add(sPanel);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(okBtn);
		this.getContentPane().add(contentPanel, BorderLayout.CENTER);
		this.getContentPane().add(btnPanel, BorderLayout.SOUTH);
		this.pack();
	}
	
	public static void centre(Window w) {
	    Dimension us = w.getSize();
	    Dimension them = Toolkit.getDefaultToolkit().getScreenSize();
	    int newX = (them.width - us.width) / 2;
	    int newY = (them.height - us.height) / 2;
	    w.setLocation(newX, newY);
	}
	
	public int getBright()
	{
		return bSlider.getValue();
	}
	
	public int getContrast()
	{
		return cSlider.getValue();
	}
	
	public int getSaturation()
	{
		return sSlider.getValue();
	}
	
	public void showUI()
	{
		centre(this);
		this.setVisible(true);
	}

	public void setupActionListener(ActionListener l)
	{
		this.okBtn.addActionListener(l);
	}

}
