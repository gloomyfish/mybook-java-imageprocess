package com.book.chapter.thirteen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.MediaTracker;
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

public class PyramidDemoUI extends JComponent implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton upButton;
	private JButton downButton;
	private BufferedImage[] reduceImages;
	private BufferedImage[] expandImages;
	private BufferedImage sourceImage;
	private Dimension mySize;
	private MediaTracker tracker;
	private PyramidAlgorithm pyramid = new PyramidAlgorithm();
	public PyramidDemoUI(File f)
	{
		initComponents(f);
	}

	private void initComponents(File f)
	{
		// TODO Auto-generated method stub
		try {  
			sourceImage = ImageIO.read(f);  
        } catch (IOException e1) {  
            e1.printStackTrace();  
        }  
          
        tracker = new MediaTracker(this);  
        tracker.addImage(sourceImage, 1);  
          
        // blocked 10 seconds to load the image data  
        try {  
            if (!tracker.waitForID(1, 10000)) {  
                System.out.println("Load error.");  
                System.exit(1);  
            }// end if  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
            System.exit(1);  
        }// end catch  
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        upButton = new JButton("Laplacian Pyramid");
        downButton = new JButton("Pyramid Down");
        upButton.addActionListener(this);
        downButton.addActionListener(this);
        btnPanel.add(upButton);
        btnPanel.add(downButton);
        mySize = new Dimension(800, 800);   
        JFrame mainFrame = new JFrame("Pyramid Demo - Gloomyfish");
        mainFrame.getContentPane().setLayout(new BorderLayout());
        mainFrame.getContentPane().add(this, BorderLayout.CENTER);
        mainFrame.getContentPane().add(btnPanel, BorderLayout.SOUTH);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
        mainFrame.pack();  
        mainFrame.setVisible(true);  
	}

	@Override
	public Dimension getPreferredSize() {
		return mySize;
	}

	@Override
	protected void paintComponent(Graphics g) 
	{
		g.drawImage(sourceImage, 10, 10, sourceImage.getWidth(), sourceImage.getHeight(), null);
		int width = 10;
		if(reduceImages != null) {
			for(int i=1; i<reduceImages.length; i++) {
				width += (10 + reduceImages[i-1].getWidth());
				g.drawImage(reduceImages[i], width, 10, reduceImages[i].getWidth(), reduceImages[i].getHeight(), null);
			}
		}
		
		width = 10;
		if(expandImages != null) {
			for(int i=1; i<expandImages.length; i++) {
				g.drawImage(expandImages[i], width, 15, expandImages[i].getWidth(), expandImages[i].getHeight(), null);
				// g.drawImage(expandImages[i], width, 15 + sourceImage.getHeight(), expandImages[i].getWidth(), expandImages[i].getHeight(), null);
				width += (10 + expandImages[i].getWidth());
			}
		}
		super.paintComponent(g);
	}
	
    public static void main(String[] args) {  
        JFileChooser chooser = new JFileChooser();  
        chooser.showOpenDialog(null);  
        File f = chooser.getSelectedFile();  
        new PyramidDemoUI(f);  
    }

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals("Laplacian Pyramid")) {
			if(reduceImages != null) {
				// int size = reduceImages.length;
				// PyramidAlgorithm pyramid = new PyramidAlgorithm();
				// expandImages = pyramid.getLaplacianPyramid(reduceImages);
				expandImages = pyramid.pyramidUp(reduceImages);
				repaint();
			} else {
				
			}

		} else if(event.getActionCommand().equals("Pyramid Down")) {
			// a.Smooth the image with Gaussian filter 5×5(1/4-a/2, 1/4, a, 1/4, 1/4-a/2) a = [0.3,0.6]
			// b.Sub sample the image by half - 选择偶数行与列
			// c.If reached desired size stop, else send the result to step 1
			// PyramidAlgorithm pyramid = new PyramidAlgorithm();
			reduceImages = pyramid.pyramidDown(sourceImage);
			repaint();
		} else {
			// do nothing
		}
		
	}  

}
