package org.lastflight;

import java.text.NumberFormat;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity implements FlightListener {
    private TextView mDurationTextView;
    private TextView mMinLFTextView;
    private TextView mMaxLFTextView;

    private Journal mJournal;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private FlightTracker mFlightTracker;

    private Flight mPreviousFlight = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mDurationTextView = (TextView) findViewById(R.id.duration_text_view);
        mDurationTextView.setText(0 + " " + getString(R.string.milliseconds));
        mMinLFTextView = (TextView) findViewById(R.id.min_LF_text_view);
        mMaxLFTextView = (TextView) findViewById(R.id.max_LF_text_view);

        mJournal = new Journal(this);
        mJournal.open();

        mFlightTracker = new FlightTracker();
        mFlightTracker.addEventListener(this);
        mFlightTracker.addEventListener(mJournal);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mFlightTracker, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mFlightTracker, mAccelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        mFlightTracker.resume();
    }

    protected void onPause() {
        super.onPause();
        mFlightTracker.pause();
        mSensorManager.unregisterListener(mFlightTracker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_journal:
            Intent intent = new Intent(this, JournalActivity.class);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStatsChanged(Stats e) {
        NumberFormat formatter = NumberFormat.getInstance();

        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);

        if (e.currentDuration > 0) {
            mDurationTextView.setText(e.currentDuration + " " + getString(R.string.milliseconds));
        }

        mMinLFTextView.setText(getString(R.string.minimum_loading_factor) + " "
                + formatter.format(e.minLF) + " g");
        mMaxLFTextView.setText(getString(R.string.maximum_loading_factor) + " "
                + formatter.format(e.maxLF) + " g");
    }

    @Override
    public void onFlightFinished(Flight e) {
        /*
         * anti-bounce technique
         */
        if (mPreviousFlight != null && mPreviousFlight.duration > e.duration
                && (e.endTime - e.duration) - mPreviousFlight.endTime < 1000) {
            mDurationTextView.setText(mPreviousFlight.duration + " "
                    + getString(R.string.milliseconds));
        }
        mPreviousFlight = e;
    }
}
