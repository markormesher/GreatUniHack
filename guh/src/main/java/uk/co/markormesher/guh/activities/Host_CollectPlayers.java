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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;
import uk.co.markormesher.guh.gcm_utils.GCMUtils;

import java.util.HashMap;
import java.util.Map;

public class Host_CollectPlayers extends ActionBarActivity {

	private String gameId;
	private int playerCount = 0;
	BroadcastReceiver playerAdded = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			++playerCount;
			((TextView) findViewById(R.id.players_so_far)).setText(getString(R.string.players_so_far, playerCount));
			findViewById(R.id.start_game_button).setEnabled(true); // TODO playerCount >= 6);
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
		Log.d("WEREWOLF", "GCM ID: " + gcmId);

		// generate a game ID
		final RequestQueue requestQueue = Volley.newRequestQueue(this);
		final Request gameIdRequest = new JsonObjectRequest(
				Request.Method.POST,
				"http://178.62.96.146/games.json",
				"{\"game\":{\"host_gcm_id\":\"" + gcmId + "\"}}",
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							gameId = response.getString("id");
						} catch (JSONException e) {
							Toast.makeText(Host_CollectPlayers.this, "Failed to start game.", Toast.LENGTH_LONG).show();
							Host_CollectPlayers.this.finish();
							return;
						}

						// swap views around
						findViewById(R.id.generating_game_loading).setVisibility(View.GONE);
						findViewById(R.id.qr_code_display).setVisibility(View.VISIBLE);

						// set texts
						((TextView) findViewById(R.id.game_id_output)).setText("Game ID: " + gameId);
						((TextView) findViewById(R.id.players_so_far)).setText(getString(R.string.players_so_far, playerCount));
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
				// loading icon
				((LinearLayout) findViewById(R.id.generating_game_loading)).getChildAt(0).setVisibility(View.GONE);
				findViewById(R.id.generating_game_loading).setVisibility(View.VISIBLE);

				// send start notification to server
				StringRequest startRequest = new StringRequest(
						Request.Method.GET,
						"http://178.62.96.146/games/" + gameId + "/start",
						new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								// move to next activity
								Intent openSetRoles = new Intent(Host_CollectPlayers.this, Host_SetRoles.class);
								openSetRoles.putExtra("game_id", gameId);
								openSetRoles.putExtra("player_count", playerCount);
								startActivity(openSetRoles);
								Host_CollectPlayers.this.finish();
							}
						},
						new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								Toast.makeText(Host_CollectPlayers.this, "Unable to start game.", Toast.LENGTH_LONG).show();
								Host_CollectPlayers.this.finish();
							}
						}
				);
				requestQueue.add(startRequest);
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