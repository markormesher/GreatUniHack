package uk.co.markormesher.guh.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import uk.co.markormesher.guh.R;
import uk.co.markormesher.guh.activities.ActivityWithPlayers;
import uk.co.markormesher.guh.objects.Player;
import uk.co.markormesher.guh.utils.VolleySingleton;

import java.util.ArrayList;

public class PlayerListFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_player_list, container, false);
		((ListView) view.findViewById(R.id.player_list_view)).setAdapter(new PlayerListAdapter(((ActivityWithPlayers) getActivity()).getPlayers()));
		return view;
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
			Player player = players.get(position);

			// image
			((NetworkImageView) view.findViewById(R.id.player_list_item_image)).setImageUrl(player.getPhotoUrl(), VolleySingleton.getInstance().getImageLoader());
			((NetworkImageView) view.findViewById(R.id.player_list_item_image)).setDefaultImageResId(R.mipmap.ic_launcher);
			if (player.getRole() != null) ((TextView) view.findViewById(R.id.player_list_item_role)).setText(player.getRole().toUpperCase());
			return view;
		}
	}
}
