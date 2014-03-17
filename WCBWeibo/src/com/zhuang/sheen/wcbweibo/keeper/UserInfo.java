package com.zhuang.sheen.wcbweibo.keeper;

import android.graphics.drawable.Drawable;

public class UserInfo {
	//public static final String ID="id";
	public static final String USERID="userId";
	public static final String TOKEN="token";
	public static final String TOKENsECRET="tokenSecret";
	public static final String USERNAME="userName";
    public static final String USERICON="icon";
	//private Integer id;
	private String userId;
	private String token;
	private String tokenSecret;
	private String userName;
	private Drawable icon;
	
	
	

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getTokenSecret() {
		return tokenSecret;
	}
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
     
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon2) {
		this.icon = icon2;
	}
	
}
