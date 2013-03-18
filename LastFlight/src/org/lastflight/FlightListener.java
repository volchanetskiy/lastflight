package org.lastflight;

public interface FlightListener {
    public void onStatsChanged(Stats e);

    public void onFlightFinished(Flight e);
}