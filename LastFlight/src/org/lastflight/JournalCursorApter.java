package org.lastflight;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

class ActivityCursorAdapter extends CursorAdapter {
    private Context mCtx;
    private LayoutInflater mInflater;

    private int mActivityIndex;
    private int mTimeIndex;

    public ActivityCursorAdapter(Context context, Cursor c) {
        super(context, c);

        mCtx = context;
        mInflater = LayoutInflater.from(context);

        mActivityIndex = c.getColumnIndex(Journal.KEY_DURATION);
        mTimeIndex = c.getColumnIndex(Journal.KEY_DATE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView durationTextView = (TextView) view.findViewById(android.R.id.text1);
        TextView dateTextView = (TextView) view.findViewById(android.R.id.text2);

        durationTextView.setText(cursor.getString(mActivityIndex) + " "
                + mCtx.getString(R.string.milliseconds));
        DateFormat formatter = DateFormat.getInstance();
        dateTextView
                .setText(formatter.format(new Date(Long.valueOf(cursor.getString(mTimeIndex)))));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(android.R.layout.simple_list_item_2, null);
    }

}