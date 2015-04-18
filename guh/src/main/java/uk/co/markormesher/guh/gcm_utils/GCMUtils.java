package uk.co.markormesher.guh.gcm_utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import uk.co.markormesher.guh.constants.Keys;

import java.io.IOException;

public class GCMUtils {

	public static boolean checkPlayServices(final Activity activity, boolean loud) {
		// check with service
		boolean result;
		final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode) && loud) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						GooglePlayServicesUtil.getErrorDialog(resultCode, activity, 9000).show();
					}
				});
			}
			result = false;
		} else {
			result = true;
		}
		return result;
	}

	public static String getRegistrationID(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationID = prefs.getString(Keys.GCM_REG_ID, "");
		if (registrationID.isEmpty()) {
			return "";
		}

		int registeredVersion = prefs.getInt(Keys.GCM_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registrationID;
	}

	public static void registerInBackground(final Context context) {
		new RegisterClass(context).execute(null, null, null);
	}

	public static void storeRegistrationID(Context context, String regID) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(Keys.GCM_PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(Keys.GCM_REG_ID, regID);
		editor.putInt(Keys.GCM_APP_VERSION, getAppVersion(context));
		editor.apply();
	}

	public static SharedPreferences getGCMPreferences(Context context) {
		return context.getSharedPreferences(Keys.GCM_PREFS, Context.MODE_PRIVATE);
	}

	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (Exception e) {
			return -1;
		}
	}

	public static class RegisterClass extends AsyncTask<Void, Void, Void> {

		private Context context;

		public RegisterClass(Context context) {
			this.context = context;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
				String regID = gcm.register("485232485888");
				storeRegistrationID(context, regID);
			} catch (IOException e) {
				// try again next time they log in
			}
			return null;
		}
	}
}