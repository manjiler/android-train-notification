/**
 * 
 */
package com.ms.android.test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Manoj Srivatsav
 *
 * Aug 21, 2013
 */
public class TestParcable implements Parcelable 
{
	
	private int trainSomeShit;
	private String trainAnotherShit;
	
	public TestParcable(int ashit, String bshit)
	{
		this.trainSomeShit = ashit;
		this.trainAnotherShit = bshit;
	}	
	
	public TestParcable(Parcel in)
	{
		readFromParcel(in);
	}
	
	public int getTrainSomeShit()
	{
		return this.trainSomeShit;
	}
	
	public String getTrainAnotherShit()
	{
		return this.trainAnotherShit;
	}
	
	public void setTrainSomeShit(int someshit)
	{
		this.trainSomeShit = someshit;
	}
	
	public void setTrainAnotherShit(String anotherShit)
	{
		this.trainAnotherShit = anotherShit;
	}
	
	public static final Parcelable.Creator<TestParcable> CREATOR = new Parcelable.Creator<TestParcable>() {

		public TestParcable createFromParcel(Parcel source) {
			return new TestParcable(source);
		}

		public TestParcable[] newArray(int size) {
			return new TestParcable[size];
		}
		
		
	};

	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	public int describeContents() 
	{		
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	public void writeToParcel(Parcel dest, int flag) 
	{
		dest.writeInt(trainSomeShit);
		dest.writeString(trainAnotherShit);
	}
	
	private void readFromParcel(Parcel in)
	{
		this.trainSomeShit = in.readInt();
		this.trainAnotherShit = in.readString();
	}

}
