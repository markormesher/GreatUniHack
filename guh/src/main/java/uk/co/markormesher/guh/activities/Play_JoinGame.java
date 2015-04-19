package uk.co.markormesher.guh.activities;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.braintreepayments.api.dropin.Customization;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;
import uk.co.markormesher.guh.gcm_utils.GCMUtils;
import uk.co.markormesher.guh.utils.VolleySingleton;

import java.util.HashMap;
import java.util.Map;

public class Play_JoinGame extends ActionBarActivity {

	private String gameId;
	private String playerId;
	private String paymentMethodNonce = null;
	private String email = null;
	private String gcmId = null;
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
		gcmId = GCMUtils.getRegistrationID(this);

		// get a client token
		final String[] clientToken = new String[1];
		findViewById(R.id.join_game_with_will_button).setEnabled(false);
		JsonObjectRequest clientTokenRequest = new JsonObjectRequest(
				Request.Method.GET,
				"http://178.62.96.146/client_token",
				"",
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							clientToken[0] = response.getString("token");
						} catch (JSONException e) {
							clientToken[0] = null;
						}
						findViewById(R.id.join_game_with_will_button).setEnabled(true);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				}
		);
		clientTokenRequest.setRetryPolicy(VolleySingleton.RETRY_POLICY);
		VolleySingleton.getInstance().getRequestQueue().add(clientTokenRequest);

		// button listeners
		findViewById(R.id.join_game_with_will_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (clientToken[0] != null) {
					Intent intent = new Intent(Play_JoinGame.this, BraintreePaymentActivity.class);
					intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, clientToken[0]);
					Customization customization = new Customization.CustomizationBuilder()
							.primaryDescription("Will Payment")
							.amount("£1")
							.submitButtonText("Pay")
							.build();
					intent.putExtra(BraintreePaymentActivity.EXTRA_CUSTOMIZATION, customization);
					startActivityForResult(intent, 123);
				} else {
					Toast.makeText(Play_JoinGame.this, "BrainTree init failed", Toast.LENGTH_LONG).show();
				}
			}
		});
		findViewById(R.id.join_game_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doGameJoinPost();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 123) {
			switch (resultCode) {
				case BraintreePaymentActivity.RESULT_OK:
					paymentMethodNonce = data.getStringExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
					AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Theme_AppCompat_Dialog));
					builder.setTitle("We'll Need an Email Too...");
					final EditText input = new EditText(this);
					input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
					builder.setView(input);
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							email = input.getText().toString();
							doGameJoinPost();
						}
					});
					builder.show();
					break;

				default:
					Toast.makeText(Play_JoinGame.this, "Failed to make will payment.", Toast.LENGTH_LONG).show();
					Play_JoinGame.this.finish();
					break;
			}
		}
	}

	private void doGameJoinPost() {
		// get game ID
		gameId = ((EditText) findViewById(R.id.game_id_input)).getText().toString();

		// show loading view
		findViewById(R.id.game_id_input).setEnabled(false);
		findViewById(R.id.join_game_button).setVisibility(View.GONE);
		findViewById(R.id.join_game_with_will_button).setVisibility(View.GONE);
		findViewById(R.id.join_game_loader).setVisibility(View.VISIBLE);

		// send info to network
		Request gameIdRequest = new JsonObjectRequest(
				Request.Method.POST,
				(
						email == null || paymentMethodNonce == null ?
								"http://178.62.96.146/players.json" :
								"http://178.62.96.146/paidplayer.json"
				),
				(
						email == null || paymentMethodNonce == null ?
								"{\"player\":{\"player_id\":" + (int) (Math.random() * 19) + ",\"gcm_id\":\"" + gcmId + "\",\"game_id\":\"" + gameId + "\",\"photo_url\":\"null\"}}" :
								"{\"player\":{\"player_id\":" + (int) (Math.random() * 19) + ",\"gcm_id\":\"" + gcmId + "\",\"game_id\":\"" + gameId + "\",\"photo_url\":\"null\",\"email\":\"" + email + "\",\"nonce\":\"" + paymentMethodNonce + "\"}}"
				),
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							playerId = response.getString("id");
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
			public Map<String, String> getHeaders() throws AuthFailureError {
				return new HashMap<String, String>() {{
					put("Content-Type", "application/json");
				}};
			}
		};
		gameIdRequest.setRetryPolicy(VolleySingleton.RETRY_POLICY);
		VolleySingleton.getInstance().getRequestQueue().add(gameIdRequest);
	}
}
