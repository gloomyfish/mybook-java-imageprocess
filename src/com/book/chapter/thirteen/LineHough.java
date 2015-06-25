package com.book.chapter.thirteen;

public class LineHough {
	private int[] input;
	private int[] output;
	private int width;
	private int height;
	private int[] acc;
	private int accSize = 6;
	private int[] results;

	public LineHough() {
		System.out.println("Hough Line Detection...");
	}

	public void init(int[] inputIn, int widthIn, int heightIn) {
		width = widthIn;
		height = heightIn;
		input = new int[width * height];
		output = new int[width * height];
		input = inputIn;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				output[y * width + x] = 0xff000000;
			}
		}
	}

	/**
	 * tell it how many lines should display...
	 * @param lines
	 */
	public void setLines(int lines) {
		accSize = lines;
	}
	/**
	 * 1. 初始化霍夫变换空间
	 * 2. 将图像的2D空间转换到霍夫空间,每个像素坐标都要转换到霍夫极坐标的对应强度值
	 * 3. 找出霍夫极坐标空间的最大强度值
	 * 4. 根据最大强度值归一化,范围为0 ~ 255
	 * 5. 根据输入前accSize值,画出前accSize个信号最强的直线
	 * @return
	 */
	public int[] process() {
		int rmax = (int) Math.sqrt(width * width + height * height);
		acc = new int[rmax * 180]; // 0 ~ 180闇嶅か鍧愭爣绌洪棿
		int r;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				if ((input[y * width + x] & 0xff) == 255) {

					for (int theta = 0; theta < 180; theta++) {
						r = (int) (x * Math.cos(((theta) * Math.PI) / 180) + y * Math.sin(((theta) * Math.PI) / 180)); // 鍍忕礌鐐筽(x, y)杞崲涓哄搴旂殑寮哄害鍊�																	
						if ((r > 0) && (r <= rmax))
							acc[r * 180 + theta] = acc[r * 180 + theta] + 1; // 寮哄害鍊煎鍔�
					}
				}
			}
		}

		// 鎵惧埌鏋佸潗鏍囩┖闂寸殑鏈�ぇ寮哄害鍊�
		int max = 0;
		for (r = 0; r < rmax; r++) {
			for (int theta = 0; theta < 180; theta++) {

				if (acc[r * 180 + theta] > max) {
					// swap the max value
					max = acc[r * 180 + theta];
				}
			}
		}

		// normalization all the values, 褰掍竴鍖栧鐞�
		int value;
		for (r = 0; r < rmax; r++) {
			for (int theta = 0; theta < 180; theta++) {

				value = (int) (((double) acc[r * 180 + theta] / (double) max) * 255.0);
				acc[r * 180 + theta] = 0xff000000 | (value << 16 | value << 8 | value);
			}
		}

		// 缁樺埗鎵惧埌n涓洿绾�
		findMaxima();

		System.out.println("done");
		return output;
	}

	private int[] findMaxima() {

		// 鏋佸潗鏍囦腑鏈�ぇ鐨勫崐寰勫�
		int rmax = (int) Math.sqrt(width * width + height * height);
		results = new int[accSize * 3];
		int[] output = new int[width * height];
		// 鏍规嵁杈撳叆鍙傛暟,鎵惧埌鍓峚ccSize涓瀬鍧愭爣绌洪棿涓己搴︽渶澶х殑鐩寸嚎R鍊�
		for (int r = 0; r < rmax; r++) {
			for (int theta = 0; theta < 180; theta++) {
				int value = (acc[r * 180 + theta] & 0xff);

				// if its higher than lowest value add it and then sort
				if (value > results[(accSize - 1) * 3]) {

					// add to bottom of array
					results[(accSize - 1) * 3] = value;
					results[(accSize - 1) * 3 + 1] = r;
					results[(accSize - 1) * 3 + 2] = theta;

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

		
		// 鏍规嵁鏋佸潗鏍囪褰曠殑R鍊�鍖归厤瀵瑰簲鐨勫儚绱犵偣,缁樺埗鍑烘娴嬪埌寰楃洿绾�										
		System.out.println("Total " + accSize + " matches:");
		for (int i = accSize - 1; i >= 0; i--) {
			drawPolarLine(results[i * 3], results[i * 3 + 1],results[i * 3 + 2]);
		}
		return output;
	}

	// 缁樺埗鐩寸嚎鏂规硶
	private void drawPolarLine(int value, int r, int theta) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int temp = (int) (x * Math.cos(((theta) * Math.PI) / 180) + y * Math.sin(((theta) * Math.PI) / 180));
				if ((temp - r) == 0) // 鍖归厤瀵瑰簲鍍忕礌鐐�缁樺埗鐩寸嚎
					output[y * width + x] = 0xffff0000; // 缁撴灉鐩寸嚎涓虹孩鑹�
			}
		}
	}

	public int[] getAcc() {
		return acc;
	}
}
