package uk.co.markormesher.guh.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import uk.co.markormesher.guh.R;

public class Play_Game extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set layout
		setContentView(R.layout.activity_play_game);
		setTitle(R.string.activity_title_game);

	}

}
