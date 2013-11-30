/**
 * 
 */
package com.ms.android.notification;

import com.ms.android.trainnotification.R;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.ViewDebug.FlagToString;

/**
 * @author Manoj Srivatsav
 *
 * Aug 25, 2013
 */
public class StatusBarNotification 
{
	public static int notificationId = 0;
	public static final String tag = "Train Status Notification App";
	
	@SuppressLint("NewApi")
	public static void generateNotification(Context context, String title, String message)
	{ 
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Builder notificationBuilder = new Notification.Builder(context)
											.setContentTitle(title)
											.setContentText(message)
											.setSmallIcon(R.drawable.ic_launcher)
											.setWhen(System.currentTimeMillis());
		
		Intent shareIntent = new Intent(Intent.ACTION_SEND);	
		String extraText = title + " has " + message;
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, extraText);
		PendingIntent pendingShareIntent = PendingIntent.getActivity(context, 0, Intent.createChooser(shareIntent, "share..."),
																	PendingIntent.FLAG_UPDATE_CURRENT);
		
		notificationBuilder.addAction(android.R.drawable.ic_menu_share, "share...", pendingShareIntent);
		//notificationBuilder.setContentIntent(pendingShareIntent);
		
		Notification bigTextNotification = new Notification.BigTextStyle(notificationBuilder)
											.setBigContentTitle(title)
											.bigText(message)
											.build();
		
		notificationManager.notify(tag, notificationId++, bigTextNotification);
	}

}
