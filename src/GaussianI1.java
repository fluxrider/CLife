/*
 * Written by David Lareau on June 30, 2010.
 * 
 * the integral of a gaussian
 * 
 * I'm using cdf instead of the erf like the paper says, hopefully thats correct, I think they are the same except that I can't find a erf that takes a variance parameter!
 */


import cern.jet.random.Normal;

public class GaussianI1 implements Kernel {

	// Attributes
	private Normal gaussian;

	// Construct
	public GaussianI1(double mean, double standardDeviation) {
		this.gaussian = new Normal(mean, standardDeviation, null);
	}

	// Kernel
	public double f(double a) {
		return gaussian.cdf(a);
	}

}
