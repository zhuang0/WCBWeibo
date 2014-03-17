package com.zhuang.sheen.wcbweibo.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.renren.api.connect.android.AsyncRenren;
import com.renren.api.connect.android.Renren;
import com.renren.api.connect.android.common.AbstractRequestListener;
import com.renren.api.connect.android.exception.RenrenAuthError;
import com.renren.api.connect.android.exception.RenrenError;
import com.renren.api.connect.android.photos.PhotoUploadRequestParam;
import com.renren.api.connect.android.photos.PhotoUploadResponseBean;
import com.renren.api.connect.android.status.StatusSetRequestParam;
import com.renren.api.connect.android.status.StatusSetResponseBean;
import com.renren.api.connect.android.view.ProfileNameView;
import com.renren.api.connect.android.view.ProfilePhotoView;
import com.renren.api.connect.android.view.RenrenAuthListener;
import com.zhuang.sheen.wcbweibo.R;
import com.zhuang.sheen.wcbweibo.keeper.WeiboInfo;
import com.zhuang.sheen.wcbweibo.tool.AsyncImageLoader;
import com.zhuang.sheen.wcbweibo.tool.ExitApplication;

public class ShareRenrenActivity extends Activity {

	private static final String TAG = "ShareRenrenActivity";

	// 照片文件
	File file;

	// 相册aid
	TextView photoAidValue;

	// 描述内容
	private String description;

	// 描述内容editText
	EditText descriptionValue;

	// 描述内容的字数计数器
	TextView descriptionCounter;

	// 图片的缩略图
	ImageView viewImage;

	// 提交按钮，上传照片
	Button submit;

	// 取消上传
	Button cancel;
	// Handler handler;
	PhotoUploadRequestParam photoParam = new PhotoUploadRequestParam();
	private ProgressDialog progress;
	private Handler handler;
	private static Toast toast1;
	private static Toast toast2;
	private Renren renren;
	private WeiboInfo mWeiboInfo;
	private String addedText;
	private String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wcbweibo/picture/renren.jpg";
	AsyncImageLoader asyncImageLoader;
	private ProfilePhotoView profilePhotoView;
	private ProfileNameView profileNameView;
	private Bitmap bm;
	private int wordLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_renren);
		// Log.v(TAG, "ShareRenActivity");
		ExitApplication.getInstance().addActivity(this);

		handler = new Handler();
		asyncImageLoader = new AsyncImageLoader();
		Intent intent = getIntent();

		renren = intent.getParcelableExtra(Renren.RENREN_LABEL);
		// Log.v(TAG, "获取人人对象");
		renren.init(getApplicationContext());

		mWeiboInfo = ShowFriendsTimelineActivity.weiboInfo;

		addedText = "[转自 " + mWeiboInfo.getUserName() + " 的新浪微博]";
		// Log.v(TAG, "附加：" + addedText);

		submit = (Button) findViewById(R.id.renren_sdk_upload_photo_submit);
		cancel = (Button) findViewById(R.id.renren_sdk_upload_photo_cancel);
		profilePhotoView = (ProfilePhotoView) findViewById(R.id.renren_sdk_profile_photo);
		profileNameView = (ProfileNameView) findViewById(R.id.renren_sdk_profile_name);
		viewImage = (ImageView) findViewById(R.id.renren_sdk_photo_view_image);

		descriptionValue = (EditText) findViewById(R.id.messageText);
		descriptionCounter = (TextView) findViewById(R.id.renren_sdk_photo_caption_counter);

		submit.setEnabled(false);

		// 初始化头像和名字控件
		profilePhotoView.setUid(renren.getCurrentUid());
		profileNameView.setUid(renren.getCurrentUid(), renren);

		if (mWeiboInfo.getHaveImage() == true) {

			Toast.makeText(ShareRenrenActivity.this, "将发布为照片", Toast.LENGTH_SHORT).show();
			bm = AsyncImageLoader.getLoacalBitmap(picPath);
			viewImage.setImageBitmap(bm);
			submit.setEnabled(true);
			wordLimit = PhotoUploadRequestParam.CAPTION_MAX_LENGTH;

		} else {
			Toast.makeText(ShareRenrenActivity.this, "无图片，将发布为人人状态", Toast.LENGTH_SHORT).show();
			Log.v(TAG, "无图片，将发布为人人状态");
			submit.setEnabled(true);
			wordLimit = StatusSetRequestParam.MAX_LENGTH;
		}

		// 获取本地图片文件
		file = new File(picPath);

		description = (mWeiboInfo.getOriginalText() + addedText).toString().trim();

		// 显示默认的新鲜事内容和字数统计
		if (description != null) {

			int length = description.length();

			Log.v(TAG, "描述内容长度：" + length);

			if (length > wordLimit) {
				description = description.substring(0, wordLimit);
				length = wordLimit;
			}
			descriptionValue.setText(description);
			// int index = description.length();
			// descriptionValue.setSelection(index);
			descriptionCounter.setText(length + "/" + wordLimit);
		}

		// 增加相片描述文本框的监听事件
		descriptionValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (descriptionValue.getText().toString().trim().toString().length() > 0) {
					submit.setEnabled(true);
				} else {
					submit.setEnabled(false);
				}
				// 设置计数器
				descriptionCounter.setText(s.length() + "/" + wordLimit);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		submit.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (renren.isAccessTokenValid()) {

					progress = ProgressDialog.show(ShareRenrenActivity.this, "提示", "正在发布，请稍候");
					if (mWeiboInfo.getHaveLargeImage() == false) {
						retweetAsStatus();
					} else {
						retweetAsPhoto();
						// retweetAsFeed();
					}
				} else {
					Toast.makeText(getApplicationContext(), "未认证，请先登录", Toast.LENGTH_SHORT).show();
					renren.authorize(ShareRenrenActivity.this, listener);
				}
			}
		});
		cancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				Toast.makeText(getApplicationContext(), "已取消发布", Toast.LENGTH_SHORT).show();
				ShareRenrenActivity.this.finish();
			}
		});
	}

	private void retweetAsStatus() {

		StatusSetRequestParam param = new StatusSetRequestParam(descriptionValue.getText().toString().trim());

		AbstractRequestListener<StatusSetResponseBean> listener = new AbstractRequestListener<StatusSetResponseBean>() {

			@Override
			public void onRenrenError(RenrenError renrenError) {
				final int errorCode = renrenError.getErrorCode();

				handler.post(new Runnable() {
					@Override
					public void run() {
						if (ShareRenrenActivity.this != null) {
							if (progress != null) {
								progress.dismiss();
							}
						}
						if (errorCode == RenrenError.ERROR_CODE_OPERATION_CANCELLED) {

							toast1 = Toast.makeText(getApplicationContext(), "状态发布被取消", Toast.LENGTH_SHORT);
							toast1.show();
						} else {
							toast1 = Toast.makeText(getApplicationContext(), "状态发布失败", Toast.LENGTH_SHORT);
							toast1.show();
						}
					}
				});
				ShareRenrenActivity.this.finish();
			}

			@Override
			public void onFault(Throwable fault) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						if (ShareRenrenActivity.this != null) {
							submit.setEnabled(true);

							if (progress != null) {
								progress.dismiss();
							}
						}
						toast1 = Toast.makeText(getApplicationContext(), "状态发布失败", Toast.LENGTH_SHORT);
						toast1.show();

					}
				});
				ShareRenrenActivity.this.finish();
			}

			@Override
			public void onComplete(StatusSetResponseBean bean) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (ShareRenrenActivity.this != null) {
							submit.setEnabled(true);
							if (progress != null) {
								progress.dismiss();
							}
						}
						toast1 = Toast.makeText(getApplicationContext(), "状态发布成功", Toast.LENGTH_SHORT);
						toast1.show();
						ShareRenrenActivity.this.finish();

					}
				});
				ShareRenrenActivity.this.finish();
			}
		};

		try {
			AsyncRenren aRenren = new AsyncRenren(renren);
			aRenren.publishStatus(param, listener, true);
		} catch (Throwable e) {
			e.printStackTrace();

		}
	}

	private void retweetAsPhoto() {
		// 设置caption参数
		String photoDescription = descriptionValue.getText().toString().trim();
		Log.v(TAG, "照片描述内容：" + photoDescription);
		if (photoDescription != null && !"".equals(photoDescription.trim())) {
			photoParam.setCaption(photoDescription);
		}
		// 设置file参数
		photoParam.setFile(file);
		// 调用SDK异步上传照片的接口
		new AsyncRenren(renren).publishPhoto(photoParam, new AbstractRequestListener<PhotoUploadResponseBean>() {
			@Override
			public void onRenrenError(RenrenError renrenError) {
				if (renrenError != null) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (ShareRenrenActivity.this != null) {

								if (progress != null) {
									progress.dismiss();
								}

							}
							toast2 = Toast.makeText(getApplicationContext(), "照片发布失败", Toast.LENGTH_SHORT);
							toast2.show();
							ShareRenrenActivity.this.finish();
						}
					});
					ShareRenrenActivity.this.finish();

				}
			}

			@Override
			public void onFault(Throwable fault) {
				if (fault != null) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (ShareRenrenActivity.this != null) {

								if (progress != null) {
									progress.dismiss();
								}
							}
							toast2 = Toast.makeText(getApplicationContext(), "照片发布失败", Toast.LENGTH_SHORT);
							toast2.show();
						}
					});
					ShareRenrenActivity.this.finish();
				}
			}

			@Override
			public void onComplete(PhotoUploadResponseBean bean) {
				if (bean != null) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (ShareRenrenActivity.this != null) {

								if (progress != null) {
									progress.dismiss();
								}
							}
							// 上传成功，直接显示成功
							toast2 = Toast.makeText(getApplicationContext(), "照片发布成功", Toast.LENGTH_SHORT);
							toast2.show();
						}
					});
					ShareRenrenActivity.this.finish();
				}
			}
		});

	}

	/*private void retweetAsFeed() {
		AsyncRenren asyncRenren = new AsyncRenren(renren);

		String title = description;
		if (title.length() > FeedPublishRequestParam.NAME_MAX_LENGTH - 3) {
			title = title.substring(0, FeedPublishRequestParam.NAME_MAX_LENGTH - 3) + "。。。";
		}
		String feedDescription = descriptionValue.getText().toString();
		Log.v(TAG, "新鲜事内容：" + feedDescription);
		String url = "http://weibo.com/sheenzhuang";
		String imageUrl = picurl;
		String subtitle = addedText;
		String actionName = "来自 无处不微博";
		String actionLink = "http://weibo.com/sheenzhuang";
		String message = "";
		FeedPublishRequestParam param = new FeedPublishRequestParam(title, description, url,
				imageUrl, subtitle, actionName, actionLink, message);
		AbstractRequestListener<FeedPublishResponseBean> listener = new AbstractRequestListener<FeedPublishResponseBean>() {

			@Override
			public void onComplete(final FeedPublishResponseBean bean) {

				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ShareRenrenActivity.this != null) {
							submit.setEnabled(true);

							if (progress != null) {
								progress.dismiss();
							}
						}
						// 上传成功，直接显示成功
						Toast.makeText(getApplicationContext(), "新鲜事发布成功", Toast.LENGTH_SHORT)
								.show();
					}
				});

			}

			@Override
			public void onRenrenError(final RenrenError renrenError) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ShareRenrenActivity.this != null) {
							submit.setEnabled(true);

							if (progress != null) {
								progress.dismiss();
							}
						}
						Toast.makeText(getApplicationContext(), "新鲜事发布失败", Toast.LENGTH_SHORT)
								.show();
					}
				});

			}

			@Override
			public void onFault(final Throwable fault) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ShareRenrenActivity.this != null) {
							submit.setEnabled(true);

							if (progress != null) {
								progress.dismiss();
							}
						}
						Toast.makeText(getApplicationContext(), "新鲜事发布失败", Toast.LENGTH_SHORT)
								.show();
					}
				});

			}
		};
		asyncRenren.publishFeed(param, listener, true);
	}
	*/
	final RenrenAuthListener listener = new RenrenAuthListener() {

		@Override
		public void onRenrenAuthError(RenrenAuthError renrenAuthError) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub

			profilePhotoView.setUid(renren.getCurrentUid());
			profileNameView.setUid(renren.getCurrentUid(), renren);
			String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(new java.util.Date(renren.getExpireTime()));
			MainActivity.renrenText.setText("认证信息：认证成功\n有效期：" + date);
			MainActivity.profilePhotoView.setUid(renren.getCurrentUid());
			MainActivity.profilePhotoView.setVisibility(View.VISIBLE);
			MainActivity.renrenUserIconView.setVisibility(View.GONE);
			MainActivity.profileNameView.setUid(renren.getCurrentUid(), renren);
			MainActivity.profileNameView.setVisibility(View.VISIBLE);
			Toast.makeText(getApplicationContext(), "登录成功,请重新转发", Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onCancelLogin() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancelAuth(Bundle values) {
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
		new AlertDialog.Builder(ShareRenrenActivity.this).setTitle("提示").setMessage("确认完全退出无处不微博？").setNegativeButton("取消", new DialogInterface.OnClickListener() {

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