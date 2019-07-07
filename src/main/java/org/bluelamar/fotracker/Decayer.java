package org.bluelamar.fotracker;

public interface Decayer {
	public long decay(long shelfLife, long orderAge, float decayRate);
}

