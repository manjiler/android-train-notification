package com.ms.android.handlers;

import com.ms.android.data.TrainInfo;
import com.ms.android.data.TrainStatusCollector;
import com.ms.android.db.SearchDataSource;
import com.ms.android.exceptions.CaptchaNotValidException;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FetchTrainInfoHandler extends AsyncTask<String, Integer, TrainInfo> 
{

	private ProgressBar m_ProgressBar;
	private Context m_Context;
	private Spinner m_StationListSpinner;
	private Spinner m_StartDateListSpinner;
	private Button m_LocationUpdateButton;
	private TextView m_TrainNameTextView;
	private boolean mInvalidCaptchaException;
	
	private static final String NO_NETWORK_TOAST = "No Network access!!!";
	private static final String INVALID_CAPTCHA = "Please verify/re-verify your captcha via Settings menu home screen";
	
	public static final String INITIAL_TEXT_STATION_LIST = "Select boarding station";
	
	public static final String INITIAL_TEXT_STARTDATES_LIST = "Select a Date";
	
	public FetchTrainInfoHandler(Context context, ProgressBar progressBar, TextView trainName,
					Spinner stationList, Spinner startDateList, Button fetchCurrentLocation)
	{
		m_Context = context;
		m_ProgressBar = progressBar;
		m_StationListSpinner = stationList;
		m_TrainNameTextView = trainName;
		m_StartDateListSpinner = startDateList;
		m_LocationUpdateButton = fetchCurrentLocation;
		
		mInvalidCaptchaException = false;
	}
	
	@Override
	protected TrainInfo doInBackground(String... params) 
	{
		TrainStatusCollector infoCollector = new TrainStatusCollector(m_Context);
		publishProgress(100);
		TrainInfo trainInfo = null;
		try
		{
			trainInfo = infoCollector.getTrainInfo(params[0]);
			if((trainInfo != null) && (trainInfo.getValidTrain())) 
			{
				//set the train number and name into the database.
				addSearchToDatabase(params[0], trainInfo.getExtendedTrainName());
			}
		}
		catch(CaptchaNotValidException ex)
		{
			mInvalidCaptchaException = true;
		}
		return trainInfo;
	}
	
	private void addSearchToDatabase(String trainNumber, String trainName) 
	{
		SearchDataSource searchData = new SearchDataSource(m_Context);
		searchData.insertSearchData(Integer.parseInt(trainNumber), trainName);
	}
	
	
	@Override
	protected void onProgressUpdate (Integer... values)
	{		
		m_ProgressBar.setVisibility(ProgressBar.VISIBLE);
		//m_ProgressBar.setProgress(values[0]);
	}
	
	
	protected void onPostExecute (TrainInfo result)
	{
		m_ProgressBar.setVisibility(ProgressBar.INVISIBLE);
		if(result == null)
		{
			if(mInvalidCaptchaException)
			{
				Toast.makeText(m_Context, INVALID_CAPTCHA, Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(m_Context, NO_NETWORK_TOAST, Toast.LENGTH_LONG).show();
			}
		}
		else
		{
			if(result.getValidTrain()) 
			{
				//Enable all the display widgets
				m_StationListSpinner.setVisibility(Spinner.VISIBLE);
				m_StartDateListSpinner.setVisibility(Spinner.VISIBLE);
				m_TrainNameTextView.setVisibility(TextView.VISIBLE);
				m_LocationUpdateButton.setVisibility(Button.VISIBLE);

				//Setting spinner adapter for stations.
				//set initial text 
				result.getListOfStations().add(0, INITIAL_TEXT_STATION_LIST);
				ArrayAdapter<String> stationListAdapter = new ArrayAdapter<String>(m_Context, android.R.layout.simple_spinner_item,
						result.getListOfStations());
				stationListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				m_StationListSpinner.setAdapter(stationListAdapter);

				//Setting the spinner adapter for start times.
				//set initial text
				result.getStartDates().add(0, INITIAL_TEXT_STARTDATES_LIST);
				ArrayAdapter<String> startDateListAdapter = new ArrayAdapter<String>(m_Context, android.R.layout.simple_spinner_item, 
						result.getStartDates());
				startDateListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				m_StartDateListSpinner.setAdapter(startDateListAdapter);

				m_TrainNameTextView.setText(result.getExtendedTrainName());
			} else {
				m_TrainNameTextView.setVisibility(TextView.VISIBLE);
				m_TrainNameTextView.setText("Invalid train number!!!");
			}
		}
	}

}
