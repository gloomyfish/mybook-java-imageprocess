package com.book.chapter.one;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class FontDemo extends JPanel {

	private static final long serialVersionUID = 1L;

	public FontDemo() {
		super();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		// 反锯齿
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);
		// 设置画笔颜色
		g2d.setPaint(Color.BLUE); 
		try {
			g2d.setFont(loadFont());
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		g2d.drawString("Font Demo", 50, 50);
		g2d.dispose(); // 释放资源
	}

	public Font loadFont() throws FontFormatException, 
	IOException {
		String fontFileName = "AMERSN.ttf";
		InputStream is = this.getClass().
				getResourceAsStream(fontFileName);
		Font actionJson = Font.createFont(Font.TRUETYPE_FONT, is);
		Font actionJsonBase = actionJson.deriveFont(Font.BOLD, 16);
		return actionJsonBase;
	}

	public static void main(String[] args) {
		JFrame ui = new JFrame("Font Demo Graphics2D");
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(new FontDemo(), 
				BorderLayout.CENTER);
		ui.setPreferredSize(new Dimension(380, 380));
		ui.pack();
		ui.setVisible(true);
	}

}
