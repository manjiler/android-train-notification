package com.ms.android.trainnotification;

import java.util.List;

import com.ms.android.data.TrainDO;
import com.ms.android.db.TrainDataSource;
import com.ms.android.swipetodismiss.SwipeDismissListViewTouchListener;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SetupNotifyActivity extends Activity 
{
	
	ListView mNotificationList;
	Context mContext;
	CustomeAdapter adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_notify_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        mNotificationList = (ListView) findViewById(R.id.notifyList);
        mContext = getBaseContext();
        TrainDataSource tds = new TrainDataSource(mContext);
        List<TrainDO> listOfTrains = tds.getAllTrains();
        adapter = new CustomeAdapter(this, listOfTrains);
        mNotificationList.setAdapter(adapter);
        mNotificationList.setLongClickable(true);
        /*registerForContextMenu(mNotificationList);
        mNotificationList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() 
        {
			public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo)
			{
				MenuInflater inflater = getMenuInflater();
		    	inflater.inflate(R.menu.setup_context_munu, menu);				
			}
		});*/
        
        //Setting up swipe to remove list entry
        SwipeDismissListViewTouchListener touchListener =
        		new SwipeDismissListViewTouchListener(
        				mNotificationList, 
        				new SwipeDismissListViewTouchListener.DismissCallbacks() {
        					//@Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            //@Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                	adapter.data.remove(adapter.getItem(position));
                                    //adapter.remove(adapter.getItem(position));
                                }
                                adapter.notifyDataSetChanged();
                            }							
						});
        mNotificationList.setOnTouchListener(touchListener);
        mNotificationList.setOnScrollListener(touchListener.makeScrollListener());
        
    }

	public boolean OnContextItemSeleted(MenuItem menuItem)
	{
		Toast.makeText(this, "!!!", Toast.LENGTH_SHORT).show();
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) menuItem.getMenuInfo();
		switch(menuItem.getItemId())
		{
			case R.id.deleteNotification:
				Toast.makeText(this, "Have to issue DELETE query to remove the record!!!", Toast.LENGTH_SHORT).show();
				return true;
			
			default:
				return super.onContextItemSelected(menuItem);
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.setup_notify_menu, menu);
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
    		
    	case R.id.addNotify:
    		Intent addNotificationIntent = new Intent(this, AddNotificationActivity.class);
    		addNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    		startActivity(addNotificationIntent);
    		returnValue = true;
    		break;
    		
    	default:
    		returnValue = super.onOptionsItemSelected(item);
    		break;
    	}
    	 
    	return returnValue;
    }
       
    public class CustomeAdapter extends BaseAdapter {
    	
    	private Activity activity;
    	private List<TrainDO> data;
    	private LayoutInflater inflater = null;
    	
    	public CustomeAdapter(Activity activity, List<TrainDO> d) {
    		this.activity = activity;
    		data = d;
    		inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	}

		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if(convertView == null) {
				view = inflater.inflate(R.layout.notify_list_row, null);
			}
			
			TextView trainName = (TextView) view.findViewById(R.id.trainNameListRow);
			TextView stationsTextView = (TextView) view.findViewById(R.id.stationCodesListRow);
			TextView updateTypeTextView = (TextView) view.findViewById(R.id.UpdateTypeListRow);
			TextView repetitionTextView = (TextView) view.findViewById(R.id.repetitionListRow);
			TextView timeIntervalTextView = (TextView) view.findViewById(R.id.timeIntervalListRow);
			TextView frequencyTextView = (TextView) view.findViewById(R.id.frequencyListRow);
			
			TrainDO trainData = data.get(position);
			
			String strTrainName = (Integer.valueOf(trainData.getTrainNumber())).toString() + " - " +
										trainData.getTrainName();
			trainName.setText(strTrainName);
			
			String stationsList = "";
			String comma = "";
			for(String station : trainData.getStationCodes()) {
				stationsList += comma + station;
				comma = ",";
			}
			
			stationsTextView.setText(stationsList);
			
			updateTypeTextView.setText("Update Type: " + trainData.getUpdateType());
			repetitionTextView.setText("Repetition: " + trainData.getRepitition());
			if(trainData.getRepitition().equalsIgnoreCase(getResources().getString(R.string.does_not_repeat))) {
				int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
		                | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH
		                | DateUtils.FORMAT_ABBREV_WEEKDAY;
				String dateString = DateUtils.formatDateTime(SetupNotifyActivity.this, trainData.getDateInMilli(), flags);
		        repetitionTextView.setText("Repetition: " + trainData.getRepitition() + " - " + dateString);
			}
			//SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
			
			//String starttime = dateFormat.format(new Date(trainData.getStartTsecs()));
			//String endtime = dateFormat.format(new Date(trainData.getEndTsecs()));
			String starttime = milliToStringConverter(trainData.getStartTsecs());
			String endtime = milliToStringConverter(trainData.getEndTsecs());
			timeIntervalTextView.setText("Time Interval: " + starttime + " - " + endtime);
			if(trainData.getFrequency() != null) {
				frequencyTextView.setText("Frequency: " + trainData.getFrequency());
			}
			
			return view;
			
		}
		
		
		
		private String milliToStringConverter(long timeInMillis)
		{
			int totalNumberOfMinutes = (int)((timeInMillis / 1000) / 60);
			int minutes = totalNumberOfMinutes % 60;
			int hours = (totalNumberOfMinutes - minutes) / 60;
			return String.format("%02d", hours) + ":" + String.format("%02d", minutes);	
		}
    	
    }

}
