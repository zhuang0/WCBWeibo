package com.zhuang.sheen.wcbweibo.keeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.renren.api.connect.android.Util;
import com.weibo.sdk.android.Oauth2AccessToken;

/**
 * 该类用于保存Oauth2AccessToken到sharepreference，并提供读取功能
 * @author xiaowei6@staff.sina.com.cn
 *
 */
public class AccessTokenKeeper {
	private static final String PREFERENCES_NAME = "com_zhuang_sheen_wcbweibo_weibo";

	/**
	 * 保存微博的accesstoken到SharedPreferences
	 * @param context Activity 上下文环境
	 * @param token Oauth2AccessToken
	 */
	public static void keepAccessToken(Context context, Oauth2AccessToken token) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		Editor editor = pref.edit();
		editor.putString("weiboToken", token.getToken());
		editor.putLong("weiboExpiresTime", token.getExpiresTime());
		editor.putString("userName", token.getUserName());
		editor.putString("userId", token.getUserId());
		editor.commit();
	}
	
 
	
	/**
	 * 清空微博sharepreference
	 * @param context
	 */
	public static void clear(Context context){
		Util.clearCookies(context);
	    SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
	    Editor editor = pref.edit();
	    editor.clear();
	    editor.commit();
	    
	}
	

	

	/**
	 * 从SharedPreferences读取微博accessstoken
	 * @param context
	 * @return Oauth2AccessToken
	 */
	public static Oauth2AccessToken readAccessToken(Context context){
		Oauth2AccessToken token = new Oauth2AccessToken();
		SharedPreferences pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
		token.setToken(pref.getString("weiboToken", ""));
		token.setExpiresTime(pref.getLong("weiboExpiresTime", 0));
		token.setUserName(pref.getString("userName", ""));
		token.setUserId(pref.getString("userId", ""));
		return token;
	}
	
	
}
