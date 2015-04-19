package uk.co.markormesher.guh.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONObject;
import uk.co.markormesher.guh.ContextHack;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.activities.ActivityWithPlayers;
import uk.co.markormesher.guh.activities.PlayerListenerActivity;
import uk.co.markormesher.guh.constants.Jobs;
import uk.co.markormesher.guh.objects.Player;
import uk.co.markormesher.guh.utils.VolleySingleton;

import java.util.ArrayList;

public class PlayerListFragment extends Fragment {

	private boolean isHost = false;
	private String gameId = "";
	private String currentPlayerId = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_player_list, container, false);
		((ListView) view.findViewById(R.id.player_list_view)).setAdapter(new PlayerListAdapter(((ActivityWithPlayers) getActivity()).getPlayers()));
		return view;
	}

	public void setIsHost(boolean isHost) {
		this.isHost = isHost;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public void setCurrentPlayerId(String currentPlayerId) {
		this.currentPlayerId = currentPlayerId;
	}

	public class PlayerListAdapter extends BaseAdapter {

		private ArrayList<Player> players;

		public PlayerListAdapter(ArrayList<Player> players) {
			this.players = players;
		}

		@Override
		public int getCount() {
			return players.size();
		}

		@Override
		public Object getItem(int position) {
			return players.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.player_list_item, null);
			} else {
				view = convertView;
			}

			// get player
			final Player player = players.get(position);

			// image
			((NetworkImageView) view.findViewById(R.id.player_list_item_image)).setDefaultImageResId(R.drawable.cristiano_betta);
			((NetworkImageView) view.findViewById(R.id.player_list_item_image)).setImageUrl(player.getPhotoUrl(), VolleySingleton.getInstance().getImageLoader());

			// role
			if (player.getRole() != null) {
				((TextView) view.findViewById(R.id.player_list_item_role)).setText(player.getRole().toUpperCase());
			} else {
				if (currentPlayerId.equals(player.getPlayerId())) {
					((TextView) view.findViewById(R.id.player_list_item_role)).setText("YOU");
				} else {
					((TextView) view.findViewById(R.id.player_list_item_role)).setText("???");
				}
			}

			// job
			((TextView) view.findViewById(R.id.player_list_item_job)).setText("The Village " + Jobs.JOBS.get(player.getJobId()));

			// dead?
			if (!player.isAlive()) {
				view.findViewById(R.id.player_list_item_image).setAlpha(0.3f);
				view.findViewById(R.id.player_list_item_x_image).setVisibility(View.VISIBLE);
				view.findViewById(R.id.player_list_item_x_image).setAlpha(0.5f);
			}

			// click
			if (isHost) view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Actions");
					builder.setItems(new String[]{
							"Kill",
							"META",
							"\"Ghosts can't talk\""
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case 0:
									// done already?
									if (!player.isAlive()) {
										Toast.makeText(ContextHack.getContext(), "Already dead.", Toast.LENGTH_LONG).show();
										return;
									}

									// update to dead
									player.setAlive(false);
									((PlayerListenerActivity) getActivity()).onPlayerDeath();
									view.findViewById(R.id.player_list_item_image).setAlpha(0.3f);
									view.findViewById(R.id.player_list_item_x_image).setVisibility(View.VISIBLE);
									view.findViewById(R.id.player_list_item_x_image).setAlpha(0.5f);

									// send kill
									JsonObjectRequest killRequest = new JsonObjectRequest(
											Request.Method.POST,
											"http://178.62.96.146/games/" + gameId + "/kill",
											"{\"player\":{\"id\":\"" + player.getPlayerId() + "\"}}",
											new Response.Listener<JSONObject>() {
												@Override
												public void onResponse(JSONObject response) {

												}
											},
											new Response.ErrorListener() {
												@Override
												public void onErrorResponse(VolleyError error) {

												}
											}
									);
									killRequest.setRetryPolicy(VolleySingleton.RETRY_POLICY);
									VolleySingleton.getInstance().getRequestQueue().add(killRequest);
									break;

								case 1:
									JsonObjectRequest metaRequest = new JsonObjectRequest(
											Request.Method.POST,
											"http://178.62.96.146/games/" + gameId + "/meta",
											"{\"player\":{\"id\":\"" + player.getPlayerId() + "\"}}",
											new Response.Listener<JSONObject>() {
												@Override
												public void onResponse(JSONObject response) {

												}
											},
											new Response.ErrorListener() {
												@Override
												public void onErrorResponse(VolleyError error) {

												}
											}
									);
									metaRequest.setRetryPolicy(VolleySingleton.RETRY_POLICY);
									VolleySingleton.getInstance().getRequestQueue().add(metaRequest);
									break;

								case 2:
									// done already?
									if (player.isAlive()) {
										Toast.makeText(ContextHack.getContext(), "Not dead yet.", Toast.LENGTH_LONG).show();
										return;
									}

									// send warning
									JsonObjectRequest noTalkingRequest = new JsonObjectRequest(
											Request.Method.POST,
											"http://178.62.96.146/games/" + gameId + "/notalking",
											"{\"player\":{\"id\":\"" + player.getPlayerId() + "\"}}",
											new Response.Listener<JSONObject>() {
												@Override
												public void onResponse(JSONObject response) {

												}
											},
											new Response.ErrorListener() {
												@Override
												public void onErrorResponse(VolleyError error) {

												}
											}
									);
									noTalkingRequest.setRetryPolicy(VolleySingleton.RETRY_POLICY);
									VolleySingleton.getInstance().getRequestQueue().add(noTalkingRequest);
									break;
							}
							dialog.dismiss();
						}
					});
					AlertDialog dialog = builder.create();
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
				}
			});
			return view;
		}
	}
}
