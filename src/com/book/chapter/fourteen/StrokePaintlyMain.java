package com.book.chapter.fourteen;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.book.chapter.four.AbstractBufferedImageOp;

public class StrokePaintlyMain extends AbstractBufferedImageOp {
	private static final int _DFS = 10; // stroke size, default is 15
	private int s = 0;
	private JFrame owner;

	public StrokePaintlyMain(JFrame owner) {
		this.owner = owner;
	}

	public void setStrokeSize(int s) {
		this.s = s;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		// stroke area
		int width = src.getWidth();
		int height = src.getHeight();
		int[] inPixels = new int[width * height];
		int[] strokeArea = new int[width * height];
		StrokeAreaFilter strokeAreaFilter = new StrokeAreaFilter();
		BufferedImage destImage = strokeAreaFilter.filter(src, null);
		getRGB(src, 0, 0, width, height, inPixels);
		getRGB(destImage, 0, 0, width, height, strokeArea);
		// get stroke
		int count = 0;
		int factor = 1;
		int level = 1;
		int size = Math.max(width, height);
		if (s == 0) {
			s = _DFS;
		}
		List<StrokeElement> allStrokes = new ArrayList<StrokeElement>();
		StrokeGenerator sg = new StrokeGenerator();
		while (size > 4 * s) {
			System.out.println("Processing level : " + factor);
			allStrokes.addAll(sg.getStrokes(inPixels, strokeArea, s, factor,
					level, width, height));
			size /= 2;
			factor *= 2;
			level++;
		}

		// sort stroke, from lager size to small size
		sortByDesc(allStrokes);
		
		// load stroke template
		java.net.URL imageURL = this.getClass().getResource("stroke.png");
		BufferedImage strokeTemplate = null;
		try {
			strokeTemplate = ImageIO.read(imageURL);
		} catch (IOException e) {
			System.err.println("An error occured when loading the image icon...");
		}
		// start to paint the stroke now!!!
		BufferedImage canvasImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] resultPixels = new int[width * height];
		// °×É«±³¾°»­²¼
		Arrays.fill(resultPixels, -1);
		int totalStroke = 0;
		for (StrokeElement element : allStrokes) {
			int sw = (int)element.getW();
			int sh = (int)element.getL();
			if(sw == 0 || sh == 0) continue;
			// ·ÅËõ
			BufferedImage scaledImage = getScaledImage(strokeTemplate, sh, sw);
			// Ðý×ª
			BufferedImage rotatedImage = rotateImage(scaledImage, element.getTheta());
			// »æÖÆ-ÏñËØ»ìºÏ
			imageBlend(resultPixels, width, height, rotatedImage, element.getRgb(), element.getXc(), element.getYc());
			totalStroke++;
		}
		System.out.println("total strokes : " + allStrokes.size());
		System.out.println("painted strokes : " + totalStroke);
		setRGB(canvasImage, 0, 0, width, height, resultPixels);
		return canvasImage;
	}

	private void sortByDesc(List<StrokeElement> allStrokes) {
		int size = allStrokes.size();
		// selection sort
		for(int i=0; i<size-1; i++)
		{
			float d = allStrokes.get(i).getD();
			StrokeElement se = allStrokes.get(i);
			int selectedIndex = i;
			for(int j=i; j<size; j++)
			{
				float dd = allStrokes.get(j).getD();
				if(d <= dd)
				{
					// swap
					selectedIndex = j;
					d = dd; 
				}
			}
			// swap it
			if(selectedIndex == i) continue;
			StrokeElement sej = allStrokes.get(selectedIndex);
			allStrokes.set(selectedIndex, se);
			allStrokes.set(i, sej);
		}
	}

	public int imageBlend(int[] out, int width, int height,
			BufferedImage stroke, int[] rgb, int xc, int yc) {
		long la, li; /* line jumps for both images */
		int xi, yi; /* lower left corner in input image */
		int xa, ya; /* corresponding corner in alpha image */
		int wa, ha; /* area in alpha image to be blended */
		int x, y;
		int r = rgb[0], g = rgb[1], b = rgb[2];

		wa = stroke.getWidth();
		xa = 0;
		xi = xc - wa / 2;
		if (xi < 0) {
			wa += xi;
			xa -= xi;
			xi = 0;
		}
		if (xi > width)
			return 0;
		if (wa <= 0)
			return 0;
		if (xi + wa >= width)
			wa = width - xi;

		ha = stroke.getHeight();
		ya = 0;
		yi = yc - ha / 2;
		if (yi < 0) {
			ha += yi;
			ya -= yi;
			yi = 0;
		}
		if (yi > height)
			return 0;
		if (ha <= 0)
			return 0;
		if (yi + ha >= height)
			ha = height - yi;

		int[] sp = new int[stroke.getWidth() * stroke.getHeight()];
		getRGB(stroke, 0, 0, stroke.getWidth(), stroke.getHeight(), sp);
		getRGB(stroke, 0, 0, stroke.getWidth(), stroke.getHeight(), sp);
		int index = 0;
		for (y = 0; y < ha; y++) {
			for (x = 0; x < wa; x++) {
				index = y * stroke.getWidth() + x;
				int index2 = (y + yi) * width + (xi + x);
				int[] argb = getColorPixels(sp, index);
				int[] argb2 = getColorPixels(out, index2);
				float ba = argb[0] / 255.0f;
				float bai = 1.0f - ba;
				int ta = (int) (ba * argb[0] + bai * argb2[0]);
				int tr = (int) (ba * r + bai * argb2[1]);
				int tg = (int) (ba * g + bai * argb2[2]);
				int tb = (int) (ba * b + bai * argb2[3]);
				setColorPixels(out, index2, new int[] { ta, tr, tg, tb });
			}
		}
		return 1;
	}

	public BufferedImage getScaledImage(BufferedImage image, int w, int l) {
		BufferedImage scaledImage = new BufferedImage(w, l,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = scaledImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, w, l, null);
		graphics2D.dispose();
		return scaledImage;
	}

	public BufferedImage rotateImage(BufferedImage image, double angle) {
		double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
		int w = image.getWidth(), h = image.getHeight();
		int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math
				.floor(h * cos + w * sin);
		GraphicsConfiguration gc = owner.getGraphicsConfiguration();
		BufferedImage result = gc.createCompatibleImage(neww, newh,
				Transparency.TRANSLUCENT);
		Graphics2D g = result.createGraphics();
		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(angle, w / 2, h / 2);
		g.drawRenderedImage(image, null);
		g.dispose();
		return result;
	}

	public static int[] getColorPixels(int[] pixels, int index) {
		int[] argb = new int[4];
		argb[0] = (pixels[index] >> 24) & 0xff;
		argb[1] = (pixels[index] >> 16) & 0xff;
		argb[2] = (pixels[index] >> 8) & 0xff;
		argb[3] = pixels[index] & 0xff;
		return argb;
	}

	public static void setColorPixels(int[] pixels, int index, int[] argb) {
		pixels[index] = (argb[0] << 24) | (argb[1] << 16) | (argb[2] << 8)
				| argb[3];
	}

}
