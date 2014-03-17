package com.zhuang.sheen.wcbweibo.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;

public class AsyncImageLoader {

	// SoftReference是软引用，是为了更好的为了系统回收变量
	private HashMap<String, SoftReference<Drawable>> imageCache;

	// private Drawable drawable;

	// private ExecutorService executorService =
	// Executors.newFixedThreadPool(25); // 固定线程数

	public AsyncImageLoader() {
		imageCache = new HashMap<String, SoftReference<Drawable>>();

	}

	public Drawable loadDrawable(final String imageUrl, final ImageView imageView,
			final String tag, final ImageCallback imageCallback) {
		// resetPurgeTimer();
		if (imageCache.containsKey(imageUrl)) {
			// 从缓存中获取
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageView, tag);
			}
		};
		// 建立新一个新的线程下载图片
		new Thread() {

			@Override
			public void run() {
				Drawable drawable;
				try {
					drawable = loadImageFromUrl(imageUrl);
					imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
					Message message = handler.obtainMessage(0, drawable);
					handler.sendMessage(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();
		return null;
	}

	public Drawable loadImageFromUrl(String url) throws IOException {
		Drawable d = null;

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

	public static void savePNG(final Bitmap bitmap, final String path) {

		File file = new File(path);
		try {
			FileOutputStream out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 加载本地图片
	 * 
	 * @param path
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String path) {
		try {
			FileInputStream fis = new FileInputStream(path);
			return BitmapFactory.decodeStream(fis); // /把流转化为Bitmap图片

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	// 回调接口
	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, ImageView imageView, String tag);
	}

}
