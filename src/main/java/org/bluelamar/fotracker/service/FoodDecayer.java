package org.bluelamar.fotracker.service;

import org.bluelamar.fotracker.Decayer;

public class FoodDecayer implements Decayer {
    @Override
    public long decay(long shelfLife, long curTimeSecs, long orderTimeSecs, float decayRate) {
        long orderAge = curTimeSecs - orderTimeSecs;
        float val = (shelfLife - orderAge) - (decayRate * (float)orderAge);
        return (long)val;
    }

    @Override
    public long normalized(long shelfLife, long curTimeSecs, long orderTimeSecs, float decayRate) {
        long val = decay(shelfLife, curTimeSecs, orderTimeSecs, decayRate);
        return val / shelfLife;
    }
}

