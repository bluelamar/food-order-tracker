package org.bluelamar.fotracker.service;

public class FoodDecayerOFlow extends FoodDecayer {
    @Override
    public long decay(long shelfLife, long curTimeSecs, long orderTimeSecs, float decayRate) {
        long orderAge = curTimeSecs - orderTimeSecs;
        float val = (shelfLife - orderAge) - (2 * decayRate * (float)orderAge);
        return (long)val;
    }
}

