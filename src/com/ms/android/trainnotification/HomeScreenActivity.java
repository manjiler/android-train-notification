package com.ms.android.trainnotification;

import java.util.Calendar;

import com.ms.android.service.NotificationService;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeScreenActivity extends Activity 
{

	public Button m_liveStatusButton;
	public Button m_setupNaviButton;
	public Button m_stationCodetoNameButton;
	public Context m_context;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        m_context = getApplicationContext();
        
        
        //Starting Alarm Manager to call the service class
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        Intent intent = new Intent(HomeScreenActivity.this, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(HomeScreenActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        long intervalMillis = 30 * 60 * 1000; 
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intervalMillis, pendingIntent);
              
        //Starting the NotificationService 
        /*
        if(!isMyServiceRunning(NotificationService.class.getName()))
        {
        	Intent serviceIntent = new Intent(this, NotificationService.class);
        	startService(serviceIntent);
        }
        */
                
        m_liveStatusButton = (Button) findViewById(R.id.liveStatusButton);
        m_setupNaviButton = (Button) findViewById(R.id.setupNotificationButton);
        
        m_liveStatusButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(m_context, LiveStatusActivity.class);
				startActivity(intent);				
			}
		});
        m_setupNaviButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(m_context, SetupNotifyActivity.class);
				startActivity(intent);				
			}
		});        
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_home_screen, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    	case R.id.menu_settings:
    		Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
    		startActivity(intent);
    		return true;
    	default:
    		super.onOptionsItemSelected(item);
    	}
    	return true;
    }
    
    private boolean isMyServiceRunning(String serviceName)
    {
    	ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    	for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
    	{
    		if(serviceName.equals(service.service.getClassName()))
    		{
    			return true;
    		}
    	}
    	return false;
    }
    /*
    OnClickListener setupNotifyScreen = new OnClickListener() {
		
		public void onClick(View v) {
			Intent intent = new Intent(m_context, SetupNotifyActivity.class);
			startActivity(intent);
		}
	};
    
    OnClickListener liveStatusScreen = new OnClickListener() {
		
		public void onClick(View v) {
			Intent intent = new Intent(m_context, LiveStatusActivity.class);
			startActivity(intent);
		}
	};
	*/	
}
