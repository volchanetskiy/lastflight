package org.lastflight;

import java.util.ArrayList;
import java.util.Iterator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

class Stats {
    long currentDuration;
    double lF;
    double minLF;
    double maxLF;
}

class Flight {
    long endTime;
    long duration;
}

public class FlightTracker implements SensorEventListener {
    private ArrayList<FlightListener> mListeners = new ArrayList<FlightListener>();

    private double mLF;
    private double mMinLF = Double.POSITIVE_INFINITY;
    private double mMaxLF = Double.NEGATIVE_INFINITY;
    private boolean mFalling = false;
    private boolean mPause = false;
    private long mStartTime;
    private long mCurrentTime;
    private static final double sFallLF = 0.1;

    public void pause() {
        if (mFalling) finishFlight();
        mPause = true;
    }

    public void resume() {
        mPause = false;
    }

    private void finishFlight() {
        mFalling = false;
        if (mCurrentTime != mStartTime) fireFlightFinished();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mPause) return;
        mLF = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2)
                + Math.pow(event.values[2], 2))
                / SensorManager.GRAVITY_EARTH;

        if (mLF > mMaxLF) mMaxLF = mLF;
        if (mLF < mMinLF) mMinLF = mLF;

        mCurrentTime = event.timestamp / 1000000;
        fireStatsChanged();

        if (mLF <= sFallLF) {
            if (!mFalling) {
                mFalling = true;
                mStartTime = mCurrentTime;
            }
        } else {
            if (mFalling) {
                finishFlight();
            }
        }

    }

    public synchronized void addEventListener(FlightListener listener) {
        mListeners.add(listener);
    }

    public synchronized void removeEventListener(FlightListener listener) {
        mListeners.remove(listener);
    }

    private synchronized void fireStatsChanged() {
        Stats event = new Stats();
        if (mFalling) event.currentDuration = mCurrentTime - mStartTime;
        else event.currentDuration = 0;
        event.lF = mLF;
        event.minLF = mMinLF;
        event.maxLF = mMaxLF;
        Iterator<FlightListener> i = mListeners.iterator();
        while (i.hasNext()) {
            i.next().onStatsChanged(event);
        }
    }

    private synchronized void fireFlightFinished() {
        Flight event = new Flight();
        event.endTime = mCurrentTime;
        event.duration = mCurrentTime - mStartTime;
        Iterator<FlightListener> i = mListeners.iterator();
        while (i.hasNext()) {
            i.next().onFlightFinished(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}