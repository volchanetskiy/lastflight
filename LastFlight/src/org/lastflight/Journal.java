package org.lastflight;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Journal implements FlightListener {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_DATE = "date";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_CREATE = "create table flights (_id integer primary key autoincrement, "
            + "duration integer not null, date integer not null);";

    private static final String DATABASE_NAME = "journal";
    private static final String DATABASE_TABLE = "flights";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS flights");
            onCreate(db);
        }
    }

    public Journal(Context ctx) {
        this.mCtx = ctx;
    }

    public Journal open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public void createRecord(Flight flight) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DURATION, flight.duration);
        initialValues.put(KEY_DATE, System.currentTimeMillis());

        mDb.insert(DATABASE_TABLE, null, initialValues);
        clean();
    }

    public Cursor fetchAllRecords() {
        return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_DURATION, KEY_DATE }, null,
                null, null, null, KEY_DURATION + " DESC");
    }

    private void clean() {
        mDb.execSQL("DELETE FROM flights WHERE _id NOT IN (SELECT _id FROM flights ORDER BY duration DESC LIMIT 30)");
    }

    @Override
    public void onStatsChanged(Stats e) {
    }

    @Override
    public void onFlightFinished(Flight e) {
        createRecord(e);
    }

}