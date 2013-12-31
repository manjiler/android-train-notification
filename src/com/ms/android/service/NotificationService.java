/**
 * 
 */
package com.ms.android.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.ms.android.constants.Constants;
import com.ms.android.data.TrainDO;
import com.ms.android.db.TrainDataSource;
import com.ms.android.trainnotification.R;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Manoj Srivatsav
 *
 * Jul 8, 2013
 */


public class NotificationService extends IntentService
{
	private static final String tag = NotificationService.class.getName();
	private static final String NOTIFICATION_WORKER_THREAD = NotificationService.class.getName();;
	private static final String TRAIN_NAME = "TRAIN_NAME";
	private static final String TRAIN_NUMBER = "TRAIN_NUMBER";
	private static final String BOARDING_STATION = "BOARDING_STATION";
	private static final String NOTIFICATION_SERVICE = "NOTIFICATION_SERVICE";
	private static final String FREQUENCY = "FREQUENCY";
	private static final String START_TSECS = "START_TSECS";
	private static final String END_TSECS = "END_TSECS";
	private static final String STATUS_BAR_NOTIFICATION = "STATUS_BAR_NOTIFICATION";	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String ERROR_MSG = "Unable to connect the railway server. Please retry after sometime!!!";
	
	//Repetition strings
	private static final String WEEKLY_REPETITION = "Weekly (every";
	
	
	private Context context;
	private Resources resource;

	public NotificationService()
	{
		super(NOTIFICATION_WORKER_THREAD);		
	}

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		// TODO Get all the data from table and check if any of the start time for a train has crossed 
		// or not. If it is crossed then start an AlarmManager to take for it.
		context = getBaseContext();
		TrainDataSource tds = TrainDataSource.getInstance(context);
		List<TrainDO> listOfTrains = tds.getAllTrains();
		resource = context.getResources();
		
		/*
		 * For each train
		 * 1. check it's repetition type. If its a one-time event then check the date, if it's a repeat event
		 *    then check whether the current day falls under it's repetition list.
		 * 2. Now check for the start time, if current time is greater than start time then kick of an
		 *    AlarmManager passing the start time, end time, frequency and other required parameter for it to 
		 *    execute.
		 */

		for(TrainDO train : listOfTrains) 
		{
			System.out.println(train.toString());
			Calendar currentCalendar = Calendar.getInstance();
			currentCalendar.setTimeInMillis(System.currentTimeMillis());
			
			if(train.getRepitition().contains("Every weekday")) 
			{
				int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
				if((currentDayOfWeek >= Calendar.MONDAY) && (currentDayOfWeek <= Calendar.FRIDAY))
				{
					Time currentTime = new Time();
					currentTime.set(System.currentTimeMillis());
					long currentTsecs = ((currentTime.hour * 3600) + (currentTime.minute * 60)) * 1000;
					if(currentTsecs >= train.getStartTsecs()) {
						//Start an repeating ALARM MANAGER.
						if(train.getUpdateType().equalsIgnoreCase(Constants.STATION_WISE_UPDATE)) {
							setStationWiseAlarm(train);
						} 
						else if(train.getUpdateType().equalsIgnoreCase(Constants.LOCATION_WISE_UPDATE)) {
							setLocationWiseAlarm(train);
							
						}
					}
				}
			}
			else if(train.getRepitition().contains(resource.getString(R.string.does_not_repeat)))
			{
				/*
				 * Check this current day/date is the date for which the alarm should be generated.
				 * If so then create an alarm manager based on update type..
				 */
				
				if(Math.abs(currentCalendar.getTimeInMillis() - train.getDateInMilli()) < (86400 * 1000)) {
					long startTime = train.getDateInMilli() + train.getStartTsecs();
					if(currentCalendar.getTimeInMillis() > startTime) {
						//Start an repeating ALARM MANAGER.
						if(train.getUpdateType().equalsIgnoreCase(Constants.STATION_WISE_UPDATE)) {
							setStationWiseAlarm(train);
						} 
						else if(train.getUpdateType().equalsIgnoreCase(Constants.LOCATION_WISE_UPDATE)) {
							setLocationWiseAlarm(train);							
						}
					}
				}				
				
			}
			else if(train.getRepitition().contains(WEEKLY_REPETITION))
			{
				/*
				 * check the current day of the week matches with the repetition day.
				 * If so then create an alram manager based on update type.
				 */
				int dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
				int repetitionDayOfWeek = extractWeeklyRepetitionDay(train.getRepitition());
				if(dayOfWeek == repetitionDayOfWeek) {
					
				}
				
				
			}
			else if(train.getRepitition().contains("Monthly (every"))
			{
				/*
				 * Get the nth day of the month. Check it with the repitition string.
				 * If they match then create an alram manager based on update type.
				 */
				
			}
			else if(train.getRepitition().contains("Monthly (on day"))
			{
				/*
				 * Get the day of monthe and check it with the repetition string. 
				 * If they match then create an alarm manager based on the update type. 
				 */
				int dayOfMonth = currentCalendar.get(Calendar.DAY_OF_MONTH);
				int repetitionDayOfMonth = extractMonthlyRepetitionDay(train.getRepitition());
			}
		}
	}
	
	private int extractWeeklyRepetitionDay(String repetition) {
		int repetitionDay = 0;
		String repetitionDayString = null;
		StringTokenizer tokenizer = new StringTokenizer(repetition, " ");
		if(tokenizer.hasMoreTokens()) {
			tokenizer.nextToken();
			tokenizer.nextToken();
			repetitionDayString = (tokenizer.nextToken().split(")"))[0];			
		}
		
		repetitionDay = convertDayStringToInt(repetitionDayString);
		return repetitionDay;
	}

	private int extractMonthlyRepetitionDay(String repetition) {
		int dayOfMonth = 0;
		String repetitionDay = null;
		StringTokenizer tokenizer = new StringTokenizer(repetition, " ");
		if(tokenizer.hasMoreTokens()) {
			tokenizer.nextToken();
			tokenizer.nextToken();
			tokenizer.nextToken();
			dayOfMonth = Integer.parseInt((tokenizer.nextToken().split(")"))[0]);
		}
		
		return dayOfMonth;
	}
	
	private int convertDayStringToInt(String day) {
		if(day.equalsIgnoreCase("SUNDAY")) {
			return 0;
		} else if(day.equalsIgnoreCase("MONDAY")) {
			return 1;
		} else if(day.equalsIgnoreCase("TUESDAY")) {
			return 2;
		} else if(day.equalsIgnoreCase("WEDNESDAY")) {
			return 3;
		} else if(day.equalsIgnoreCase("THURSDAY")) {
			return 4;
		} else if(day.equalsIgnoreCase("FRIDAY")) {
			return 5;
		} else if(day.equalsIgnoreCase("SATURDAY")) {
			return 7;
		} else {
			//It should never ever come here. If it does then you are as good as dead!!!!
			return -1;
		}
		
	}
	
	private void setStationWiseAlarm(TrainDO train) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent stationUpdateServiceIntent = new Intent(context, TrainStationUpdateService.class);
		String notificationStations = "";
		String delim = "";
		for(int i = 1; i < train.getStationCodes().size(); i++)
		{
			notificationStations += delim + train.getStationCodes().get(i);
			delim = ",";
		}
		stationUpdateServiceIntent.putExtra(TRAIN_NAME, train.getTrainName());
		stationUpdateServiceIntent.putExtra(TRAIN_NUMBER, train.getTrainNumber());
		stationUpdateServiceIntent.putExtra(BOARDING_STATION, train.getStationCodes().get(0));
		stationUpdateServiceIntent.putExtra(NOTIFICATION_SERVICE, notificationStations);
		stationUpdateServiceIntent.putExtra(START_TSECS, train.getStartTsecs());
		stationUpdateServiceIntent.putExtra(END_TSECS, train.getEndTsecs());
		stationUpdateServiceIntent.putExtra(STATUS_BAR_NOTIFICATION, train.getStatusBarNofiy());
		long intervalMillis = Integer.parseInt(train.getFrequency()) * 60 * 1000;
		stationUpdateServiceIntent.putExtra(FREQUENCY, intervalMillis);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, stationUpdateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), /*intervalMillis, */pendingIntent);
		Log.i(tag, "Started an RTC_WAKEUP alarm for TrainStationUpdateService for " + train.getTrainNumber());
	}
	
	private void setLocationWiseAlarm(TrainDO train) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent locationUpdateServiceIntent = new Intent(context, TrainLocationUpdateService.class);
		locationUpdateServiceIntent.putExtra(TRAIN_NAME, train.getTrainName());
		locationUpdateServiceIntent.putExtra(TRAIN_NUMBER, train.getTrainNumber());
		locationUpdateServiceIntent.putExtra(BOARDING_STATION, train.getStationCodes().get(0));
		locationUpdateServiceIntent.putExtra(START_TSECS, train.getStartTsecs());
		locationUpdateServiceIntent.putExtra(END_TSECS, train.getEndTsecs());
		locationUpdateServiceIntent.putExtra(STATUS_BAR_NOTIFICATION, train.getStatusBarNofiy());
		long intervalMillis = Integer.parseInt(train.getFrequency()) * 60 * 1000;
		locationUpdateServiceIntent.putExtra(FREQUENCY, intervalMillis);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, locationUpdateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), /*intervalMillis, */pendingIntent);							
		Log.i(tag, "Started an RTC_WAKEUP alarm for TrainLocationUpdateService for " + train.getTrainNumber());
	}
}