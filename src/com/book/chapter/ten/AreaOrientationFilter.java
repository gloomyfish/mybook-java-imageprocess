package com.book.chapter.ten;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.book.chapter.four.AbstractBufferedImageOp;

public class AreaOrientationFilter extends AbstractBufferedImageOp {

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		int width = src.getWidth();
        int height = src.getHeight();

        if ( dest == null )
        	dest = createCompatibleDestImage( src, null );

        // 第一步：获取输入图像像素数组
        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        getRGB( src, 0, 0, width, height, inPixels );
        int index = 0;
        // 初始化每个像素节点状态
        List<PixelPoint> pixelList = new ArrayList<PixelPoint>();
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		PixelPoint p = new PixelPoint(row, col, (inPixels[index] >> 16) & 0xff);
        		pixelList.add(p);
        	}
        }
        // 添加每个像素节点的四邻域像素
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		PixelPoint p = pixelList.get(index);
        		
        		// add four neighbors for each pixel
        		if((row - 1) >= 0)
        		{
        			index = (row-1) * width + col;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((row + 1) < height)
        		{
        			index = (row+1) * width + col;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((col - 1) >= 0)
        		{
        			index = row * width + col-1;
        			p.addNeighour(pixelList.get(index));
        		}
        		if((col+1) < width)
        		{
        			index = row * width + col+1;
        			p.addNeighour(pixelList.get(index));
        		}
        	}
        }
        
        // second step, connected component labeling algorithm
        // 深度优先搜索算法，连通组件标记
        DFSAlgorithm dfs = new DFSAlgorithm(pixelList);
        dfs.process();
        int max = dfs.getTotalOfLabels();
        System.out.println("Total Number of Labels : " + max);
        // 计算三个参数啊a,b,c, 角度theta, Emin, Emax
        int[] input = new int[pixelList.size()];
        GeometricMomentsAlg momentsAlg = new GeometricMomentsAlg();
        momentsAlg.setBACKGROUND(0);
        double[][] labelCenterPos = new double[max][2];
        double[][] centerAngles = new double[max][3];
        for(int i=1; i<=max; i++)
        {
        	for(int p=0; p<input.length; p++)
        	{
        		if(pixelList.get(p).getLabel() == i)
        		{
        			input[p] = pixelList.get(p).getLabel();        			
        		}
        		else
        		{
        			input[p] = 0;
        		}
        	}
        	// 计算每个组件的质心
        	labelCenterPos[i-1] = momentsAlg.getGeometricCenterCoordinate(input, width, height);
        	// 计算每个组件的
        	double a = momentsAlg.centralMoments(input, width, height, 0, 2);
        	double b = momentsAlg.centralMoments(input, width, height, 1, 1);
        	double c = momentsAlg.centralMoments(input, width, height, 2, 0);
        	double bb = b*b;
        	double ac2 = Math.pow((a-c),2);
        	double sum = 2 * Math.sqrt(bb + ac2);
        	double angle = Math.atan((2*b)/(a - c))/2.0;
        	double emax=(a+c)/2 - (ac2/sum) - (bb/sum);
        	double emin=(a+c)/2 + (ac2/sum) + (bb/sum);
        	// 范围为0~180之间
        	centerAngles[i-1][0] = angle + Math.PI/2.0;
        	centerAngles[i-1][1] = emax;
        	centerAngles[i-1][2] = emin;
        }
        
        // render the angle/orientation info for each region
        // render the each connected component center position
        for(int row=0; row<height; row++) {
        	for(int col=0; col<width; col++) {
        		index = row * width + col;
        		if(pixelList.get(index).getLabel() == 0)
        		{
        			outPixels[index] = (255 << 24) | (0 << 16) | (0 << 8) | 0; // make it as black for background
        		}
        		else
        		{
        			outPixels[index] = (255 << 24) | (0 << 16) | (0 << 8) | 100; // make it as blue for each region area
        		}
        	}
        }
        
        int labelCount = centerAngles.length;
        for(int i=0; i<labelCount; i++)
        {
        	System.out.println("Region " + i + "'s angle = " + centerAngles[i][0]);
        	System.out.println("Region " + i + " e = " + (centerAngles[i][1]/centerAngles[i][2]));
        	double sin = Math.sin(centerAngles[i][0]);
        	double cos = Math.cos(centerAngles[i][0]);
        	System.out.println("sin = " + sin);
        	System.out.println("cos = " + cos);
        	System.out.println();
        	int crow = (int)labelCenterPos[i][0];
        	int ccol = (int)labelCenterPos[i][1];
        	int radius = (int)centerAngles[i][1];
        	// it is trick, display correct angle as you see!!!
        	for(int j=0; j<radius; j++)
        	{
        		int drow = (int)(crow - j * sin); 
        		int dcol = (int)(ccol + j * cos);
        		if(drow >= height || drow <=0 ) continue;
        		if(dcol >= width || dcol <=0 ) continue;
        		index = drow * width + dcol;
            	outPixels[index] = (255 << 24) | (255 << 16) | (255 << 8) | 0; 
        	}
        	int cx = (int)labelCenterPos[i][1];
        	// 根据中心点显示水平直线
        	for(int px=cx;px<width; px++)
        	{
        		int cy = (int)labelCenterPos[i][0];
        		index = cy * width + px;
        		outPixels[index] = (255 << 24) | (255 << 16) | (255 << 8) | 0; 
        	}
        }
        
        // make it as white color for each center position
        for(int i=0; i<max; i++)
        {
        	int crow = (int)labelCenterPos[i][0];
        	int ccol = (int)labelCenterPos[i][1];
        	index = crow * width + ccol;
        	outPixels[index] = (255 << 24) | (255 << 16) | (255 << 8) | 255; 
        }
        
        setRGB( dest, 0, 0, width, height, outPixels );
		return dest;
	}

}
