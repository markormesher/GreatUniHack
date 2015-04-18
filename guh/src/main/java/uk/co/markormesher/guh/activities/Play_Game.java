package uk.co.markormesher.guh.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;

public class Play_Game extends ActionBarActivity {

	private String gameId;
	private String playerId;
	private String role;

	BroadcastReceiver roleAssigned = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			role = intent.getExtras().getString("role");
			Toast.makeText(Play_Game.this, "Great, you're a " + role + "!", Toast.LENGTH_LONG).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set layout
		setContentView(R.layout.activity_play_game);
		setTitle(R.string.activity_title_game);

		// get game ID and player ID
		Bundle extras = getIntent().getExtras();
		gameId = extras.getString("game_id");
		playerId = extras.getString("player_id");
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter(Keys.INTENT_ROLE_ASSIGNED);
		LocalBroadcastManager.getInstance(this).registerReceiver(roleAssigned, iff);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(roleAssigned);
	}

}
