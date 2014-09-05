package net.htjs.mobile.nyoa;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.htjs.mobile.nyoa.db.ConfigInfo;
import net.htjs.mobile.nyoa.gw.ListGwWebView;
import net.htjs.mobile.nyoa.subview.BadgeView;
import net.htjs.mobile.nyoa.task.ProgressBarasyncTask;
import net.htjs.mobile.nyoa.updateapk.Myinfodata;
import net.htjs.mobile.nyoa.util.L;
import net.htjs.mobile.nyoa.util.Util;
import net.htjs.mobile.nyoa.util.UtilIntent;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ListViewMain extends BaseActivity {
	ArrayList array = null;
	private List meumList;
	String uri;

	ListView lv;
	SimpleAdapter adapter;
	ImageAdapter saMenuItem;
	// GetListHandler handler = null;
	ProgressDialog progressDialog;

	private static String TAG = "listview";
	private static String[] appName = { "待办公文              ",
			"公文列表              ",/* "网站                      ",*/
			"电子公告              ", "电子邮件              ",
			"通讯录                  ", "皮肤切换              " };
	private static String[] infoName = { "待办公文", "公文列表", /*"网站", */"电子公告", "电子邮件",
			"通讯录", "皮肤切换" };

	String s_thisversion = null;
	String s_preurl = null;

	public void updateNewVersion() {
		Util.Tag("开始检查更新 ", this.getApplicationContext());

		final Myinfodata mi = new Myinfodata();
		
		new Thread(new Runnable(){
		    @Override
		    public void run() {
		    	final Myinfodata myinfo = mi.getmyinfodata();
				if (myinfo.NewVersion != null) {
					if (myinfo.NewVersion.compareTo(s_thisversion) > 0) {
						Util.dialog(ListViewMain.this, "新版本已发布，是否升级？", "立即升级", "以后再升级",
								ListViewMain.this, "updateApp",
								new Object[] { myinfo }, ListViewMain.this, null, null,
								false);

					} else {
						L.l("不需要更新");
					}

				} else {
					L.l("取得版本信息异常");
				}
		    }
		}).start();
		
	}

	protected static String dowloadDir = Environment.getExternalStorageDirectory() + "/ideasdownload/";
	ProgressDialog xh_pDialog;
	 int xh_count;
	protected String filePath = "";

	public void updateApp(Myinfodata myinfo) {

		if (L.LocationToInstall) {
			dowloadDir = "/sdcard/ideasdownload/";
		}
		filePath = dowloadDir + "nyoa" + myinfo.NewVersion + ".apk";
		this.download(myinfo.DownUrl, filePath);
	}

	public void download(final String url, String fileName) {

		/*
		 * 下载进度条初始化
		 */
		xh_count = 0;

		// 创建ProgressDialog对象
		xh_pDialog = new ProgressDialog(ListViewMain.this);

		// 设置进度条风格，风格为长条形的
		xh_pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		xh_pDialog.setTitle("下载提示");
		xh_pDialog.setMessage("下载文件进度");
		xh_pDialog.setIndeterminate(false);
		xh_pDialog.setProgress(100);
		xh_pDialog.setIcon(R.drawable.logonyoa);

		xh_pDialog.setCancelable(true);

		xh_pDialog.show();

		new Thread() {
			@Override
			public void run() {
				try {
					while (xh_count >= 0 && xh_count < 100) {
						// 由线程来控制进度
						xh_pDialog.setProgress(xh_count);
						Thread.sleep(100);
					}
					if (xh_count < 0) {
						Util.dialog(ListViewMain.this, "下载失败，是否重新下载？", "重新下载",
								"取消", ListViewMain.this, "download",
								new String[] { url, filePath }, null,
								"openFile", new String[] { url, filePath },
								false);

					} else {
						xh_pDialog.cancel();
						Thread.sleep(500);
						Intent intent = UtilIntent.getApkIntent(filePath);
						startActivity(intent);
					}

				} catch (Exception e) {
					xh_pDialog.cancel();
				}
			}
		}.start();
		// 获取SD卡目录
		File file = new File(dowloadDir);
		// 创建下载目录
		if (!file.exists()) {
			file.mkdirs();
		}

		// 读取下载线程数，如果为空，则单线程下载
		int downloadTN = 1;

		new downloadTask(url, Integer.valueOf(downloadTN), fileName).start();
	}

	/**
	 * @author ideasandroid 主下载线程
	 */
	public class downloadTask extends Thread {
		protected int blockSize, downloadSizeMore;
		private int threadNum = 5;
		String urlStr, threadNo, fileName;

		public downloadTask(String urlStr, int threadNum, String fileName) {
			this.urlStr = urlStr;
			this.threadNum = threadNum;
			this.fileName = fileName;
		}

		@Override
		public void run() {
			Log.d("ListView", urlStr);
			Log.d("ListView", fileName);
			try {
				HttpPost request = new HttpPost(urlStr);
				DefaultHttpClient client = new DefaultHttpClient();
				HttpResponse httpResponse = client.execute(request);

				File file = new File(fileName);
			 
				RandomAccessFile fos = new RandomAccessFile(file, "rw");
				fos.write(EntityUtils.toByteArray(httpResponse.getEntity()));
				byte[] ab = new byte[1024];
				fos.read(ab, 0, 1024);
				System.out.println(new String(ab));
				fos.close();

				xh_count = new Random().nextInt(30) + 20;

				sleep(300);
				xh_count = new Random().nextInt(50) + 20;

				sleep(300);
				xh_count = new Random().nextInt(70) + 20;

				sleep(300);
				xh_count = 100;
				sleep(300);

			} catch (Exception e) {
				xh_count = -1;
				e.printStackTrace();
			}

		}
	}

	public void finish() {
		if (L.isNotUseWIFI)
			unregisterReceiver(mWifiStateReceiver);
		if (L.isUseAPN)
			apnutility.StopYidongApn();
		super.finish();
	}

	public void finishun() {
		if (L.isNotUseWIFI)
			unregisterReceiver(mWifiStateReceiver);
		super.finish();
	}

	WifiStateReceiver mWifiStateReceiver = null;

	public void registerReceiversWIFI() {
		if (L.isNotUseWIFI) {
			mWifiStateReceiver = new WifiStateReceiver();
			registerReceiver(mWifiStateReceiver, new IntentFilter(
					WifiManager.WIFI_STATE_CHANGED_ACTION));
		}

	}

	int[] WIFI_STATES = { 0, 1, 2, 3, 4 };

	class WifiStateReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			Bundle bundle = intent.getExtras();
			int oldInt = bundle.getInt("previous_wifi_state");
			int newInt = bundle.getInt("wifi_state");
			String oldStr = (oldInt >= 0 && oldInt < WIFI_STATES.length) ? WIFI_STATES[oldInt]
					+ ""
					: "?";
			String newStr = (newInt >= 0 && oldInt < WIFI_STATES.length) ? WIFI_STATES[newInt]
					+ ""
					: "?";
			Log.e(TAG, "oldS=" + oldStr + ", newS=" + newStr);
			if (newInt == WifiManager.WIFI_STATE_DISABLED
					|| newInt == WifiManager.WIFI_STATE_ENABLED) {
				onWifiStateChange(); // define this function elsewhere!
			} else if (newInt == WifiManager.WIFI_STATE_DISABLING
					|| newInt == WifiManager.WIFI_STATE_ENABLING) {
			}
		}
	}

	WifiManager mMainWifi = null;

	private void onWifiStateChange() {

		String ip_str = "";

		WifiInfo info = mMainWifi.getConnectionInfo();
		if (info != null) {
			int ipaddr = info.getIpAddress();
			ip_str = " (ip=" + StringizeIp(ipaddr) + ")";
		}

		if (mMainWifi.isWifiEnabled() == true)
			mMainWifi.setWifiEnabled(false);

	}

	public static String StringizeIp(int ip) {
		int ip4 = (ip >> 24) & 0x000000FF;
		int ip3 = (ip >> 16) & 0x000000FF;
		int ip2 = (ip >> 8) & 0x000000FF;
		int ip1 = ip & 0x000000FF;
		return Integer.toString(ip1) + "." + ip2 + "." + ip3 + "." + ip4;
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.xinwenzhanshi);
		/*if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}*/
		array = null;

		s_thisversion = getResources().getText(R.string.version).toString();
		s_preurl = getResources().getText(R.string.urlheader).toString();
		meumList = new ArrayList();
		for (int i = 1; i < 7; i++) {

			HashMap<String, Object> map = new HashMap<String, Object>();

			map.put("ItemImage", R.drawable.icon);
			{
				if (i <= appName.length) {
					map.put("ItemText", appName[i - 1]);
					map.put("InfoText", infoName[i - 1]);
				} else
					map.put("ItemText", "未知app " + (i - appName.length));
			}

			if (i == 1) {
				map.put("ItemImage", R.drawable.icon_11db);
				map.put("ItemImageF", R.drawable.icon_12db);
			} else if (i == 2) {
				map.put("ItemImage", R.drawable.icon_21zb);
				map.put("ItemImageF", R.drawable.icon_22zb);
			} /*else if (i == 3) {
				map.put("ItemImage", R.drawable.icon_31sy);
				map.put("ItemImageF", R.drawable.icon_32sy);
			}*/ else if (i == 3) {
				map.put("ItemImage", R.drawable.icon_41gg);
				map.put("ItemImageF", R.drawable.icon_42gg);
			} else if (i == 4) {
				map.put("ItemImage", R.drawable.icon_51mail);
				map.put("ItemImageF", R.drawable.icon_52mail);
			} else if (i == 5) {
				map.put("ItemImage", R.drawable.icon_61txl);
				map.put("ItemImageF", R.drawable.icon_62txl);
			} else if (i == 6) {
				map.put("ItemImage", R.drawable.icon_71pfqh);
				map.put("ItemImageF", R.drawable.icon_72pfqh);
			}
			meumList.add(map);
		}

		saMenuItem = new ImageAdapter(this, meumList, // 数据源
				R.layout.xinwenzhanshi_item, // xml实现
				new String[] { "ItemImage", "ItemText", "ItemText1" }, // 对应map的Key
				new int[] { R.id.ItemImage_list, R.id.title_list,
						R.id.info_list }); // 对应R的Id
		lv = (ListView) ListViewMain.this.findViewById(R.id.listview);
		lv.setAdapter(saMenuItem);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				L.l(arg2 + "|||||||||||||||");
				if (arg2 == 0) {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();

					bundle.putString(L.ZT, "DB");
					bundle.putString(L.LX, L.Nine.GW);

					intent.setClass(ListViewMain.this, ListGwWebView.class);

					intent.putExtras(bundle);

					startActivity(intent);
					return;
				} else if (arg2 == 1) {
					new AlertDialog.Builder(ListViewMain.this)
							.setTitle(appName[1])
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(
									new String[] { "在办公文", "所有公文" }, 0,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (which == 1) {
												Intent intent = new Intent();

												Bundle bundle = new Bundle();

												bundle.putString(L.ZT, "SY");
												bundle.putString(L.LX,
														L.Nine.GW);

												intent.setClass(
														ListViewMain.this,
														ListGwWebView.class);

												intent.putExtras(bundle);

												startActivity(intent);
												return;
											} else if (which == 0) {
												Intent intent = new Intent();

												Bundle bundle = new Bundle();

												bundle.putString(L.ZT, "ZB");
												bundle.putString(L.LX,
														L.Nine.GW);

												intent.setClass(
														ListViewMain.this,
														ListGwWebView.class);

												intent.putExtras(bundle);

												startActivity(intent);
												return;
											}
										}
									})
							.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}

									}).show();

				} /*else if (arg2 == 2) {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();

					bundle.putString(L.ZT, "SY");
					bundle.putString(L.LX, L.Nine.WZ);

					intent.setClass(ListViewMain.this, ListGwWebView.class);

					intent.putExtras(bundle);

					startActivity(intent);
					return;
				} */else if (arg2 == 2) {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();

					bundle.putString(L.ZT, "SY");
					bundle.putString(L.LX, L.Nine.DZGG);

					intent.setClass(ListViewMain.this, ListGwWebView.class);

					intent.putExtras(bundle);

					startActivity(intent);
					return;
				} else if (arg2 == 3) {
					
					new Thread(new Runnable(){
					    @Override
					    public void run() {

							HttpPost request = new HttpPost(L.mailAuthUrl);

							JSONObject param = new JSONObject();
							try {
								// 绑定到请求 Entry
								StringEntity se = new StringEntity(param.toString());
								request.setEntity(se);
								// 发送请求
								DefaultHttpClient client = new DefaultHttpClient();
								client.getCookieStore().addCookie(L.cookie);
								HttpResponse httpResponse = client.execute(request);
								// 得到应答的字符串，这也是一个 JSON 格式保存的数据
								String retSrc = EntityUtils.toString(httpResponse
										.getEntity());
								// 生成 JSON 对象
								System.out.println("______________________" + retSrc);
								JSONObject ja = new JSONObject(retSrc.trim()
										.replaceAll("msg", "").replaceAll("=", ""));

								if ("0".equals(ja.getString("ret_code"))) {
									L.Nine.MAILSESSIONID = ja.getString("sessionid");
									List<Cookie> cookies = client.getCookieStore()
											.getCookies();
									if (!cookies.isEmpty()) {
										for (int i = 0; i < cookies.size(); i++) {
											L.mailcookie = cookies.get(i);
										}
									}
									Intent intent = new Intent();

									Bundle bundle = new Bundle();

									bundle.putString(L.LX, L.Nine.DZYX);

									intent.setClass(ListViewMain.this,
											ListGwWebView.class);

									intent.putExtras(bundle);
									startActivity(intent);
									return;
								} else {
									AlertDialog.Builder builder = new Builder(
											ListViewMain.this);
									builder.setTitle("提示");
									builder.setMessage("用户名或密码错误");
									Dialog dialog = builder.create();
									builder.setPositiveButton("确定",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													// 这里添加点击确定后的逻辑
													dialog.dismiss();
												}
											});

									builder.create().show();
								}
							} catch (JSONException e) {
								e.printStackTrace();
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (ClientProtocolException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
					    }
					}).start();
					

				} else if (arg2 == 4) {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();

					bundle.putString(L.LX, L.Nine.TXL);

					intent.setClass(ListViewMain.this, ListGwWebView.class);

					intent.putExtras(bundle);

					startActivity(intent);
					return;
				} else if (arg2 == 5) {
					if ("1".equals(pattern)) {
						new AlertDialog.Builder(ListViewMain.this)
								.setTitle("请选择皮肤")
								.setIcon(android.R.drawable.ic_dialog_info)
								.setSingleChoiceItems(
										new String[] { "红黄色皮肤", "蓝白色皮肤" }, 1,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												if (which == 0) {
													dbh.addConfigInfo(new ConfigInfo(
															0, "1", "0"));
													pattern = "0";
													Intent intent = new Intent();

													intent.setClass(
															ListViewMain.this,
															TabMainActivity.class);

													startActivity(intent);
													dialog.dismiss();
													ListViewMain.this
															.finishun();
												} else if (which == 1) {
													dialog.dismiss();
												}
											}
										})
								.setNegativeButton("取消",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
											}

										}).show();
					}
					return;
				}
			}
		});

		mMainWifi = (WifiManager) getApplicationContext().getSystemService(
				Service.WIFI_SERVICE);
		if (L.isNotUseWIFI)
			registerReceiversWIFI();
	}

	public class ImageAdapter extends SimpleAdapter {
		private Context mContext;
		private LayoutInflater lif;
		int icon[];

		public ImageAdapter(Context c, List l, int t, String[] a, int[] i) {
			super(c, l, t, a, i);
			mContext = c;
			lif = LayoutInflater.from(c);
			icon = i;
		}

		public int getCount() {
			return meumList.size();
		}

		public Object getItem(int position) {
			return meumList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public void exchange(int startPosition, int endPosition) {
			// 比较一下 使startPosition永远小于endPosition的值 解决问题1 ，2
			if (startPosition > endPosition) {
				int temp = endPosition;
				endPosition = startPosition;
				startPosition = temp;
			}
			Object endObject = getItem(endPosition);
			Object startObject = getItem(startPosition);
			System.out.println(startPosition + "========" + endPosition);
			meumList.set(startPosition, endObject);
			meumList.set(endPosition, startObject);
			notifyDataSetChanged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// ImageView iv;
			if (convertView == null) {
				convertView = lif.inflate(R.layout.xinwenzhanshi_item, null);
			}

			try {
				final View zuheView = convertView.findViewById(R.id.zhuhe_list);
				final TextView infotextView = (TextView) convertView
						.findViewById(R.id.info_list);
				infotextView.setText(((Map) meumList.get(position)).get(
						"InfoText").toString());
				final TextView textView = (TextView) convertView
						.findViewById(R.id.title_list);
				textView.setText(((Map) meumList.get(position)).get("ItemText")
						.toString());
				final ImageView imgView = (ImageView) convertView
						.findViewById(R.id.ItemImage_list);
				final ImageView arrowView = (ImageView) convertView
						.findViewById(R.id.arrow_list);
				arrowView.setBackgroundResource(R.drawable.list_jt1);

				final Object o = ((Map) meumList.get(position))
						.get("ItemImage");
				final Object of = ((Map) meumList.get(position))
						.get("ItemImageF");
				if (null == o) {
					imgView.setBackgroundResource(R.drawable.icon);
				} else {
					imgView.setBackgroundResource(Integer.valueOf(o.toString()));
				}
				convertView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View arg0, MotionEvent arg1) {
						return false;
					}

				});
				textView.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							arrowView
									.setBackgroundResource(R.drawable.list_jt2);
							imgView.setBackgroundResource(Integer.valueOf(of
									.toString()));
							v.setBackgroundColor(0x1A7CBD);
							textView.setBackgroundColor(0xffffff);
							infotextView.setBackgroundColor(0xB5DBF7);
						} else {
							arrowView.setBackgroundResource(R.drawable.star);
							imgView.setBackgroundResource(Integer.valueOf(o
									.toString()));
							textView.setBackgroundColor(0x0E4061);
							infotextView.setBackgroundColor(0xA8A8A8);
							v.setBackgroundColor(0xffffff);
						}
					}
				});
				if (position == 0
						&& (L.notificationNum.get("db") != null && L.notificationNum
								.get("db").length() > 0)) {
					BadgeView badge1 = new BadgeView(ListViewMain.this, imgView);
					badge1.setText(L.notificationNum.get("db"));
					badge1.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
					badge1.show();
				} else if (position == 1
						&& (L.notificationNum.get("zb") != null && L.notificationNum
								.get("zb").length() > 0)) {
					BadgeView badge1 = new BadgeView(ListViewMain.this, imgView);
					badge1.setText(L.notificationNum.get("zb"));
					badge1.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
					badge1.show();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

	}

	public void refreshNum() {
		saMenuItem.notifyDataSetChanged();
		lv.setAdapter(saMenuItem);
	}

	public void onResume() {
		final Handler mHandler = new Handler() {

			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					if (!L.HASUPDATE) {
						updateNewVersion();
						L.HASUPDATE = true;
					}
					refreshNum();
					break;
				}
			};
		};
		new Thread(new Runnable(){
		    @Override
		    public void run() {
		    	ProgressBarasyncTask asyncTask=new ProgressBarasyncTask(mHandler);
		        asyncTask.execute(3000);
		    }
		}).start();
		
		super.onResume();
	}
}
