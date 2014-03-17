package com.weibo.sdk.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.webkit.CookieSyncManager;

import com.weibo.sdk.android.util.Utility;

/**
 * 
 * @author luopeng (luopeng@staff.sina.com.cn)
 */
public class Weibo implements Parcelable {
	public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://open.weibo.cn/oauth2/authorize";

	private static Weibo mWeiboInstance = null;
	public static final String WEIBO_LABEL = "Weibo";
	public static String app_key = "";//第三方应用的appkey
	public static String redirecturl = "";// 重定向url

	public Oauth2AccessToken accessToken = null;//AccessToken实例
	public static final String APP_KEY = "app_key";
	public static final String REDIRECTURL = "redirecturl";
	public static final String KEY_TOKEN = "access_token";
	public static final String KEY_EXPIRES = "expires_in";
	public static final String KEY_REFRESHTOKEN = "refresh_token";
	public static boolean isWifi=false;//当前是否为wifi
	/**
	 * 
	 * @param appKey 第三方应用的appkey
	 * @param redirectUrl 第三方应用的回调页
	 * @return Weibo的实例
	 */
	public synchronized static Weibo getInstance(String appKey, String redirectUrl) {
		if (mWeiboInstance == null) {
			mWeiboInstance = new Weibo(appKey, redirectUrl);
		}
		app_key = appKey;
		Weibo.redirecturl = redirectUrl;
		return mWeiboInstance;
		
	}
	
	public Weibo(Parcel in) {
		Bundle bundle = Bundle.CREATOR.createFromParcel(in);
		app_key = bundle.getString(APP_KEY);
		redirecturl = bundle.getString(REDIRECTURL);
	}
	
	

	public Weibo(String appKey, String redirectUrl2) {
		// TODO Auto-generated constructor stub
		Weibo.app_key = appKey;
		Weibo.redirecturl = redirectUrl2;
	}

	/**
	 * 设定第三方使用者的appkey和重定向url
	 * @param appKey 第三方应用的appkey
	 * @param redirectUrl 第三方应用的回调页
	 */
	public void setupConsumerConfig(String appKey,String redirectUrl) {
		app_key = appKey;
		redirecturl = redirectUrl;
	}
	/**
	 * 
	 * 进行微博认证
	 * @param activity 调用认证功能的Context实例
	 * @param listener WeiboAuthListener 微博认证的回调接口
	 */
	public void authorize(Context context, WeiboAuthListener listener) {
		isWifi=Utility.isWifi(context);
		startAuthDialog(context, listener);
	}
	
	
	public void startAuthDialog(Context context, final WeiboAuthListener listener) {
		WeiboParameters params = new WeiboParameters();
//		CookieSyncManager.createInstance(context);
		startDialog(context, params, new WeiboAuthListener() {
			@Override
			public void onComplete(Bundle values) {
				// ensure any cookies set by the dialog are saved
				CookieSyncManager.getInstance().sync();
				if (null == accessToken) {
					accessToken = new Oauth2AccessToken();
				}
				accessToken.setToken(values.getString(KEY_TOKEN));
				accessToken.setExpiresIn(values.getString(KEY_EXPIRES));
				accessToken.setRefreshToken(values.getString(KEY_REFRESHTOKEN));
				if (accessToken.isSessionValid()) {
					Log.d("Weibo-authorize",
							"Login Success! access_token=" + accessToken.getToken() + " expires="
									+ accessToken.getExpiresTime() + " refresh_token="
									+ accessToken.getRefreshToken());
					listener.onComplete(values);
				} else {
					Log.d("Weibo-authorize", "Failed to receive access token");
					listener.onWeiboException(new WeiboException("Failed to receive access token."));
				}
			}

			@Override
			public void onError(WeiboDialogError error) {
				Log.d("Weibo-authorize", "Login failed: " + error);
				listener.onError(error);
			}

			@Override
			public void onWeiboException(WeiboException error) {
				Log.d("Weibo-authorize", "Login failed: " + error);
				listener.onWeiboException(error);
			}

			@Override
			public void onCancel() {
				Log.d("Weibo-authorize", "Login canceled");
				listener.onCancel();
			}
		});
	}
	
	

	public void startDialog(Context context, WeiboParameters parameters,
			final WeiboAuthListener listener) {
		parameters.add("client_id", app_key);
		parameters.add("response_type", "token");
		parameters.add("redirect_uri", redirecturl);
		parameters.add("display", "mobile");

		if (accessToken != null && accessToken.isSessionValid()) {
			parameters.add(KEY_TOKEN, accessToken.getToken());
		}
		String url = URL_OAUTH2_ACCESS_AUTHORIZE + "?" + Utility.encodeUrl(parameters);
		if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
			Utility.showAlert(context, "Error",
					"Application requires permission to access the Internet");
		} else {
			new WeiboDialog(context, url, listener).show();
		}
	}
	
	
	
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		Bundle bundle = new Bundle();
		if (app_key != null) {
			bundle.putString(APP_KEY, app_key);
		}
		if (redirecturl != null) {
			bundle.putString(REDIRECTURL, redirecturl);
		}
		
		bundle.writeToParcel(dest, flags);
	}
	public static final Parcelable.Creator<Weibo> CREATOR = new Parcelable.Creator<Weibo>() {
		@Override
		public Weibo createFromParcel(Parcel in) {
			return new Weibo(in);
		}

		@Override
		public Weibo[] newArray(int size) {
			return new Weibo[size];
		}
	};
	

}
