package com.zhuang.sheen.wcbweibo.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.weibo.sdk.android.api.WeiboAPI.FEATURE;
import com.weibo.sdk.android.net.RequestListener;
import com.zhuang.sheen.wcbweibo.R;
import com.zhuang.sheen.wcbweibo.keeper.AccessTokenKeeper;
import com.zhuang.sheen.wcbweibo.keeper.UserInfo;
import com.zhuang.sheen.wcbweibo.keeper.WeiboAdapter;
import com.zhuang.sheen.wcbweibo.keeper.WeiboInfo;
import com.zhuang.sheen.wcbweibo.tool.AsyncImageLoader;
import com.zhuang.sheen.wcbweibo.tool.ExitApplication;

public class ShowFriendsTimelineActivity extends Activity {

	final public static String TAG = "ShowFriendsTimelineActivity";
	static Oauth2AccessToken weiboAccessToken;
	private static Renren renren;
	private static Weibo weibo;
	private UserInfo mUserInfo;
	private WeiboInfo mWeiboInfo;
	public static WeiboInfo weiboInfo;
	private LinearLayout loadingLayout;
	private List<WeiboInfo> wbList;
	private ListView Msglist;
	private int page;
	private Handler handler;
	private View list_load_more;
	private TextView moreWeiboText;
	private Button refreshButton;
	private Button returnButton;
	private WeiboAdapter adapter;
	private String iconPath = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/wcbweibo/icon/user.png";
	private int touchAction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_friends_timeline);
		ExitApplication.getInstance().addActivity(this);

		Intent intent = getIntent();
		renren = intent.getParcelableExtra(Renren.RENREN_LABEL);
		// Log.v(TAG, "获取人人对象");
		renren.init(getApplicationContext());
		weibo = intent.getParcelableExtra(Weibo.WEIBO_LABEL);
		// Log.v(TAG, "获取Weibo对象");

		handler = new Handler();
		loadingLayout = (LinearLayout) findViewById(R.id.loadingLayout);
		list_load_more = getLayoutInflater().inflate(R.layout.load_more, null);
		moreWeiboText = (TextView) list_load_more.findViewById(R.id.moreWeiboText);
		refreshButton = (Button) findViewById(R.id.refreshWeibo);
		returnButton = (Button) findViewById(R.id.returnMain);
		Msglist = (ListView) findViewById(R.id.Msglist);
		mUserInfo = new UserInfo();
		mWeiboInfo = new WeiboInfo();
		ShowFriendsTimelineActivity.weiboAccessToken = AccessTokenKeeper
				.readAccessToken(getApplicationContext());

		refreshWeibo();

		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				refreshWeibo();
			}
		});

		returnButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(ShowFriendsTimelineActivity.this).setTitle("提示")
				.setMessage("确认完全退出无处不微博？")
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

	private void refreshWeibo() {

		page = 1;
		loadingLayout.setVisibility(View.VISIBLE);
		wbList = new ArrayList<WeiboInfo>();
		StatusesAPI statusesAPI = new StatusesAPI(ShowFriendsTimelineActivity.weiboAccessToken);
		statusesAPI.friendsTimeline(0, 0, 30, page, false, FEATURE.ALL, false,
				new RequestListener() {

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

						try {

							JSONObject data = new JSONObject(response);
							JSONArray statusesArr = data.getJSONArray("statuses");
							for (int i = 0; i < statusesArr.length(); i++) {
								JSONObject statusesObj = statusesArr.getJSONObject(i);
								if (statusesObj != null) {
									JSONObject u = statusesObj.getJSONObject("user");
									String id = statusesObj.getString("id");
									String userId = u.getString("id");
									String userName = u.getString("screen_name");
									String userIcon = u.getString("profile_image_url");
									String time = statusesObj.getString("created_at");
									Date date = new Date(time);
									time = ConvertTime(date);
									String text = statusesObj.getString("text");
									String source = statusesObj.getString("source");
									source = getLinkTitle(source);
									String thumbnailPic = null;
									Boolean haveImg = false;
									Boolean haveLargeImg = false;
									String originWeiboText = null;
									String picurl = null;

									WeiboInfo w = new WeiboInfo();
									w.setId(id);
									w.setUserId(userId);
									w.setUserName(userName);
									w.setTime(time);
									w.setUserIcon(userIcon);
									w.setSource(source);

									if (statusesObj.has("retweeted_status")) {
										JSONObject retweeted_status = statusesObj
												.getJSONObject("retweeted_status");
										while (true) {
											u = retweeted_status.getJSONObject("user");
											userName = u.getString("screen_name");
											text = text + "//@" + userName + ":"
													+ retweeted_status.getString("text");
											if (retweeted_status.has("retweeted_status")) {
												retweeted_status = retweeted_status
														.getJSONObject("retweeted_status");

											} else {
												if (retweeted_status.has("thumbnail_pic")) {
													haveImg = true;
													thumbnailPic = retweeted_status
															.getString("thumbnail_pic");

												} else {

												}
												if (retweeted_status.has("bmiddle_pic")) {
													haveLargeImg = true;
													picurl = retweeted_status
															.getString("bmiddle_pic");

												} else {
													if (retweeted_status.has("origin_pic")) {
														haveLargeImg = true;
														picurl = retweeted_status
																.getString("origin_pic");

													} else {

													}
												}
												originWeiboText = retweeted_status
														.getString("text");

												break;
											}

										}
									} else {
										if (statusesObj.has("thumbnail_pic")) {
											haveImg = true;
											thumbnailPic = statusesObj.getString("thumbnail_pic");

										} else {

										}
										if (statusesObj.has("bmiddle_pic")) {
											haveLargeImg = true;
											picurl = statusesObj.getString("bmiddle_pic");

										} else {
											if (statusesObj.has("origin_pic")) {
												haveLargeImg = true;
												picurl = statusesObj.getString("origin_pic");

											} else {

											}
										}
										originWeiboText = statusesObj.getString("text");
									}
									w.setHaveImage(haveImg);
									w.setHaveLargeImage(haveLargeImg);
									w.setThumbnailPic(thumbnailPic);
									w.setLargePic(picurl);
									w.setText(text);
									w.setOriginalText(originWeiboText);

									Log.v(TAG,
											"-----------------------------------------\n"
													+ w.getUserName() + ":" + w.getTime() + "前，来自 "
													+ w.getSource() + "\n" + w.getText() + "\n"
													+ w.getHaveImage() + w.getThumbnailPic() + "\n"
													+ w.getHaveLargeImage() + w.getLargePic());

									if (wbList == null) {
										wbList = new ArrayList<WeiboInfo>();
									}
									wbList.add(w);

								}
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}
						Log.v(TAG, "微博列表是否null？" + (wbList == null));
						handler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								if (wbList != null) {

									Msglist.addFooterView(list_load_more);
									adapter = new WeiboAdapter(getApplicationContext(), wbList,
											Msglist);

									Msglist.setAdapter(adapter);
									adapter.start = Msglist.getFirstVisiblePosition();
									adapter.end = Msglist.getLastVisiblePosition() + 1;
									adapter.loadImage();

									Msglist.setOnScrollListener(new OnScrollListener() {

										@Override
										public void onScrollStateChanged(AbsListView view,
												int scrollState) {
											// TODO Auto-generated method stub

											switch (scrollState) {
												case AbsListView.OnScrollListener.SCROLL_STATE_FLING :
													adapter.syncImageLoader1.lock();
													adapter.syncImageLoader2.lock();
													break;
												case AbsListView.OnScrollListener.SCROLL_STATE_IDLE :

													adapter.loadImage();
													break;
												case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL :
													adapter.syncImageLoader1.lock();
													adapter.syncImageLoader2.lock();
													break;

												default :
													break;
											}
										}

										@Override
										public void onScroll(AbsListView view,
												int firstVisibleItem, int visibleItemCount,
												int totalItemCount) {
											// TODO Auto-generated method stub
											adapter.start = Msglist.getFirstVisiblePosition() - 1;
											adapter.end = Msglist.getLastVisiblePosition() + 1;
											if (touchAction == MotionEvent.ACTION_MOVE) {
												adapter.loadImage();
											}

										}
									});

									Msglist.setOnTouchListener(new OnTouchListener() {

										@Override
										public boolean onTouch(View v, MotionEvent event) {
											// TODO Auto-generated method stub

											touchAction = event.getAction();

											return false;
										}
									});

									Msglist.setOnItemClickListener(new OnItemClickListener() {

										@Override
										public void onItemClick(AdapterView<?> arg0, View view,
												int arg2, long arg3) {
											// Log.v(TAG, "arg2:" + arg2);
											// Log.v(TAG, "arg3:" + arg3);
											// Log.v(TAG, "tag" +
											// view.getTag().toString());
											if (arg3 == -1) {
												page++;
												// Log.v(TAG, "page" + page);
												moreWeiboText.setText("加载中...");
												loadingLayout.setVisibility(View.VISIBLE);
												loadWeibo(page);
												adapter.notifyDataSetChanged(); // 数据集变化后,通知adapter

											} else {

												weiboInfo = (WeiboInfo) adapter.getItem(arg2);

												if (weiboInfo != null) {

													ShowFriendsTimelineActivity.weiboAccessToken = AccessTokenKeeper
															.readAccessToken(getApplicationContext());
													if (ShowFriendsTimelineActivity.weiboAccessToken
															.isSessionValid()) {
														Intent intent = new Intent(
																ShowFriendsTimelineActivity.this,
																ViewActivity.class);

														Bundle bundle = new Bundle();
														bundle.putParcelable(Renren.RENREN_LABEL,
																renren);
														// Log.v(TAG,
														// "传递Renren对象");
														bundle.putParcelable(Weibo.WEIBO_LABEL,
																weibo);
														// Log.v(TAG,
														// "传递Weibo对象");
														intent.putExtras(bundle);

														startActivity(intent);
													} else {
														weibo.authorize(
																ShowFriendsTimelineActivity.this,
																new WeiboAuthListener() {

																	@Override
																	public void onWeiboException(
																			WeiboException e) {
																		// TODO
																		// Auto-generated
																		// method
																		// stub

																	}

																	@Override
																	public void onError(
																			WeiboDialogError e) {
																		// TODO
																		// Auto-generated
																		// method
																		// stub

																	}

																	@Override
																	public void onComplete(
																			Bundle values) {
																		// TODO
																		// Auto-generated
																		// method
																		// stub
																		String token = values
																				.getString("access_token");
																		// Log.v(TAG,
																		// "token:"
																		// +
																		// token);

																		String expires_in = values
																				.getString("expires_in");
																		// Log.v(TAG,
																		// "expires_in:"
																		// +
																		// expires_in);

																		String uid = values
																				.getString("uid");
																		// Log.v(TAG,
																		// "userId:"
																		// +
																		// uid);

																		ShowFriendsTimelineActivity.weiboAccessToken = new Oauth2AccessToken(
																				token, expires_in,
																				uid);
																		// Log.v(TAG,
																		// "accesstoken赋值:");

																		mUserInfo.setToken(token);
																		// Log.v(TAG,
																		// "token赋值:"
																		// +
																		// mUserInfo.getToken());

																		mUserInfo.setUserId(uid);
																		// Log.v(TAG,
																		// "uid赋值:"
																		// +
																		// mUserInfo.getUserId());

																		if (ShowFriendsTimelineActivity.weiboAccessToken
																				.isSessionValid()) {
																			AccessTokenKeeper
																					.keepAccessToken(
																							ShowFriendsTimelineActivity.this,
																							ShowFriendsTimelineActivity.weiboAccessToken);
																			String date = new SimpleDateFormat(
																					"yyyy/MM/dd HH:mm:ss",
																					Locale.CHINA)
																					.format(new java.util.Date(
																							ShowFriendsTimelineActivity.weiboAccessToken
																									.getExpiresTime()));
																			MainActivity.weiboText
																					.setText("认证信息：认证成功\n有效期："
																							+ date);
																			MainActivity.weiboCancelBtn
																					.setEnabled(true);
																			MainActivity.weiboAuthBtn
																					.setEnabled(false);

																			Toast.makeText(
																					ShowFriendsTimelineActivity.this,
																					"认证成功",
																					Toast.LENGTH_SHORT)
																					.show();

																			UsersAPI usersAPI = new UsersAPI(
																					ShowFriendsTimelineActivity.weiboAccessToken);
																			// Log.v(TAG,
																			// "mUserInfo.getUserId()"
																			// +
																			// mUserInfo.getUserId());
																			usersAPI.show(
																					Long.parseLong(mUserInfo
																							.getUserId()),
																					new RequestListener() {

																						@Override
																						public void onIOException(
																								IOException e) {
																							// TODO
																							// Auto-generated
																							// method
																							// stub

																						}

																						@Override
																						public void onError(
																								WeiboException e) {
																							// TODO
																							// Auto-generated
																							// method
																							// stub

																						}

																						@Override
																						public void onComplete(
																								String response) {
																							// TODO
																							// Auto-generated
																							// method
																							// stub
																							try {

																								JSONObject jsonObject = new JSONObject(
																										response);
																								String name = jsonObject
																										.getString("screen_name");
																								ShowFriendsTimelineActivity.weiboAccessToken
																										.setUserName(name);
																								AccessTokenKeeper
																										.keepAccessToken(
																												ShowFriendsTimelineActivity.this,
																												ShowFriendsTimelineActivity.weiboAccessToken);
																								Log.v(TAG,
																										"userName:"
																												+ name);
																								String iconUrl = jsonObject
																										.getString("profile_image_url");
																								// Log.v(TAG,
																								// "userIcon:"
																								// +
																								// iconUrl);
																								mUserInfo
																										.setUserName(name);
																								AsyncImageLoader asyncImageLoader = new AsyncImageLoader();
																								Drawable cachedIcon;
																								try {
																									cachedIcon = asyncImageLoader
																											.loadImageFromUrl(iconUrl);
																									if (cachedIcon == null) {
																										Log.v(TAG,
																												"无我的头像");
																									} else {
																										mUserInfo
																												.setIcon(cachedIcon);
																										BitmapDrawable bd = (BitmapDrawable) cachedIcon;
																										Bitmap bm = bd
																												.getBitmap();

																										AsyncImageLoader
																												.savePNG(
																														bm,
																														iconPath);
																										Log.v(TAG,
																												"下载我的头像");
																									}

																								} catch (IOException e) {
																									// TODO
																									// Auto-generated
																									// catch
																									// block
																									e.printStackTrace();
																								}

																								handler.post(new Runnable() {

																									@Override
																									public void run() {
																										// TODO
																										MainActivity.userNameText
																												.setText("用户名："
																														+ mUserInfo
																																.getUserName());
																										if (mUserInfo
																												.getIcon() == null) {

																										} else {
																											Bitmap bitmap = AsyncImageLoader
																													.getLoacalBitmap(iconPath);
																											MainActivity.userIconView
																													.setImageBitmap(bitmap);
																										}

																										MainActivity.startWeibo
																												.setEnabled(true);
																									}
																								});

																							} catch (JSONException e) {
																								// TODO:
																								// handle
																								// exception
																								e.printStackTrace();
																							}

																						}
																					});

																			Intent intent = new Intent(
																					ShowFriendsTimelineActivity.this,
																					ViewActivity.class);

																			Bundle bundle = new Bundle();
																			bundle.putParcelable(
																					Renren.RENREN_LABEL,
																					renren);
																			// Log.v(TAG,
																			// "传递Renren对象");
																			bundle.putParcelable(
																					Weibo.WEIBO_LABEL,
																					weibo);
																			// Log.v(TAG,
																			// "传递Weibo对象");

																			intent.putExtras(bundle);

																			startActivity(intent);
																		}

																	}

																	@Override
																	public void onCancel() {
																		// TODO
																		// Auto-generated
																		// method
																		// stub

																	}
																});
													}
												}
											}

										}

									});

								}

								loadingLayout.setVisibility(View.GONE);
							}
						});

					}

				});

	}
	private void loadWeibo(final int page) {

		StatusesAPI statusesAPI = new StatusesAPI(ShowFriendsTimelineActivity.weiboAccessToken);
		statusesAPI.friendsTimeline(0, 0, 30, page, false, FEATURE.ALL, false,
				new RequestListener() {

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
						try {

							JSONObject data = new JSONObject(response);
							JSONArray statusesArr = data.getJSONArray("statuses");

							for (int i = 0; i < statusesArr.length(); i++) {
								JSONObject statusesObj = statusesArr.getJSONObject(i);

								if (statusesObj != null) {
									JSONObject u = statusesObj.getJSONObject("user");
									String id = statusesObj.getString("id");
									String userId = u.getString("id");
									String userName = u.getString("screen_name");
									String userIcon = u.getString("profile_image_url");
									String time = statusesObj.getString("created_at");
									Date date = new Date(time);
									time = ConvertTime(date);
									String text = statusesObj.getString("text");
									String source = statusesObj.getString("source");
									source = getLinkTitle(source);
									String thumbnailPic = null;
									Boolean haveImg = false;
									Boolean haveLargeImg = false;
									String originWeiboText = null;
									String picurl = null;

									WeiboInfo w = new WeiboInfo();
									w.setId(id);
									w.setUserId(userId);
									w.setUserName(userName);
									w.setTime(time);
									w.setUserIcon(userIcon);
									w.setSource(source);

									if (statusesObj.has("retweeted_status")) {
										JSONObject retweeted_status = statusesObj
												.getJSONObject("retweeted_status");
										while (true) {
											u = retweeted_status.getJSONObject("user");
											userName = u.getString("screen_name");
											text = text + "//@" + userName + ":"
													+ retweeted_status.getString("text");
											if (retweeted_status.has("retweeted_status")) {
												retweeted_status = retweeted_status
														.getJSONObject("retweeted_status");

											} else {
												if (retweeted_status.has("thumbnail_pic")) {
													haveImg = true;
													thumbnailPic = retweeted_status
															.getString("thumbnail_pic");

												} else {

												}
												if (retweeted_status.has("bmiddle_pic")) {
													haveLargeImg = true;
													picurl = retweeted_status
															.getString("bmiddle_pic");

												} else {
													if (retweeted_status.has("origin_pic")) {
														haveLargeImg = true;
														picurl = retweeted_status
																.getString("origin_pic");

													} else {

													}
												}
												originWeiboText = retweeted_status
														.getString("text");

												break;
											}

										}
									} else {
										if (statusesObj.has("thumbnail_pic")) {
											haveImg = true;
											thumbnailPic = statusesObj.getString("thumbnail_pic");

										} else {

										}
										if (statusesObj.has("bmiddle_pic")) {
											haveLargeImg = true;
											picurl = statusesObj.getString("bmiddle_pic");

										} else {
											if (statusesObj.has("origin_pic")) {
												haveLargeImg = true;
												picurl = statusesObj.getString("origin_pic");

											} else {

											}
										}
										originWeiboText = statusesObj.getString("text");
									}
									w.setHaveImage(haveImg);
									w.setHaveImage(haveLargeImg);
									w.setThumbnailPic(thumbnailPic);
									w.setLargePic(picurl);
									w.setText(text);
									w.setOriginalText(originWeiboText);

									Log.v(TAG,
											"-----------------------------------------\n"
													+ w.getUserName() + ":" + w.getTime() + "前，来自 "
													+ w.getSource() + "\n" + w.getText() + "\n"
													+ w.getHaveImage() + w.getThumbnailPic() + "\n"
													+ w.getHaveLargeImage() + w.getLargePic());
									adapter.addItem(w);

								}

							}
							if (statusesArr.length() == 0) {
								handler.post(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										moreWeiboText.setText("没有更多");
										loadingLayout.setVisibility(View.GONE);
									}
								});

							} else {
								handler.postDelayed(new Runnable() {
									@Override
									public void run() {

										moreWeiboText.setText("更多");
										loadingLayout.setVisibility(View.GONE);
									}
								}, 2000);
							}

						} catch (JSONException e) {

							e.printStackTrace();

						}

					}
				});

	}

	// 时间转换
	private String ConvertTime(Date olddate) {
		Date nowDate = new Date();
		Long timeSub;
		if (nowDate.getTime() - olddate.getTime() > 0) {
			timeSub = (nowDate.getTime() - olddate.getTime()) / 1000;
		} else {
			timeSub = (olddate.getTime() - nowDate.getTime()) / 1000;
		}

		int day = (int) (timeSub / (3600 * 24));
		if (day > 1) {
			return (day + "天前");
		} else {
			int hours = (int) (timeSub % (3600 * 24));
			int hour = hours / (3600);
			if (hour > 1)
				return (hour + "小时前");
			int mins = hours % 3600;
			int min = mins / (60);
			if (min > 1) {
				return (min + "分种前");
			} else {
				int sec = mins % 60;
				return (sec + "秒前");
			}

		}

	}

	// 提取链接
	private String getLinkTitle(String s) {
		String regex = "<a.*?/a>";

		Pattern pt = Pattern.compile(regex);
		Matcher mt = pt.matcher(s);
		if (mt.find()) {

			Matcher title = Pattern.compile(">.*?</a>").matcher(mt.group());
			if (title.find()) {
				s = title.group().replaceAll(">|</a>", "");
				return s;
			} else {
				return null;
			}
		} else {
			return null;
		}

	}

}
