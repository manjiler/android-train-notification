package com.ms.android.trainnotification;

import com.ms.android.db.SearchDataSource;
import com.ms.android.handlers.FetchCurrentLocation;
import com.ms.android.handlers.FetchTrainInfoHandler;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LiveStatusActivity extends Activity
{
	
	private Context m_Context;
	private Button m_FetchTrainInfoButton;
	private AutoCompleteTextView m_TrainNumberEditText;
	private ProgressBar m_ProgressBar;
	private String m_TrainNumber;
	private Spinner m_StationListSpinner;
	private Spinner m_StartDateListSpinner;
	private Button m_UpdateLocationButton;
	private TextView m_CurrentLocationTextView;
	private TextView m_TrainNameTextView;
	
	private static final String tag = "LiveStatusActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_status_activity);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        // fetch the context so that they be passed on the other function where they are needed.
        m_Context = getBaseContext(); //getApplicationContext();
        
        
        m_FetchTrainInfoButton = (Button) findViewById(R.id.fetchTrainInfoButton);
        m_TrainNumberEditText = (AutoCompleteTextView) findViewById(R.id.trainNumberEditText1);
        
        m_ProgressBar = (ProgressBar) findViewById(R.id.imageLoadingProgressBar);
        //Setting the progress bar to invisible
        m_ProgressBar.setVisibility(ProgressBar.INVISIBLE);
        
        m_StationListSpinner = (Spinner) findViewById(R.id.stationListSpinner);
        m_StationListSpinner.setVisibility(Spinner.INVISIBLE);
        
        m_StartDateListSpinner = (Spinner) findViewById(R.id.startDateListSpinner);
        m_StartDateListSpinner.setVisibility(Spinner.INVISIBLE);
        
        m_CurrentLocationTextView = (TextView) findViewById(R.id.locationTextView);
        m_CurrentLocationTextView.setVisibility(TextView.INVISIBLE);
        
        m_UpdateLocationButton = (Button) findViewById(R.id.updateLiveStatusButton);
        m_UpdateLocationButton.setVisibility(Button.INVISIBLE);
        
        m_TrainNameTextView = (TextView) findViewById(R.id.trainNameTextView);
        m_TrainNameTextView.setVisibility(TextView.INVISIBLE);
        
        m_FetchTrainInfoButton.setOnClickListener(fetchTrainInfo);
        m_UpdateLocationButton.setOnClickListener(fetchCurrentLocation);
        
        SearchDataSource searchData = new SearchDataSource(m_Context);
        ArrayAdapter<String> recentSearchAdapter = new ArrayAdapter<String>(m_Context, android.R.layout.simple_dropdown_item_1line,
        														searchData.getAllSearchData());
        m_TrainNumberEditText.setAdapter(recentSearchAdapter);
        m_TrainNumberEditText.setOnItemClickListener(recentSearchListener);
    }
	
	private OnClickListener fetchTrainInfo = new OnClickListener() {
		
		public void onClick(View v) {
			m_TrainNumber = m_TrainNumberEditText.getText().toString();
			if(m_TrainNumber == "") {
				Toast.makeText(m_Context, "Moron!!!... Enter a train number", Toast.LENGTH_SHORT).show();
				return;
			}
			m_CurrentLocationTextView.setText("");
			new FetchTrainInfoHandler(m_Context, m_ProgressBar, m_TrainNameTextView, 
					m_StationListSpinner, m_StartDateListSpinner, m_UpdateLocationButton)
					.execute(m_TrainNumber);
			
		}
	};

	
	private OnClickListener fetchCurrentLocation = new OnClickListener() {
		
		public void onClick(View v) {
			String selectedStation = extractSelectedStation();
			String selectedDate = extractSelectedDate();
			
			if((selectedDate == null) || (selectedStation == null)) {
				Toast.makeText(m_Context, "Check your selection honey!!!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			new FetchCurrentLocation(m_Context, m_ProgressBar, m_CurrentLocationTextView)
						.execute(m_TrainNumber, selectedDate, selectedStation);
			
		}
	};
	
	private OnItemClickListener recentSearchListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			String selectedTrain = (((String)parent.getItemAtPosition(position)).split(" "))[0];
			m_TrainNumberEditText.setText(selectedTrain);
			
		}
		
	};
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.live_train_status_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	boolean returnValue = false;
    	switch(item.getItemId())
    	{
	    	case android.R.id.home:
	    		Intent backScreenIntent = new Intent(this, HomeScreenActivity.class);
	    		backScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	    		startActivity(backScreenIntent);
	    		returnValue = true;
	    		break;
	    		
	    	case R.id.share:
	    		Intent messageIntent = new Intent(Intent.ACTION_SEND);
	    		messageIntent.setType("text/plain");
	    		if ((m_CurrentLocationTextView.getText().toString() != null) && (!(m_CurrentLocationTextView.getText().toString().contentEquals("")))) {
	    			String text = m_TrainNameTextView.getText().toString() + " has " + 
	    							m_CurrentLocationTextView.getText().toString();
	    			//messageIntent.setPackage("com.whatsapp");
	    			messageIntent.putExtra(Intent.EXTRA_TEXT, text);
	    			startActivity(Intent.createChooser(messageIntent, "Share with"));
	    		} else {
	    			Toast.makeText(m_Context, "Location Text is empty!!!", Toast.LENGTH_SHORT).show();
	    		}
	    		break;
    	
    	default:
    		returnValue = super.onOptionsItemSelected(item);    			
    	}
    	
    	return returnValue;
    }
    
    private String extractSelectedStation() {
    	String station = null;
    	if(m_StationListSpinner.getSelectedItemPosition() > 0) {
    		station = (String) m_StationListSpinner.getSelectedItem();
    	}
    	Log.i(tag, "Selected Station : " + station);
    	return station;
    }
    
    private String extractSelectedDate() {
    	String date = null;
    	if(m_StartDateListSpinner.getSelectedItemPosition() > 0) {
    		date = (String) m_StartDateListSpinner.getSelectedItem();
    	}
    	Log.i(tag, "Selected Date : " + date);
    	return date;
    }

}
