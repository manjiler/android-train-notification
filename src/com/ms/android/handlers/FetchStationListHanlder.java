package com.ms.android.handlers;

import java.util.List;

import com.ms.android.data.TrainInfo;
import com.ms.android.data.TrainStatusCollector;
import com.ms.android.exceptions.CaptchaNotValidException;
import com.ms.android.trainnotification.AddNotificationActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;

public class FetchStationListHanlder extends AsyncTask<String, Integer, TrainInfo> 
{
	
	private static final String tag = FetchStationListHanlder.class.toString();
	public static final String INITIAL_STATION_SELECTION_TEXT = "Select a station for borading";
	private static final String INVALID_CAPTCHA = "Please verify/re-verify your captcha via Settings menu home screen";
	
	private ProgressBar mProgressBar;
	private Spinner mSpinner;
	private TextView mExtendedTrainName;
	private Button mAddMoreStations;
	private Context mContext;
	private ImageView mRemoveSpinner;
	private RadioGroup mRadioGroup;
	private boolean mInvalidCaptcha;
		
	public FetchStationListHanlder(Context context, ProgressBar progressBar, TextView extendedTrainName, 
								Spinner stationListSpinner,	ImageView removeSpinner, Button addMoreStations,
								RadioGroup radioGroup) {
		mContext = context;
		mProgressBar = progressBar;
		mExtendedTrainName = extendedTrainName;
		mSpinner = stationListSpinner;
		mRemoveSpinner = removeSpinner;
		mAddMoreStations = addMoreStations;
		mRadioGroup = radioGroup;
	}
		
	protected TrainInfo doInBackground(String... params) 
	{
		TrainStatusCollector trainStatus = new TrainStatusCollector(mContext);
		publishProgress(0);
		TrainInfo trainInfo = null;
		try
		{
			trainInfo = trainStatus.getTrainInfo(params[0]);
		}
		catch(CaptchaNotValidException ex)
		{
			mInvalidCaptcha = true;
		}
		return trainInfo;
	}
	
	@Override
	protected void onProgressUpdate (Integer... values)
	{
		mProgressBar.setVisibility(ProgressBar.VISIBLE);
		
	}
	
	@Override
	protected void onPostExecute (TrainInfo trainInfo)	
	{
		mProgressBar.setVisibility(ProgressBar.INVISIBLE);
		if(trainInfo == null) 
		{
			if(mInvalidCaptcha)
			{
				Toast.makeText(mContext, INVALID_CAPTCHA, Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(mContext, "Unable to list of station. Check your Internet connection", Toast.LENGTH_SHORT)
										.show();
			}
			
		}
		else 
		{
			
			mExtendedTrainName.setVisibility(TextView.VISIBLE);
			mExtendedTrainName.setText(trainInfo.getExtendedTrainName());
			
			mRadioGroup.setVisibility(RadioGroup.VISIBLE);
			
			mAddMoreStations.setVisibility(Button.VISIBLE);
			mSpinner.setVisibility(Spinner.VISIBLE);
			trainInfo.getListOfStations().add(0, INITIAL_STATION_SELECTION_TEXT);
			ArrayAdapter<String> stationListAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,
														trainInfo.getListOfStations());
			stationListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mSpinner.setAdapter(stationListAdapter);
			
			mRemoveSpinner.setVisibility(ImageView.VISIBLE);
			
			AddNotificationActivity.mListOfStations = trainInfo.getListOfStations();
			
		}
	}

}
