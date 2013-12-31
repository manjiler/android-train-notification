package com.ms.android.trainnotification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.ms.android.constants.Constants;
import com.ms.android.data.TrainDO;
import com.ms.android.db.TrainDataSource;
import com.ms.android.handlers.FetchStationListHanlder;

import android.R.anim;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TimePicker;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AddNotificationActivity extends Activity 
{
	
	private Button mFetchStationListButton;
	private Button mAddMoreStationButton;
	private EditText mTrainNumberEditText;
	private ProgressBar mStationListProgressBar;
	private Spinner mStationListSpinner;
	private Switch mStatusBarSwitch;
	private Switch mSmsNotifySwitch;
	private Switch mEmailNotifySwitch;
	private TextView mExtendedTrainName;
	private Context mContext;
	public static List<String> mListOfStations; 
	private ImageView mRemoveSpinner;
	private int numberOfStationsAdded;
	private static int INDEX_NUMBER_OF_FIRST_SPINNER = 5;
	private LinearLayout llOuter;
	protected Object mActionMode;
	private Spinner mRepetitionSpinner;
	private Button mStartTimeButton;
	private Button mEndTimeButton;
	private Time mStartTime;
	private RadioGroup mUpdateTypeRadioGroup;
	private Spinner mFrequencySpinner;
	private LinearLayout mFreqLinearLayout;
	private Button mStartDateButton;
	private LinearLayout mStartDateLinearLayout;
	
	
    private long mStartTimeInMillis;
    private long mEndTimeInMillis;
	private long mDateInMillis;
	
	private class TimeClickListener implements View.OnClickListener {
        private Time mTime;

        public TimeClickListener(Time time) {
            mTime = time;
        }

        public void onClick(View v) {
            TimePickerDialog tp = new TimePickerDialog(AddNotificationActivity.this, new TimeListener(v, mTime), mTime.hour,
                    mTime.minute, DateFormat.is24HourFormat(AddNotificationActivity.this));
            tp.setCanceledOnTouchOutside(true);
            tp.show();
        }
    }
	
	private class TimeListener implements OnTimeSetListener {
        private View mView;
        private Time mTime;
        
        public TimeListener(View view, Time time) {
            mView = view;
            mTime = time;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        	
        	Time startTime = mTime;
        	long startMillis;
        	startTime.hour = hourOfDay;
        	startTime.minute = minute;
        	startMillis = startTime.normalize(true);
        	if(mView == mStartTimeButton) {
        		setTime(mStartTimeButton, startMillis);
        		mStartTimeInMillis = (hourOfDay * 3600 + minute * 60) * 1000;
        	} else if(mView == mEndTimeButton) {
        		setTime(mEndTimeButton, startMillis);
        		mEndTimeInMillis = (hourOfDay * 3600 + minute * 60) * 1000;
        	}
        }
    }
	
	private class DateClickListener implements View.OnClickListener {
		private Time mTime;
		
		public DateClickListener(Time time) {
			mTime = time;
		}
		
		public void onClick(View v) {
			DatePickerDialog dpd = new DatePickerDialog(AddNotificationActivity.this, new DateListener(v), mTime.year, 
									mTime.month, mTime.monthDay);
			CalendarView cv = dpd.getDatePicker().getCalendarView();
			cv.setFirstDayOfWeek(Calendar.SUNDAY);
			cv.setShowWeekNumber(true);
			dpd.setCanceledOnTouchOutside(true);
			dpd.show();			
		}
		
	}
	
	private class DateListener implements OnDateSetListener {
		private View mView;
		
		public DateListener(View view) {
			mView = view;
		}
		
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Time startDate = new Time();
			startDate.year = year;
			startDate.month = monthOfYear;
			startDate.monthDay = dayOfMonth;
			setDate(mStartDateButton, startDate.normalize(true));
			mDateInMillis = startDate.normalize(true);
		}
	}
	
	private void setTime(TextView view, long millis) {
		int flags = DateUtils.FORMAT_SHOW_TIME;
        if (DateFormat.is24HourFormat(AddNotificationActivity.this)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        
        String timeString;
        timeString = DateUtils.formatDateTime(AddNotificationActivity.this, millis, flags);
        view.setText(timeString);
	}
	
	private void setDate(TextView view, long millis) {
		int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
                | DateUtils.FORMAT_ABBREV_WEEKDAY;
		String dateString;
		
        dateString = DateUtils.formatDateTime(AddNotificationActivity.this, millis, flags);
        // setting the default back to null restores the correct behavior
        TimeZone.setDefault(null);
        view.setText(dateString);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_notify_screen);
		
		//setting the app icon to act as navigation for previous hierarchical screen.
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		mContext = getBaseContext();
		mFetchStationListButton = (Button) findViewById(R.id.fetchStationListButton);
		mAddMoreStationButton = (Button) findViewById(R.id.AddMoreStationButton);
		mStationListSpinner = (Spinner) findViewById(R.id.stationListSpinner3);
		mTrainNumberEditText = (EditText) findViewById(R.id.trainNumberEditText);
		mStationListProgressBar = (ProgressBar) findViewById(R.id.stationListProgressBar);
		mStatusBarSwitch = (Switch) findViewById(R.id.statusBarNotifySwitch);
		mSmsNotifySwitch = (Switch) findViewById(R.id.smsNotifySwitch);
		mEmailNotifySwitch = (Switch) findViewById(R.id.emailNotifySwitch);	
		mExtendedTrainName = (TextView) findViewById(R.id.trainNameAddNotify);
		mRemoveSpinner = (ImageView) findViewById(R.id.removeStationSpinner3);
		mRemoveSpinner.setVisibility(ImageView.INVISIBLE);
		mStationListProgressBar.setVisibility(ProgressBar.INVISIBLE);
		mStationListSpinner.setVisibility(ProgressBar.INVISIBLE);
		mAddMoreStationButton.setVisibility(Button.INVISIBLE);
		//m_StatusBarSwitch.setVisibility(Switch.INVISIBLE);
		//m_SmsNotifySwitch.setVisibility(Switch.INVISIBLE);
		//m_EmailNotifySwitch.setVisibility(Switch.INVISIBLE);
		mRepetitionSpinner = (Spinner) findViewById(R.id.repeatSpinner);
		mStartTimeButton = (Button) findViewById(R.id.startTimeButton);
		mEndTimeButton = (Button) findViewById(R.id.endTimeButton);
		mUpdateTypeRadioGroup = (RadioGroup) findViewById(R.id.updateTypeRadioGroup);
		mUpdateTypeRadioGroup.setVisibility(RadioGroup.INVISIBLE);
		mFrequencySpinner = (Spinner) findViewById(R.id.frequencySpinner);
		mFreqLinearLayout = (LinearLayout) findViewById(R.id.freqLinearLayout);
		mFreqLinearLayout.setVisibility(LinearLayout.INVISIBLE);
		
		mFetchStationListButton.setOnClickListener(fetchStationList);
		mAddMoreStationButton.setOnClickListener(addMoreStations);
		
		Time time = new Time();
		time.set(System.currentTimeMillis());
		mStartTimeButton.setOnClickListener(new TimeClickListener(time));
		mEndTimeButton.setOnClickListener(new TimeClickListener(time));
		populateRepeatSpinner(mRepetitionSpinner);
		
		numberOfStationsAdded = 0;
		llOuter = (LinearLayout) findViewById(R.id.linearLayoutOuter);
		
		mUpdateTypeRadioGroup.setOnCheckedChangeListener(updateTypeChangeListener);
		setFrequencyAdapter();
		
		mStartDateButton = (Button) findViewById(R.id.startDateButton);
		mStartDateLinearLayout = (LinearLayout) findViewById(R.id.startDateLinearLayout);
		mRepetitionSpinner.setOnItemSelectedListener(itemSelectedListener);
		mStartDateButton.setOnClickListener(new DateClickListener(time));
	}
	
	private void populateRepeatSpinner(Spinner repeatSpinner) {
		List<String> itemsList = new ArrayList<String>();
        Time time = new Time();
        time.set(System.currentTimeMillis());
        Resources res = getResources();
        
        String[] days = new String[] {
                DateUtils.getDayOfWeekString(Calendar.SUNDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.MONDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.TUESDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.WEDNESDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.THURSDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.FRIDAY, DateUtils.LENGTH_MEDIUM),
                DateUtils.getDayOfWeekString(Calendar.SATURDAY, DateUtils.LENGTH_MEDIUM), };
        String[] ordinals = res.getStringArray(R.array.ordinal_labels);
       
        int dayNumber = (time.monthDay - 1) / 7;
        System.out.println("Day Number: " + dayNumber);
        itemsList.add(res.getString(R.string.does_not_repeat));
        /* TODO: Need to think back as to why this stupid check was done.
        if((time.weekDay != 0) && (time.weekDay != 6)) {
        	itemsList.add(res.getString(R.string.every_weekday));
        }
        */
        itemsList.add(res.getString(R.string.every_weekday));
        itemsList.add(res.getString(R.string.weekly, time.format("%A")));
        itemsList.add(res.getString(R.string.monthly_on_day_count, ordinals[dayNumber], days[time.weekDay]));
        itemsList.add(res.getString(R.string.monthly_on_day, time.monthDay));
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, itemsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(adapter);
	}
	
	public void setFrequencyAdapter() {
		@SuppressWarnings("serial")
		List<String> frequencyList = new ArrayList<String>() {{
			add("15");
			add("30");
			add("45");
			add("60");			
		}};
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, frequencyList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mFrequencySpinner.setAdapter(adapter);
		
	}
	
	OnClickListener addMoreStations = new OnClickListener() 
	{		
		public void onClick(View v) 
		{
			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutOuter);
			
			LinearLayout stationLayout = new LinearLayout(mContext);
			stationLayout.setOrientation(LinearLayout.HORIZONTAL);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 10, 0, 0);
			
			Spinner newSpinner = new Spinner(mContext, Spinner.MODE_DROPDOWN);
			LayoutParams spinnerParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.8f);
			newSpinner.setLayoutParams(spinnerParams);
			mListOfStations.remove(0);
			mListOfStations.add(0, "Select a station for notification");
			ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, 
															mListOfStations);
			spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			newSpinner.setAdapter(spinnerAdapter);
			
			ImageView removeSpinner = new ImageView(mContext);
			LayoutParams imageParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f);
			removeSpinner.setLayoutParams(imageParams);
			removeSpinner.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
			removeSpinner.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayoutOuter);
					int position = ((Integer) ((View)v.getParent()).getTag()).intValue();
					ll.removeViewAt(INDEX_NUMBER_OF_FIRST_SPINNER + position);
					numberOfStationsAdded--;
					//Reset the tags of all the linear layouts created dynamically
					for(int i = 0; i < numberOfStationsAdded; i++) {
						((LinearLayout)ll.getChildAt(INDEX_NUMBER_OF_FIRST_SPINNER + i)).setTag(i);
					}
				}
			});
			
			stationLayout.addView(newSpinner);
			stationLayout.addView(removeSpinner);
			stationLayout.setTag(numberOfStationsAdded);
			linearLayout.addView(stationLayout, (INDEX_NUMBER_OF_FIRST_SPINNER + numberOfStationsAdded));
			numberOfStationsAdded++;			
		}
	};
	
	OnClickListener fetchStationList = new OnClickListener() 
	{		
		public void onClick(View v) 
		{
			new FetchStationListHanlder(mContext, mStationListProgressBar, mExtendedTrainName, 
					mStationListSpinner, mRemoveSpinner, mAddMoreStationButton,
					mUpdateTypeRadioGroup).execute(mTrainNumberEditText.getText().toString());
			
			mActionMode = AddNotificationActivity.this.startActionMode(mActionModeCallBack);			
		}
	};
	
	OnCheckedChangeListener updateTypeChangeListener = new OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if(checkedId == R.id.stationUpdateRadioButton) {
				mAddMoreStationButton.setVisibility(Button.VISIBLE);
				mStationListSpinner.setVisibility(Spinner.VISIBLE);
				/*
				mListOfStations.add(0, FetchStationListHanlder.INITIAL_STATION_SELECTION_TEXT);
				ArrayAdapter<String> stationListAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,
															mListOfStations);
				stationListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				mStationListSpinner.setAdapter(stationListAdapter);
				*/
				mRemoveSpinner.setVisibility(ImageView.VISIBLE);
				if(mFreqLinearLayout.getVisibility() == LinearLayout.VISIBLE) {
					mFreqLinearLayout.setVisibility(LinearLayout.INVISIBLE);
				}
			} else if (checkedId == R.id.locationUpdateRadioButton) {
				//Toast.makeText(mContext, "Location Updates Selected", Toast.LENGTH_SHORT).show();
				if(mFreqLinearLayout.getVisibility() == LinearLayout.INVISIBLE) {
					mFreqLinearLayout.setVisibility(LinearLayout.VISIBLE);
				}
				
				//Remove all the stations that have added for notifications
				LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutOuter);
				Log.i("AddNotificationActivity.OnCheckedChangedListener", "Value of numberOfStationsAdded: " + numberOfStationsAdded);
				while(numberOfStationsAdded > 0) {
					linearLayout.removeViewAt(INDEX_NUMBER_OF_FIRST_SPINNER);
					numberOfStationsAdded--;
				}
				
				/*for(int i = 0; i < numberOfStationsAdded; i++) {
					linearLayout.removeViewAt(INDEX_NUMBER_OF_FIRST_SPINNER);
					numberOfStationsAdded--;
				}*/

				if(mAddMoreStationButton.getVisibility() == Button.VISIBLE) {
					mAddMoreStationButton.setVisibility(Button.INVISIBLE);
				}				
			}			
		}
	};
	
	OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			Resources res = getResources();
			String selectedItem = (String)parent.getItemAtPosition(position);
			if(selectedItem.equalsIgnoreCase(res.getString(R.string.does_not_repeat))) {
				mStartDateLinearLayout.setVisibility(LinearLayout.VISIBLE);
			} else {
				mStartDateLinearLayout.setVisibility(LinearLayout.GONE);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			
		}
		
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean returnValue = false;
		switch(item.getItemId())
		{
		case android.R.id.home:
			Intent previousScreenIntent = new Intent(this, SetupNotifyActivity.class);
			previousScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(previousScreenIntent);
			returnValue = true;
			break;
			
		default:
			returnValue = super.onOptionsItemSelected(item);
			break;
		}
		return returnValue;
	}
	
	public ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {
		
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}
		
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			
		}
		
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater menuInflater = mode.getMenuInflater();
			menuInflater.inflate(R.menu.add_notify_contextual, menu);
			mTrainNumberEditText.setEnabled(false);
			return true;
		}
		
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			boolean returnValue = false;
			switch(item.getItemId()) {
			case R.id.save:
				//TODO: need to rework on this.
				TrainDO selectedTrainData = new TrainDO();
				boolean bContinue = true;
				selectedTrainData.setTrainNumber(Integer.parseInt(mTrainNumberEditText.getText().toString()));
				selectedTrainData.setTrainName(mExtendedTrainName.getText().toString());
				
				List<String> selectedStations = new ArrayList<String>();
				if(mStationListSpinner.getSelectedItemPosition() > 0) {
					selectedStations.add((String) mStationListSpinner.getSelectedItem());
				} else {
					bContinue = false;
				}

				if(mUpdateTypeRadioGroup.getCheckedRadioButtonId() == R.id.stationUpdateRadioButton) {
					if(bContinue) {
						for(int i = 0; i < numberOfStationsAdded; i++) {

							int position = ((Spinner) (((LinearLayout)llOuter.getChildAt(INDEX_NUMBER_OF_FIRST_SPINNER + i))
									.getChildAt(0))).getSelectedItemPosition();
							if(position > 0) {
								selectedStations.add((String)(((Spinner) (((LinearLayout)llOuter.getChildAt(INDEX_NUMBER_OF_FIRST_SPINNER + i))
										.getChildAt(0))).getSelectedItem()));
							} else {
								bContinue = false;
								break;
							}

						}
						
					}
					selectedTrainData.setUpdateType(Constants.STATION_WISE_UPDATE);

				} else if(mUpdateTypeRadioGroup.getCheckedRadioButtonId() == R.id.locationUpdateRadioButton) {
					selectedTrainData.setUpdateType(Constants.LOCATION_WISE_UPDATE);					
				}
				
				selectedTrainData.setFrequency(mFrequencySpinner.getSelectedItem().toString());
				selectedTrainData.setStationCodes(selectedStations);
						
				selectedTrainData.setRepitition(mRepetitionSpinner.getSelectedItem().toString());
				if(getResources().getString(R.string.does_not_repeat).equalsIgnoreCase(selectedTrainData.getRepitition())) {
					if(!mStartDateButton.getText().toString().equalsIgnoreCase("")) {
						selectedTrainData.setDateInMilli(mDateInMillis);
					} else {
						bContinue = false;
					}
				}
				
				//Extract start time - this number of seconds since the start of the day.
				if(!(mStartTimeButton.getText().toString().equalsIgnoreCase(""))) {
					//String time = mStartTimeButton.getText().toString();
					//SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
					//long startTsecs = simpleDateFormat.parse(time).getTime();

					selectedTrainData.setStartTsecs(mStartTimeInMillis);
				} else {
					bContinue = false;				
				}

				//Extract end time - this is the number of seconds since the start of the day.
				if(!(mEndTimeButton.getText().toString().equalsIgnoreCase(""))) {
					//String time = mEndTimeButton.getText().toString();
					//SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
					//long endTsecs = simpleDateFormat.parse(time).getTime();

					selectedTrainData.setEndTsecs(mEndTimeInMillis);
				} else {
					bContinue = false;
				}
				 
				
				if(bContinue) {
					if(mStatusBarSwitch.isChecked()) {
						selectedTrainData.setStatusBarNofiy(1);
					} else {
						selectedTrainData.setStatusBarNofiy(0);
					}

					if(mSmsNotifySwitch.isChecked()) {
						selectedTrainData.setSmsNotify(1);
					} else {
						selectedTrainData.setSmsNotify(0);
					}

					if(mEmailNotifySwitch.isChecked()) {
						selectedTrainData.setEmailNotify(1);
					} else {
						selectedTrainData.setEmailNotify(0);
					}
					
					//Insert the data
					TrainDataSource tds = TrainDataSource.getInstance(mContext);
					tds.insert(selectedTrainData);
					mode.finish();
					Intent previousScreenIntent = new Intent(getBaseContext(), SetupNotifyActivity.class);
					previousScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(previousScreenIntent);
					//mTrainNumberEditText.setEnabled(true);
					returnValue = true;
				} else {
					Toast.makeText(mContext, "Check your selection!!!", Toast.LENGTH_SHORT).show();					
				}
				
			break;
				
			case R.id.cancel:
				mExtendedTrainName.setText("");
				mExtendedTrainName.setVisibility(TextView.INVISIBLE);
				
				mStationListSpinner.setVisibility(Spinner.INVISIBLE);
				mRemoveSpinner.setVisibility(ImageView.INVISIBLE);
				mAddMoreStationButton.setVisibility(Button.INVISIBLE);
				
				for(int i= 0; i < numberOfStationsAdded; i++) {
					llOuter.removeViewAt(INDEX_NUMBER_OF_FIRST_SPINNER);
				}
				
				mStatusBarSwitch.setChecked(false);
				mSmsNotifySwitch.setChecked(false);
				mEmailNotifySwitch.setChecked(false);
				mode.finish();
				Intent previousScreenIntent = new Intent(getBaseContext(), SetupNotifyActivity.class);
				previousScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(previousScreenIntent);
				//mTrainNumberEditText.setEnabled(true);
				//numberOfStationsAdded = 0;
				returnValue = true;
				break;
			}
			
			return returnValue;
		}
	};
}
