package com.zhuang.sheen.wcbweibo.keeper;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhuang.sheen.wcbweibo.R;
import com.zhuang.sheen.wcbweibo.R.drawable;
import com.zhuang.sheen.wcbweibo.tool.NewAsyncImageLoader;

public class WeiboAdapter extends BaseAdapter {

	// private AsyncImageLoader asyncImageLoader1;
	// private AsyncImageLoader asyncImageLoader2;
	public NewAsyncImageLoader syncImageLoader1;
	public NewAsyncImageLoader syncImageLoader2;
	private List<WeiboInfo> wbList;
	private ListView mListView;
	public int start;
	public int end;
	private LayoutInflater inflater;

	public WeiboAdapter(Context context, List<WeiboInfo> weiboList, ListView listView) {

		this.wbList = weiboList;
		this.mListView = listView;
		syncImageLoader1 = new NewAsyncImageLoader();
		syncImageLoader2 = new NewAsyncImageLoader();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return wbList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return wbList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// asyncImageLoader1 = new AsyncImageLoader();
		// asyncImageLoader2 = new AsyncImageLoader();

		WeiBoHolder wh;
		WeiboInfo wb;

		/*if (convertView == null || convertView.getTag() == null) {
			convertView = inflater.inflate(R.layout.weibo, null);

			wh = new WeiBoHolder();
			wh.wbicon = (ImageView) convertView.findViewById(R.id.wbicon);
			wh.wbimage = (ImageView) convertView.findViewById(R.id.wbimage);
			wh.wbtext = (TextView) convertView.findViewById(R.id.wbtext);
			wh.wbtime = (TextView) convertView.findViewById(R.id.wbtime);
			wh.wbsource = (TextView) convertView.findViewById(R.id.wbsource);
			wh.wbuser = (TextView) convertView.findViewById(R.id.wbuser);
			convertView.setTag(wh);
		} else {
			wh = (WeiBoHolder) convertView.getTag();
		}*/

		if (convertView == null || convertView.getTag() == null) {
			convertView = inflater.inflate(R.layout.weibo, null);
		}
		wb = wbList.get(position);
		convertView.setTag(position);
		wh = new WeiBoHolder();
		wh.wbicon = (ImageView) convertView.findViewById(R.id.wbicon);
		wh.wbimage = (ImageView) convertView.findViewById(R.id.wbimage);
		wh.wbtext = (TextView) convertView.findViewById(R.id.wbtext);
		wh.wbtime = (TextView) convertView.findViewById(R.id.wbtime);
		wh.wbsource = (TextView) convertView.findViewById(R.id.wbsource);
		wh.wbuser = (TextView) convertView.findViewById(R.id.wbuser);
		if (wb != null) {
			wh.wbicon.setTag(wb.getId() + wb.getUserIcon());
			wh.wbimage.setTag(wb.getId());
			wh.wbuser.setText(wb.getUserName());
			wh.wbtime.setText(wb.getTime());
			wh.wbsource.setText("来自：" + wb.getSource());
			wh.wbtext.setText(wb.getText(), TextView.BufferType.SPANNABLE);

			Class<drawable> drawable = R.drawable.class;
			Field field = null;
			try {
				field = drawable.getField("add_selector");
				int r_id = field.getInt(field.getName());
				wh.wbicon.setImageDrawable(null);
				wh.wbicon.setBackgroundResource(r_id);
			} catch (Exception e) {
				Log.e("ERROR", "PICTURE NOT　FOUND！");
			}
			syncImageLoader1.loadImage(position, wb.getUserIcon(), imageLoadListener1);
			/*Drawable cachedIcon = asyncImageLoader1.loadDrawable(wb.getUserIcon(), wh.wbicon, wb.getId() + wb.getUserIcon(), new ImageCallback() {

				@Override
				public void imageLoaded(Drawable imageDrawable, ImageView imageView, String tag) {
					if (imageView.getTag().toString().equals(tag)) {
						imageView.setImageDrawable(imageDrawable);
					}

				}

			});*/

			if (wb.getHaveImage()) {
				// asyncImageLoader2.resetPurgeTimer();
				wh.wbimage.setImageResource(R.drawable.images);
				syncImageLoader2.loadImage(position, wb.getThumbnailPic(), imageLoadListener2);
				/*	Drawable cachedImage = asyncImageLoader2.loadDrawable(wb.getThumbnailPic(), wh.wbimage, wb.getId(), new ImageCallback() {

						@Override
						public void imageLoaded(Drawable imageDrawable, ImageView imageView, String tag) {
							if (imageView.getTag().toString().equals(tag)) {
								imageView.setImageDrawable(imageDrawable);
							}

						}

					});
					if (cachedImage == null) {
						wh.wbimage.setImageResource(R.drawable.images);
					}*/
			} else {
				wh.wbimage.setImageDrawable(null);
			}

		}
		//Log.v("get view:", String.valueOf(start) + "," + String.valueOf(end));
		return convertView;
	}

	NewAsyncImageLoader.OnImageLoadListener imageLoadListener1 = new NewAsyncImageLoader.OnImageLoadListener() {

		@Override
		public void onImageLoad(Integer t, Drawable drawable) {
			// BookModel model = (BookModel) getItem(t);
			View view = mListView.findViewWithTag(t);

			if (view != null) {
				ImageView iv = (ImageView) view.findViewById(R.id.wbicon);
				iv.setImageDrawable(drawable);
			}
		}

		@Override
		public void onError(Integer t) {

			/*View view = mListView.findViewWithTag(t);
			if (view != null) {
				ImageView iv = (ImageView) view.findViewWithTag(wb.getId() + wb.getUserIcon());
				Class<drawable> drawable = R.drawable.class;
				Field field = null;
				try {
					field = drawable.getField("add_selector");
					int r_id = field.getInt(field.getName());
					iv.setImageDrawable(null);
					iv.setBackgroundResource(r_id);
				} catch (Exception e) {
					Log.e("ERROR", "PICTURE NOT　FOUND！");
				}
			}*/
		}

	};
	NewAsyncImageLoader.OnImageLoadListener imageLoadListener2 = new NewAsyncImageLoader.OnImageLoadListener() {

		@Override
		public void onImageLoad(Integer t, Drawable drawable) {
			// BookModel model = (BookModel) getItem(t);
			View view = mListView.findViewWithTag(t);

			if (view != null) {
				ImageView iv = (ImageView) view.findViewById(R.id.wbimage);
				iv.setImageDrawable(drawable);
			}
		}

		@Override
		public void onError(Integer t) {

			/*View view = mListView.findViewWithTag(t);
			if (view != null) {
				ImageView iv = (ImageView) view.findViewWithTag(wb.getId() + wb.getUserIcon());
				Class<drawable> drawable = R.drawable.class;
				Field field = null;
				try {
					field = drawable.getField("add_selector");
					int r_id = field.getInt(field.getName());
					iv.setImageDrawable(null);
					iv.setBackgroundResource(r_id);
				} catch (Exception e) {
					Log.e("ERROR", "PICTURE NOT　FOUND！");
				}
			}*/
		}

	};

	public void loadImage() {
		//Log.v("load image:", String.valueOf(start) + "," + String.valueOf(end));
		if (end >= getCount()) {
			end = getCount() - 1;
		}
		if (start < 0) {
			start = 0;
		}
		syncImageLoader1.setLoadLimit(start, end);
		syncImageLoader2.setLoadLimit(start, end);
		syncImageLoader1.unlock();
		syncImageLoader2.unlock();
	}

	public void addItem(WeiboInfo item) {
		wbList.add(item);
	}

}
