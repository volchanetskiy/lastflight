package org.lastflight;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;

public class JournalActivity extends ListActivity {
    private Journal mJournal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJournal = new Journal(this);
        mJournal.open();
        fillData();
    }

    private void fillData() {
        Cursor c = mJournal.fetchAllRecords();
        startManagingCursor(c);

        ActivityCursorAdapter records = new ActivityCursorAdapter(this, c);
        setListAdapter(records);
    }
}