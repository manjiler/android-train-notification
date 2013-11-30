package com.ms.android.data;

import java.text.SimpleDateFormat;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class TrainDO implements Parcelable
{

	private int mTrainNumber;
	private String mTrainName;
	private List<String> mStationCodes;
	private String mUpdateType;
	private long mStartTsecs;
	private long mEndTsecs;
	private String mRepitition;
	private long mDateInMillis;
	private String mFrequency;
	private int mStatusBarNofiy;
	private int mSmsNotify;
	private int mEmailNotify;
	
	public TrainDO()
	{
		
	}
	
	public TrainDO(Parcel parcel)
	{
		readFromParcel(parcel);
	}
	
	public int getTrainNumber() {
		return mTrainNumber;
	}
	public void setTrainNumber(int mTrainNumber) {
		this.mTrainNumber = mTrainNumber;
	}
	public String getTrainName() {
		return mTrainName;
	}
	public void setTrainName(String mTrainName) {
		this.mTrainName = mTrainName;
	}
	public List<String> getStationCodes() {
		return mStationCodes;
	}
	public void setStationCodes(List<String> mStationCodes) {
		
		this.mStationCodes = mStationCodes;
	}
	public int getStatusBarNofiy() {
		return mStatusBarNofiy;
	}
	public void setStatusBarNofiy(int mStatusBarNofiy) {
		this.mStatusBarNofiy = mStatusBarNofiy;
	}
	public int getSmsNotify() {
		return mSmsNotify;
	}
	public void setSmsNotify(int mSmsNotify) {
		this.mSmsNotify = mSmsNotify;
	}
	public int getEmailNotify() {
		return mEmailNotify;
	}
	public void setEmailNotify(int mEmailNotify) {
		this.mEmailNotify = mEmailNotify;
	}
	
	public void addStationCode(String stationCode) {
		this.mStationCodes.add(stationCode);
	}
	public String getUpdateType() {
		return mUpdateType;
	}
	public void setUpdateType(String mUpdateType) {
		this.mUpdateType = mUpdateType;
	}
	public long getStartTsecs() {
		return mStartTsecs;
	}
	public void setStartTsecs(long mStartTsecs) {
		this.mStartTsecs = mStartTsecs;
	}
	public long getEndTsecs() {
		return mEndTsecs;
	}
	public void setEndTsecs(long mEndTsecs) {
		this.mEndTsecs = mEndTsecs;
	}
	public String getRepitition() {
		return mRepitition;
	}
	public void setRepitition(String mRepitition) {
		this.mRepitition = mRepitition;
	}
	public String getFrequency() {
		return mFrequency;
	}
	public void setFrequency(String mFrequency) {
		this.mFrequency = mFrequency;
	}
	public long getDateInMilli() {
		return mDateInMillis;
	}
	public void setDateInMilli(long mDateInMillis) {
		this.mDateInMillis = mDateInMillis;
	}
	
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("TrainDO [mTrainName=" + mTrainName);
		stringBuilder.append(", mTrainNumber=" + mTrainNumber);
		stringBuilder.append(", mUpdateType=" + mUpdateType);
		stringBuilder.append(", mRepetition=" + mRepitition);
		stringBuilder.append(", mStartTsecs=" + mStartTsecs);
		stringBuilder.append(", mEndTsecs=" + mEndTsecs + "]");
		
		return stringBuilder.toString();		
	}
	
	
	public static final Parcelable.Creator<TrainDO> CREATOR = new Parcelable.Creator<TrainDO>() {
		
		public TrainDO[] newArray(int size) 
		{
			return new TrainDO[size];
		}
		
		public TrainDO createFromParcel(Parcel source) 
		{
			return new TrainDO(source);
		}
	
	};
	
	public int describeContents() 
	{
		return 0;
	}
	
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeInt(mTrainNumber);
		dest.writeString(mTrainName);
		dest.writeStringList(mStationCodes);
		dest.writeString(mUpdateType);
		dest.writeLong(mStartTsecs);
		dest.writeLong(mEndTsecs);
		dest.writeString(mRepitition);
		dest.writeLong(mDateInMillis);
		dest.writeString(mFrequency);
		dest.writeInt(mStatusBarNofiy);
		dest.writeInt(mSmsNotify);
		dest.writeInt(mEmailNotify);
	}
	
	public void readFromParcel(Parcel src)
	{
		mTrainNumber = src.readInt();
		mTrainName = src.readString();
		mStationCodes = (List<String>)src.readArrayList(String.class.getClassLoader());
		mUpdateType = src.readString();
		mStartTsecs = src.readLong();
		mEndTsecs = src.readLong();
		mRepitition = src.readString();
		mDateInMillis = src.readLong();
		mFrequency = src.readString();
		mStatusBarNofiy = src.readInt();
		mSmsNotify = src.readInt();
		mEmailNotify = src.readInt();
		
	}
	
}
