/**
 * 
 */
package com.ms.android.data;

import java.util.List;

/**
 * @author Manoj Srivatsav
 * @description Contains Train Name, List of station where the train stops and list of dates where the train runs.
 */
public class TrainInfo 
{
	private String m_ExtendedTrainName;
	private List<String> m_ListOfStations;
	private List<String> m_StartDates;
	private boolean mValidTrain;
	
	public String getExtendedTrainName() 
	{
		return m_ExtendedTrainName;
	}
	
	public void setExtendedTrainName(String m_ExtendedTrainName) 
	{
		this.m_ExtendedTrainName = m_ExtendedTrainName;
	}
	
	public List<String> getListOfStations() 
	{
		return m_ListOfStations;
	}
	
	public void setListOfStations(List<String> m_ListOfStations) 
	{
		this.m_ListOfStations = m_ListOfStations;
	}
	
	public List<String> getStartDates() 
	{
		return m_StartDates;
	}
	
	public void setStartDates(List<String> m_StartDates) 
	{
		this.m_StartDates = m_StartDates;
	}
	
	public void setValidTrain(boolean valid) {
		this.mValidTrain = valid;
	}
	
	public boolean getValidTrain() {
		return this.mValidTrain;
	}
	
	
}
