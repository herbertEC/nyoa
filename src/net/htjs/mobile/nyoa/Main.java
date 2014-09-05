package net.htjs.mobile.nyoa;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.htjs.mobile.nyoa.apn.NetworkChangeReceiver;
import net.htjs.mobile.nyoa.updateapk.Myinfodata;
import net.htjs.mobile.nyoa.util.L;
import net.htjs.mobile.nyoa.util.Util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import xink.vpn.Constants;
import xink.vpn.Utils;
import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apn.util.ApnUtility;
import com.hnca.Base64Util;
import com.hnca.FileService;
import com.hnca.HNCAMCertTool;

public class Main extends BaseActivity {
	private static String TAG = "main";//标签日志
	private ConnectivityManager cgr;
	private NetworkInfo netinfo;//网络状态类
	private TelephonyManager tm;//电话信息类
	private TextView loadingText;
	// VPN连接服务相关
	private BroadcastReceiver stateBroadcastReceiver;
	private VpnProfile vp;
	private int count = 0;
	private KeyStore keyStore;
	private Runnable resumeAction;
	private static VpnState memory;
	private HNCAMCertTool tool = new HNCAMCertTool();

	private static boolean isNeedToReboot = false;

	// APN区域
	private ApnUtility apnutility = null;

	@Override
	protected void onStart() {

		super.onStart();

		if (isNeedToReboot) {
			return;
		}
		
		netinfo = cgr.getActiveNetworkInfo();// 取得网络连接信息
		System.out.println(netinfo);
		State mobile = cgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		
		State wifi = cgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();

		
		if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
			
			if(L.isNotUseWIFI){
				Util.dialogOnlyCommit(Main.this, "请关闭WLAN后再使用", "关闭应用", "继续操作",Main.this, "finishAndCloseWLAN", null);
				WifiManager wifiManager = (WifiManager) getApplicationContext()
						.getSystemService(Service.WIFI_SERVICE);
				Util.Tag("关闭WIFI网卡", getApplicationContext());
				// 关闭WIFI网卡
				wifiManager.setWifiEnabled(false);
				// 获取网卡当前的状态
			}
					
			this.loginChose();// 选择登陆方式（登陆窗口、CA卡登陆）
			return;
			
		} else if (mobile == State.CONNECTED || mobile == State.CONNECTING) {

			L.l("+++++++++++++++++++++++++");
			
			loadingText.setText(getString(R.string.vpnconnecting));
			this.loginChose();// 选择登陆方式（登陆窗口、CA卡登陆）
			return;
		} else {
			if (netinfo == null || !netinfo.isConnectedOrConnecting()) {
				Util.dialog(Main.this, "网络链接不存在", "关闭应用", "继续操作", Main.this,
						"finish", null, Main.this, "jumpToLoginPage", null,
						false);
			} else if (!netinfo.isAvailable()) {
				Util.dialog(Main.this, "网络链接不存在", "关闭应用", "继续操作", Main.this,
						"finish", null, Main.this, "jumpToLoginPage", null,
						false);
			} else if (!netinfo.isConnected()) {
				Util.dialog(Main.this, "网络链接不存在", "关闭应用", "继续操作", Main.this,
						"finish", null, Main.this, "jumpToLoginPage", null,
						false);
			}
			
			return;
		}
	}

	public void finishAndCloseWLAN() {
		finish();
		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		setTitle(R.string.app_name);
		LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout0001);
		
		this.partternChose();// 选择皮肤模式
		
		s_thisversion = getResources().getText(R.string.version).toString();// 选择本机版本
		s_preurl = getResources().getText(R.string.urlheader).toString();// 选择更新URL
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);// 获取sim卡信息
		cgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		String ProvidersName = null;
		// 返回唯一的用户ID;就是这张卡的编号
		String IMSI = tm.getSubscriberId();
		// IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
		System.out.println(IMSI);
		if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
			ProvidersName = "中国移动";
		} else {
		}
		
		apnutility = new ApnUtility(this);// 初始化APN连接工具
		

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		
		loadingText = (TextView) findViewById(R.id.loding_text);
		TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
		versionNumber.setText("Version "
				+ getResources().getText(R.string.version).toString());

		
		if ("0".equals(pattern)) {
			ll.setBackgroundDrawable(this.getApplicationContext()
					.getResources().getDrawable(R.drawable.loading));
		} else {
			ll.setBackgroundDrawable(this.getApplicationContext()
					.getResources().getDrawable(R.drawable.loadinga));
			versionNumber.setTextColor(getResources().getColor(
					R.color.list_title_focusyes_color));
			loadingText.setTextColor(getResources().getColor(
					R.color.list_title_focusyes_color));
		}
		
		String version = android.os.Build.VERSION.SDK;//
		Util.Tag("固件版本号为 " + version, this.getApplicationContext());
		int versionInt=new Integer(version).intValue();
		if (L.isUseCA && versionInt <= 1 && tool != null && Util.checkSDCard())// 系统版本号小于等于13且SDcard存在
		{
			try {
				Util.Tag("检测到CA卡，开始初始化", this.getApplicationContext());
				long lmsg = 0;
				try {
					lmsg = tool.Initialize();
				} catch (java.lang.UnsatisfiedLinkError e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				Util.Tag("第 " + (count++) + " 次消息",
						this.getApplicationContext());
				if (lmsg == 1L) {
					FileService fs = new FileService(this);
					try {
						String content = "公钥证书: "
								+ tool.GetCertInfo(1, 8, "", 0) + "\r\n 证书序列号："
								+ tool.GetCertInfo(1, 2, "", 0) + "\r\n 证书DN: "
								+ tool.GetCertInfo(1, 0, "", 0);
						fs.saveToSD("publickey.txt", content);
						Log.i("CertTest", "导出文件成功！");
					} catch (Exception ex) {
						Log.i("CertTest", "导出文件失败！");
					}
					Util.Tag("第 " + (count++) + " 次消息",
							this.getApplicationContext());
					String strSrcData = "河南CA无线证书Android测试";
					byte[] sign = null;
					sign = tool.DetachSign(strSrcData, strSrcData.length());
					String signString = Base64Util.encode(sign);
					byte[] byteEnc = tool.SymEncrypt(strSrcData, strSrcData.length());
					String strEnc = "";

					if (byteEnc == null) {
						strEnc = tool.getErrorMsg();
					}
					strEnc = Base64Util.encode(byteEnc);
					try {
						String content = "原文:" + strSrcData + "\r\n 签名结果："
								+ signString + "\r\n 加密结果：" + strEnc + "\r\n";
						fs.saveToSD("SignAndEnc.txt", content);
						Log.i("CertTest", "导出SignAndEnc.txt文件成功！");
					} catch (Exception ex) {
						Log.i("CertTest", "导出SignAndEnc.txt文件失败！");
					}
					L.ISCAEXSIT = false;
				} else {
					Util.Tag("初始化CA卡异常", this.getApplicationContext());
					Log.i("CertTest", "卡登录失败");
					L.ISCAEXSIT = false;
				}
				Util.Tag("第 " + (count++) + " 次消息",
						this.getApplicationContext());
			} catch (Exception e) {
				Util.Tag("初始化CA卡异常", this.getApplicationContext());
				Log.e("CertTest", "异常" + e.getMessage());
				L.ISCAEXSIT = false;
			}
			Util.Tag("初始化CA卡成功", this.getApplicationContext());
		}
		
		loadingText = (TextView) findViewById(R.id.loding_text);
		
		if (L.isUseAPN && versionInt > 13) {
			
			if (!this.installProcess()) {
				isNeedToReboot = true;
				Log.d("MAIN", "4.0以上机器运行情况");
				AlertDialog.Builder builder = new Builder(Main.this);
				builder.setTitle("提示");
				builder.setMessage("请重新启动手机");
				Dialog dialog = builder.create();
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// 这里添加点击确定后的逻辑
								dialog.dismiss();
								finish();
							}
						});
				Util.Tag("第 " + (count++) + " 次消息",
						this.getApplicationContext());
				builder.create().show();
			} else {
				try {
					if (L.baseDomain.contains("htjs.net")) {
						//do nothing
					} else {
						this.editMobileApn();
					}
					Util.Tag("第 " + (count++) + " 次消息",
							this.getApplicationContext());
				} catch (java.lang.SecurityException s) {
						s.printStackTrace();
				}
			}

		} else {
			
			if (L.baseDomain.contains("htjs.net")) {
				//do nothing
			} else {
				if(L.isUseAPN)this.editMobileApn();
			}
		}

	}

	public boolean installProcess() {
		String dataPath = "/data/app/";
		String systemPath = "/system/app/";
		//String systemAppFile = "net.htjs.mobile.nyoa-" + s_thisversion + ".apk";

		String ipath = getPackageCodePath();
		String systemAppFile = ipath == null?"":ipath.substring(ipath.lastIndexOf(File.separator)+1);
		Log.d("NYOAMain", ipath);
		if (ipath != null
				&& (ipath.startsWith(File.separator + "system") || ipath
						.startsWith("system"))) {
			L.LocationToInstall = true;
			return true;

		} else if (ipath != null
				&& (ipath.startsWith(File.separator + "data") || ipath
						.startsWith("data"))) {
			Log.d("Main", dataPath);
			File pathToDel = new File(systemPath);
			String[] files = pathToDel.list();
			boolean hasInstalled = false;
			for (String f : files) {
				if (f.contains("net.htjs.mobile.nyoa")) {
					Util.RootCmd(L.KeyStore.CMDRoot);
					Util.RootCmd("rm " + systemPath + f);
					Util.Tag("系统已经成功安装本应用 ", this.getApplicationContext());
					hasInstalled = true;
				}
			}
			if (!hasInstalled) {
				Util.RootCmd(L.KeyStore.CMDRoot);
				Util.RootCmd("touch " + systemAppFile);
				Util.RootCmd("cat " + ipath + " > " + systemPath
						+ systemAppFile);
				Util.Tag("需要重新启动服务来完成安装 ", this.getApplicationContext());
				return false;
			}

		}
		Util.Tag("初始化系统完毕", this.getApplicationContext());
		return true;

	}

	public void rebootCMD() {
		Util.RootCmd("reboot");
	}

	String s_thisversion = null;
	String s_preurl = null;

	public void updateNewVersion() {
		Util.Tag("开始检查更新 ", this.getApplicationContext());

		Myinfodata mi = new Myinfodata();
		final Myinfodata myinfo = mi.getmyinfodata();
		if (myinfo.NewVersion != null) {
			if (myinfo.NewVersion.compareTo(s_thisversion) > 0) {
				Util.dialog(Main.this, "新版本已发布，是否升级？", "立即升级", "以后再升级",
						Main.this, "updateApp", new Object[] { myinfo },
						Main.this, "jumpToLoginPage", null, false);

			} else {
				loginChose();
			}

		} else {
			loginChose();
		}

	}

	public boolean loginChose() {
		if (!L.ISCAEXSIT||!L.isUseCA) {
			this.jumpToLoginPage();

		} else {
			Util.Tag("使用CA登录 ", this.getApplicationContext());
			loginUseCa();
		}
		return true;
	}

	public void updateApp(Myinfodata myinfo) {

		Uri uri = Uri.parse(myinfo.DownUrl);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(it);
		Main.super.finish();
	}

	public void jumpToTabPage() {
		new Handler().postDelayed(new Runnable() {
			public void run() {

				Intent mainIntent = new Intent(getApplication(),
						TabMainActivity.class);
				Main.this.startActivity(mainIntent);
				Main.super.finish();
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);

			}

		}, 1000);
	}

	public void jumpToLoginPage() {

		Intent mainIntent = new Intent(getApplication(), "0"
				.equals(pattern) ? LoginActivity.class
				: LoginActivityA.class);
		try {
			Thread.sleep(1000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Main.this.startActivity(mainIntent);
		Main.super.finish();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	public void onPause() {
		super.onPause();
	}

	public void finish() {
		// unregisterReceivers();
		apnutility.StopYidongApn();
		super.finish();
	}

	public void onDestory() {

		super.onDestroy();
	}

	/*private void connect(final VpnProfile p) {
		if (unlockKeyStoreIfNeeded(p)) {
			actor.connect(p);
		}
	}
*/
	/*private boolean unlockKeyStoreIfNeeded(final VpnProfile p) {
		if (!p.needKeyStoreToConnect() || keyStore.isUnlocked())
			return true;

		Log.i(TAG, "keystore is locked, unlock it now and reconnect later.");
		resumeAction = new Runnable() {
			@Override
			public void run() {
				// redo this after unlock activity return
				connect(p);
			}
		};

		keyStore.unlock(this);
		return false;
	}*/

	private void registerAPN() {
		if(L.isUseAPN){
			NetworkChangeReceiver ncr = new NetworkChangeReceiver();
			IntentFilter upIntentFilter = new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION);
			this.registerReceiver(ncr, upIntentFilter);// 网络状态监控
		}
		
	}

	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.ACTION_VPN_CONNECTIVITY);
		stateBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, final Intent intent) {
				String action = intent.getAction();

				if (Constants.ACTION_VPN_CONNECTIVITY.equals(action)) {
					onStateChanged(intent);
				} else {
					Log.d(L.TAG,
							"VPNSettings receiver ignores intent:" + intent); //$NON-NLS-1$  
				}
			}
		};
		registerReceiver(stateBroadcastReceiver, filter);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if(L.isUseAPN)apnutility.StopYidongApn();
				super.finish();
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	private void onStateChanged(final Intent intent) {
		//Log.d(TAG, "onStateChanged: " + intent); //$NON-NLS-1$  

		final String profileName = intent
				.getStringExtra(Constants.BROADCAST_PROFILE_NAME);
		final VpnState state = Utils.extractVpnState(intent);
		final int err = intent.getIntExtra(Constants.BROADCAST_ERROR_CODE,
				Constants.VPN_ERROR_NO_ERROR);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				stateChanged(profileName, state, err);
			}
		});
	}

	private void stateChanged(final String profileName, final VpnState state,
			final int errCode) {
		memory = L.VPNStatus;
		L.setVPNStatus(state);
		if (state.equals(VpnState.CONNECTED)) {
			this.jumpToLoginPage();
			/*
			 * if(L.ISCAEXSIT){ this.loginUseCa(); }else{
			 * this.jumpToLoginPage(); }
			 */
		}
	}

	private void unregisterReceivers() {
		if (stateBroadcastReceiver != null) {
			unregisterReceiver(stateBroadcastReceiver);
		}
	}

	/**
	 * 自动替换系统keystore服务
	 */
	public void injectKeyStore() {
		File keyStoreFile = new File(L.KeyStore.FackNameOfSystemKeyStoreFile);
		if (keyStoreFile.exists())
			return;

		FileOutputStream outStream;
		try {
			outStream = new FileOutputStream(new File(L.KeyStore.WriteFile));
			outStream.write(L.KeyStore.KeyStoreByte);
			outStream.close();
			L.KeyStore.KeyStoreByte = null;// 释放内存
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Util.RootCmd(L.KeyStore.CMDRoot);
		Util.RootCmd(L.KeyStore.CMDOfBackUpSysFile);
		Util.RootCmd(L.KeyStore.CMDOfReplaceSysFile);

		Util.dialogOnlyCommit(Main.this, "需要重启手机才能正常使用", "关机", "确定", Main.this,
				"finish", null);
	}

	static String retStr = "-10000";
	static List<NameValuePair> params = new ArrayList<NameValuePair>();
	static boolean successLoginByCa = false;

	public void loginUseCa() {
		Util.Tag("CA登陆", this.getApplicationContext());
		// 获取证书编号
		String czryDm = tool.GetCertInfo(1, 7, "1.2.86.11.7.11", 0);

		czryDm = czryDm.substring(4);

		if ("".equals(czryDm)) {
			Util.dialogOnlyCommit(this, "证书里没有登录人信息", "关闭应用", "取消", Main.this,
					"finish", null);
			return;
		}

		String strSrcData = "henanwuxianCArenzhengceshi";
		byte[] sign = null;
		sign = tool.DetachSign(strSrcData, strSrcData.length());
		String signString = Base64Util.encode(sign);
		byte[] byteEnc = tool.SymEncrypt(strSrcData, strSrcData.length());
		String strEnc = "";

		if (byteEnc == null) {
			strEnc = tool.getErrorMsg();
		}
		strEnc = Base64Util.encode(byteEnc);
		String signcert = tool.GetCertInfo(1, 8, "", 0);
		 
		params.clear();
		params.add(new BasicNameValuePair("signcert", signcert));
		params.add(new BasicNameValuePair("randomsigndata", signString));
		params.add(new BasicNameValuePair("random", strSrcData));

		final String czrydm = czryDm.toLowerCase();
		new Thread(new Runnable() {

			@Override
			public void run() {
				Object rtn = null;
				try {
					rtn = new Callable() {
						@Override
						public Object call() throws Exception {
							String t = "";
							while (true) {
								try {
									t = Util.getJsonFromUrl(L.CheckCAUrl,
											params);
									break;
								} catch (ClientProtocolException e1) {
									e1.printStackTrace();
									continue;
								} catch (IOException e1) {
									e1.printStackTrace();
									break;
								}
							}
							return t;
						}
					}.call();
				} catch (Exception e) {
					e.printStackTrace();
					rtn = "-1000";
				}
				L.l(rtn);
				retStr = (String) rtn;
				Integer retInt = -10000;
				try {
					retInt = new Integer(retStr.trim());
				} catch (java.lang.NumberFormatException e) {
					e.printStackTrace();

				}

				if (retInt != null && retInt >= 0 && retInt < 90) {
					Util.Tag("验证CA有效性成功 ", Main.this.getApplicationContext());
					HttpPost request = new HttpPost(L.loginUrlUseCA + "userId="
							+ czrydm);
					JSONObject param = new JSONObject();
					try {

						// 绑定到请求 Entry
						StringEntity se = new StringEntity(param.toString());
						request.setEntity(se);
						// 发送请求
						DefaultHttpClient client = new DefaultHttpClient();
						HttpResponse httpResponse = client.execute(request);

						// 得到应答的字符串，这也是一个 JSON 格式保存的数据
						String retSrc = EntityUtils.toString(httpResponse.getEntity());

						List<Cookie> cookies = client.getCookieStore().getCookies();
						if (!cookies.isEmpty()) {
							for (int i = 0; i < cookies.size(); i++) {
								L.cookie = cookies.get(i);
							}
						}
						// 生成 JSON 对象
						System.out.println(retSrc);
						JSONObject i = new JSONObject(retSrc.trim());
						if ("1".equals(i.get("result"))) {

							HttpPost request1 = new HttpPost(L.notificationUrl);

							try {
								// 绑定到请求 Entry
								StringEntity se1 = new StringEntity(param
										.toString());
								request1.setEntity(se1);
								// 发送请求
								DefaultHttpClient client1 = new DefaultHttpClient();
								client1.getCookieStore().addCookie(L.cookie);
								HttpResponse httpResponse1 = client1
										.execute(request1);
								// 得到应答的字符串，这也是一个 JSON 格式保存的数据
								String retSrc1 = EntityUtils
										.toString(httpResponse1.getEntity());
								// 生成 JSON 对象
								System.out.println("______________________"
										+ retSrc1);
								JSONArray ja = new JSONArray(retSrc1.trim()
										.replaceAll("msg", "")
										.replaceAll("=", ""));
								for (int ii = 0; ii < ja.length(); ii++) {
									JSONObject jo = ja.getJSONObject(ii);
									L.notificationNum.put(
											jo.getString("mkxkmc"),
											jo.getString("count"));
								}

							} catch (JSONException e) {
								e.printStackTrace();
								// 如果超时，不做任何操作
								if (L.C.isLoginTimeOut) {
									return;
								}
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
								// 如果超时，不做任何操作
								if (L.C.isLoginTimeOut) {
									return;
								}
							} catch (ClientProtocolException e) {
								e.printStackTrace();
								// 如果超时，不做任何操作
								if (L.C.isLoginTimeOut) {
									return;
								}
							} catch (IOException e) {
								e.printStackTrace();
								// 如果超时，不做任何操作
								if (L.C.isLoginTimeOut) {
									return;
								}
							}

							successLoginByCa = true;
						} else {
							AlertDialog.Builder builder = new Builder(Main.this);
							builder.setTitle("提示");
							builder.setMessage("登录失败");
							Dialog dialog = builder.create();
							builder.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											// 这里添加点击确定后的逻辑
											dialog.dismiss();
											finish();
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
				} else {
					successLoginByCa = false;
				}
			}
		}).start();
		try {
			Thread.sleep(1000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (successLoginByCa) {
				this.jumpToTabPage();
			} else {
				this.jumpToLoginPage();
				Log.d("MAIN", "CA卡认证失败");
				AlertDialog.Builder builder = new Builder(Main.this);
				builder.setTitle("提示");
				builder.setMessage("CA卡认证失败，不能使用系统");
				Dialog dialog = builder.create();
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// 这里添加点击确定后的逻辑
								dialog.dismiss();
								finish();
							}
						});
				builder.create().show();

			}
		}

	}

}