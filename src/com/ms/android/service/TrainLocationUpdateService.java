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
	private static final String START_TSECS = "START_TSECS";
	private static final String END_TSECS = "END_TSECS";
	private static final String STATUS_BAR_NOTIFICATION = "STATUS_BAR_NOTIFICATION";	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String ERROR_MSG = "Unable to connect the railway server. Please retry after sometime!!!";
	private static final String INVALID_CAPTCHA = "Please verify/re-verify your captcha via Settings menu home screen";
	
	public TrainLocationUpdateService() 
	{
		super(tag);		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		String trainname = intent.getStringExtra(TRAIN_NAME);
		int trainnumber = intent.getIntExtra(TRAIN_NUMBER, 0);
		String boardingStationName = intent.getStringExtra(BOARDING_STATION);
		long starttsecs = intent.getLongExtra(START_TSECS, 0);
		long endtsecs = intent.getLongExtra(END_TSECS, 0);
		int statusBarNotification = intent.getIntExtra(STATUS_BAR_NOTIFICATION, 0);
		boolean invalidCaptcha = false;
		
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
					String notificationMsg = INVALID_CAPTCHA;
					StatusBarNotification.generateNotification(this.getBaseContext(), title, notificationMsg);
				}
				else
				{
					String currentLocation = lastStationInfo.getLastLocation() + ". " + "ETA " + 
							lastStationInfo.getActualArrival() + ". " +
							lastStationInfo.getLastUpdatedTime();
					StatusBarNotification.generateNotification(this.getBaseContext(), title, currentLocation);
				}
				
			}

			
//			String latestRunningDate = infoCollector.getLatestRunningDate(trainnumber);
//			String currentLocation = ""; //TrainStatusUtility.convertCurrentLocationToString(lastStationInfo);
//			if(currentLocation != null)
//			{
//				String estimatedTimeOfArrival = infoCollector.getEstimatedTimeOfArrival(Integer.valueOf(trainnumber).toString(), 
//						latestRunningDate, boardingStationName, 0/*lastStationInfo.getDelayInMinutes()*/);
//				if(estimatedTimeOfArrival != null)
//				{
//					currentLocation += "Expected arrival at " + boardingStationName + " is " + estimatedTimeOfArrival + ".";
//				}
//			}
//			else
//			{
//				currentLocation = ERROR_MSG;
//			}
		}
		else
		{
			//Cancel all further alarms.
			cancelAlarm();
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
	
}
