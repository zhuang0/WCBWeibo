package com.zhuang.sheen.wcbweibo.tool;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.drawable.Drawable;
import android.os.Handler;

public class NewAsyncImageLoader {

	private Object lock = new Object();

	private boolean mAllowLoad = true;

	private boolean firstLoad = true;

	private int mStartLoadLimit = 0;

	private int mStopLoadLimit = 0;

	final Handler handler = new Handler();

	private HashMap<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();

	private ExecutorService executorService = Executors.newFixedThreadPool(10); // 固定线程数

	public interface OnImageLoadListener {
		public void onImageLoad(Integer t, Drawable drawable);

		public void onError(Integer t);
	}

	public void setLoadLimit(int startLoadLimit, int stopLoadLimit) {
		if (startLoadLimit > stopLoadLimit) {
			return;
		}
		mStartLoadLimit = startLoadLimit;
		mStopLoadLimit = stopLoadLimit;
	}

	public void restore() {
		mAllowLoad = true;
		firstLoad = true;
	}

	public void lock() {
		mAllowLoad = false;
		firstLoad = false;
	}

	public void unlock() {
		mAllowLoad = true;
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	public void loadImage(Integer t, String imageUrl, OnImageLoadListener listener) {
		final OnImageLoadListener mListener = listener;
		final String mImageUrl = imageUrl;
		final Integer mt = t;

		executorService.submit(new Runnable() {

			@Override
			public void run() {
				if (!mAllowLoad) {

					synchronized (lock) {
						try {
							lock.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				if (mAllowLoad && firstLoad) {
					loadImage(mImageUrl, mt, mListener);
				}

				if (mAllowLoad && mt <= mStopLoadLimit && mt >= mStartLoadLimit) {
					loadImage(mImageUrl, mt, mListener);
				}
			}

		});
	}

	private void loadImage(final String mImageUrl, final Integer mt, final OnImageLoadListener mListener) {

		if (imageCache.containsKey(mImageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(mImageUrl);
			final Drawable d = softReference.get();
			if (d != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (mAllowLoad) {
							mListener.onImageLoad(mt, d);
						}
					}
				});
				return;
			}
		}
		try {
			final Drawable d = loadImageFromUrl(mImageUrl);
			if (d != null) {
				imageCache.put(mImageUrl, new SoftReference<Drawable>(d));
			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					if (mAllowLoad) {
						mListener.onImageLoad(mt, d);
					}
				}
			});
		} catch (IOException e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					mListener.onError(mt);
				}
			});
			e.printStackTrace();
		}
	}

	Drawable d = null;

	public Drawable loadImageFromUrl(String url) throws IOException {

		/*if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wcbweibo/cache/" + url.substring(url.lastIndexOf("/")+1));
			if(f.exists()){
				FileInputStream fis = new FileInputStream(f);
				Drawable d = Drawable.createFromStream(fis, "src");
				return d;
			}
			URL m = new URL(url);
			InputStream i = (InputStream) m.getContent();
			DataInputStream in = new DataInputStream(i);
			FileOutputStream out = new FileOutputStream(f);
			byte[] buffer = new byte[1024];
			int   byteread=0;
			while ((byteread = in.read(buffer)) != -1) {
				out.write(buffer, 0, byteread);
			}
			in.close();
			out.close();
			Drawable d = Drawable.createFromStream(i, "src");
			return d;
		}else{
			URL m = new URL(url);
			InputStream i = (InputStream) m.getContent();
			Drawable d = Drawable.createFromStream(i, "src");
			return d;
		}*/

		for (int reTry = 0; reTry < 2; reTry++) {

			URL myURL = new URL(url);
			URLConnection conn = myURL.openConnection();
			HttpURLConnection httpUrlConnection = (HttpURLConnection) conn;
			httpUrlConnection.setDoInput(true);
			httpUrlConnection.setConnectTimeout(5000);
			httpUrlConnection.setRequestMethod("GET");
			httpUrlConnection.connect();

			if (httpUrlConnection.getResponseCode() == -1) {

				httpUrlConnection.disconnect();
				if (reTry == 0) {
					continue;
				}

			} else {

				InputStream is = httpUrlConnection.getInputStream();

				if (is == null) {

					throw new RuntimeException("stream is null");
				}

				d = Drawable.createFromStream(is, "src");
			}
		}

		return d;

	}
}
