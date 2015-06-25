package com.book.chapter.thirteen;
/***
 * 
 * 浼犲叆鐨勫浘鍍忎负浜屽?鍥惧儚锛岃儗鏅负榛戣壊锛岀洰鏍囧墠鏅鑹蹭负涓虹櫧鑹?
 * @author gloomyfish
 * 
 */
public class CircleHough {

	private int[] input;
	private int[] output;
	private int width;
	private int height;
	private int[] acc;
	private int accSize = 1;
	private int[] results;
	private int r; // 鍦嗗懆鐨勫崐寰勫ぇ灏?

	public CircleHough() {
		System.out.println("Hough Circle Detection...");
	}

	public void init(int[] inputIn, int widthIn, int heightIn, int radius) {
		r = radius;
		width = widthIn;
		height = heightIn;
		input = new int[width * height];
		output = new int[width * height];
		input = inputIn;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				output[x + (width * y)] = 0xff000000; //榛樿鍥惧儚鑳屾櫙棰滆壊涓洪粦鑹?
			}
		}
	}

	public void setCircles(int circles) {
		accSize = circles; // 妫?祴鐨勪釜鏁?
	}
	
	/**
	 * 霍夫变换处理 - 检测半径大小符合的圆的个数
	 * 1. 将图像像素从2D空间坐标转换到极坐标空间
	 * 2. 在极坐标空间中归一化各个点强度，使之在0〜255之间
	 * 3. 根据极坐标的R值与输入参数(圆的半径)相等，寻找2D空间的像素点
	 * 4. 对找出的空间像素点赋予结果颜色(红色)
	 * 5. 返回结果2D空间像素集合
	 * @return int []
	 */
	public int[] process() {

		// 瀵逛簬鍦嗙殑鏋佸潗鏍囧彉鎹㈡潵璇达紝鎴戜滑闇?360搴︾殑绌洪棿姊害鍙犲姞鍊?
		acc = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				acc[y * width + x] = 0;
			}
		}
		int x0, y0;
		double t;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				if ((input[y * width + x] & 0xff) == 255) {

					for (int theta = 0; theta < 360; theta++) {
						t = (theta * 3.14159265) / 180; // 瑙掑害鍊? ~ 2*PI
						x0 = (int) Math.round(x - r * Math.cos(t));
						y0 = (int) Math.round(y - r * Math.sin(t));
						if (x0 < width && x0 > 0 && y0 < height && y0 > 0) {
							acc[x0 + (y0 * width)] += 1;
						}
					}
				}
			}
		}

		// now normalise to 255 and put in format for a pixel array
		int max = 0;

		// Find max acc value
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				if (acc[x + (y * width)] > max) {
					max = acc[x + (y * width)];
				}
			}
		}

		// 鏍规嵁鏈?ぇ鍊硷紝瀹炵幇鏋佸潗鏍囩┖闂寸殑鐏板害鍊煎綊涓?寲澶勭悊
		int value;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				value = (int) (((double) acc[x + (y * width)] / (double) max) * 255.0);
				acc[x + (y * width)] = 0xff000000 | (value << 16 | value << 8 | value);
			}
		}
		
		// 缁樺埗鍙戠幇鐨勫渾
		findMaxima();
		System.out.println("done");
		return output;
	}

	private int[] findMaxima() {
		results = new int[accSize * 3];
		int[] output = new int[width * height];
		
		// 鑾峰彇鏈?ぇ鐨勫墠accSize涓?
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int value = (acc[x + (y * width)] & 0xff);

				// if its higher than lowest value add it and then sort
				if (value > results[(accSize - 1) * 3]) {

					// add to bottom of array
					results[(accSize - 1) * 3] = value; //鍍忕礌鍊?
					results[(accSize - 1) * 3 + 1] = x; // 鍧愭爣X
					results[(accSize - 1) * 3 + 2] = y; // 鍧愭爣Y

					// shift up until its in right place
					int i = (accSize - 2) * 3;
					while ((i >= 0) && (results[i + 3] > results[i])) {
						for (int j = 0; j < 3; j++) {
							int temp = results[i + j];
							results[i + j] = results[i + 3 + j];
							results[i + 3 + j] = temp;
						}
						i = i - 3;
						if (i < 0)
							break;
					}
				}
			}
		}

		// 鏍规嵁鎵惧埌鐨勫崐寰凴锛屼腑蹇冪偣鍍忕礌鍧愭爣p(x, y)锛岀粯鍒跺渾鍦ㄥ師鍥惧儚涓?
		System.out.println("top " + accSize + " matches:");
		for (int i = accSize - 1; i >= 0; i--) {
			drawCircle(results[i * 3], results[i * 3 + 1], results[i * 3 + 2]);
		}
		return output;
	}

	private void setPixel(int value, int xPos, int yPos) {
		/// output[(yPos * width) + xPos] = 0xff000000 | (value << 16 | value << 8 | value);
		output[(yPos * width) + xPos] = 0xffff0000;
	}

	// draw circle at x y
	private void drawCircle(int pix, int xCenter, int yCenter) {
		pix = 250; // 棰滆壊鍊硷紝榛樿涓虹櫧鑹?

		int x, y, r2;
		int radius = r;
		r2 = r * r;
		
		// 缁樺埗鍦嗙殑涓婁笅宸﹀彸鍥涗釜鐐?
		setPixel(pix, xCenter, yCenter + radius);
		setPixel(pix, xCenter, yCenter - radius);
		setPixel(pix, xCenter + radius, yCenter);
		setPixel(pix, xCenter - radius, yCenter);

		y = radius;
		x = 1;
		y = (int) (Math.sqrt(r2 - 1) + 0.5);
		
		// 杈圭紭濉厖绠楁硶锛?鍏跺疄鍙互鐩存帴瀵瑰惊鐜墍鏈夊儚绱狅紝璁＄畻鍒板仛涓績鐐硅窛绂绘潵鍋?
		// 杩欎釜鏂规硶鏄埆浜哄啓鐨勶紝鍙戠幇瓒呰禐锛岃秴濂斤紒
		while (x < y) {
			setPixel(pix, xCenter + x, yCenter + y);
			setPixel(pix, xCenter + x, yCenter - y);
			setPixel(pix, xCenter - x, yCenter + y);
			setPixel(pix, xCenter - x, yCenter - y);
			setPixel(pix, xCenter + y, yCenter + x);
			setPixel(pix, xCenter + y, yCenter - x);
			setPixel(pix, xCenter - y, yCenter + x);
			setPixel(pix, xCenter - y, yCenter - x);
			x += 1;
			y = (int) (Math.sqrt(r2 - x * x) + 0.5);
		}
		if (x == y) {
			setPixel(pix, xCenter + x, yCenter + y);
			setPixel(pix, xCenter + x, yCenter - y);
			setPixel(pix, xCenter - x, yCenter + y);
			setPixel(pix, xCenter - x, yCenter - y);
		}
	}

	public int[] getAcc() {
		return acc;
	}

}
