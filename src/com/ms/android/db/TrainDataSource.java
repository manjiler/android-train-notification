package com.ms.android.db;

import java.util.ArrayList;
import java.util.List;

import com.ms.android.data.TrainDO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class TrainDataSource {
	private static String tag = "TrainDataSource";
	private SQLiteDatabase mDatabase;
	private MySQLiteHelper mDatabaseHelper;
	
	private static TrainDataSource trainDataSource;
	
	public static TrainDataSource getInstance(Context context) {
		if(trainDataSource == null) {
			trainDataSource = new TrainDataSource(context);
		}
		return trainDataSource;
	}
	
	private TrainDataSource(Context context) {
		mDatabaseHelper = new MySQLiteHelper(context);
	}
	
	public void open() {
		mDatabase = mDatabaseHelper.getWritableDatabase();
	}
	
	public void close() {
		mDatabaseHelper.close();
		mDatabase.close();
	}
	
	public void insert(TrainDO trainData) {
		ContentValues contentValues = null;
		open();
		try {
			
			for(String station : trainData.getStationCodes()) {
				contentValues = new ContentValues();
				contentValues.put(MySQLiteHelper.TRAIN_NUMBER, trainData.getTrainNumber());
				contentValues.put(MySQLiteHelper.TRAIN_NAME, trainData.getTrainName());
				contentValues.put(MySQLiteHelper.UPDATE_TYPE, trainData.getUpdateType());
				contentValues.put(MySQLiteHelper.STATION_CODE, station);
				contentValues.put(MySQLiteHelper.REPETITIONS, trainData.getRepitition());
				contentValues.put(MySQLiteHelper.START_DATE_TSECS, trainData.getDateInMilli());
				contentValues.put(MySQLiteHelper.START_TSECS, trainData.getStartTsecs());
				contentValues.put(MySQLiteHelper.END_TSECS, trainData.getEndTsecs());
				contentValues.put(MySQLiteHelper.FREQUENCY, trainData.getFrequency());
				contentValues.put(MySQLiteHelper.STATUS_BAR_NOTIF, trainData.getStatusBarNofiy());
				contentValues.put(MySQLiteHelper.SMS_NOTIF, trainData.getSmsNotify());
				contentValues.put(MySQLiteHelper.EMAIL_NOTIF, trainData.getEmailNotify());
				mDatabase.insert(MySQLiteHelper.TABLE_TRAIN_NOTIFICATION, null, contentValues);
			}
			
		} catch (Exception ex) {
			Log.e(tag, ex.getMessage());
		}		
		
		finally {
			close();
		}
		
	}
	
	public void delete(TrainDO train) {
		/*
		String query = "DELETE * FROM " + MySQLiteHelper.TABLE_TRAIN_NOTIFICATION + 
						" WHERE " + MySQLiteHelper.TRAIN_NUMBER + " = " + train.getTrainNumber();
		open();
		try {
			mDatabase.rawQuery(query, null);
		}
		catch(Exception ex) {
			Log.e(tag, ex.getMessage());
		}
		finally {
			close();
		}
		*/
		
		String whereClause = MySQLiteHelper.TRAIN_NUMBER + "= " + train.getTrainNumber();
		open();
		try {
			mDatabase.delete(MySQLiteHelper.TABLE_TRAIN_NOTIFICATION, whereClause, null);
		}
		catch(Exception ex) {
			Log.e(tag, ex.getMessage());
		}
		finally {
			close();
		}
	}
	
	/**
	 * Fetches all the train's related data from database.
	 * @return a list of TrainDO objects.
	 */
	public List<TrainDO> getAllTrains() {
		String query = "SELECT * FROM " + MySQLiteHelper.TABLE_TRAIN_NOTIFICATION;
		List<TrainDO> listOfTrains = new ArrayList<TrainDO>();
		open();
		try {
			Cursor cursor = mDatabase.rawQuery(query, null);
			//if(cursor.getCount() > 0) {

			while(cursor.moveToNext()) {
				//check if the train number, train name already exists as TrainDO object in
				// list. If so then just add the station code to existing object in the list else
				// create a new trainDO object and add it to the list.
				if(checkIfTrainExists(listOfTrains, cursor)) {
					for(TrainDO train: listOfTrains) {
						if(train.getTrainNumber() == cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.TRAIN_NUMBER))) {
							train.addStationCode(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.STATION_CODE)));
						}
					}
				} else {
					TrainDO train = new TrainDO();
					train.setTrainNumber(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.TRAIN_NUMBER)));
					train.setTrainName(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.TRAIN_NAME)));
					train.setUpdateType(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.UPDATE_TYPE)));
					List<String> stationList = new ArrayList<String>();
					stationList.add(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.STATION_CODE)));
					train.setStationCodes(stationList);
					train.setRepitition(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.REPETITIONS)));
					train.setDateInMilli(cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.START_DATE_TSECS)));
					train.setStartTsecs(cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.START_TSECS)));
					train.setEndTsecs(cursor.getLong(cursor.getColumnIndex(MySQLiteHelper.END_TSECS)));
					train.setFrequency(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.FREQUENCY)));
					train.setStatusBarNofiy(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.STATUS_BAR_NOTIF)));
					train.setSmsNotify(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.SMS_NOTIF)));
					train.setEmailNotify(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.EMAIL_NOTIF)));
					listOfTrains.add(train);
				}

			}
		}
		finally {
			close();
		}
		
		return listOfTrains;
	}
	
	
	private boolean checkIfTrainExists(List<TrainDO> listOfTrains, Cursor cursor) {
		boolean bExists = false;
		
		int trainNumber = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.TRAIN_NUMBER));
		
		for(TrainDO train : listOfTrains) {
			if(train.getTrainNumber() == trainNumber) {
				bExists = true;
				break;
			}
		}
		return bExists;
	}
	
	
}
