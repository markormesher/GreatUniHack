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
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;
import uk.co.markormesher.guh.gcm_utils.GCMUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Host_CollectPlayers extends ActionBarActivity {

	final ArrayList<String> players = new ArrayList<>();
	BroadcastReceiver playerAdded = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			players.add(intent.getExtras().getString("player_id"));
			((TextView) findViewById(R.id.players_so_far)).setText(getString(R.string.players_so_far, players.size()));
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set layout
		setContentView(R.layout.activity_host_collect_players);
		setTitle(R.string.activity_title_collect_players);

		// get the GCM ID
		final String gcmId = GCMUtils.getRegistrationID(this);
		Log.d("WEREWOLF", "GCMID: " + gcmId);

		// generate a game ID
		RequestQueue requestQueue = Volley.newRequestQueue(this);
		Request gameIdRequest = new StringRequest(
				Request.Method.GET,
				//"http://178.62.96.146/api/game.json",
				"http://point.markormesher.co.uk/api",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						// TODO get from the real backend
						String gameId;
						try {
							JSONObject jsonResponse = new JSONObject(response);
							gameId = jsonResponse.getString("latestApiVersion");
						} catch (JSONException e) {
							Toast.makeText(Host_CollectPlayers.this, "Failed to start game.", Toast.LENGTH_LONG).show();
							Host_CollectPlayers.this.finish();
							return;
						}

						// swap views around
						findViewById(R.id.generating_game_loading).setVisibility(View.GONE);
						findViewById(R.id.qr_code_display).setVisibility(View.VISIBLE);

						// set texts
						((TextView) findViewById(R.id.qr_code_output)).setText("Game ID: " + gameId); // TODO make a QR code
						((TextView) findViewById(R.id.players_so_far)).setText(getString(R.string.players_so_far, players.size()));
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(Host_CollectPlayers.this, "Failed to start game.", Toast.LENGTH_LONG).show();
						Host_CollectPlayers.this.finish();
					}
				}
		) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return new HashMap<String, String>() {{
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

		// click listener on button
		findViewById(R.id.start_game_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO go to next activity
				Toast.makeText(Host_CollectPlayers.this, "Players: " + players.toString(), Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter(Keys.INTENT_PLAYER_JOINED);
		LocalBroadcastManager.getInstance(this).registerReceiver(playerAdded, iff);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(playerAdded);
	}
}