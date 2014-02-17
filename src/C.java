/* 
 * Written by David Lareau on May 8, 2009
 * 
 * Color conversion & decoding function
 * 
 * NOTE: Abridged for this project.
 */

public class C {

	// Static conversion
	public static int rgb(int r, int g, int b) {
		if (r > 255 || r < 0 || g > 255 || g < 0 || b > 255 || b < 0) throw new IllegalArgumentException(r + " " + g + " " + b);
		return (r << 16) | (g << 8) | b;
	}

	public static int fromGray(int gray) {
		return rgb(gray, gray, gray);
	}

	public static int r(int rgb) {
		return (rgb & 0x00FF0000) >> 16;
	}

	public static int g(int rgb) {
		return (rgb & 0x0000FF00) >> 8;
	}

	public static int b(int rgb) {
		return (rgb & 0x000000FF);
	}

	public static int grayDX(int rgb) {
		return (int) (0.2125 * r(rgb) + 0.7154 * g(rgb) + 0.0721 * b(rgb));
	}

	public static double toDouble(int b255) {
		return b255 / (double) 255;
	}

	public static int to255(double v) {
		return (int) Math.round(v * 255);
	}

}
