package com.zhuang.sheen.wcbweibo.activity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
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
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.renren.api.connect.android.Renren;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.Weibo;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.api.UsersAPI;
import com.weibo.sdk.android.api.WeiboAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.zhuang.sheen.wcbweibo.R;
import com.zhuang.sheen.wcbweibo.keeper.AccessTokenKeeper;
import com.zhuang.sheen.wcbweibo.keeper.WeiboInfo;
import com.zhuang.sheen.wcbweibo.tool.AsyncImageLoader;
import com.zhuang.sheen.wcbweibo.tool.AsyncImageLoader.ImageCallback;
import com.zhuang.sheen.wcbweibo.tool.ExitApplication;

public class ViewActivity extends Activity {

	private String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/wcbweibo/picture/renren.jpg";
	private String iconPath = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/wcbweibo/icon/user.png";
	private WeiboInfo mWeiboInfo;
	private static Renren renren;
	private static Weibo weibo;
	private static Oauth2AccessToken weiboAccessToken;
	AsyncImageLoader asyncImageLoader;
	String commentText = "";
	TextView utv;
	TextView ttv;
	TextView timetv;
	TextView sourcetv;
	ImageView pic;
	ImageView iv;
	Button retweetbButton;
	Button commentButton;
	Button refreshButton;
	Button favoriteButton;
	Button returnButton;
	private LinearLayout viewLoadingLayout;
	private ProgressBar pb;
	private TextView tv;
	private int fileSize;
	private int downLoadFileSize;
	private Boolean ifReturn = false;
	final public static String TAG = "ViewActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view);
		ExitApplication.getInstance().addActivity(this);
		Log.v(TAG, "运行ViewActivity");

		ViewActivity.weiboAccessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
		mWeiboInfo = new WeiboInfo();
		asyncImageLoader = new AsyncImageLoader();
		utv = (TextView) findViewById(R.id.user_name);
		ttv = (TextView) findViewById(R.id.text);
		timetv = (TextView) findViewById(R.id.time);
		sourcetv = (TextView) findViewById(R.id.source);
		pic = (ImageView) findViewById(R.id.pic);
		iv = (ImageView) findViewById(R.id.user_icon);
		retweetbButton = (Button) findViewById(R.id.btn_retweet);
		returnButton = (Button) findViewById(R.id.btn_return);
		viewLoadingLayout = (LinearLayout) findViewById(R.id.viewLoadingLayout);
		pb = (ProgressBar) findViewById(R.id.viewloading);
		tv = (TextView) findViewById(R.id.viewloadingText);

		Intent intent = getIntent();
		renren = intent.getParcelableExtra(Renren.RENREN_LABEL);
		// Log.v(TAG, "获取人人对象");
		weibo = intent.getParcelableExtra(Weibo.WEIBO_LABEL);
		// Log.v(TAG, "获取Weibo对象");

		mWeiboInfo = ShowFriendsTimelineActivity.weiboInfo;
		viewLoadingLayout.setVisibility(View.GONE);
		retweetbButton.setEnabled(false);

		Log.v(TAG, "开始显示微博具体内容");
		utv.setText(mWeiboInfo.getUserName());
		Log.v(TAG, "加载用户名");
		ttv.setText(mWeiboInfo.getText());
		Log.v(TAG, "加载微博内容");
		timetv.setText(mWeiboInfo.getTime());
		Log.v(TAG, "加载微博创建时间");
		sourcetv.setText("来自：" + mWeiboInfo.getSource());
		Log.v(TAG, "加载微博来源信息");

		if (mWeiboInfo.getUserIcon() != null) {
			Drawable cachedIcon = asyncImageLoader.loadDrawable(mWeiboInfo.getUserIcon(), iv,
					mWeiboInfo.getUserIcon(), new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable, ImageView imageView,
								String tag) {

							imageView.setImageDrawable(imageDrawable);
						}
					});
			Log.v(TAG, "异步加载用户头像");
		}
		if (mWeiboInfo.getHaveImage() == true) {
			viewLoadingLayout.setVisibility(View.VISIBLE);
			new Thread() {
				public void run() {
					try {
						load_file(mWeiboInfo.getLargePic());
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}.start();

			Log.v(TAG, "异步加载微博大图");

		} else {
			Toast.makeText(ViewActivity.this, "该微博无大图", Toast.LENGTH_SHORT).show();
			retweetbButton.setEnabled(true);
		}

		returnButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ifReturn = true;
				ViewActivity.this.finish();

			}
		});

		// 转发按钮的事件
		retweetbButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v(TAG, "转发");
				new AlertDialog.Builder(ViewActivity.this).setTitle("转发到")
						.setItems(R.array.dialog_items, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								String[] arr = getResources().getStringArray(R.array.dialog_items);
								switch (which) {
									case 0 :
										Log.v(TAG, "retweet to " + arr[which]);
										retweetToRenren();
										break;
									case 1 :
										Log.v(TAG, "retweet to " + arr[which]);
										retweetToWeibo();
									default :
										break;
								}
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
								Log.v(TAG, "取消转发");
							}
						}).show();
			}
		});

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {// 定义一个Handler，用于处理下载线程与UI间通讯
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
					case 0 :
						pb.setMax(100);
					case 1 :
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								int result = downLoadFileSize * 100 / fileSize;
								pb.setProgress(result);
								tv.setText("正在加载图片：" + result + "%");
							}
						});

						break;
					case 2 :
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if (ifReturn == false) {
									Toast.makeText(ViewActivity.this, "图片加载完成", Toast.LENGTH_SHORT)
											.show();
									Bitmap bm = AsyncImageLoader.getLoacalBitmap(picPath);
									pic.setImageBitmap(bm);
									viewLoadingLayout.setVisibility(View.GONE);
									retweetbButton.setEnabled(true);
								}

							}
						});

						break;

					case -1 :
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub

								Toast.makeText(ViewActivity.this, "图片加载出错", Toast.LENGTH_SHORT)
										.show();
							}
						});

						break;
				}
			}
			super.handleMessage(msg);
		}
	};

	private void sendMsg(int flag) {
		Message msg = new Message();
		msg.what = flag;
		handler.dispatchMessage(msg);

	}

	public void load_file(String url) throws IOException {

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
				} else {
					sendMsg(-1);
				}

			} else {

				InputStream is = httpUrlConnection.getInputStream();
				this.fileSize = httpUrlConnection.getContentLength();// 根据响应获取文件大小
				if (this.fileSize <= 0) {
					sendMsg(-1);
					throw new RuntimeException("无法获知文件大小 ");
				}
				if (is == null) {
					sendMsg(-1);
					throw new RuntimeException("stream is null");
				}
				FileOutputStream fos = new FileOutputStream(picPath);
				// 把数据存入路径+文件名
				byte buf[] = new byte[1024];
				downLoadFileSize = 0;
				sendMsg(0);
				do {
					// 循环读取
					int numread = is.read(buf);
					if (numread == -1) {
						break;
					}
					fos.write(buf, 0, numread);
					downLoadFileSize += numread;

					sendMsg(1);// 更新进度条
				} while (true);
				sendMsg(2);// 通知下载完成
				try {
					httpUrlConnection.disconnect();
					fos.close();
					is.close();
				} catch (Exception ex) {
					Log.e("tag", "error: " + ex.getMessage(), ex);
				}
				break;

			}

		}

	}

	// 转发到微博的方法
	private void retweetToWeibo() {
		Log.v(TAG, "开始转发到微博");
		ViewActivity.weiboAccessToken = AccessTokenKeeper.readAccessToken(ViewActivity.this);
		if (ViewActivity.weiboAccessToken.isSessionValid()) {
			StatusesAPI statusesAPI = new StatusesAPI(ViewActivity.weiboAccessToken);
			statusesAPI.repost(Long.parseLong(mWeiboInfo.getId()), commentText,
					WeiboAPI.COMMENTS_TYPE.NONE, new RequestListener() {

						@Override
						public void onIOException(IOException e) {
							// TODO Auto-generated method stub
							handler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(ViewActivity.this, "转发失败", Toast.LENGTH_SHORT)
											.show();
								}
							});

							Log.e(TAG, e.toString());

						}

						@Override
						public void onError(WeiboException e) {
							// TODO Auto-generated method stub

							handler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(ViewActivity.this, "转发失败", Toast.LENGTH_SHORT)
											.show();
								}
							});

							Log.e(TAG, e.toString());
						}

						@Override
						public void onComplete(String response) {
							// TODO Auto-generated method stub
							handler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									handler.post(new Runnable() {

										@Override
										public void run() {
											// TODO Auto-generated method stub
											Toast.makeText(ViewActivity.this, "转发成功",
													Toast.LENGTH_SHORT).show();
										}
									});

									Log.v(TAG, "转发成功");
								}
							});

						}
					});
		} else {
			weibo.authorize(getApplicationContext(), weiboListener);
		}

	}

	// 转发到人人的方法
	private void retweetToRenren() {

		Log.v(TAG, "开始转发到人人");
		Intent intent = new Intent(ViewActivity.this, ShareRenrenActivity.class);
		intent.putExtra(Renren.RENREN_LABEL, renren);
		// Log.v(TAG, "传递人人对象");
		startActivity(intent);
		// Log.v(TAG, "转到ShareRenrenActivity");

	}

	final WeiboAuthListener weiboListener = new WeiboAuthListener() {

		@Override
		public void onWeiboException(WeiboException e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(WeiboDialogError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			String token = values.getString("access_token");
			Log.v(TAG, "token:" + token);

			String expires_in = values.getString("expires_in");
			Log.v(TAG, "expires_in:" + expires_in);

			String uid = values.getString("uid");
			Log.v(TAG, "userId:" + uid);

			ViewActivity.weiboAccessToken = new Oauth2AccessToken(token, expires_in, uid);

			if (ViewActivity.weiboAccessToken.isSessionValid()) {

				AccessTokenKeeper.keepAccessToken(ViewActivity.this, ViewActivity.weiboAccessToken);
				Toast.makeText(getApplicationContext(), "认证成功", Toast.LENGTH_SHORT).show();
				String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA)
						.format(new java.util.Date(ViewActivity.weiboAccessToken.getExpiresTime()));
				MainActivity.weiboText.setText("认证信息：仍在有效期内,无需再次登录\n有效期：" + date);
				UsersAPI usersAPI = new UsersAPI(ViewActivity.weiboAccessToken);
				usersAPI.show(uid, new RequestListener() {

					@Override
					public void onIOException(IOException e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onError(WeiboException e) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onComplete(String response) {
						// TODO Auto-generated method stub

						JSONObject jsonObject;
						try {
							jsonObject = new JSONObject(response);
							String name = jsonObject.getString("screen_name");
							ViewActivity.weiboAccessToken.setUserName(name);
							AccessTokenKeeper.keepAccessToken(ViewActivity.this,
									ViewActivity.weiboAccessToken);
							Log.v(TAG, "userName:" + name);
							String iconUrl = jsonObject.getString("profile_image_url");
							Log.v(TAG, "userIcon:" + iconUrl);
							AsyncImageLoader asyncImageLoader = new AsyncImageLoader();
							Drawable cachedIcon;
							try {
								cachedIcon = asyncImageLoader.loadImageFromUrl(iconUrl);
								if (cachedIcon == null) {
									Log.v(TAG, "无我的头像");
								} else {

									BitmapDrawable bd = (BitmapDrawable) cachedIcon;
									Bitmap bm = bd.getBitmap();
									AsyncImageLoader.savePNG(bm, iconPath);
									Log.v(TAG, "下载我的头像");
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});

			}
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(ViewActivity.this).setTitle("提示").setMessage("确认完全退出无处不微博？")
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
