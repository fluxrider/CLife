/* 
 * Written by David Lareau on March 13, 2011
 * 
 * A variant of Conway Game of Life which uses continuous values.
 * I haven't seen anybody try this, and I was curious to see what it looks like. 
 */

import java.awt.image.BufferedImage;

import edu.mines.jtk.util.CubicInterpolator;

public class LifeC {

	// Parameters
	private int depth;
	private int samples;
	private double sigmaK;
	private double sigmaW;
	CubicInterpolator.Method interpolation;
	private double aliveThreshold;
	private double aliveMin, aliveMax;
	private double birthMin, birthMax;

	// Setup
	public String toString() {
		return "Life Continous";
	}

	public void defaultSetup() {
		// LIFE parameters
		aliveThreshold = .2;
		aliveMin = .125;
		aliveMax = .5;
		birthMin = .25;
		birthMax = .5;
		// smoothing historgram paramaters
		depth = 256;
		samples = 20;
		sigmaK = 13.0;
		sigmaW = 2.0;
		interpolation = CubicInterpolator.Method.LINEAR;
	}

	// Execute
	public BufferedImage execute(BufferedImage source) {
		int W = source.getWidth();
		int H = source.getHeight();
		BufferedImage out = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB); // the reason I don't do it in-place is because the source is often monochrome and I don't want to be sometime in-place and sometime not

		// Compute the integral of local histograms
		Histogram integral = new Histogram();
		integral.execute(depth, samples, sigmaW, new GaussianI1(0, sigmaK), source, true, interpolation);

		// for each pixel
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				int rgb = source.getRGB(x, y);
				// convert rgb to life force
				double life = C.toDouble(C.grayDX(rgb));
				// get the percentage of life in the neighborhood
				double aliveNeighborhood = integral.f(x, y, aliveThreshold * 255);
				// I need to remove the life force at the current position from the neighborhood count, but I'm not sure how to compute the population percentage
				// value of one member of the neighborhood
				// I'm doing an approximation that gaussianW of 1 is approx 3x3 window, and gaussianW of 6 is approx 15x15
				if (life >= aliveThreshold) {
					double n = 43.2 * sigmaW - 34.2;
					aliveNeighborhood -= 1 / n;
				}
				// if alive
				if (life >= aliveThreshold) {
					// stay alive and adjust life force if neighborhood alive population is within 12.5% to 50%, with life force being at its peak at 31.25%
					double min = aliveMin;
					double max = aliveMax;
					double half = (max + min) / 2;
					double delta = half - min;
					if (aliveNeighborhood < min || aliveNeighborhood > max) life = 0;
					else life = 1 - (Math.abs(aliveNeighborhood - half) / delta);
				}
				// if almost dead or dead
				else {
					// come to life if neighbohood alive population is withing 25% to 50%, with life force being at its peak at 37.5%
					double min = birthMin;
					double max = birthMax;
					double half = (max + min) / 2;
					double delta = half - min;
					if (aliveNeighborhood < min || aliveNeighborhood > max) life = 0;
					else life = 1 - (Math.abs(aliveNeighborhood - half) / delta);
				}
				// convert life to rgb
				rgb = C.fromGray(C.to255(life));
				out.setRGB(x, y, rgb);
			}
		}

		return out;
	}
}
