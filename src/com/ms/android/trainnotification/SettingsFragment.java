/**
 * 
 */
package com.ms.android.trainnotification;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author Manoj Srivatsav
 *
 * Sep 11, 2013
 */
public class SettingsFragment extends PreferenceFragment 
{
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
	}
}
