package org.bluelamar.fotracker.service;

import org.bluelamar.fotracker.Decayer;

public class FoodDecayer implements Decayer {
	@Override
	public long decay(long shelfLife, long orderAge, float decayRate) {
		float val = (shelfLife - orderAge) - (decayRate * (float)orderAge);
		return (long)val;
	}
}

