package uk.co.markormesher.guh.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Toast;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.gcm_utils.GCMUtils;
import uk.co.markormesher.guh.utils.Utils;

import java.io.IOException;


public class LaunchScreen extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set layout
		setContentView(R.layout.activity_launch_screen);
		setTitle(R.string.activity_title_join_or_host);

		// write a device ID
		try {
			Utils.writeDeviceID(this);
		} catch (IOException e) {
			Toast.makeText(this, "Apparently your device sucks. Sorry.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// work out a GCM ID
		if (GCMUtils.checkPlayServices(this, true)) {
			GCMUtils.registerInBackground(this);
		} else {
			finish();
			return;
		}

		// button listeners
		findViewById(R.id.host_a_game_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent openPlayerCollection = new Intent(LaunchScreen.this, Host_CollectPlayers.class);
				startActivity(openPlayerCollection);
			}
		});
		findViewById(R.id.join_a_game_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent openJoinGame = new Intent(LaunchScreen.this, Play_JoinGame.class);
				startActivity(openJoinGame);
			}
		});
	}

}