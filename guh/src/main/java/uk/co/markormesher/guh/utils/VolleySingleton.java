package uk.co.markormesher.guh.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import uk.co.markormesher.guh.ContextHack;

public class VolleySingleton {
	private static VolleySingleton mInstance = null;
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	private VolleySingleton() {
		mRequestQueue = Volley.newRequestQueue(ContextHack.getContext());
		mImageLoader = new ImageLoader(this.mRequestQueue, new ImageLoader.ImageCache() {
			private final LruCache<String, Bitmap> mCache = new LruCache<>(10);

			public void putBitmap(String url, Bitmap bitmap) {
				mCache.put(url, bitmap);
			}

			public Bitmap getBitmap(String url) {
				return mCache.get(url);
			}
		});
	}

	public static VolleySingleton getInstance() {
		if (mInstance == null) {
			mInstance = new VolleySingleton();
		}
		return mInstance;
	}

	public RequestQueue getRequestQueue() {
		return this.mRequestQueue;
	}

	public ImageLoader getImageLoader() {
		return this.mImageLoader;
	}

	public static final RetryPolicy RETRY_POLICY = new DefaultRetryPolicy(
			0,
			DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
			DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

}
