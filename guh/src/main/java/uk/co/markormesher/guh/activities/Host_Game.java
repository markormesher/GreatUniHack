package uk.co.markormesher.guh.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.fragments.PlayerListFragment;
import uk.co.markormesher.guh.objects.Player;
import uk.co.markormesher.guh.utils.VolleySingleton;

import java.util.ArrayList;

public class Host_Game extends ActionBarActivity implements ActivityWithPlayers {

	private String gameId;
	private ArrayList<Player> players = new ArrayList<>();

	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get game ID and player ID
		Bundle extras = getIntent().getExtras();
		gameId = extras.getString("game_id");

		// set layout
		setContentView(R.layout.activity_host_game);
		setTitle(getString(R.string.activity_title_game, gameId));

		// view pager stuff
		HostPagerAdapter hostPagerAdapter = new HostPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.game_view_pager);
		viewPager.setAdapter(hostPagerAdapter);

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
								players.add(new Player(jsonPlayer.getString("id"), jsonPlayer.getString("photo_url"), jsonPlayer.getString("role")));
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						// flip views
						findViewById(R.id.game_loading).setVisibility(View.GONE);
						viewPager.setVisibility(View.VISIBLE);
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Toast.makeText(Host_Game.this, "Something went wrong. Bye.", Toast.LENGTH_LONG).show();
						Host_Game.this.finish();
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

	public class HostPagerAdapter extends FragmentPagerAdapter {
		public HostPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment;
			Bundle arguments = new Bundle();
			if (i == 0) {
				fragment = new HostFragment1();
			} else {
				fragment = new PlayerListFragment();
				((PlayerListFragment) fragment).setIsHost(true);
				((PlayerListFragment) fragment).setGameId(gameId);
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
			return position == 0 ? "GLOBAL" : "VILLAGERS";
		}
	}

	public static class HostFragment1 extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_host_game_1, container, false);
			view.findViewById(R.id.global_daytime_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Host_Game) getActivity()).startDaytime();
				}
			});
			view.findViewById(R.id.global_nighttime_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Host_Game) getActivity()).startNighttime();
				}
			});
			view.findViewById(R.id.global_buzz_villagers_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Host_Game) getActivity()).buzz("villager");
				}
			});
			view.findViewById(R.id.global_buzz_wolves_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Host_Game) getActivity()).buzz("wolf");
				}
			});
			view.findViewById(R.id.global_buzz_seers_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Host_Game) getActivity()).buzz("seer");
				}
			});
			view.findViewById(R.id.global_buzz_healers_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Host_Game) getActivity()).buzz("healer");
				}
			});
			view.findViewById(R.id.global_buzz_hackers_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Host_Game) getActivity()).buzz("hacker");
				}
			});
			return view;
		}
	}

	@Override
	public ArrayList<Player> getPlayers() {
		return players;
	}

	private void startDaytime() {
		StringRequest request = new StringRequest(
				Request.Method.GET,
				"http://178.62.96.146/games/" + gameId + "/daytime",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {

					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				}
		);
		VolleySingleton.getInstance().getRequestQueue().add(request);
	}

	private void startNighttime() {
		StringRequest request = new StringRequest(
				Request.Method.GET,
				"http://178.62.96.146/games/" + gameId + "/nighttime",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {

					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				}
		);
		VolleySingleton.getInstance().getRequestQueue().add(request);
	}

	private void buzz(String role) {
		StringRequest request = new StringRequest(
				Request.Method.GET,
				"http://178.62.96.146/games/" + gameId + "/buzz/" + role,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {

					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				}
		);
		VolleySingleton.getInstance().getRequestQueue().add(request);
	}

}
