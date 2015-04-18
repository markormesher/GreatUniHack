package uk.co.markormesher.guh.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;
import uk.co.markormesher.guh.gcm_utils.GCMUtils;

import java.util.HashMap;
import java.util.Map;

public class Play_JoinGame extends ActionBarActivity {

	private String gameId;
	private String playerId;
	BroadcastReceiver gameStarted = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Intent openGame = new Intent(context, Play_Game.class);
			openGame.putExtra("game_id", gameId);
			openGame.putExtra("player_id", playerId);
			startActivity(openGame);
			Play_JoinGame.this.finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set layout
		setContentView(R.layout.activity_play_join_game);
		setTitle(R.string.activity_title_join_game);

		// get the GCM ID
		final String gcmId = GCMUtils.getRegistrationID(this);
		Log.d("WEREWOLF", "GCM ID: " + gcmId);

		// button listener
		findViewById(R.id.join_game_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get game ID
				gameId = ((EditText) findViewById(R.id.game_id_input)).getText().toString();

				// show loading view
				findViewById(R.id.game_id_input).setEnabled(false);
				findViewById(R.id.join_game_button).setVisibility(View.GONE);
				findViewById(R.id.join_game_loader).setVisibility(View.VISIBLE);

				// send to network
				RequestQueue requestQueue = Volley.newRequestQueue(Play_JoinGame.this);
				Request gameIdRequest = new StringRequest(
						Request.Method.GET,
						//"http://178.62.96.146/api/game.json",
						"http://point.markormesher.co.uk/api",
						new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								// TODO get from the real backend
								response = "{\"player_id\":\"456\"}";

								try {
									JSONObject jsonResponse = new JSONObject(response);
									playerId = jsonResponse.getString("player_id");
								} catch (JSONException e) {
									Toast.makeText(Play_JoinGame.this, "Failed to join game.", Toast.LENGTH_LONG).show();
									Play_JoinGame.this.finish();
									return;
								}

								// swap layouts
								findViewById(R.id.join_game_loading).setVisibility(View.GONE);
								findViewById(R.id.join_game_done).setVisibility(View.VISIBLE);
							}
						},
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Toast.makeText(Play_JoinGame.this, "Failed to join game.", Toast.LENGTH_LONG).show();
								Play_JoinGame.this.finish();
							}
						}
				) {
					@Override
					protected Map<String, String> getParams() throws AuthFailureError {
						return new HashMap<String, String>() {{
							put("game_id", gameId);
							put("gcm_id", gcmId);
						}};
					}

					@Override
					public Map<String, String> getHeaders() throws AuthFailureError {
						return new HashMap<String, String>() {{
							put("Content-Type", "application/json");
						}};
					}
				};
				requestQueue.add(gameIdRequest);
				requestQueue.start();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter(Keys.INTENT_GAME_STARTED);
		LocalBroadcastManager.getInstance(this).registerReceiver(gameStarted, iff);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(gameStarted);
	}

}
