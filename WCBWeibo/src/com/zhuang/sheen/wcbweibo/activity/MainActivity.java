package com.zhuang.sheen.wcbweibo.activity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.view.ProfileNameView;
import com.renren.api.connect.android.view.ProfilePhotoView;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.util.Utility;
import com.zhuang.sheen.wcbweibo.R;
import com.zhuang.sheen.wcbweibo.R.drawable;
import com.zhuang.sheen.wcbweibo.keeper.AccessTokenKeeper;
import com.zhuang.sheen.wcbweibo.keeper.UserInfo;
import com.zhuang.sheen.wcbweibo.tool.AsyncImageLoader;
import com.zhuang.sheen.wcbweibo.tool.ExitApplication;

/**
 * 
 * @author liyan (liyan9@staff.sina.com.cn)
 */
public class MainActivity extends Activity {

	private TabHost mTabHost;
	private LinearLayout loadingLayout;
	public static final String TAG = "MainActivity";
	private String iconPath = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/wcbweibo/icon/user.png";
	private static Weibo weibo;
	private UserInfo mUserInfo;

	private static final String CONSUMER_KEY = "1652613961";// 替换为开发者的appkey，例如"1646212860";
	private static final String REDIRECT_URL = "http://www.sina.com";
	public static Button weiboAuthBtn, weiboCancelBtn, startWeibo;
	public static TextView weiboText;
	public static TextView userNameText;
	public static ImageView userIconView;
	private static Oauth2AccessToken weiboAccessToken;

	// Renren的应用ID,API Key,SECRET Key
	private static final String APP_ID = "229194";
	private static final String API_KEY = "3084a3f122d948ce9f7473b413ac297c";
	private static final String SECRET_KEY = "cbabf792349c47e992b5a555844df59d";
	private static Renren renren;
	private Handler handler;
	private Button renrenAuthBtn;
	private Button renrenCacelBtn;
	public static TextView renrenText;
	public static ImageView renrenUserIconView;
	public static ProfilePhotoView profilePhotoView;
	public static ProfileNameView profileNameView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ExitApplication.getInstance().addActivity(this);

		handler = new Handler();
		loadingLayout = (LinearLayout) findViewById(R.id.loadingInMainLayout);
		mTabHost = (TabHost) findViewById(R.id.tabhost);
		mTabHost.setup();

		mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("新浪微博").setContent(R.id.tab1));
		mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("人人").setContent(R.id.tab2));

		weibo = Weibo.getInstance(CONSUMER_KEY, REDIRECT_URL);
		mUserInfo = new UserInfo();
		weiboText = (TextView) findViewById(R.id.weiboShow);
		userNameText = (TextView) findViewById(R.id.userName);
		userIconView = (ImageView) findViewById(R.id.userIcon);
		weiboAuthBtn = (Button) findViewById(R.id.weiboAuth);
		weiboCancelBtn = (Button) findViewById(R.id.weiboCancel);
		startWeibo = (Button) findViewById(R.id.startWeibo);

		renren = new Renren(API_KEY, SECRET_KEY, APP_ID, this);
		renren.init(MainActivity.this);
		renrenAuthBtn = (Button) findViewById(R.id.renrenAuth);
		renrenCacelBtn = (Button) findViewById(R.id.renrenCancel);
		renrenText = (TextView) findViewById(R.id.renrenShow);
		renrenUserIconView = (ImageView) findViewById(R.id.renrenUserIcon);
		profilePhotoView = (ProfilePhotoView) findViewById(R.id.renren_sdk_profile_photo);
		profileNameView = (ProfileNameView) findViewById(R.id.renren_sdk_profile_name);
		renrenText.setText("认证信息：\n有效期：");
		weiboText.setText("认证信息：\n有效期：");

		showLoginBtn(true);

		// 创建文件夹，用于存储图片
		String sdPath = Environment.getExternalStorageDirectory().toString(); // 获得SD卡路径
		File file1 = new File(sdPath + "/wcbweibo/icon");
		if (!file1.exists())
			file1.mkdir();
		Log.v(TAG, "新建文件夹/wcbweibo/wcbweibo/icon");
		File file2 = new File(sdPath + "/wcbweibo/picture");
		if (!file2.exists())
			file2.mkdir();
		Log.v(TAG, "新建文件夹/wcbweibo/wcbweibo/picture");

		MainActivity.weiboAccessToken = AccessTokenKeeper.readAccessToken(this);

		if (MainActivity.weiboAccessToken.isSessionValid()) {
			Weibo.isWifi = Utility.isWifi(this);
			String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA)
					.format(new java.util.Date(MainActivity.weiboAccessToken.getExpiresTime()));
			weiboText.setText("认证信息：仍在有效期内,无需再次登录\n有效期：" + date);
			userNameText.setText("用户名：" + MainActivity.weiboAccessToken.getUserName());
			Log.v(TAG, "userName:" + MainActivity.weiboAccessToken.getUserName());
			Bitmap bitmap = AsyncImageLoader.getLoacalBitmap(iconPath);
			userIconView.setImageBitmap(bitmap);
			weiboAuthBtn.setEnabled(false);
			weiboCancelBtn.setEnabled(true);
			startWeibo.setEnabled(true);
		} else {

			weiboText.setText("认证信息：认证无效，请重新登录\n有效期：");
			userNameText.setText("用户名：" + MainActivity.weiboAccessToken.getUserName());
			Class<drawable> drawable = R.drawable.class;
			Field field = null;
			try {
				field = drawable.getField("add_selector");
				int r_id = field.getInt(field.getName());
				userIconView.setImageDrawable(null);
				userIconView.setBackgroundResource(r_id);
			} catch (Exception e) {
				Log.e("ERROR", "PICTURE NOT　FOUND！");
			}
		}
		Log.v(TAG, "检查人人");
		if (renren.isAccessTokenValid()) {

			String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA)
					.format(new java.util.Date(renren.getExpireTime()));
			renrenText.setText("认证信息：仍在有效期内,无需再次登录\n有效期：" + date);
			profilePhotoView.setUid(renren.getCurrentUid());
			profileNameView.setUid(renren.getCurrentUid(), renren);

			showLoginBtn(false);

		} else {

			renrenText.setText("认证信息：认证无效,请重新登录\n有效期：");
			showLoginBtn(true);
			profileNameView.setVisibility(View.GONE);
			profilePhotoView.setVisibility(View.GONE);
			renrenUserIconView.setVisibility(View.VISIBLE);

		}

		// 微博登陆按钮事件
		weiboAuthBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadingLayout.setVisibility(View.VISIBLE);
				weibo.authorize(MainActivity.this, weiboListener);

			}
		});

		// 微博注销按钮事件
		weiboCancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AccessTokenKeeper.clear(MainActivity.this);
				weiboAuthBtn.setEnabled(true);
				weiboCancelBtn.setEnabled(false);
				startWeibo.setEnabled(false);
				weiboText.setText("认证信息：\n有效期：");
				userNameText.setText("用户名：");
				Class<drawable> drawable = R.drawable.class;
				Field field = null;
				try {
					field = drawable.getField("add_selector");
					int r_id = field.getInt(field.getName());
					userIconView.setImageDrawable(null);
					userIconView.setBackgroundResource(r_id);
				} catch (Exception e) {
					Log.e("ERROR", "PICTURE NOT　FOUND！");
				}
				if (loadingLayout.getVisibility() == View.VISIBLE) {
					loadingLayout.setVisibility(View.GONE);
				}

			}
		});

		// 人人登录按钮的事件
		renrenAuthBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				renren.authorize(MainActivity.this, listener);

			}
		});

		// 人人退出按钮事件
		renrenCacelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				renren.logout(getApplicationContext());
				showLoginBtn(true);
				profileNameView.setVisibility(View.GONE);
				profilePhotoView.setVisibility(View.GONE);
				renrenUserIconView.setVisibility(View.VISIBLE);
				renrenText.setText("认证信息:\n有效期：");
			}
		});

		// 获取微博内容按钮事件
		startWeibo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MainActivity.weiboAccessToken = AccessTokenKeeper
						.readAccessToken(getApplicationContext());
				if (!MainActivity.weiboAccessToken.isSessionValid()) {
					weibo.authorize(getApplicationContext(), weiboListener);
				} else {

					Intent intent = new Intent(MainActivity.this, ShowFriendsTimelineActivity.class);

					Bundle bundle = new Bundle();
					bundle.putParcelable(Renren.RENREN_LABEL, renren);
					// Log.v(TAG, "传递Renren对象");
					bundle.putParcelable(Weibo.WEIBO_LABEL, weibo);
					// Log.v(TAG, "传递Weibo对象");

					intent.putExtras(bundle);
					startActivity(intent);

				}

			}
		});

	}

	// 微博登录的listener
	final WeiboAuthListener weiboListener = new WeiboAuthListener() {

		@Override
		public void onComplete(Bundle values) {

			String token = values.getString("access_token");
			// Log.v(TAG, "token:" + token);

			String expires_in = values.getString("expires_in");
			// Log.v(TAG, "expires_in:" + expires_in);

			String uid = values.getString("uid");
			// Log.v(TAG, "userId:" + uid);

			MainActivity.weiboAccessToken = new Oauth2AccessToken(token, expires_in, uid);
			// Log.v(TAG, "accesstoken赋值:");

			mUserInfo.setToken(token);
			// Log.v(TAG, "token赋值:" + mUserInfo.getToken());

			mUserInfo.setUserId(uid);
			// Log.v(TAG, "uid赋值:" + mUserInfo.getUserId());

			if (MainActivity.weiboAccessToken.isSessionValid()) {
				String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA)
						.format(new java.util.Date(MainActivity.weiboAccessToken.getExpiresTime()));
				weiboText.setText("认证信息：认证成功\n有效期：" + date);

				weiboCancelBtn.setEnabled(true);
				weiboAuthBtn.setEnabled(false);
				AccessTokenKeeper.keepAccessToken(MainActivity.this, MainActivity.weiboAccessToken);

				Toast.makeText(MainActivity.this, "认证成功", Toast.LENGTH_SHORT).show();

				UsersAPI usersAPI = new UsersAPI(MainActivity.weiboAccessToken);
				// Log.v(TAG, "mUserInfo.getUserId()" + mUserInfo.getUserId());
				usersAPI.show(Long.parseLong(mUserInfo.getUserId()), new RequestListener() {

					@Override
					public void onIOException(IOException e) {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								loadingLayout.setVisibility(View.GONE);
							}
						});

					}

					@Override
					public void onError(WeiboException e) {
						// TODO Auto-generated method stub
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								loadingLayout.setVisibility(View.GONE);
							}
						});
					}

					@Override
					public void onComplete(String response) {
						// TODO Auto-generated method stub
						try {

							JSONObject jsonObject = new JSONObject(response);
							String name = jsonObject.getString("screen_name");
							MainActivity.weiboAccessToken.setUserName(name);
							AccessTokenKeeper.keepAccessToken(MainActivity.this,
									MainActivity.weiboAccessToken);
							Log.v(TAG, "userName:" + name);
							String iconUrl = jsonObject.getString("profile_image_url");
							// Log.v(TAG, "userIcon:" + iconUrl);
							mUserInfo.setUserName(name);
							AsyncImageLoader asyncImageLoader = new AsyncImageLoader();
							Drawable cachedIcon;
							try {
								cachedIcon = asyncImageLoader.loadImageFromUrl(iconUrl);
								if (cachedIcon == null) {
									Log.v(TAG, "无我的头像");
								} else {
									mUserInfo.setIcon(cachedIcon);
									BitmapDrawable bd = (BitmapDrawable) cachedIcon;
									Bitmap bm = bd.getBitmap();
									AsyncImageLoader.savePNG(bm, iconPath);
									Log.v(TAG, "下载我的头像");

								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							handler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									userNameText.setText("用户名：" + mUserInfo.getUserName());
									if (mUserInfo.getIcon() == null) {

									} else {
										Bitmap bitmap = AsyncImageLoader.getLoacalBitmap(iconPath);
										userIconView.setImageBitmap(bitmap);
									}
									loadingLayout.setVisibility(View.GONE);
									startWeibo.setEnabled(true);
								}
							});

						} catch (JSONException e) {
							// TODO: handle exception
							e.printStackTrace();
						}

					}
				});
			}

		}

		@Override
		public void onError(WeiboDialogError e) {
			Toast.makeText(getApplicationContext(), "Auth error : " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(), "Auth exception : " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}

	};

	// 人人登录的listener
	final RenrenAuthListener listener = new RenrenAuthListener() {

		@Override
		public void onComplete(Bundle values) {

			if (MainActivity.renren.isAccessTokenValid()) {
				String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA)
						.format(new java.util.Date(MainActivity.renren.getExpireTime()));
				renrenText.setText("认证信息：认证成功\n有效期：" + date);
				showLoginBtn(false);
				profileNameView.setVisibility(View.VISIBLE);
				profilePhotoView.setVisibility(View.VISIBLE);
				profilePhotoView.setUid(renren.getCurrentUid());
				profileNameView.setUid(renren.getCurrentUid(), renren);
				renrenUserIconView.setVisibility(View.GONE);
				Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
			Toast.makeText(MainActivity.this, "登录失败，请重新登录", Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onCancelLogin() {
		}

		@Override
		public void onCancelAuth(Bundle values) {
		}

	};

	// 控制人人页面按钮显示
	private void showLoginBtn(boolean flag) {
		if (flag) {
			renrenAuthBtn.setEnabled(true);
			renrenCacelBtn.setEnabled(false);
		} else {
			renrenAuthBtn.setEnabled(false);
			renrenCacelBtn.setEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(MainActivity.this).setTitle("提示").setMessage("确认完全退出无处不微博？")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						ExitApplication.getInstance().exit();
					}
				}).show();

		return super.onOptionsItemSelected(item);
	}

}
