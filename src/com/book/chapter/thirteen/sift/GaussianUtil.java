package com.book.chapter.thirteen.sift;


public class GaussianUtil {

	public static void downsample(Float2DArray src, Float2DArray dst) {
		int ws = 2 * src.width;
		int rs = 0;
		for (int r = 0; r < dst.data.length; r += dst.width) {
			int xs = 0;
			for (int x = 0; x < dst.width; ++x) {
				dst.data[r + x] = src.data[rs + xs];
				xs += 2;
			}
			rs += ws;
		}
	}

	/**
	 * return a integer that is flipped in the range [0 ... mod - 1]
	 * 
	 * @param a
	 *            the value to be flipped
	 * @param range
	 *            the size of the range
	 * @return a flipped in range like a ping pong ball
	 */
	public static final int flipInRange(int a, int mod) {
		int p = 2 * mod;
		if (a < 0)
			a = p + a % p;
		if (a >= p)
			a = a % p;
		if (a >= mod)
			a = mod - a % mod - 1;
		return a;
	}

	/**
	 * convolve an image with a horizontal and a vertical kernel simple
	 * straightforward, not optimized---replace this with a trusted better
	 * version soon
	 * 
	 * @param input
	 *            the input image
	 * @param h
	 *            horizontal kernel
	 * @param v
	 *            vertical kernel
	 * 
	 * @return convolved image
	 */
	public static Float2DArray convolveSeparable(Float2DArray input, float[] h,
			float[] v) {
		Float2DArray output = new Float2DArray(input.width, input.height);
		Float2DArray temp = new Float2DArray(input.width, input.height);

		int hl = h.length / 2;
		int vl = v.length / 2;

		int xl = input.width - h.length + 1;
		int yl = input.height - v.length + 1;

		// create lookup tables for coordinates outside the image range
		int[] xb = new int[h.length + hl - 1];
		int[] xa = new int[h.length + hl - 1];
		for (int i = 0; i < xb.length; ++i) {
			xb[i] = flipInRange(i - hl, input.width);
			xa[i] = flipInRange(i + xl, input.width);
		}

		int[] yb = new int[v.length + vl - 1];
		int[] ya = new int[v.length + vl - 1];
		for (int i = 0; i < yb.length; ++i) {
			yb[i] = input.width * flipInRange(i - vl, input.height);
			ya[i] = input.width * flipInRange(i + yl, input.height);
		}

		xl += hl;
		yl += vl;
		// horizontal convolution per row
		int rl = input.height * input.width;
		for (int r = 0; r < rl; r += input.width) {
			for (int x = hl; x < xl; ++x) {
				int c = x - hl;
				float val = 0;
				for (int xk = 0; xk < h.length; ++xk) {
					val += h[xk] * input.data[r + c + xk];
				}
				temp.data[r + x] = val;
			}
			for (int x = 0; x < hl; ++x) {
				float valb = 0;
				float vala = 0;
				for (int xk = 0; xk < h.length; ++xk) {
					valb += h[xk] * input.data[r + xb[x + xk]];
					vala += h[xk] * input.data[r + xa[x + xk]];
				}
				temp.data[r + x] = valb;
				temp.data[r + x + xl] = vala;
			}
		}

		// vertical convolution per column
		rl = yl * temp.width;
		int vlc = vl * temp.width;
		for (int x = 0; x < temp.width; ++x) {
			for (int r = vlc; r < rl; r += temp.width) {
				float val = 0;
				int c = r - vlc;
				int rk = 0;
				for (int yk = 0; yk < v.length; ++yk) {
					val += v[yk] * temp.data[c + rk + x];
					rk += temp.width;
				}
				output.data[r + x] = val;
			}
			for (int y = 0; y < vl; ++y) {
				int r = y * temp.width;
				float valb = 0;
				float vala = 0;
				for (int yk = 0; yk < v.length; ++yk) {
					valb += h[yk] * temp.data[yb[y + yk] + x];
					vala += h[yk] * temp.data[ya[y + yk] + x];
				}
				output.data[r + x] = valb;
				output.data[r + rl + x] = vala;
			}
		}

		return output;
	}

	public static Float2DArray blurWith1DGaussian(Float2DArray input,
			float sigma) {
		Float2DArray output = new Float2DArray(input.getWidth(),
				input.getHeight());

		float avg, kernelsum = 0;
		float[] kernel = createGaussianKernel1D(sigma, true);
		int filterSize = kernel.length;

		// get kernel sum
		for (double value : kernel)
			kernelsum += value;

		// fold in x
		for (int x = 0; x < input.getWidth(); x++)
			for (int y = 0; y < input.getHeight(); y++) {
				avg = 0;

				if (x - filterSize / 2 >= 0
						&& x + filterSize / 2 < input.getWidth())
					for (int f = -filterSize / 2; f <= filterSize / 2; f++)
						avg += input.getValue(x + f, y)
								* kernel[f + filterSize / 2];
				else
					for (int f = -filterSize / 2; f <= filterSize / 2; f++)
						avg += input.getZero(x + f, y)
								* kernel[f + filterSize / 2];

				output.setValue(x, y, avg / kernelsum);

			}

		// fold in y
		for (int x = 0; x < input.getWidth(); x++) {
			float[] temp = new float[input.getHeight()];

			for (int y = 0; y < input.getHeight(); y++) {
				avg = 0;

				if (y - filterSize / 2 >= 0
						&& y + filterSize / 2 < input.getHeight())
					for (int f = -filterSize / 2; f <= filterSize / 2; f++)
						avg += output.getValue(x, y + f)
								* kernel[f + filterSize / 2];
				else
					for (int f = -filterSize / 2; f <= filterSize / 2; f++)
						avg += output.getZero(x, y + f)
								* kernel[f + filterSize / 2];

				temp[y] = avg / kernelsum;
			}

			for (int y = 0; y < input.getHeight(); y++)
				output.setValue(x, y, temp[y]);
		}

		return output;
	}

	public static void smoothImageWithGaussian(Float2DArray input, float sigma) {
		float[] kernels = createGaussianKernel1D(sigma, true);
		float[] tempData = new float[input.width * input.height];
		blur(input.data, tempData, input.width, input.height, kernels); // H
																		// Gaussian
		blur(tempData, input.data, input.height, input.width, kernels); // V
																		// Gaussain
	}

	private static void blur(float[] inPixels, float[] outPixels, int width,
			int height, float[] kernel) {
		int subCol = 0;
		int index = 0, index2 = 0;
		float graySum = 0;
		int filterSize = kernel.length / 2;
		for (int row = 0; row < height; row++) {
			index = row;
			for (int col = 0; col < width; col++) {
				graySum = 0;
				for (int m = -filterSize; m <= filterSize; m++) {
					subCol = col + m;
					if (subCol < 0 || subCol >= width) {
						subCol = 0;
					}
					index2 = row * width + subCol;
					graySum += (inPixels[index2] * kernel[m + filterSize]);
				}
				outPixels[index] = graySum;
				index += height;// correct index at here!!!, out put pixels
								// matrix,
			}
		}
	}

	/**
	 * Create a 1d-Gaussian kernel of appropriate size
	 * 
	 * @param sigma
	 *            Standard deviation of the Gaussian kernel
	 * @param normalize
	 *            Normalize integral of the Gaussian kernel to 1 or not...
	 * 
	 * @return float[] Gaussian kernel of appropriate size
	 */
	public static float[] createGaussianKernel1D(float sigma, boolean normalize) {
		int size = 3;
		float[] gaussianKernel;

		if (sigma <= 0) {
			gaussianKernel = new float[3];
			gaussianKernel[1] = 1;
		} else {
			size = Math.max(3, (int) (2 * (int) (3 * sigma + 0.5) + 1));
			System.out.println("Gaussian 1D windows size : " + size);
			float two_sq_sigma = 2 * sigma * sigma;
			gaussianKernel = new float[size];

			for (int x = size / 2; x >= 0; --x) {
				float val = (float) Math.exp(-(float) (x * x) / two_sq_sigma);

				gaussianKernel[size / 2 - x] = val;
				gaussianKernel[size / 2 + x] = val;
			}
		}

		if (normalize) {
			float sum = 0;
			for (float value : gaussianKernel)
				sum += value;

			for (int i = 0; i < gaussianKernel.length; i++)
				gaussianKernel[i] /= sum;
		}

		return gaussianKernel;
	}
    public static Float2DArray[] createGradients( Float2DArray array)
    {
    	Float2DArray[] gradients = new Float2DArray[2];
        gradients[0] = new Float2DArray(array.width, array.height);
        gradients[1] = new Float2DArray(array.width, array.height);

        for (int y = 0; y < array.height; ++y)
        {
                int[] ro = new int[3];
                    ro[0] = array.width * Math.max(0, y - 1);
                    ro[1] = array.width * y;
                    ro[2] = array.width * Math.min(y + 1, array.height - 1);
                for (int x = 0; x < array.width; ++x)
                {
                        // L(x+1, y) - L(x-1, y)
                        float der_x = (
                                        array.data[ro[1] + Math.min(x + 1, array.width - 1)] -
                                        array.data[ro[1] + Math.max(0, x - 1)]) / 2;

                        // L(x, y+1) - L(x, y-1)
                        float der_y = (
                                array.data[ro[2] + x] -
                                array.data[ro[0] + x]) / 2;

                        //! amplitude
                        gradients[0].data[ro[1]+x] = (float)Math.sqrt( Math.pow( der_x, 2 ) + Math.pow( der_y, 2 ) );
                        //! orientation
                        gradients[1].data[ro[1]+x] = (float)Math.atan2( der_y, der_x );
                }
        }
        //ImageArrayConverter.FloatArrayToImagePlus( gradients[ 1 ], "gradients", 0, 0 ).show();
        return gradients;
    }
	/**
	 * 
	 * @param sigma
	 * @param offset_x
	 * @param offset_y
	 * @param normalize
	 * @return
	 */
	public static Float2DArray create_gaussian_kernel_2D_offset(float sigma,
			float offset_x, float offset_y, boolean normalize) {
		int size = 3;
		Float2DArray gaussian_kernel;
		if (sigma == 0) {
			gaussian_kernel = new Float2DArray(3, 3);
			gaussian_kernel.data[4] = 1;
		} else {
			size = Math.max(3, (int) (2 * Math.round(3 * sigma) + 1));
			float two_sq_sigma = 2 * sigma * sigma;
			// float normalization_factor = 1.0/(float)M_PI/two_sq_sigma;
			gaussian_kernel = new Float2DArray(size, size);
			for (int x = size - 1; x >= 0; --x) {
				float fx = (float) (x - size / 2);
				for (int y = size - 1; y >= 0; --y) {
					float fy = (float) (y - size / 2);
					float val = (float) (Math
							.exp(-(Math.pow(fx - offset_x, 2) + Math.pow(fy
									- offset_y, 2))
									/ two_sq_sigma));
					gaussian_kernel.setValue(x, y, val);
				}
			}
		}
		if (normalize) {
			float sum = 0;
			for (float value : gaussian_kernel.data)
				sum += value;

			for (int i = 0; i < gaussian_kernel.data.length; i++)
				gaussian_kernel.data[i] /= sum;
		}
		return gaussian_kernel;
	}
}
