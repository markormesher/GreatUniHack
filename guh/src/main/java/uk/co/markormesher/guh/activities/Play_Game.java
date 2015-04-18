package uk.co.markormesher.guh.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.constants.Keys;

import java.util.ArrayList;

public class Play_Game extends ActionBarActivity {

	private String gameId;
	private String playerId;
	private String role;
	private ArrayList<Player> players = new ArrayList<>();

	private boolean roleSet = false;
	private boolean gameLoaded = false;

	private RequestQueue requestQueue;

	private ViewPager viewPager;

	BroadcastReceiver roleAssigned = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			role = intent.getExtras().getString("role");
			roleSet = true;
			checkAllLoaded();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get game ID and player ID
		Bundle extras = getIntent().getExtras();
		gameId = extras.getString("game_id");
		playerId = extras.getString("player_id");

		// set layout
		setContentView(R.layout.activity_play_game);
		setTitle(getString(R.string.activity_title_game, gameId));

		// view pager stuff
		PlayPagerAdapter playPagerAdapter = new PlayPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.game_view_pager);
		viewPager.setAdapter(playPagerAdapter);

		// get game info from server
		requestQueue = Volley.newRequestQueue(this);
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
								players.add(new Player(jsonPlayer.getString("id"), jsonPlayer.getString("photo_url")));
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
		requestQueue.add(gameRequest);
		requestQueue.start();
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

	private void checkAllLoaded() {
		if (roleSet && gameLoaded) {
			// flip views
			findViewById(R.id.game_loading).setVisibility(View.GONE);
			viewPager.setVisibility(View.VISIBLE);
		}
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
				arguments.putInt("image", getResources().getIdentifier("drawable/" + role, null, getPackageName()));
				arguments.putString("role", role);
			} else {
				fragment = new PlayFragment2();
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

	public static class PlayFragment2 extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_play_game_2, container, false);
		}
	}

	public class Player {

		private String playerId;
		private String photoUrl;

		public Player(String playerId, String photoUrl) {
			this.playerId = playerId;
			this.photoUrl = photoUrl;
		}

		public String getPlayerId() {
			return playerId;
		}

		public String getPhotoUrl() {
			return photoUrl;
		}
	}

}