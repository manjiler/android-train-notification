package com.ms.android.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SearchDataSource {

	private SQLiteDatabase mDatabase;
	private MySQLiteHelper mDatabaseHelper;
	
	public SearchDataSource(Context context) {
		mDatabaseHelper = new MySQLiteHelper(context);
	}
	
	public void open() {
		mDatabase = mDatabaseHelper.getWritableDatabase();
	}
	
	public void close() {
		mDatabaseHelper.close();
		mDatabase.close();
	}
	
	public void insertSearchData(int trainNumber, String trainName) {
		open();
		ContentValues contentValues = new ContentValues();
		contentValues.put(MySQLiteHelper.TRAIN_NUMBER, trainNumber);
		contentValues.put(MySQLiteHelper.TRAIN_NAME, trainName);
		mDatabase.insert(MySQLiteHelper.TABLE_RECENT_SEARCH, null, contentValues);
		close();
	}
	
	public List<String> getAllSearchData() {
		open();
		List<String> listOfTrains = new ArrayList<String>();
		String query = "SELECT * FROM " + MySQLiteHelper.TABLE_RECENT_SEARCH;
		Cursor cursor = mDatabase.rawQuery(query, null);
		
		while(cursor.moveToNext()) {
			listOfTrains.add(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.TRAIN_NUMBER)) + " - " +
								cursor.getString(cursor.getColumnIndex(MySQLiteHelper.TRAIN_NAME)));
		}
		
		return listOfTrains;
	}
	
}
