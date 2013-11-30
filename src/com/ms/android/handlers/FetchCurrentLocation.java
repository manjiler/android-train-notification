package com.ms.android.handlers;

import com.ms.android.data.StationInfo;
import com.ms.android.data.TrainStatusCollector;
import com.ms.android.data.TrainStatusUtility;
import com.ms.android.exceptions.CaptchaNotValidException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FetchCurrentLocation extends AsyncTask<String, Integer, String> {

	private Context m_Context;
	private ProgressBar m_ProgressBar;
	private TextView m_CurrentLocationTextView;
	
	public FetchCurrentLocation(Context context, ProgressBar progressBar, TextView currentLocation) {
		m_Context = context;
		m_ProgressBar = progressBar;
		m_CurrentLocationTextView = currentLocation;
	}
	
	@Override
	protected String doInBackground(String... params) 
	{
		TrainStatusCollector infoCollector = new TrainStatusCollector(m_Context);
		publishProgress(10);
		String currentLocation = "";
		StationInfo lastStationInfo = null;
		try
		{
			lastStationInfo = infoCollector.getLastStationLocation(params[0], params[1], params[2]);
			if(lastStationInfo != null) 
			{
				currentLocation = lastStationInfo.getLastLocation() + ". " + "ETA " + 
									lastStationInfo.getActualArrival() + ". " +
									lastStationInfo.getLastUpdatedTime();				
			}
			else
			{
				currentLocation = "Unable to retreive data for time being. Please retry after sometime!!!";
			}
		}
		catch(CaptchaNotValidException ex)
		{
			currentLocation = ex.getMessage() + "." +
								" Please verify/re-verify the captcha in settings menu on Home screen of the App.";
		}
		return currentLocation;
	}

	protected void onProgressUpdate(Integer... values) 
	{
		m_ProgressBar.setVisibility(ProgressBar.VISIBLE);
	}
	
	protected void onPostExecute(String currentLocation) 
	{
		m_ProgressBar.setVisibility(ProgressBar.INVISIBLE);
		m_CurrentLocationTextView.setVisibility(TextView.VISIBLE);
		m_CurrentLocationTextView.setText(currentLocation);		
	}
	
}
