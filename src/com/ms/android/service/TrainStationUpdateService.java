/**
 * 
 */
package com.ms.android.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ms.android.constants.Constants;
import com.ms.android.data.StationInfo;
import com.ms.android.data.TrainDO;
import com.ms.android.data.TrainInfo;
import com.ms.android.data.TrainStatusCollector;
import com.ms.android.data.TrainStatusUtility;
import com.ms.android.exceptions.CaptchaNotValidException;
import com.ms.android.notification.StatusBarNotification;
import com.ms.android.trainnotification.R;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

/**
 * @author Manoj Srivatsav
 *
 * Aug 20, 2013
 */
public class TrainStationUpdateService extends IntentService
{
	private static Map<String, Boolean> stationNotificationGenerated;
	
	private static final String tag = TrainStationUpdateService.class.getName();
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
	private static final String INVALID_CAPTCHA = "Please verify/re-verify your captcha via Settings menu home screen";
	private static final String LEFT_BOARDING_STATION = "The train has already departed from borading station";
	
	private String trainname;
	private int trainnumber;
	private String boardingStationName;
	private String notificationStationName;
	private long starttsecs;
	private long endtsecs;
	private int statusBarNotification;
	long intervalInMillis;
	
	
	static {
		stationNotificationGenerated = new HashMap<String, Boolean>();
	}
	
	public TrainStationUpdateService() 
	{
		super(tag);		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		trainname = intent.getStringExtra(TRAIN_NAME);
		trainnumber = intent.getIntExtra(TRAIN_NUMBER, 0);
		boardingStationName = intent.getStringExtra(BOARDING_STATION);
		notificationStationName = intent.getStringExtra(NOTIFICATION_SERVICE);
		starttsecs = intent.getLongExtra(START_TSECS, 0);
		endtsecs = intent.getLongExtra(END_TSECS, 0);
		statusBarNotification = intent.getIntExtra(STATUS_BAR_NOTIFICATION, 0);
		intervalInMillis = intent.getLongExtra(FREQUENCY, 15 * 60 * 1000);
		boolean invalidCaptcha = false;
		boolean hasTheTrainDepartedBoradingStation = false;
		
		long beginTsecsMillis = System.currentTimeMillis();
		
		List<String> notificationStations = new ArrayList<String>();
		Collections.addAll(notificationStations, notificationStationName.split(","));
		
		SharedPreferences sharedPreference = getSharedPreferences(getString(R.string.station_update_preference_file, trainnumber), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreference.edit();
		for(String station : notificationStations) {
			if(!sharedPreference.contains(trainnumber + "-" + station)) {
				editor.putBoolean(trainnumber + "-" + station, Boolean.valueOf(false));
			}
			
			//stationNotificationGenerated.put(trainnumber + "-" + station, Boolean.valueOf(false));
		}
		editor.commit();
		
		
		// First check if the train has already crossed the boarding station.
		// If so then notify saying that train has already crossed the boarding station and cancel all future alarms.
		// If no the check of the stations for which notification should be generated.
		SimpleDateFormat format = new SimpleDateFormat("HH:mm dd-MMM-yyyy");
		
		Calendar calendar = Calendar.getInstance();
		int minutesElasped = calendar.get(Calendar.MINUTE);
		int hoursElasped = calendar.get(Calendar.HOUR_OF_DAY);
		int currentTimeMillis = (hoursElasped * 3600 + minutesElasped * 60) * 1000;
		if(currentTimeMillis <= endtsecs) {
			TrainStatusCollector infoCollector = new TrainStatusCollector(this.getBaseContext());
			StationInfo lastStationInfo = null;
			StationInfo notificationStationInfo = null;
			try	{
				lastStationInfo = infoCollector.getLastStationLocation(Integer.valueOf(trainnumber).toString(),
											Constants.TODAY_START_DATE, boardingStationName);
				System.out.println(lastStationInfo.getStationName());
				Calendar departure = Calendar.getInstance();
				if(!lastStationInfo.getActualDeparture().contains("Waiting for update")) {
					departure.setTime(format.parse(lastStationInfo.getActualDeparture() + "-" + calendar.get(Calendar.YEAR)));
					if(calendar.getTimeInMillis() >= departure.getTimeInMillis()) {
						//TODO: generate notification and cancel all future alarms
						//System.out.println(LEFT_BOARDING_STATION + boardingStationName);
						hasTheTrainDepartedBoradingStation = true;
						//cancelAlarm();
					}
				}
				else {
					if(statusBarNotification == 1) {
						String title = trainnumber + " - " + trainname;
						String message = lastStationInfo.getLastLocation() + ". " + "ETA at " + boardingStationName + " is "+ 
											lastStationInfo.getActualArrival() + ". " + lastStationInfo.getLastUpdatedTime();
						StatusBarNotification.generateNotification(this, title, message);
					}
				}
				
				
				if(!hasTheTrainDepartedBoradingStation) {
					for(String stationName : notificationStations) {
						if(sharedPreference.getBoolean(trainnumber + "-" + stationName, false) == false) {
							notificationStationInfo = infoCollector.getLastStationLocation(Integer.valueOf(trainnumber).toString(),
									Constants.TODAY_START_DATE, stationName);
							departure = Calendar.getInstance();
							if(notificationStationInfo.getActualArrival().contains("Waiting for update")) {
								if(statusBarNotification == 1) {
									String title = trainnumber + " - " + trainname;
									StationInfo info = infoCollector.getLastStationLocation(Integer.valueOf(trainnumber).toString(), Constants.TODAY_START_DATE, boardingStationName);
									String message = info.getLastLocation() + ". " + "ETA at " + boardingStationName + " is "+ info.getActualArrival() + ". " +
											info.getLastUpdatedTime();
									StatusBarNotification.generateNotification(this.getBaseContext(), title, message);
									editor.putBoolean(trainnumber + "-" + stationName, Boolean.valueOf(false));
									editor.commit();
								}
							}
							else {
								departure.setTime(format.parse(notificationStationInfo.getActualDeparture() + "-" + calendar.get(Calendar.YEAR)));
								if(calendar.getTimeInMillis() >= departure.getTimeInMillis()) {
									//Generate Notification.
									if(statusBarNotification == 1) {
										String title = trainnumber + " - " + trainname;
										StationInfo info = infoCollector.getLastStationLocation(Integer.valueOf(trainnumber).toString(), Constants.TODAY_START_DATE, boardingStationName);
										String message = info.getLastLocation() + ". " + "ETA at " + boardingStationName + " is "+ info.getActualArrival() + ". " +
												info.getLastUpdatedTime();
										StatusBarNotification.generateNotification(this.getBaseContext(), title, message);
										editor.putBoolean(trainnumber + "-" + stationName, Boolean.valueOf(true));
										editor.commit();
										//stationNotificationGenerated.put(trainnumber + "-" + stationName, Boolean.valueOf(true));
									}
								}
							}
						}
					}
				}
			}
			catch(CaptchaNotValidException ex) {
				invalidCaptcha = true;
				String title = trainnumber + " - " + trainname;
				StatusBarNotification.generateNotification(this, title, INVALID_CAPTCHA);
			}
			catch(ParseException parseEx) {
				Log.e(tag, "ParseException: " + parseEx.getMessage());
			}
			
			if(!hasTheTrainDepartedBoradingStation) {
				long endTsecsMillis = System.currentTimeMillis();
				long triggerAtMillis = System.currentTimeMillis() + (intervalInMillis - (endTsecsMillis - beginTsecsMillis));
				setAlarm(triggerAtMillis);
			}
			else {
				for(String station : notificationStations) {
					editor.remove(trainnumber + "-" + station);
					//stationNotificationGenerated.remove(trainnumber + "-" + station);
				}
				editor.commit();
			}
		}
		else {
			for(String station : notificationStations) {
				editor.remove(trainnumber + "-" + station);
				//stationNotificationGenerated.remove(trainnumber + "-" + station);
			}
			editor.commit();
			//cancelAlarm();
		}
		
	}
	
	private void cancelAlarm() {
		AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent stationUpdateServiceIntent = new Intent(this, TrainStationUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, stationUpdateServiceIntent, 0);
		alarmManager.cancel(pendingIntent);
		Log.i(tag, "Stopping all further alarms for TrainStationUpdateService");
	}

	private void setAlarm(long triggerAtMillis) {
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent stationUpdateServiceIntent = new Intent(this, TrainStationUpdateService.class);
		stationUpdateServiceIntent.putExtra(TRAIN_NAME, trainname);
		stationUpdateServiceIntent.putExtra(TRAIN_NUMBER, trainnumber);
		stationUpdateServiceIntent.putExtra(BOARDING_STATION, boardingStationName);
		stationUpdateServiceIntent.putExtra(NOTIFICATION_SERVICE, notificationStationName);
		stationUpdateServiceIntent.putExtra(START_TSECS, starttsecs);
		stationUpdateServiceIntent.putExtra(END_TSECS, endtsecs);
		stationUpdateServiceIntent.putExtra(STATUS_BAR_NOTIFICATION, statusBarNotification);
		stationUpdateServiceIntent.putExtra(FREQUENCY, intervalInMillis);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, stationUpdateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		//if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
		alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, /*intervalMillis, */pendingIntent);
		Log.i(tag, "Started an RTC_WAKEUP alarm for TrainStationUpdateService " + trainnumber);
	}
	
}
