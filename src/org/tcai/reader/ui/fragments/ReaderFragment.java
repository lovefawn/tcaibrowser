package org.tcai.reader.ui.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.tcai.models.BookmarkHistoryItem;
import org.tcai.providers.BookmarksProvider;
import org.tcai.providers.BookmarksWrapper;
import org.tcai.reader.R;
import org.tcai.reader.model.DateAdapter;
import org.tcai.reader.models.ReaderItemAdapter;
import org.tcai.reader.ui.components.DragGrid;
import org.tcai.reader.ui.components.ScrollLayout;
import org.tcai.reader.util.Configure;
import org.tcai.utils.Constants;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ReaderFragment extends Fragment {
	public interface OnStartRssPageItemClickedListener {
		public void onStartRssPageItemClicked(String url);
	}

	private View mContainer = null;
	private OnStartRssPageItemClickedListener mListener = null;

	/** GridView. */
	private LinearLayout linear;
	private RelativeLayout relate;
	private DragGrid gridView;
	private ScrollLayout lst_views;
	private ViewGroup pages;
	private ImageView imageView;
	private ImageView runImage, delImage;
	LinearLayout.LayoutParams param;

	TranslateAnimation left, right;
	Animation up, down;

	public static final int PAGE_SIZE = 8;
	ArrayList<DragGrid> gridviews = new ArrayList<DragGrid>();

	ArrayList<ArrayList<String>> lstPages = new ArrayList<ArrayList<String>>();// lists.size()==countpage;
	ArrayList<String> lstItems = new ArrayList<String>();
	ArrayList<ImageView> imageViews = new ArrayList<ImageView>();
	SensorManager sm;
	SensorEventListener lsn;
	boolean isClean = false;
	Vibrator vibrator;
	int rockCount = 0;

	public void setOnStartRssPageItemClickedListener(
			OnStartRssPageItemClickedListener listener) {
		mListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mContainer == null) {
			mContainer = inflater.inflate(R.layout.main, container, false);

			int limit = Integer
					.parseInt(PreferenceManager
							.getDefaultSharedPreferences(getActivity())
							.getString(
									Constants.PREFERENCE_START_PAGE_LIMIT,
									Integer.toString(getResources()
											.getInteger(
													org.tcai.R.integer.default_start_page_items_number))));

			// init rss data

			String[] HISTORY_BOOKMARKS_PROJECTION = new String[] {
					BookmarksProvider.Columns._ID,
					BookmarksProvider.Columns.TITLE,
					BookmarksProvider.Columns.URL,
					BookmarksProvider.Columns.VISITS,
					BookmarksProvider.Columns.CREATION_DATE,
					BookmarksProvider.Columns.VISITED_DATE,
					BookmarksProvider.Columns.BOOKMARK,
					BookmarksProvider.Columns.IS_FOLDER,
					BookmarksProvider.Columns.PARENT_FOLDER_ID,
					BookmarksProvider.Columns.FAVICON,
					BookmarksProvider.Columns.THUMBNAIL };
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			c.add(Calendar.DAY_OF_YEAR, -14);

			String whereClause = BookmarksProvider.Columns.VISITED_DATE + " > "
					+ Long.toString(c.getTimeInMillis());

			String orderClause = BookmarksProvider.Columns.VISITS + " DESC, "
					+ BookmarksProvider.Columns.VISITED_DATE + " DESC LIMIT "
					+ Integer.toString(limit);
			ContentResolver contentResolver = getActivity()
					.getContentResolver();

			Cursor cursor = contentResolver.query(
					BookmarksProvider.BOOKMARKS_URI,
					HISTORY_BOOKMARKS_PROJECTION, whereClause, null,
					orderClause);

			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int titleIndex = cursor
						.getColumnIndex(BookmarksProvider.Columns.TITLE);
				lstItems.add(cursor.getString(titleIndex)); // to String
			}

			// init
			relate = (RelativeLayout) mContainer.findViewById(R.id.relate);
			lst_views = (ScrollLayout) mContainer.findViewById(R.id.views);

			pages = (ViewGroup) mContainer.findViewById(R.id.tv_pages);

			org.tcai.reader.util.Configure.init(getActivity());
			param = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.FILL_PARENT,
					android.view.ViewGroup.LayoutParams.FILL_PARENT);
			param.rightMargin = 20;
			param.leftMargin = 20;
			if (gridView != null) {
				lst_views.removeAllViews();
			}

			// init data
			initData();

			for (int i = 0; i < Configure.countPages; i++) {
				lst_views.addView(addGridView(i));
			}

			lst_views.setPageListener(new ScrollLayout.PageListener() {
				@Override
				public void page(int page) {
					setCurPage(page);
				}
			});

			for (int i = 0; i < Configure.countPages; i++) {
				imageView = new ImageView(getActivity());
				imageView.setLayoutParams(new LayoutParams(20, 20));
				imageView.setPadding(20, 0, 20, 0);

				if (i == 0) {
					imageView
							.setBackgroundResource(R.drawable.page_indicator_focused);
				} else {
					imageView.setBackgroundResource(R.drawable.page_indicator);
				}
				imageViews.add(imageView);

				pages.addView(imageViews.get(i));
			}

			runImage = (ImageView) mContainer.findViewById(R.id.run_image);
			runAnimation();
			delImage = (ImageView) mContainer.findViewById(R.id.dels);
			relate.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					System.out.println("LongClick");
					return false;
				}
			});
			vibrator = (Vibrator) getActivity().getSystemService(
					Context.VIBRATOR_SERVICE);
			sm = (SensorManager) getActivity().getSystemService(
					Context.SENSOR_SERVICE);
			Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			lsn = new SensorEventListener() {
				public void onSensorChanged(SensorEvent e) {
					if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
						if (!isClean && rockCount >= 10) {
							isClean = true;
							rockCount = 0;
							vibrator.vibrate(100);
							CleanItems();
							return;
						}
						float newX = e.values[SensorManager.DATA_X];
						float newY = e.values[SensorManager.DATA_Y];
						float newZ = e.values[SensorManager.DATA_Z];
						// if ((newX >= 18 || newY >= 20||newZ >= 20
						// )&&rockCount<4)
						// {
						if ((newX >= 18 || newY >= 20 || newZ >= 20)
								&& rockCount % 2 == 0) {
							rockCount++;
							return;
						}
						if ((newX <= -18 || newY <= -20 || newZ <= -20)
								&& rockCount % 2 == 1) {
							rockCount++;
							return;
						}

					}
				}

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
					// TODO Auto-generated method stub

				}
			};

			sm.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_GAME);

		}

		return mContainer;
	}

	public void initData() {
		Configure.countPages = (int) Math.ceil(lstItems.size()
				/ (float) PAGE_SIZE);
		lstPages = new ArrayList<ArrayList<String>>();

		for (int i = 0; i < Configure.countPages; i++) {
			lstPages.add(new ArrayList<String>());
			for (int j = PAGE_SIZE * i; j < (PAGE_SIZE * (i + 1) > lstItems
					.size() ? lstItems.size() : PAGE_SIZE * (i + 1)); j++)
				lstPages.get(i).add(lstItems.get(j));
		}
		boolean isLast = true;
		for (int i = lstPages.get(Configure.countPages - 1).size(); i < PAGE_SIZE; i++) {
			if (isLast) {
				lstPages.get(Configure.countPages - 1).add(null);
				isLast = false;
			} else
				lstPages.get(Configure.countPages - 1).add("none");
		}
	}

	public void CleanItems() {
		lstItems = new ArrayList<String>();
		for (int i = 0; i < lstPages.size(); i++) {
			for (int j = 0; j < lstPages.get(i).size(); j++) {
				if (lstPages.get(i).get(j) != null
						&& !lstPages.get(i).get(j).equals("none")) {
					lstItems.add(lstPages.get(i).get(j).toString());
					System.out.println("-->"
							+ lstPages.get(i).get(j).toString());
				}
			}
		}
		System.out.println(lstItems.size());
		initData();
		lst_views.removeAllViews();
		gridviews = new ArrayList<DragGrid>();
		for (int i = 0; i < Configure.countPages; i++) {
			lst_views.addView(addGridView(i));
		}
		isClean = false;
		lst_views.snapToScreen(0);
	}

	public int getFristNonePosition(ArrayList<String> array) {
		for (int i = 0; i < array.size(); i++) {
			if (array.get(i) != null && array.get(i).toString().equals("none")) {
				return i;
			}
		}
		return -1;
	}

	public int getFristNullPosition(ArrayList<String> array) {
		for (int i = 0; i < array.size(); i++) {
			if (array.get(i) == null) {
				return i;
			}
		}
		return -1;
	}

	public LinearLayout addGridView(int pageNumber) {

		linear = new LinearLayout(getActivity());
		gridView = new DragGrid(getActivity());
		gridView.setAdapter(new ReaderItemAdapter(getActivity(), lstPages
				.get(pageNumber)));
		gridView.setNumColumns(2);
		gridView.setHorizontalSpacing(0);
		gridView.setVerticalSpacing(0);
		final int pn = pageNumber;
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					final int postion, long id) {
				if (lstPages.get(pn).get(postion) != null) {
					if (mListener != null) {
						BookmarkHistoryItem item = BookmarksWrapper
								.getBookmarkById(getActivity()
										.getContentResolver(), id);

						if (item != null) {
							mListener.onStartRssPageItemClicked(item.getUrl());
						}
					}

				} else {
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.Add)
							.setItems(R.array.items,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											final String[] arrayAddItems = getResources()
													.getStringArray(
															R.array.items); // array
											lstPages.get(pn).add(postion,
													arrayAddItems[which]);
											lstPages.get(pn)
													.remove(postion + 1);

											if (getFristNonePosition(lstPages
													.get(pn)) > 0
													&& getFristNullPosition(lstPages
															.get(pn)) < 0) {
												lstPages.get(pn)
														.set(getFristNonePosition(lstPages
																.get(pn)), null);
											}
											if (getFristNonePosition(lstPages
													.get(pn)) < 0
													&& getFristNullPosition(lstPages
															.get(pn)) < 0) {
												System.out.println("===");
												if (pn == Configure.countPages - 1
														|| (getFristNullPosition(lstPages.get(lstPages
																.size() - 1)) < 0 && getFristNonePosition(lstPages.get(lstPages
																.size() - 1)) < 0)) {
													lstPages.add(new ArrayList<String>());
													lstPages.get(
															lstPages.size() - 1)
															.add(null);
													for (int i = 1; i < PAGE_SIZE; i++)
														lstPages.get(
																lstPages.size() - 1)
																.add("none");

													lst_views
															.addView(addGridView(Configure.countPages));
													Configure.countPages++;
													imageView = new ImageView(
															getActivity());
													imageView
															.setLayoutParams(new LayoutParams(
																	20, 20));
													imageView.setPadding(20, 0,
															20, 0);
													imageView
															.setBackgroundResource(R.drawable.page_indicator);
													imageViews.add(imageView);
													pages.addView(imageView);
												} else if (getFristNonePosition(lstPages
														.get(lstPages.size() - 1)) > 0
														&& getFristNullPosition(lstPages.get(lstPages
																.size() - 1)) < 0) {
													lstPages.get(
															lstPages.size() - 1)
															.set(getFristNonePosition(lstPages
																	.get(lstPages
																			.size() - 1)),
																	null);
													((DateAdapter) ((gridviews.get(lstPages
															.size() - 1))
															.getAdapter()))
															.notifyDataSetChanged();
												}
											}
											((DateAdapter) ((gridviews.get(pn))
													.getAdapter()))
													.notifyDataSetChanged();
										}
									})
							.setNegativeButton(R.string.Cancel,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).show();
				}
			}
		});
		gridView.setSelector(R.anim.grid_light);
		gridView.setPageListener(new DragGrid.G_PageListener() {
			@Override
			public void page(int cases, int page) {
				switch (cases) {
				case 0:// 滑动页面
					lst_views.snapToScreen(page);
					setCurPage(page);
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							Configure.isChangingPage = false;
						}
					}, 800);
					break;
				case 1:// 删除按钮上来
					delImage.setBackgroundResource(R.drawable.del);
					delImage.setVisibility(0);
					delImage.startAnimation(up);
					break;
				case 2:// 删除按钮变深
					delImage.setBackgroundResource(R.drawable.del_check);
					Configure.isDelDark = true;
					break;
				case 3:// 删除按钮变淡
					delImage.setBackgroundResource(R.drawable.del);
					Configure.isDelDark = false;
					break;
				case 4:// 删除按钮下去
					delImage.startAnimation(down);
					break;
				case 5:// 松手动作
					delImage.startAnimation(down);
					// Configure.isDelRunning = false;
					lstPages.get(Configure.curentPage).add(
							Configure.removeItem, null);
					lstPages.get(Configure.curentPage).remove(
							Configure.removeItem + 1);
					((DateAdapter) ((gridviews.get(Configure.curentPage))
							.getAdapter())).notifyDataSetChanged();
					break;
				}
			}
		});
		gridView.setOnItemChangeListener(new DragGrid.G_ItemChangeListener() {
			@Override
			public void change(int from, int to, int count) {
				String toString = (String) lstPages.get(
						Configure.curentPage - count).get(from);

				lstPages.get(Configure.curentPage - count).add(from,
						(String) lstPages.get(Configure.curentPage).get(to));
				lstPages.get(Configure.curentPage - count).remove(from + 1);
				lstPages.get(Configure.curentPage).add(to, toString);
				lstPages.get(Configure.curentPage).remove(to + 1);

				((DateAdapter) ((gridviews.get(Configure.curentPage - count))
						.getAdapter())).notifyDataSetChanged();
				((DateAdapter) ((gridviews.get(Configure.curentPage))
						.getAdapter())).notifyDataSetChanged();
			}
		});
		gridviews.add(gridView);
		linear.addView(gridviews.get(pageNumber), param);
		return linear;
	}

	public void runAnimation() {
		down = AnimationUtils.loadAnimation(getActivity(), R.anim.del_down);
		up = AnimationUtils.loadAnimation(getActivity(), R.anim.del_up);
		down.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				delImage.setVisibility(8);
			}
		});

		right = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f,
				Animation.RELATIVE_TO_PARENT, -1f,
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				0f);
		left = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1f,
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT,
				0f, Animation.RELATIVE_TO_PARENT, 0f);
		right.setDuration(25000);
		left.setDuration(25000);
		right.setFillAfter(true);
		left.setFillAfter(true);

		right.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				runImage.startAnimation(left);
			}
		});
		left.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				runImage.startAnimation(right);
			}
		});
		runImage.startAnimation(right);
	}

	public void setCurPage(final int page) {

		for (int i = 0; i < imageViews.size(); i++) {
			if (page == i) {

				imageViews.get(i).setBackgroundResource(
						R.drawable.page_indicator_focused);
			} else {
				imageViews.get(i).setBackgroundResource(
						R.drawable.page_indicator);
			}
			// imageViews.set(i,imageView);
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		sm.unregisterListener(lsn);
	}

}
