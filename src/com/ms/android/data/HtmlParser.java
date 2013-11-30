/**
 * 
 */
package com.ms.android.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.ms.android.exceptions.CaptchaNotValidException;

import android.util.Log;

/**
 * @author Manoj Srivatsav
 *
 * Sep 9, 2013
 */
public class HtmlParser 
{
	private static final String tag = HtmlParser.class.getName();
	
	private static final String CAPTCHA_ERROR_MESSAGE = "Captcha not validated";
	
	/**
	 * This extracts out the train name and the list of stations from the HTML string parsed.
	 * @param html
	 * @return an object of type TrainInfo
	 */
	public static TrainInfo extractTrainDetails(String html) throws CaptchaNotValidException
	{
		TrainInfo trainInfo = new TrainInfo();
		List<String> listOfStations = new ArrayList<String>();
		List<String> listOfStartDates = new ArrayList<String>();
		if(html.contains(CAPTCHA_ERROR_MESSAGE))
		{
			throw new CaptchaNotValidException(CAPTCHA_ERROR_MESSAGE);
		}		
		Document document = Jsoup.parse(html);
		Elements tableElements = document.select("table");
		Elements spanElements = tableElements.select("span");
		try
		{
			if(spanElements != null)
			{
				trainInfo.setExtendedTrainName(spanElements.get(0).childNode(0).toString());
				System.out.println("TRAIN NAME : " + spanElements.get(0).childNode(0));
			}

			Elements tableRowElements = tableElements.select("font");

			if(tableRowElements != null)
			{
				for(int i = 10; i < tableRowElements.size(); i=i+6)
				{
					String stationName = tableRowElements.get(i).childNode(0).toString();
					System.out.println("STATION NAME: " + tableRowElements.get(i).childNode(0));
					i++;
					String stationCode = tableRowElements.get(i).childNode(0).toString();
					System.out.println("STATION CODE: " + tableRowElements.get(i).childNode(0));
					listOfStations.add("(" + stationCode + ") " + stationName );			
				}
				trainInfo.setListOfStations(listOfStations);
				listOfStartDates.add("YESTERDAY");
				listOfStartDates.add("TODAY");
				listOfStartDates.add("TOMORROW");

				trainInfo.setStartDates(listOfStartDates);
				trainInfo.setValidTrain(true);
			}
		}
		catch(Exception ex)
		{
			Log.e(tag, "Obtained html string: " + html + ": Exception: " + ex.getMessage());
		}
		return trainInfo;
	}

	/**
	 * This extracts the current location, delay, Actual Arrival, scheduled arrival and etc from the HTML string.
	 * @param html
	 * @return
	 */
	public static StationInfo extractLocationDetails(String html) throws CaptchaNotValidException
	{
		StationInfo stationInfo = null;
		if(html.contains(CAPTCHA_ERROR_MESSAGE))
		{
			throw new CaptchaNotValidException(CAPTCHA_ERROR_MESSAGE);
		}
		html = html.replaceAll("&nbsp;", "");
		Document locationDocument = Jsoup.parse(html);
		Element runningTableElements = locationDocument.getElementById("tableRun");
		Elements tableRowElements = runningTableElements.select("td");
		Elements fontElements = locationDocument.select("font");
		if((tableRowElements != null) && (fontElements != null))
		{
			try
			{
				stationInfo = new StationInfo();
				stationInfo.setTrainName(Jsoup.parseBodyFragment(tableRowElements.get(1).toString()).text());
				stationInfo.setStationName(Jsoup.parseBodyFragment(tableRowElements.get(3).toString()).text());
				stationInfo.setArrivalDate(Jsoup.parseBodyFragment(tableRowElements.get(5).toString()).text());
				stationInfo.setScheduledArrival(Jsoup.parseBodyFragment(tableRowElements.get(7).toString()).text());
				stationInfo.setActualArrival(Jsoup.parseBodyFragment(tableRowElements.get(9).toString()).text());
				stationInfo.setDelayInArrival(Jsoup.parseBodyFragment(tableRowElements.get(11).toString()).text());
				stationInfo.setScheduledDeparture(Jsoup.parseBodyFragment(tableRowElements.get(13).toString()).text());
				stationInfo.setActualDeparture(Jsoup.parseBodyFragment(tableRowElements.get(15).toString()).text());
				stationInfo.setDelayInDeparture(Jsoup.parseBodyFragment(tableRowElements.get(17).toString()).text());
				stationInfo.setLastLocation(Jsoup.parseBodyFragment(tableRowElements.get(tableRowElements.size() - 1).toString()).text());
				stationInfo.setLastUpdatedTime(Jsoup.parseBodyFragment(fontElements.get(fontElements.size() - 3).toString()).text());
				
			}
			catch(Exception ex)
			{
				Log.e(tag, ex.getMessage());
			}
		}
		
		return stationInfo;		
	}
	
	/**
	 * Extracts the hidden name value pair from the html page. This is used when verifying captcha.
	 * @param html
	 * @return
	 */
	public static BasicNameValuePair extractHiddenData(String html)
	{
		Document doc = Jsoup.parse(html);
		Elements inputElements = doc.select("input");
		String name = inputElements.get(0).attr("name");
		String value = inputElements.get(0).attr("value");
		System.out.println("Hidden name value pair: " + name + "," + value);
		return new BasicNameValuePair(name, value);
	}
}
