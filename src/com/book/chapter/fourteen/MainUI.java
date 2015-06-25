package com.book.chapter.fourteen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
public class MainUI extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3570033620825245822L;
	public static final String PAINT_CMD = "Paint";
	public static final String SELECT_CMD = "Select Image...";
	private JButton paintBtn;
	private JButton selectBtn;
	private BufferedImage srcImage;
	private BufferedImage destImage;
	private JComponent imagePanel ;
	public MainUI()
	{
		super("Automatic Paintly Render - GloomyFish");
		initComponent();
	}

	private void initComponent() {
		imagePanel = new JComponent()
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				g.clearRect(0, 0, getWidth(), getHeight());
				if(srcImage != null)
				{
					g.drawImage(srcImage, 0, 0, srcImage.getWidth(), srcImage.getHeight(), null);
				}
				if(destImage != null && srcImage != null)
				{
					g.drawImage(destImage, srcImage.getWidth() + 10, 0, destImage.getWidth(), destImage.getHeight(), null);
				}
				if(srcImage == null && destImage == null)
				{
					g.drawString("Please select your image...", 100, 200);
				}
			}
		};		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(imagePanel, BorderLayout.CENTER);
		paintBtn = new JButton(PAINT_CMD);
		selectBtn = new JButton(SELECT_CMD);
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		btnPanel.add(selectBtn);
		btnPanel.add(paintBtn);
		this.getContentPane().add(btnPanel, BorderLayout.SOUTH);
		selectBtn.addActionListener(this);
		paintBtn.addActionListener(this);
	}
	
	public void openView()
	{
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		java.net.URL imageURL = this.getClass().getResource("rainbow-fish-md.png");
		try {
			setIconImage(ImageIO.read(imageURL));
		} catch (IOException e) {
			System.err.println("An error occured when loading the image icon...");
		}
		this.setPreferredSize(new Dimension(800, 660));
		pack();
		centreView(this);
		setVisible(true);
	}
	
	public static void centreView(Window w) {
		Dimension me = w.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int newX = (screenSize.width - me.width)/2;
		int newY = (screenSize.height - me.height)/2;
		w.setLocation(newX, newY);
	}
	
	public static void main(String[] args)
	{
		MainUI ui = new MainUI();
		ui.openView();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		System.out.println("Command : " + command);
		if(command.equals(SELECT_CMD))
		{
			JFileChooser chooser = new JFileChooser();
			chooser.showOpenDialog(null);
			File f = chooser.getSelectedFile();
			if(f == null) return;
			try {
				srcImage = ImageIO.read(f);
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			this.repaint();
		}
		else if(command.equals(PAINT_CMD))
		{
			// stroke area
			StrokePaintlyMain spm = new StrokePaintlyMain(this);
			destImage = spm.filter(srcImage, null);
			this.repaint();
		}
	}

}
