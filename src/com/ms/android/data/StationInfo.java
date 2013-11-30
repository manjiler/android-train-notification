package com.ms.android.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Manoj Srivatsav
 *
 * Mar 7, 2013
 */


public class StationInfo 
{
	private String trainName;
	private String stationName;
	private String arrivalDate;
	private String scheduledArrival;
	private String actualArrival;
	private String delayInArrival;
	private String scheduledDeparture;
	private String actualDeparture;
	private String delayInDeparture;
	private String lastLocation;
	private String lastUpdatedTime;
	
	public String getTrainName() 
	{
		return trainName;
	}
	
	public void setTrainName(String trainName) 
	{
		this.trainName = trainName;
	}
	
	public String getStationName() 
	{
		return stationName;
	}
	
	public void setStationName(String stationName) 
	{
		this.stationName = stationName;
	}
	
	public String getArrivalDate() 
	{
		return arrivalDate;
	}
	
	public void setArrivalDate(String arrivalDate) 
	{
		this.arrivalDate = arrivalDate;
	}
	
	public String getScheduledArrival() 
	{
		return scheduledArrival;
	}
	
	public void setScheduledArrival(String scheduledArrival) 
	{
		this.scheduledArrival = scheduledArrival;
	}
	
	public String getActualArrival() 
	{
		return actualArrival;
	}
	
	public void setActualArrival(String actualArrival) 
	{
		this.actualArrival = actualArrival;
	}
	
	public String getDelayInArrival() 
	{
		return delayInArrival;
	}
	
	public void setDelayInArrival(String delayInArrival) 
	{
		this.delayInArrival = delayInArrival;
	}
	
	public String getScheduledDeparture() 
	{
		return scheduledDeparture;
	}
	
	public void setScheduledDeparture(String scheduledDeparture) 
	{
		this.scheduledDeparture = scheduledDeparture;
	}
	
	public String getActualDeparture() 
	{
		return actualDeparture;
	}
	
	public void setActualDeparture(String actualDeparture) 
	{
		this.actualDeparture = actualDeparture;
	}
	
	public String getDelayInDeparture() 
	{
		return delayInDeparture;
	}
	
	public void setDelayInDeparture(String delayInDeparture) 
	{
		this.delayInDeparture = delayInDeparture;
	}
	
	public String getLastLocation() 
	{
		return lastLocation;
	}
	
	public void setLastLocation(String lastLocation) 
	{
		this.lastLocation = lastLocation;
	}
	
	public String getLastUpdatedTime()
	{
		return lastUpdatedTime;
	}
	
	public void setLastUpdatedTime(String lastUpdatedTime)
	{
		this.lastUpdatedTime = "Last " + lastUpdatedTime + ".";
	}
	
	
}
