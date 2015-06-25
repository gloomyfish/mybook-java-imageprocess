package com.book.chapter.two;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.LookupOp;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MyFilters extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage image = null;
	private BufferedImage destImage = null;
	public MyFilters()
	{
		super();
		java.net.URL imageURL = 
				this.getClass().getResource("lena.jpg");
		try {
			image = ImageIO.read(imageURL);
		} catch (IOException e) {
			System.err.println("An error occured when loading the image resource...");
		}
	}
	
	public void setImage(BufferedImage selectImage)
	{
		this.image = selectImage;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
	    if(image != null)
	    {
	    	g2d.drawImage(image, 0, 0, image.getWidth(), 
	    			image.getHeight(), null);
	    	if(destImage != null)
	    	{
	    		g2d.drawImage(destImage, image.getWidth() + 10 , 
	    				0, destImage.getWidth(), 
	    				destImage.getHeight(), null);
	    	}
	    }
	}
	
	public void doColorGray(BufferedImage bi)
	{
		ColorConvertOp filterObj = new ColorConvertOp(
				ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		destImage = filterObj.filter(bi, null);
	}
	
	public void doBinaryImage(BufferedImage bi)
	{
		doColorGray(bi);
		byte[] threshold = new byte[256];
		for (int i = 0; i < 256; i++)
		{
			threshold[i] = (i < 128) ? (byte)0 : (byte)255;
		}
		BufferedImageOp thresholdOp =
		new LookupOp(new ByteLookupTable(0, threshold), null);
		destImage = thresholdOp.filter(destImage, null);
	}
	
	public void doBlur(BufferedImage bi)
	{
		// fix issue - unable to convolve src image
		if (bi.getType()==BufferedImage.TYPE_3BYTE_BGR)
		{
			bi=convertType(bi, BufferedImage.TYPE_INT_RGB);
		}
		
		float ninth = 1.0f / 9.0f;
		float[] blurKernel = {
				ninth, ninth, ninth,
				ninth, ninth, ninth,
				ninth, ninth, ninth
			};
		BufferedImageOp blurFilter = 
				new ConvolveOp(new Kernel(3, 3, blurKernel));
		destImage = blurFilter.filter(bi, null);
	}
	
	public BufferedImage doLookUp(BufferedImage bi)
	{
		byte[][] lookupData = new byte[3][256];
	    for (int cnt = 0; cnt < 256; cnt++){
	      lookupData[0][cnt] = (byte)(255-cnt);
	      lookupData[1][cnt] = (byte)(255-cnt);
	      lookupData[2][cnt] = (byte)(255-cnt);
	    }
	    ByteLookupTable lookupTable = 
                new ByteLookupTable(0,lookupData);
	    BufferedImageOp filterObj = 
                new LookupOp(lookupTable,null);
		return filterObj.filter(bi, null);
	}
	
	public void doScale(BufferedImage bi, double sx, double sy)
	{
		AffineTransformOp atfFilter = new AffineTransformOp(
				AffineTransform.getScaleInstance(sx, sy),				
				AffineTransformOp.TYPE_BILINEAR);
		int nw = (int)(bi.getWidth() * sx);
		int nh = (int)(bi.getHeight() * sy);
		BufferedImage result = new BufferedImage(
				nw, nh, BufferedImage.TYPE_3BYTE_BGR);
		destImage = atfFilter.filter(bi, result);
	}
	
	/**
	 * you always get this issue - unable to convolve src image
	 * 
	 * @param src
	 * @param type
	 * @return
	 */
	private BufferedImage convertType(BufferedImage src, int type)
	{
		ColorConvertOp cco=new ColorConvertOp(null);
		BufferedImage dest=new BufferedImage(
				src.getWidth(), src.getHeight(), type);
		cco.filter(src, dest);
		return dest;
	}
	
	public static void main(String[] args) {
		JFrame ui = new JFrame("Font Demo Graphics2D");
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.getContentPane().setLayout(new BorderLayout());
		ui.getContentPane().add(new MyFilters(), 
				BorderLayout.CENTER);
		ui.setPreferredSize(new Dimension(380, 380));
		ui.pack();
		ui.setVisible(true);
	}
}
