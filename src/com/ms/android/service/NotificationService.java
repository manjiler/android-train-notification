/**
 * 
 */
package com.ms.android.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.ms.android.constants.Constants;
import com.ms.android.data.TrainDO;
import com.ms.android.db.TrainDataSource;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
	private static final String START_TSECS = "START_TSECS";
	private static final String END_TSECS = "END_TSECS";
	private static final String STATUS_BAR_NOTIFICATION = "STATUS_BAR_NOTIFICATION";	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String ERROR_MSG = "Unable to connect the railway server. Please retry after sometime!!!";
	private Context context;

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
		TrainDataSource tds = new TrainDataSource(context);
		List<TrainDO> listOfTrains = tds.getAllTrains();

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
			int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);

			if(train.getRepitition().contains("Every weekday")) 
			{
				if((currentDayOfWeek >= Calendar.MONDAY) && (currentDayOfWeek <= Calendar.FRIDAY))
				{
					Time currentTime = new Time();
					currentTime.set(System.currentTimeMillis());
					long currentTsecs = ((currentTime.hour * 3600) + (currentTime.minute * 60)) * 1000;
					if(currentTsecs >= train.getStartTsecs())
					{
						//Start an repeating ALARM MANAGER.
						if(train.getUpdateType().equalsIgnoreCase(Constants.STATION_WISE_UPDATE))
						{
							AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
							Intent stationUpdateServiceIntent = new Intent(context, TrainStationUpdateService.class);
							String notificationStations = "";
							String delim = "";
							for(int i = 1; i < train.getStationCodes().size(); i++)
							{
								notificationStations = delim + train.getStationCodes().get(i);
								delim = ",";
							}
							stationUpdateServiceIntent.putExtra(TRAIN_NAME, train.getTrainName());
							stationUpdateServiceIntent.putExtra(TRAIN_NUMBER, train.getTrainNumber());
							stationUpdateServiceIntent.putExtra(BOARDING_STATION, train.getStationCodes().get(0));
							stationUpdateServiceIntent.putExtra(NOTIFICATION_SERVICE, notificationStations);
							stationUpdateServiceIntent.putExtra(START_TSECS, train.getStartTsecs());
							stationUpdateServiceIntent.putExtra(END_TSECS, train.getEndTsecs());
							stationUpdateServiceIntent.putExtra(STATUS_BAR_NOTIFICATION, train.getStatusBarNofiy());
							PendingIntent pendingIntent = PendingIntent.getService(context, 0, stationUpdateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
							long intervalMillis = Integer.parseInt(train.getFrequency()) * 60 * 1000;
							alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pendingIntent);
							Log.i(tag, "Started a repeated RTC_WAKEUP alarm for TrainStationUpdateService for " + train.getTrainNumber());
						} 
						else if(train.getUpdateType().equalsIgnoreCase(Constants.LOCATION_WISE_UPDATE))
						{
							AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
							Intent locationUpdateServiceIntent = new Intent(context, TrainLocationUpdateService.class);
							locationUpdateServiceIntent.putExtra(TRAIN_NAME, train.getTrainName());
							locationUpdateServiceIntent.putExtra(TRAIN_NUMBER, train.getTrainNumber());
							locationUpdateServiceIntent.putExtra(BOARDING_STATION, train.getStationCodes().get(0));
							locationUpdateServiceIntent.putExtra(START_TSECS, train.getStartTsecs());
							locationUpdateServiceIntent.putExtra(END_TSECS, train.getEndTsecs());
							locationUpdateServiceIntent.putExtra(STATUS_BAR_NOTIFICATION, train.getStatusBarNofiy());
							PendingIntent pendingIntent = PendingIntent.getService(context, 0, locationUpdateServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
							long intervalMillis = Integer.parseInt(train.getFrequency()) * 60 * 1000;
							alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, pendingIntent);
							Log.i(tag, "Started a repeated RTC_WAKEUP alarm for TrainLocationUpdateService for " + train.getTrainNumber());							
						}
					}
				}
			}			
		}
	}

}

/*public class NotificationService extends Service
{

	private Context mContext;

	 (non-Javadoc)
 * @see android.app.Service#onBind(android.content.Intent)

	@Override
	public IBinder onBind(Intent intent) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() 
	{

	}

 *//**
 * Best way to get the notification and monitoring to get going is to 
 * start the service through startService() and make it unbound.
 * This service would in turn spawn a thread to check the start times in the
 * database. If any of the train's start time to monitor is crossed then an
 * appropriate AlarmManager will be started.
 *//*
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) 
	{
		Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
		mContext = getBaseContext();

		List<Future<String>> list = new ArrayList<Future<String>>();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Callable<String> monitorThread = new DBMonitoringThread(mContext);
		Future<String> submit = executorService.submit(monitorThread);
		list.add(submit);



		for(Future<String> future : list) 
		{
			try 
			{
				System.out.println("Return value from monitoring thread: " + future.get());
				//Toast.makeText(this, "Return value form monitoring thread: " + future.get(), 
				//					Toast.LENGTH_SHORT).show();
			} 
			catch(ExecutionException ex) 
			{
				Log.e("NotificationService", ex.getMessage());
			} 
			catch(CancellationException ex) 
			{
				Log.e("NotificationService", ex.getMessage());
			} 
			catch(InterruptedException ex) 
			{
				Log.e("NotificationService", ex.getMessage());
			}

		}		

		executorService.shutdown();

		return START_STICKY;
	}

	public void onDestory() 
	{
		Toast.makeText(this, "Service destoryed", Toast.LENGTH_SHORT).show();
	}

}

  */