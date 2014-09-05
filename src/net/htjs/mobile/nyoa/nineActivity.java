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
import net.htjs.mobile.nyoa.db.DatabaseHandler;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class nineActivity extends BaseActivity {
	private GridView gridview;
	private List meumList;
	static int count=9;
	private static String[] appName = { "待办公文", "公文列表", /*"网站",*/ "电子公告", "电子邮件",
			"通讯录", "皮肤切换" };
	protected static String dowloadDir = Environment.getExternalStorageDirectory() + "/ideasdownload/";
	    
	protected String filePath = "";
	
	ImageAdapter saMenuItem;
	
	public void refreshNum(){
		saMenuItem.notifyDataSetChanged();
		gridview.setAdapter(saMenuItem);
	}
	
	String s_thisversion=null;
	String s_preurl=null;
	public void updateNewVersion(){
		Util.Tag("开始检查更新 ", this.getApplicationContext());
		final Myinfodata mi = new Myinfodata();
		
		new Thread(new Runnable(){
		    @Override
		    public void run() {
		    	final Myinfodata myinfo = mi.getmyinfodata();
				if (myinfo.NewVersion != null) {
					if (myinfo.NewVersion.compareTo(s_thisversion) > 0) {
						Util.dialog(nineActivity.this, "新版本已发布，是否升级？", "立即升级", "以后再升级",
								nineActivity.this, "updateApp",
								new Object[] { myinfo }, nineActivity.this, null, null,
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
	public void updateApp(Myinfodata myinfo){
		 
		if(L.LocationToInstall){
			dowloadDir="/sdcard/ideasdownload/";
		}
	    filePath = dowloadDir +  "nyoa" + myinfo.NewVersion + ".apk";
		this.download(myinfo.DownUrl, filePath);
	}

	public void onResume(){
		 final Handler mHandler = new Handler(){  
	          
		        public void handleMessage(Message msg) {  
		            switch (msg.what) {  
		            case 1:  
		            	if(!L.HASUPDATE){
		            		updateNewVersion();
		            		L.HASUPDATE=true;
		            	}
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
	
	
	@SuppressLint({ "NewApi", "NewApi", "NewApi" })
	public void onCreate(Bundle b) {
		super.onCreate(b);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.nine);
		Resources res = getResources();
		Drawable drawable = res.getDrawable(R.drawable.background_login);
		this.getWindow().setBackgroundDrawable(drawable);

		s_thisversion = getResources().getText(R.string.version).toString();
		s_preurl = getResources().getText(R.string.urlheader).toString();
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		dbh = new DatabaseHandler(this);
		
		gridview = (GridView) findViewById(R.id.GridView); // 构造GridView组件
		gridview.setNumColumns(3);

		meumList = new ArrayList();
		for (int i = 1; i < 7; i++) {

			HashMap<String, Object> map = new HashMap<String, Object>();

			map.put("ItemImage", R.drawable.icon);
			{
				if (i <= appName.length) {
					map.put("ItemText", appName[i - 1]);
				} else
					map.put("ItemText", "未知app " + (i - appName.length));
			}

			if (i == 1) {
				map.put("ItemImage", R.drawable.dbgw);
			} else if (i == 122) {//网站
				map.put("ItemImage", R.drawable.zbgw);
			} else if (i == 2) {
				map.put("ItemImage", R.drawable.sygw);
			} else if (i == 3) {
				map.put("ItemImage", R.drawable.dzgg);
			} else if (i == 4) {
				map.put("ItemImage", R.drawable.dzyj);
			} else if (i == 5) {
				map.put("ItemImage", R.drawable.txl);
			}else if (i == 6) {
				map.put("ItemImage",  R.drawable.pfqh);
			}
			meumList.add(map);
		}

		saMenuItem = new ImageAdapter(this, meumList, // 数据源
				R.layout.menuitem, // xml实现
				new String[] { "ItemImage", "ItemText", "ItemText1" }, // 对应map的Key
				new int[] { R.id.ItemImage, R.id.ItemText }); // 对应R的Id

		gridview.setAdapter(saMenuItem);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				L.l(arg2+"|||||||||||||||");
				if (arg2 == 0) {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();

					bundle.putString(L.ZT, "DB");
					bundle.putString(L.LX, L.Nine.GW);

					intent.setClass(nineActivity.this, ListGwWebView.class);

					intent.putExtras(bundle);

					nineActivity.this.startActivity(intent);
					return;
				} else if (arg2 == 1) {
					
					new AlertDialog.Builder(nineActivity.this).setTitle(appName[1]).setIcon(
						    android.R.drawable.ic_dialog_info).setSingleChoiceItems(
					     new String[] { "在办公文", "所有公文"  },0,new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if(which==1){
									Intent intent = new Intent();

									Bundle bundle = new Bundle();

									bundle.putString(L.ZT, "SY");
									bundle.putString(L.LX, L.Nine.GW);

									intent.setClass(nineActivity.this, ListGwWebView.class);

									intent.putExtras(bundle);

									nineActivity.this.startActivity(intent);
									return;								
								}else if(which==0){
									Intent intent = new Intent();

									Bundle bundle = new Bundle();

									bundle.putString(L.ZT, "ZB");
									bundle.putString(L.LX, L.Nine.GW);

									intent.setClass(nineActivity.this, ListGwWebView.class);

									intent.putExtras(bundle);

									nineActivity.this.startActivity(intent);
									return;
								}
							}
						  }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
						 }).show();
				} else if (arg2 == 122) {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();

					bundle.putString(L.ZT, "SY");
					bundle.putString(L.LX, L.Nine.WZ);

					intent.setClass(nineActivity.this, ListGwWebView.class);

					intent.putExtras(bundle);

					nineActivity.this.startActivity(intent);
					return;
				} else if (arg2 == 2) {
					Intent intent = new Intent();

					Bundle bundle = new Bundle();

					bundle.putString(L.ZT, "SY");
					bundle.putString(L.LX, L.Nine.DZGG);

					intent.setClass(nineActivity.this, ListGwWebView.class);

					intent.putExtras(bundle);

					nineActivity.this.startActivity(intent);
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

									intent.setClass(nineActivity.this,
											ListGwWebView.class);

									intent.putExtras(bundle);
									nineActivity.this.startActivity(intent);
									return;
								} else {
									AlertDialog.Builder builder = new Builder(
											nineActivity.this);
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

					intent.setClass(nineActivity.this, ListGwWebView.class);

					intent.putExtras(bundle);

					nineActivity.this.startActivity(intent);
					return;
				} else if (arg2 == 5) {
					
					if("0".equals(pattern)){
						 
						
						new AlertDialog.Builder(nineActivity.this).setTitle("请选择皮肤").setIcon(
							    android.R.drawable.ic_dialog_info).setSingleChoiceItems(
						     new String[] { "红黄色皮肤", "蓝白色皮肤"  },0,new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if(which==1){
										dbh.addConfigInfo(new ConfigInfo(0, "1","1"));
										pattern="1";
										Intent intent = new Intent();
	
										intent.setClass(nineActivity.this, ListViewMain.class);
				 
										nineActivity.this.startActivity(intent);
										dialog.dismiss();
										((TabMainActivity)(nineActivity.this.getParent())).finish4Cancel();
									}else if(which==0){
										dialog.dismiss();
									}
								}
							 }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
							}).show();
					} else if("1".equals(pattern)){
						 
						new AlertDialog.Builder(nineActivity.this).setTitle("请选择皮肤").setIcon(
							    android.R.drawable.ic_dialog_info).setSingleChoiceItems(
						     new String[] { "红黄色皮肤", "蓝白色皮肤"  },0,new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									if(which==1){
										dbh.addConfigInfo(new ConfigInfo(1, "1","0"));
										pattern="0";
										Intent intent = new Intent();
	
										intent.setClass(nineActivity.this, ListViewMain.class);
				 
										nineActivity.this.startActivity(intent);
										dialog.dismiss();
										((TabMainActivity)(nineActivity.this.getParent())).finish4Cancel();
									}else if(which==0){
										dialog.dismiss();
									}
								}
							 }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
							}).show();
					}
				}
			}
		});
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

		/**
		 * 遇到的问题： 1、删除错了 会有重复的图片 这个是由 remove引起的 删除时 我们先删除了小的position 比如说
		 * startPosition = 2;endPosition = 3; 当我先删startPosition时 这时
		 * 删除前position为3的项 已经是position为2了 2、数组越界 异常 这个是由 add引起的 比如说
		 * startPosition = 8;endPosition = 7; 一共gridview有9个元素 也就是说8 已经是最大的了
		 * 当删除完后 你先增加爱 startposition时 就会异常了 3、preview问题 当我拖拽互换几次后 机会出现 当前的图片
		 * 显示的是另一个图片的preview
		 * 
		 * 得调用 destroyDrawingCache
		 * 
		 * @param startPosition
		 * @param endPosition
		 */
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
				convertView = lif.inflate(R.layout.menuitem, null);
			}

			try {

				TextView textView = (TextView) convertView
						.findViewById(R.id.ItemText);
				textView.setText(((Map) meumList.get(position)).get("ItemText")
						.toString());
				ImageView imgView = (ImageView) convertView
						.findViewById(R.id.ItemImage);

				Object o = ((Map) meumList.get(position)).get("ItemImage");
				if (null == o) {
					imgView.setBackgroundResource(R.drawable.icon);
				} else {
					imgView.setBackgroundResource(Integer.valueOf(o.toString()));
				}
				if (position == 0
						&& (L.notificationNum.get("db") != null && L.notificationNum
								.get("db").length() > 0)) {
					BadgeView badge1 = new BadgeView(nineActivity.this, imgView);
					badge1.setText(L.notificationNum.get("db"));
					badge1.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
					badge1.show();
				} else if (position == 1
						&& (L.notificationNum.get("zb") != null && L.notificationNum
								.get("zb").length() > 0)) {
					BadgeView badge1 = new BadgeView(nineActivity.this, imgView);
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
	
	ProgressDialog progressDialog, xh_pDialog;
    int xh_count;
    
	public void download(final String url, String fileName) {
		
        /*
         * 下载进度条初始化
         */
        xh_count = 0;

        // 创建ProgressDialog对象
        xh_pDialog = new ProgressDialog(nineActivity.this);

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
                                while (xh_count>=0&&xh_count < 100) {
                                        // 由线程来控制进度
                                        xh_pDialog.setProgress(xh_count);
                                        Thread.sleep(100);
                                }
                                if(xh_count<0){
                                	Util.dialog(nineActivity.this, "下载失败，是否重新下载？",
                                            "重新下载", "取消", nineActivity.this, "download",
                                            new String[] { url, filePath },
                                            null, "openFile", new String[] {
                                                            url, filePath }, false);
                                	
                                }else{
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
                        /*
                         * if(!file.exists()){ file.createNewFile(); }
                         */
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
                		xh_count=-1;
                        e.printStackTrace();
                }

        }
	}
}
