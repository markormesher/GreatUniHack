package uk.co.markormesher.guh.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;
import uk.co.markormesher.guh.gcm_utils.GCMUtils;
import uk.co.markormesher.guh.utils.VolleySingleton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Play_JoinGame extends ActionBarActivity {

	private String gameId;
	private String playerId;
	private String currentPhotoPath = null;
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

				// send info to network
				Request gameIdRequest = new JsonObjectRequest(
						Request.Method.POST,
						"http://178.62.96.146/players.json",
						"{\"player\":{\"player_id\":" + (int) (Math.random() * 19) + ",\"gcm_id\":\"" + gcmId + "\",\"game_id\":\"" + gameId + "\",\"photo_url\":\"null\"}}",
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
				VolleySingleton.getInstance().getRequestQueue().add(gameIdRequest);
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
		if (requestCode == 1 && resultCode == RESULT_OK) {
			try {
				ExifInterface ei = new ExifInterface(currentPhotoPath);
				int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				if (orientation != ExifInterface.ORIENTATION_NORMAL) {
					Bitmap photo = BitmapFactory.decodeFile(currentPhotoPath);
					Matrix matrix = new Matrix();
					switch (orientation) {
						case ExifInterface.ORIENTATION_ROTATE_90:
							matrix.postRotate(90);
							break;
						case ExifInterface.ORIENTATION_ROTATE_180:
							matrix.postRotate(180);
							break;
						case ExifInterface.ORIENTATION_ROTATE_270:
							matrix.postRotate(270);
							break;
					}
					photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
					FileOutputStream foStream;
					try {
						foStream = new FileOutputStream(currentPhotoPath);
						photo.compress(Bitmap.CompressFormat.JPEG, 85, foStream);
						foStream.flush();
						foStream.close();
					} catch (Exception e) {
					}
				}
			} catch (IOException e) {
				return;
			}
			((ImageView) findViewById(R.id.join_game_selfie)).setImageURI(Uri.fromFile(new File(currentPhotoPath)));
		}
	}

	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);
		currentPhotoPath = image.getAbsolutePath();
		return image;
	}
}
