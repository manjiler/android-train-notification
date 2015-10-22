package com.ms.android.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ms.android.constants.Constants;
import com.ms.android.exceptions.CaptchaNotValidException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

public class TrainStatusCollector 
{

	public static Context mContext;
	
	public static String TRAIN_ENQUIRY_SERVER = "http://mobile.trainenquiry.com/l";
	
	//public static String SCHEDULE_SERVER = "http://stage.railyatri.in/te/schedule/";
	//public static String SEARCH_SERVICE_HOST = "http://coa-search-193678880.ap-southeast-1.elb.amazonaws.com/te/schedule/";
	public static String SEARCH_SERVICE_HOST = "http://coa-search-193678880.ap-southeast-1.elb.amazonaws.com/";

	//public static String ETA_SERVER = "http://coa.railyatri.in/train/location/eta.json?";
	//public static String COA_SERVICE_HOST = "http://coa-433841822.ap-southeast-1.elb.amazonaws.com/train/location/eta.json?";
	public static String COA_SERVICE_HOST = "http://coa-433841822.ap-southeast-1.elb.amazonaws.com/";

	//public static String LOCATION_SERVER = "http://coa.railyatri.in/train/location.json?callback=jQuery164011875250330194831";
	//public static String LOCATION_SERVER = "http://coa-433841822.ap-southeast-1.elb.amazonaws.com/train/location.json?callback=jQuery164011875250330194831";
	public static String LOCATION_SERVER = TRAIN_ENQUIRY_SERVER + "/ajax/location.json?";
	
	public static final String CURRENT_LOCATION_TRAIN_URL = "http://enquiry.indianrail.gov.in/mntes/MntesServlet?action=TrainRunning&subAction=ShowRunC";
	public static final String SEARCH_TRAIN_URL = "http://enquiry.indianrail.gov.in/mntes/MntesServlet?action=TrainRunning&subAction=TrainSchedule&event=show&trainNo=";

	public static final String JSON = ".json";

	public static final String DATE_FORMAT_BY_RAILWAYS_STA = "yyyy-MM-dd'T'HH:mm:ss";

	public static final String DATE_FORMAT_BY_RAILWAY_DELAY = "mm";

	public static final String DATE_FORMAT_BY_ME = "HH:mm";

	public static final String tag = "TrainStatusCollector";

	private static final String EXTENDED_TRAIN_NAME = "extended_train_name";
	
	private static final String TRAIN_NAME = "name";

	private static final String ROUTES = "routes";

	private static final String STATIONS = "stations"; 
	
	public TrainStatusCollector(Context context)
	{
		mContext = context;
	}

	/**
	 * Returns a TrainInfo object populated with train name, list of station and list of start dates.
	 * @param trainNumber train number
	 * @return a TrainInfo object
	 */
	public TrainInfo getTrainInfo(String trainNumber) throws CaptchaNotValidException
	{
		//String trainInfoUrl = SEARCH_SERVICE_HOST + "search" + JSON + "?q=" + trainNumber;
		String trainInfoUrl = SEARCH_TRAIN_URL + trainNumber;
		String refererUrl = "http://enquiry.indianrail.gov.in/mntes/MntesServlet?action=TrainRunning&subAction=ShowRun";
		//List<String> listOfStations = null;
		//List<String> startDate = null;
		TrainInfo trainInfo = null;
		//Forming the hidden form data for the URL since its going be POST call.
		ArrayList<NameValuePair> formData = new ArrayList<NameValuePair>();
		formData.add(new BasicNameValuePair("jDateType", ""));
		formData.add(new BasicNameValuePair("jDateTD", ""));
		formData.add(new BasicNameValuePair("jDateYS", ""));
		formData.add(new BasicNameValuePair("jDateTM", ""));
		formData.add(new BasicNameValuePair("trainNo", trainNumber));
		formData.add(new BasicNameValuePair("jStation", ""));
		String htmlData = fetchData(trainInfoUrl, formData, refererUrl);
		
		if(htmlData != null)
		{
			trainInfo = HtmlParser.extractTrainDetails(htmlData);
		}		
		return trainInfo;
	}

	/**
	 * 
	 * @param trainNumber
	 * @param startDate
	 * @param stationName is in the format ((station_code) station_name )
	 * @return StationInfo object (which holds the last station details)
	 */
	public StationInfo getLastStationLocation(String trainNumber, String startDate,
				String stationName) throws CaptchaNotValidException 
	{
		//String lastStationUrl = LOCATION_SERVER + "t=" + trainNumber + "&s=" + startDate;
		String lastStationUrl = CURRENT_LOCATION_TRAIN_URL;
		String refererUrl = "http://enquiry.indianrail.gov.in/mntes/MntesServlet?action=TrainRunning&subAction=FindStationList";
		StationInfo stationInfo = null;
		String jDateType = "";
		String jDateTD = "";
		String jDateYS = ""; 
		String jDateTM = "";
		String trainNo = trainNumber;
		String jStation = stationName.substring((stationName.indexOf("(") + 1), stationName.lastIndexOf(")"));
		if(startDate.equalsIgnoreCase(Constants.TODAY_START_DATE))
		{
			long startTsecsMillis = System.currentTimeMillis();
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(startTsecsMillis);
			jDateType = Constants.startDateMapping.get(Constants.TODAY_START_DATE);
			jDateTD = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
			calendar.setTimeInMillis(startTsecsMillis - (86400 * 1000));
			jDateYS = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
			calendar.setTimeInMillis(startTsecsMillis + (86400 * 1000));
			jDateTM = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
		}
		else if(startDate.equalsIgnoreCase(Constants.YESTERDAY_START_DATE))
		{
			long startTsecsMillis = System.currentTimeMillis() - (86400 * 1000);
			Calendar calendar = Calendar.getInstance();
			jDateType = Constants.startDateMapping.get(Constants.YESTERDAY_START_DATE);
			jDateTD = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
			calendar.setTimeInMillis(startTsecsMillis - (86400 * 1000));
			jDateYS = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
			calendar.setTimeInMillis(startTsecsMillis + (86400 * 1000));
			jDateTM = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
		}
		else if(startDate.equalsIgnoreCase(Constants.TOMORROW_START_DATE))
		{
			long startTsecsMillis = System.currentTimeMillis() + (86400 * 1000);
			Calendar calendar = Calendar.getInstance();
			jDateType = Constants.startDateMapping.get(Constants.TOMORROW_START_DATE);
			jDateTD = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
			calendar.setTimeInMillis(startTsecsMillis - (86400 * 1000));
			jDateYS = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
			calendar.setTimeInMillis(startTsecsMillis + (86400 * 1000));
			jDateTM = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
		}
		
		ArrayList<NameValuePair> formData = new ArrayList<NameValuePair>();
		formData.add(new BasicNameValuePair(Constants.JDATETYPE, jDateType));
		formData.add(new BasicNameValuePair(Constants.JDATETD, jDateTD));
		formData.add(new BasicNameValuePair(Constants.JDATETM, jDateTM));
		formData.add(new BasicNameValuePair(Constants.JDATEYS, jDateYS));
		formData.add(new BasicNameValuePair(Constants.JSTATION, jStation + "#0"));
		formData.add(new BasicNameValuePair(Constants.TRAINNO, trainNo));
		
		String htmlData = fetchData(lastStationUrl, formData, refererUrl);
		if(htmlData != null)
		{
			stationInfo = HtmlParser.extractLocationDetails(htmlData);
		}
		
		return stationInfo;
	}

	/**
	 * 
	 * @param trainNumber
	 * @param startDate
	 * @param stationName
	 * @param delayInMinutesLocation
	 * @return
	 */
	public String getEstimatedTimeOfArrival(String trainNumber, String startDate, String stationName, int delayInMinutesLocation) 
	{
		String scheduleInfoUrl = SEARCH_SERVICE_HOST + "te/schedule/" + trainNumber + "/" + startDate + ".json";
		
		String stationCode = stationName.substring((stationName.indexOf("(") + 1), stationName.lastIndexOf(")"));
		String etaInfoUrl = COA_SERVICE_HOST + "train/location/eta.json?callback=jQuery16405237756767310202_" + System.currentTimeMillis() + "&t=" + 
							trainNumber + "&s=" + startDate + "&codes=" + stationCode + "&_" + System.currentTimeMillis();										
		//String etaInfoUrl = COA_SERVICE_HOST + "train/location/eta.json?t=" + trainNumber + "&s=" + startDate + "&codes=" + stationCode;
		
		String standardTimeOfArrival = "";
		String delayInArrival = "";
		String estimatedTimeOfArrival = null;
		
		String scheduleInfo = fetchData(scheduleInfoUrl, null, null);
		String etaInfo = fetchData(etaInfoUrl, null, null);
		try 
		{
			if((scheduleInfo != null) && (etaInfo != null)) 
			{
				etaInfo = etaInfo.substring((etaInfo.indexOf("(") + 1), etaInfo.lastIndexOf(")"));
				delayInArrival = (new JSONArray(etaInfo)).getJSONObject(0).getString("delay_in_arrival");
				JSONArray scheduleInfoArray = new JSONArray(scheduleInfo);
				for(int i = 0; i < scheduleInfoArray.length(); i ++) 
				{
					JSONObject schedInfoForStation = scheduleInfoArray.getJSONObject(i);
					if(stationCode.equalsIgnoreCase(schedInfoForStation.getString("station_code"))) 
					{
						standardTimeOfArrival = schedInfoForStation.getString("sta");
						break;
					}
				}
				
				String temp = delayInArrival;
				temp = temp.replaceAll("-", "");
				if
				(delayInMinutesLocation > Integer.parseInt(delayInArrival)) 
				{
					estimatedTimeOfArrival = calculateETA(standardTimeOfArrival, Integer.valueOf(delayInMinutesLocation).toString());
				}
				else 
				{
					estimatedTimeOfArrival = calculateETA(standardTimeOfArrival, delayInArrival);
				}
			}
		} 
		catch (JSONException jsonException) 
		{
			Log.e(TrainStatusCollector.class.toString(), jsonException.getMessage());
		} 
		catch (ParseException parseException) 
		{
			Log.e(TrainStatusCollector.class.toString(), parseException.getMessage());
		}
		
		return estimatedTimeOfArrival;
	}
	
	public String getLatestRunningDate(int trainNumber)
	{
		String latestRunningDate = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		
		StringBuilder latestRunningDateUrlBuilder = new StringBuilder();
		latestRunningDateUrlBuilder.append(COA_SERVICE_HOST);
		latestRunningDateUrlBuilder.append("train/last-station-test.json?");
		latestRunningDateUrlBuilder.append("keys[]=").append(trainNumber).append("_").append(dateFormat.format(new Date(calendar.getTimeInMillis())));
		calendar.add(Calendar.DATE,	-1);
		latestRunningDateUrlBuilder.append("&keys[]=").append(trainNumber).append("_").append(dateFormat.format(new Date(calendar.getTimeInMillis())));
		calendar.add(Calendar.DATE,	-1);
		latestRunningDateUrlBuilder.append("&keys[]=").append(trainNumber).append("_").append(dateFormat.format(new Date(calendar.getTimeInMillis())));
		calendar.add(Calendar.DATE,	-1);
		latestRunningDateUrlBuilder.append("&keys[]=").append(trainNumber).append("_").append(dateFormat.format(new Date(calendar.getTimeInMillis())));
		latestRunningDateUrlBuilder.append("&callback=jQuery16405144044277258217_").append(System.currentTimeMillis());
		latestRunningDateUrlBuilder.append("&_=").append(System.currentTimeMillis());
		
		String runningDateInfo = fetchData(latestRunningDateUrlBuilder.toString(), null, null);
		if(runningDateInfo != null)
		{
			runningDateInfo = runningDateInfo.substring(runningDateInfo.indexOf("["), (runningDateInfo.indexOf("]") + 1));
			try
			{
				JSONArray runningDateJS = new JSONArray(runningDateInfo);
				for(int i = 0; i < runningDateJS.length(); i++)
				{
					JSONObject runningdatejson = runningDateJS.getJSONObject(i);
					if (!(runningdatejson.get("station_code").toString().equalsIgnoreCase("null")) && 
							!(runningdatejson.get("eta_station_code").toString().equalsIgnoreCase("null")))
					{
						latestRunningDate = runningdatejson.getString("key").split("_")[1];
						break;
					}
				}
			}
			catch(JSONException ex)
			{
				Log.e(TrainStatusCollector.class.getName(), ex.getMessage());
			}
		}
		return latestRunningDate;
	}
	
	private String fetchData(String url, ArrayList<NameValuePair> formData, String refererUrl) 
	{
		StringBuilder data = new StringBuilder();
		boolean isSuccess = false;
		HttpClient stageClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		//HttpGet httpGet = new HttpGet(url);
		IndianRailEnquiryCookies indianRailCookies = IndianRailEnquiryCookies.getInstance();
		if(indianRailCookies != null) 
		{
			((DefaultHttpClient)stageClient).setCookieSpecs(indianRailCookies.getCookieSpecRegistry());
			((DefaultHttpClient)stageClient).setCookieStore(indianRailCookies.getCookieStore());
		}
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		String captchaId = sharedPreferences.getString("captchaID", "");
		
		httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		//httpPost.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httpPost.setHeader("Accept-Language", "en-US,en;q=0.8");
		httpPost.setHeader("Cache-Control", "max-age=0");
		httpPost.setHeader("Connection","keep-alive");
		httpPost.setHeader("Cookie", "captchaId=" + captchaId /*p0cid46ywg1378834952543 + indianRailCookies.getCaptchaId()*/);
		httpPost.setHeader("Referer", refererUrl);
		httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36");
		
		/*
		httpGet.setHeader("Accept", "application/json");
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.97 Safari/537.22");
		httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		*/
		
		try
		{
			httpPost.setEntity(new UrlEncodedFormEntity(formData)); 
			HttpResponse response = stageClient.execute(httpPost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if(statusCode == 200)
			{
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = bufferedReader.readLine()) != null)
				{
					data.append(line);
					isSuccess = true;
				}				

			}
			else
			{
				Log.e(tag, "Failed to access: " + url + ". status code = " + statusCode);
				isSuccess = false;
			}
		}
		catch (ClientProtocolException clientProtocolException)
		{
			clientProtocolException.printStackTrace();
			Log.e(tag, clientProtocolException.getMessage());
			isSuccess = false;
		}
		catch (IOException ioException)
		{
			ioException.printStackTrace();
			Log.e(tag, ioException.getMessage());
			isSuccess = false;
		}

		if(isSuccess)
		{
			return data.toString();
		}
		else
		{
			return null;
		}


	}

	private String calculateETA(String standardTimeOfArrival, String delayInMinutes) throws ParseException 
	{
		SimpleDateFormat sourceDateFormat = new SimpleDateFormat(DATE_FORMAT_BY_RAILWAYS_STA);
		SimpleDateFormat destinationDateFormat = new SimpleDateFormat(DATE_FORMAT_BY_ME);
		SimpleDateFormat delayDateFormat = new SimpleDateFormat(DATE_FORMAT_BY_RAILWAY_DELAY);
		boolean bTrainIsComingSoonerThanExpected = false;
		Date staDate  = sourceDateFormat.parse(standardTimeOfArrival);
		if(delayInMinutes.contains("-")) 
		{
			delayInMinutes = delayInMinutes.replaceAll("-", "");
			bTrainIsComingSoonerThanExpected = true;
		}
		Date delayDate = delayDateFormat.parse(delayInMinutes);
		
		Calendar staCalendar = Calendar.getInstance();
		staCalendar.setTime(staDate);
		
		Calendar delayCalendar = Calendar.getInstance();
		delayCalendar.setTime(delayDate);
		
		Calendar etaCalendar = (Calendar) staCalendar.clone();
		if(bTrainIsComingSoonerThanExpected) 
		{
			etaCalendar.add(Calendar.MINUTE, -delayCalendar.get(Calendar.MINUTE));
			etaCalendar.add(Calendar.HOUR, -delayCalendar.get(Calendar.HOUR));
		}
		else 
		{
			etaCalendar.add(Calendar.MINUTE, delayCalendar.get(Calendar.MINUTE));
			etaCalendar.add(Calendar.HOUR, delayCalendar.get(Calendar.HOUR));
		}
		
		return destinationDateFormat.format(etaCalendar.getTime());
	}	
}





