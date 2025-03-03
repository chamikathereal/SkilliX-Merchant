package com.skillix.merchant.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "skillix_notes_db";
    private static final int DATABASE_VERSION = 1;

    // Table name and column names
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOTE_TEXT = "note_text";

    // SQL query to create the table
    private static final String CREATE_TABLE_QUERY =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOTE_TEXT + " TEXT)";

    public NoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_QUERY);
    }

    // Called when the database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // Insert a new note into the database
    public long insertNote(String noteText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_TEXT, noteText);
        return db.insert(TABLE_NOTES, null, values);
    }

    // Retrieve the note text from the database
    public String getNote() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, new String[]{COLUMN_NOTE_TEXT}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Get the column index
            int columnIndex = cursor.getColumnIndex(COLUMN_NOTE_TEXT);
            if (columnIndex != -1) {
                String noteText = cursor.getString(columnIndex);
                cursor.close();
                return noteText;
            }
            cursor.close();
        }

        return null; // No note saved
    }

    // Update the existing note text in the database
    public int updateNote(String noteText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_TEXT, noteText);
        return db.update(TABLE_NOTES, values, null, null);
    }
}


