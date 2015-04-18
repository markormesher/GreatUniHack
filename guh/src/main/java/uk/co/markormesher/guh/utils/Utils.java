package uk.co.markormesher.guh.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class Utils {

	public static String writeDeviceID(Context context) throws IOException {
		String deviceID = UUID.randomUUID().toString();
		FileOutputStream outputStream = new FileOutputStream(new File(context.getFilesDir(), "devid"));
		outputStream.write(deviceID.getBytes());
		outputStream.close();
		return deviceID;
	}

	public static String readDeviceID(Context context) throws IOException {
		RandomAccessFile f = new RandomAccessFile(new File(context.getFilesDir(), "devid"), "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

}
