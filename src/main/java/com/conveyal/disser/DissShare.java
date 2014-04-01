package com.conveyal.disser;

import org.opengis.feature.Feature;

public class DissShare {
	public double mag;
	public Feature diss;
	
	public DissShare(Feature diss, double mag) {
		this.diss = diss;
		this.mag = mag;
	}
}
