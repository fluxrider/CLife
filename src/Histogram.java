/*
 * Written by David Lareau on June 30, 2010.
 * 
 * Computes the smoothed local histogram, using the technique described in
 * Smoothed Local Histogram Filters Pixar Technical Memo 10-02 by Michael Kass and Justin Solomon
 */
import java.awt.image.BufferedImage;

import edu.mines.jtk.dsp.RecursiveGaussianFilter;
import edu.mines.jtk.util.CubicInterpolator;

public class Histogram {

	// Attributes
	public int depth;
	public int samples;
	public double sigmaW;
	public Kernel smoothingKernel;

	public float s[];
	public double lookup[][];
	public float map[][][]; // [i][x][y] the Mines-jtk RecursiveGaussianFilter works with floats
	public float smooth[][][]; // [i][x][y]

	private CubicInterpolator interpolation[][]; // pre-cached interpolator for each position

	// Construct
	public Histogram() {
	}

	// Methods
	// depth; number of discrete intensities (explicit number, not base 2 power, examples: 16, 256, 10) 
	// samples; number of discrete samples (paper uses 15 or 20)
	// sigmaW; sigma to be used by the Gaussian W, which is applied using the Mines-jtk library which uses Deriche's technique just like in the paper.
	// smoothingKernel; kernel K, I have made a box, gaussian and gaussian derivative smoothing kernel in the kernel package
	public void execute(int depth, int samples, double sigmaW, Kernel smoothingKernel, BufferedImage image, boolean useW, CubicInterpolator.Method method) {
		this.depth = depth;
		this.samples = samples;
		this.sigmaW = sigmaW;
		this.smoothingKernel = smoothingKernel;

		int W = image.getWidth();
		int H = image.getHeight();

		// compute values for s[i] (always samples the extreme values of 0 and depth-1, the others are in between (so min samples is 2)
		if (samples < 2) throw new RuntimeException("need at least 2 samples: samples=" + samples);
		s = new float[samples];
		double offset = (depth - 1) / (double) (samples - 1);
		for (int i = 0; i < samples; i++) {
			s[i] = (float) (i * offset);
		}

		// Storage
		lookup = new double[samples][depth];
		map = new float[samples][W][H];
		smooth = new float[samples][W][H];

		// for each samples
		for (int i = 0; i < samples; i++) {
			// compute lookup table for each intensities
			for (int intensity = 0; intensity < depth; intensity++) {
				lookup[i][intensity] = smoothingKernel.f(intensity - s[i]);
			}
			// map each pixel of image
			for (int y = 0; y < H; y++) {
				for (int x = 0; x < W; x++) {
					// scale pixel intensity to depth 
					int intensity = toDepth(image.getRGB(x, y));
					// map with lookup
					map[i][x][y] = (float) lookup[i][intensity];
				}
			}
			// smooth result
			if (useW) {
				RecursiveGaussianFilter gaussianW = new RecursiveGaussianFilter(sigmaW);
				gaussianW.apply00(map[i], smooth[i]);
			} else {
				// copy instead of smoothing
				for (int y = 0; y < H; y++) {
					for (int x = 0; x < W; x++) {
						smooth[i][x][y] = map[i][x][y];
					}
				}
			}
		}

		// Cache interpolators
		if (method == null) interpolation = null;
		else {
			interpolation = new CubicInterpolator[W][H];
			float ys[] = new float[samples];
			for (int y = 0; y < H; y++) {
				for (int x = 0; x < W; x++) {
					for (int i = 0; i < samples; i++) {
						ys[i] = smooth[i][x][y];
					}
					interpolation[x][y] = new CubicInterpolator(method, samples, s, ys);
				}
			}
		}
	}

	public double f(int x, int y, double intensity) {
		// Use pre-cached interpolator
		if (interpolation != null) {
			return interpolation[x][y].interpolate((float) intensity);
		} else {
			// Find nearest neighbor
			if (intensity < 0 || intensity > depth - 1) throw new RuntimeException("intensity must be between 0 and depth-1: intensity=" + intensity + " depth=" + depth);
			int i = 0;
			while (i < samples && intensity > s[i])
				i++;
			if (i == samples) return smooth[samples - 1][x][y];
			if (i == 0) return smooth[0][x][y];
			if (s[i] - intensity > intensity - s[i - 1]) return smooth[i - 1][x][y];
			return smooth[i][x][y];
		}
	}

	public double look(int sample, int intensity) {
		return lookup[sample][intensity];
	}

	public int toDepth(int intensity256) {
		return (int) Math.round(C.toDouble(C.grayDX(intensity256)) * (depth - 1));
	}

}
