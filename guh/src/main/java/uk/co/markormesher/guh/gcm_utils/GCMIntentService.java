package uk.co.markormesher.guh.gcm_utils;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import uk.co.markormesher.guh.constants.Keys;

public class GCMIntentService extends IntentService {

	public GCMIntentService() {
		super("NotificationIntentService");
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		// get service
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

		// get info about the message
		Bundle extras = intent.getExtras();
		String messageType = gcm.getMessageType(intent);

		// handle the message
		if (extras != null && !extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				String action = extras.getString("action");
				if (action != null) {

					// a player joined
					if (action.equals("player_joined")) {
						Intent i = new Intent(Keys.INTENT_PLAYER_JOINED);
						i.putExtra("player_id", extras.getString("player_id"));
						LocalBroadcastManager.getInstance(this).sendBroadcast(i);
					}
				}
			}
		}
	}
}
