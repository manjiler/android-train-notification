/**
 * 
 */
package com.ms.android.trainnotification;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Manoj Srivatsav
 *
 * Sep 11, 2013
 */
public class SettingsActivity extends Activity 
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
	}
}
