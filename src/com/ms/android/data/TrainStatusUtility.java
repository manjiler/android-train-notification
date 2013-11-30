/**
 * 
 */
package com.ms.android.data;

/**
 * @author Manoj Srivatsav
 *
 * Aug 25, 2013
 */
public class TrainStatusUtility 
{
	/*
	public static String convertCurrentLocationToString(StationInfo lastStationInfo)
	{
		String currentLocation = null;		
		if(lastStationInfo.getStatusCode().equalsIgnoreCase("reached")) 
		{
			currentLocation = lastStationInfo.getRunningStatus();
		} 
		else if(lastStationInfo.getStatusCode().equalsIgnoreCase("not_started")) 
		{
			currentLocation = lastStationInfo.getRunningStatus();
		}
		else 
		{
			String postfix = "";
			if(lastStationInfo.getDelayInMinutes() > 0) 
			{
				postfix += " Delayed by " + lastStationInfo.getDelayInMinutes() + " minutes.";
			}
			else if(lastStationInfo.getDelayInMinutes() == 0) 
			{
				postfix += " On time.";
			}
			else 
			{
				postfix += " Coming earlier by " + lastStationInfo.getDelayInMinutes() + " minutes.";
			}
			currentLocation = "Departed station (" + lastStationInfo.getStationCode() + ") " + 
					lastStationInfo.getStationName() + " at " + lastStationInfo.getDepartedTime() + "." +
					postfix + "\n";
		}
		return currentLocation;
	}
	*/
}
