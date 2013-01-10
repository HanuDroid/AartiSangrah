package org.varunverma.aartisangrah;


import org.varunverma.CommandExecuter.ResultObject;
import org.varunverma.hanu.Application.HanuGCMIntentService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class GCMIntentService extends HanuGCMIntentService {
	
	@Override
	protected void onMessage(Context context, Intent intent) {
		
		String message = intent.getExtras().getString("message");
		if(message.contentEquals("InfoMessage")){
			// Show Info Message to the User
			showInfoMessage(intent);
		}
		else{
			
			ResultObject result = processMessage(context,intent);
			
			if(result.isCommandExecutionSuccess() && result.getResultCode() == 200){
				createNotification();
			}
		}
	}

	private void showInfoMessage(Intent intent) {
		// Show Info Message
		String subject = intent.getExtras().getString("subject");
		String content = intent.getExtras().getString("content");
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = subject;
		notification.when = System.currentTimeMillis();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		// Create Intent and Set Extras
		Intent notificationIntent = new Intent(this, DisplayFile.class);
		
		notificationIntent.putExtra("Title", "Info:");
		notificationIntent.putExtra("Subject", subject);
		notificationIntent.putExtra("Content", content);		
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, subject, content, contentIntent);
				
		nm.notify(2, notification);
		
	}

	private void createNotification() {
		// Create Notification
		
		String message = "New aartis have been downloaded";
		
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = message;
		notification.when = System.currentTimeMillis();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
			
		Intent notificationIntent = new Intent(this, Main.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(this, message, message, contentIntent);
				
		nm.notify(1, notification);
				
	}
	
}