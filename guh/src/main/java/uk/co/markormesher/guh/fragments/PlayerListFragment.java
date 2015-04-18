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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import org.json.JSONObject;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.activities.ActivityWithPlayers;
import uk.co.markormesher.guh.objects.Player;
import uk.co.markormesher.guh.utils.VolleySingleton;

import java.util.ArrayList;

public class PlayerListFragment extends Fragment {

	private boolean isHost = false;
	private String gameId = "";

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
			View view = convertView;
			if (view == null) view = getActivity().getLayoutInflater().inflate(R.layout.player_list_item, null);

			// get player
			final Player player = players.get(position);

			// image
			((NetworkImageView) view.findViewById(R.id.player_list_item_image)).setImageUrl(player.getPhotoUrl(), VolleySingleton.getInstance().getImageLoader());
			((NetworkImageView) view.findViewById(R.id.player_list_item_image)).setDefaultImageResId(R.mipmap.ic_launcher);

			// role
			if (player.getRole() != null)
				((TextView) view.findViewById(R.id.player_list_item_role)).setText(player.getRole().toUpperCase());

			// click
			if (isHost) view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Actions");
					builder.setItems(new String[]{
							"Kill",
							"META"
					}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case 0:
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
									VolleySingleton.getInstance().getRequestQueue().add(metaRequest);
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
