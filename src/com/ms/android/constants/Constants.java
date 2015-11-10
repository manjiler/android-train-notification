package com.ms.android.constants;

import java.util.HashMap;

public class Constants 
{
	
	public static final String LOCATION_WISE_UPDATE = "location-wise";
	
	public static final String STATION_WISE_UPDATE = "station-wise";

	public static final String TODAY_START_DATE = "TODAY";
	public static final String YESTERDAY_START_DATE = "YESTERDAY";
	public static final String TOMORROW_START_DATE = "TOMORROW";
	
	public static final String JDATETYPE = "jDateType";
	public static final String JDATETD = "jDateTD";
	public static final String JDATEYS = "jDateYS";
	public static final String JDATETM = "jDateTM";
	public static final String TRAINNO = "trainNo";
	public static final String JSTATION = "jStation";

	// new form parameters as of 10th November 2015
	public static final String JDATE = "jDate";
	public static final String JDATEDAY = "jDateDay";
	
	public static HashMap<String, String> startDateMapping;
	
	static
	{
		startDateMapping = new HashMap<String, String>();
		startDateMapping.put(YESTERDAY_START_DATE, "YS");
		startDateMapping.put(TODAY_START_DATE, "TD");
		startDateMapping.put(TOMORROW_START_DATE, "TM");
	}
		
}
