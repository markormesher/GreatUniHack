package uk.co.markormesher.guh.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import uk.co.markormesher.guh.R;

public class Host_SetRoles extends ActionBarActivity {

	private String gameId;
	private int playerCount;

	private SeekBar villagerSeekbar;
	private SeekBar wolfSeekbar;
	private SeekBar seerSeekbar;
	private SeekBar healerSeekbar;
	private SeekBar hackerSeekbar;
	private TextView villagerTextview;
	private TextView wolfTextview;
	private TextView seerTextview;
	private TextView healerTextview;
	private TextView hackerTextview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set layout
		setContentView(R.layout.activity_host_set_roles);
		setTitle(R.string.activity_title_set_roles);

		// get game ID and player count
		Bundle extras = getIntent().getExtras();
		gameId = extras.getString("game_id");
		playerCount = extras.getInt("player_count");

		// get views
		villagerSeekbar = (SeekBar) findViewById(R.id.role_count_villager);
		wolfSeekbar = (SeekBar) findViewById(R.id.role_count_wolf);
		seerSeekbar = (SeekBar) findViewById(R.id.role_count_seer);
		healerSeekbar = (SeekBar) findViewById(R.id.role_count_healer);
		hackerSeekbar = (SeekBar) findViewById(R.id.role_count_hacker);
		villagerTextview = (TextView) findViewById(R.id.role_label_villager);
		wolfTextview = (TextView) findViewById(R.id.role_label_wolf);
		seerTextview = (TextView) findViewById(R.id.role_label_seer);
		healerTextview = (TextView) findViewById(R.id.role_label_healer);
		hackerTextview = (TextView) findViewById(R.id.role_label_hacker);

		// set max and default
		villagerSeekbar.setMax(playerCount);
		wolfSeekbar.setMax(playerCount);
		seerSeekbar.setMax(playerCount);
		healerSeekbar.setMax(playerCount);
		hackerSeekbar.setMax(playerCount);

		int wolves = (int) Math.round(playerCount * 0.15);
		int seers = (int) Math.round(playerCount * 0.05);
		int healers = (int) Math.round(playerCount * 0.10);
		int hackers = 1;
		int villagers = playerCount - wolves - seers - healers - hackers;

		villagerSeekbar.setProgress(villagers);
		wolfSeekbar.setProgress(wolves);
		seerSeekbar.setProgress(seers);
		healerSeekbar.setProgress(healers);
		hackerSeekbar.setProgress(hackers);

		// set listeners
		SeekBar.OnSeekBarChangeListener changeListener = new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				refreshCounts();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		};
		villagerSeekbar.setOnSeekBarChangeListener(changeListener);
		wolfSeekbar.setOnSeekBarChangeListener(changeListener);
		seerSeekbar.setOnSeekBarChangeListener(changeListener);
		healerSeekbar.setOnSeekBarChangeListener(changeListener);
		hackerSeekbar.setOnSeekBarChangeListener(changeListener);

		// initial refresh
		refreshCounts();

		// button listener
		findViewById(R.id.set_roles_done_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int villagers = villagerSeekbar.getProgress();
				int wolves = wolfSeekbar.getProgress();
				int seers = seerSeekbar.getProgress();
				int healers = healerSeekbar.getProgress();
				int hackers = hackerSeekbar.getProgress();

				// TODO post to server
			}
		});
	}

	private void refreshCounts() {
		int villagers = villagerSeekbar.getProgress();
		int wolves = wolfSeekbar.getProgress();
		int seers = seerSeekbar.getProgress();
		int healers = healerSeekbar.getProgress();
		int hackers = hackerSeekbar.getProgress();

		villagerTextview.setText(getString(R.string.set_role_label_villager, villagers));
		wolfTextview.setText(getString(R.string.set_role_label_wolf, wolves));
		seerTextview.setText(getString(R.string.set_role_label_seer, seers));
		healerTextview.setText(getString(R.string.set_role_label_healer, healers));
		hackerTextview.setText(getString(R.string.set_role_label_hacker, hackers));

		findViewById(R.id.set_roles_done_button).setEnabled(villagers + wolves + seers + healers + hackers == playerCount);
	}
}