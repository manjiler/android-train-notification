/**
 * 
 */
package com.ms.android.service;

import com.ms.android.data.TrainDO;

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
public class TrainStationUpdateService extends IntentService
{
	private static final String tag = TrainStationUpdateService.class.getName();
	private static final String ERROR_MSG = "Unable to connect the railway server. Please retry after sometime!!!";
	private static final String INVALID_CAPTCHA = "Please verify/re-verify your captcha via Settings menu home screen";
	
	public TrainStationUpdateService() 
	{
		super(tag);		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		//TODO: Your logic goes here		
	}
	
	

}
