package org.bluelamar.fotracker;

public interface Decayer {
    public long decay(long shelfLife, long curTimeSecs, long orderTimeSecs, float decayRate);
    public long normalized(long shelfLife, long curTimeSecs, long orderTimeSecs, float decayRate);
}

