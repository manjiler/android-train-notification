/**
 * 
 */
package com.ms.android.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ms.android.data.StationInfo;
import com.ms.android.data.TrainDO;
import com.ms.android.data.TrainStatusCollector;
import com.ms.android.data.TrainStatusUtility;
import com.ms.android.exceptions.CaptchaNotValidException;
import com.ms.android.notification.StatusBarNotification;
import com.ms.android.test.TestParcable;
import com.ms.android.constants.Constants;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Manoj Srivatsav
 *
 * Aug 20, 2013
 */
public class TrainLocationUpdateService extends IntentService 
{
	private static final String tag = TrainLocationUpdateService.class.getName();
	private static final String TRAIN_NAME = "TRAIN_NAME";
	private static final String TRAIN_NUMBER = "TRAIN_NUMBER";
	private static final String BOARDING_STATION = "BOARDING_STATION";
	private static final String FREQUENCY = "FREQUENCY";
	private static final String START_TSECS = "START_TSECS";
	private static final String END_TSECS = "END_TSECS";
	private static final String STATUS_BAR_NOTIFICATION = "STATUS_BAR_NOTIFICATION";	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String ERROR_MSG = "Unable to connect the railway server. Please retry after sometime!!!";
	private static final String INVALID_CAPTCHA = "Please verify/re-verify your captcha via Settings menu home screen";
	
	private String trainname;
	private int trainnumber;
	private String boardingStationName;
	private long starttsecs;
	private long endtsecs;
	private int statusBarNotification;
	private long frequency;
	
	public TrainLocationUpdateService() 
	{
		super(tag);		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		trainname = intent.getStringExtra(TRAIN_NAME);
		trainnumber = intent.getIntExtra(TRAIN_NUMBER, 0);
		boardingStationName = intent.getStringExtra(BOARDING_STATION);
		starttsecs = intent.getLongExtra(START_TSECS, 0);
		endtsecs = intent.getLongExtra(END_TSECS, 0);
		statusBarNotification = intent.getIntExtra(STATUS_BAR_NOTIFICATION, 0);
		frequency = intent.getLongExtra(FREQUENCY, 15 * 60 * 1000);
		boolean invalidCaptcha = false;
		
		long beginTsecsMillis = System.currentTimeMillis();
		
		Calendar calendar = Calendar.getInstance();
		int minutesElasped = calendar.get(Calendar.MINUTE);
		int hoursElasped = calendar.get(Calendar.HOUR_OF_DAY);
		int currentTimeMillis = (hoursElasped * 3600 + minutesElasped * 60) * 1000;
		if(currentTimeMillis <= endtsecs)
		{
			TrainStatusCollector infoCollector = new TrainStatusCollector(this.getBaseContext());
			StationInfo lastStationInfo = null;
			try
			{
				lastStationInfo = infoCollector.getLastStationLocation(Integer.valueOf(trainnumber).toString(),
							Constants.TODAY_START_DATE, boardingStationName);
			}
			catch(CaptchaNotValidException ex)
			{
				invalidCaptcha = true;
			}
			
			if(statusBarNotification == 1)
			{
				String title = trainnumber + " - " + trainname;
				if(invalidCaptcha)
				{
					StatusBarNotification.generateNotification(this.getBaseContext(), title, INVALID_CAPTCHA);
				}
				else
				{
					String currentLocation = lastStationInfo.getLastLocation() + ". " + "ETA at " + boardingStationName + " is " + 
							lastStationInfo.getActualArrival() + ". " +
							lastStationInfo.getLastUpdatedTime();
					StatusBarNotification.generateNotification(this.getBaseContext(), title, currentLocation);
				}
				
			}
			long endTsecsMillis = System.currentTimeMillis();
			long triggerAtMillis = System.currentTimeMillis() + (frequency - (endTsecsMillis - beginTsecsMillis));
			setAlarm(triggerAtMillis);
		}
	}	
	
	private void cancelAlarm()
	{
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent locationUpdateServiceIntent = new Intent(this, TrainLocationUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, locationUpdateServiceIntent, 0);
		alarmManager.cancel(pendingIntent);
		Log.i(tag, "Stopping all further alarms for TrainLocationUpdateService");
	}
	
	private void setAlarm(long triggerAtMillis) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent locationUpdateServiceIntent = new Intent(this, TrainLocationUpdateService.class);
		locationUpdateServiceIntent.putExtra(TRAIN_NAME, trainname);
		locationUpdateServiceIntent.putExtra(TRAIN_NUMBER, trainnumber);
		locationUpdateServiceIntent.putExtra(BOARDING_STATION, boardingStationName);
		locationUpdateServiceIntent.putExtra(START_TSECS, starttsecs);
		locationUpdateServiceIntent.putExtra(END_TSECS, endtsecs);
		locationUpdateServiceIntent.putExtra(STATUS_BAR_NOTIFICATION, statusBarNotification);
		long intervalMillis = frequency * 60 * 1000;
		locationUpdateServiceIntent.putExtra(FREQUENCY, intervalMillis);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, locationUpdateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);							
		Log.i(tag, "Started an RTC_WAKEUP alarm for TrainLocationUpdateService for " + trainnumber);
	}
	
}
