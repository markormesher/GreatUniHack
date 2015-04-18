package uk.co.markormesher.guh.activities;

import android.app.AlertDialog;
import android.content.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;
import uk.co.markormesher.guh.fragments.PlayerListFragment;
import uk.co.markormesher.guh.objects.Player;
import uk.co.markormesher.guh.utils.VolleySingleton;

import java.util.ArrayList;

public class Play_Game extends ActionBarActivity implements ActivityWithPlayers {

	private String gameId;
	private String role;
	private ArrayList<Player> players = new ArrayList<>();

	private boolean roleSet = false;
	private boolean gameLoaded = false;
	private boolean isNighttime = false;

	private ViewPager viewPager;

	BroadcastReceiver roleAssigned = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			role = intent.getExtras().getString("role");
			roleSet = true;
			checkAllLoaded();
		}
	};

	BroadcastReceiver died = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// play sound
			MediaPlayer player = MediaPlayer.create(Play_Game.this, R.raw.roar);
			player.start();

			// buzz
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			if (vibrator != null) vibrator.vibrate(3000);

			// visual
			findViewById(R.id.dead_overlay).setVisibility(View.VISIBLE);
		}
	};


	BroadcastReceiver daytime = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			isNighttime = false;
		}
	};

	BroadcastReceiver nighttime = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			isNighttime = true;
			playNighttimeNoise();
		}
	};

	BroadcastReceiver meta = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// play sound
			MediaPlayer player = MediaPlayer.create(Play_Game.this, R.raw.meta);
			player.start();

			// buzz
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			if (vibrator != null) vibrator.vibrate(3000);
		}
	};

	BroadcastReceiver buzz = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().getString("role").equals(role)) {
				// buzz
				Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				if (vibrator != null) vibrator.vibrate(1500);
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get game ID and player ID
		Bundle extras = getIntent().getExtras();
		gameId = extras.getString("game_id");

		// set layout
		setContentView(R.layout.activity_play_game);
		setTitle(getString(R.string.activity_title_game, gameId));
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// view pager stuff
		PlayPagerAdapter playPagerAdapter = new PlayPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.game_view_pager);
		viewPager.setAdapter(playPagerAdapter);

		// get game info from server
		Request gameRequest = new JsonObjectRequest(
				Request.Method.GET,
				"http://178.62.96.146/games/" + gameId + ".json",
				"",
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// parse in players
						try {
							JSONArray jsonPlayers = response.getJSONArray("players");
							for (int i = 0; i < jsonPlayers.length(); ++i) {
								JSONObject jsonPlayer = jsonPlayers.getJSONObject(i);
								players.add(new Player(jsonPlayer.getString("id"), jsonPlayer.getString("photo_url"), null));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						// done!
						gameLoaded = true;
						checkAllLoaded();
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(Play_Game.this, "Something went wrong. Bye.", Toast.LENGTH_LONG).show();
						Play_Game.this.finish();
					}
				}
		);
		VolleySingleton.getInstance().getRequestQueue().add(gameRequest);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Time to Head South...")
				.setMessage("Are you sure you want to leave the village?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				})
				.setNegativeButton("No", null)
				.show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter iff = new IntentFilter(Keys.INTENT_ROLE_ASSIGNED);
		LocalBroadcastManager.getInstance(this).registerReceiver(roleAssigned, iff);

		IntentFilter iff2 = new IntentFilter(Keys.INTENT_DIED);
		LocalBroadcastManager.getInstance(this).registerReceiver(died, iff2);

		IntentFilter iff3 = new IntentFilter(Keys.INTENT_DAYTIME);
		LocalBroadcastManager.getInstance(this).registerReceiver(daytime, iff3);

		IntentFilter iff4 = new IntentFilter(Keys.INTENT_NIGHTTIME);
		LocalBroadcastManager.getInstance(this).registerReceiver(nighttime, iff4);

		IntentFilter iff5 = new IntentFilter(Keys.INTENT_META);
		LocalBroadcastManager.getInstance(this).registerReceiver(meta, iff5);

		IntentFilter iff6 = new IntentFilter(Keys.INTENT_BUZZ);
		LocalBroadcastManager.getInstance(this).registerReceiver(meta, iff6);
	}

	@Override
	protected void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(roleAssigned);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(died);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(daytime);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(nighttime);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(meta);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(buzz);
	}

	private void checkAllLoaded() {
		if (roleSet && gameLoaded) {
			// flip views
			findViewById(R.id.game_loading).setVisibility(View.GONE);
			viewPager.setVisibility(View.VISIBLE);
		}
	}

	private void playNighttimeNoise() {
		if (!isNighttime) return;

		// pick a noise
		double rand = Math.random();
		int cow;
		if (rand < 0.25) {
			cow = R.raw.cow1;
		} else if (rand < 0.5) {
			cow = R.raw.cow2;
		} else if (rand < 0.75) {
			cow = R.raw.cow3;
		} else {
			cow = R.raw.cow4;
		}

		// play noise
		MediaPlayer player = MediaPlayer.create(Play_Game.this, cow);
		player.start();
		player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				playNighttimeNoise();
			}
		});
	}

	@Override
	public ArrayList<Player> getPlayers() {
		return players;
	}

	public class PlayPagerAdapter extends FragmentPagerAdapter {
		public PlayPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment;
			Bundle arguments = new Bundle();
			if (i == 0) {
				fragment = new PlayFragment1();
				arguments.putInt("image", getResources().getIdentifier("drawable/" + role + (role.equals("villager") && Math.random() < 0.5 ? "_2" : ""), null, getPackageName()));
				arguments.putString("role", role);
			} else {
				fragment = new PlayerListFragment();
			}
			fragment.setArguments(arguments);
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return position == 0 ? "YOU" : "VILLAGE";
		}
	}

	public static class PlayFragment1 extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			String role = getArguments().getString("role");

			View view = inflater.inflate(R.layout.fragment_play_game_1, container, false);
			((ImageView) view.findViewById(R.id.player_profile_image)).setImageResource(getArguments().getInt("image"));
			((TextView) view.findViewById(R.id.player_profile_message)).setText(role.equals("villager") ? "You are but a humble villager." : "You are a " + role + ".");
			return view;
		}
	}

}
