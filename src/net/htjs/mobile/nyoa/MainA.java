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
import xink.vpn.VpnActor;
import xink.vpn.VpnProfileRepository;
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
import android.widget.TextView;
import android.widget.Toast;

import com.apn.util.ApnUtility;
import com.hnca.Base64Util;
import com.hnca.FileService;
import com.hnca.HNCAMCertTool;
/**
 * @author Herbert
 *
 */
public class MainA  extends BaseActivity {
	private static String TAG = "main";
	ConnectivityManager cgr;
	NetworkInfo netinfo;
	TelephonyManager tm;
	TextView loadingText;

	// VPN连接服务相关
	private BroadcastReceiver stateBroadcastReceiver;
	private VpnProfileRepository repository;
	private static VpnActor actor;
	VpnProfile vp;
	private KeyStore keyStore;
	private Runnable resumeAction;
	static VpnState memory;
	private HNCAMCertTool tool =new HNCAMCertTool();

	private static boolean isNeedToReboot=false;
	//CA区域
	
	//APN区域
	private ApnUtility apnutility = null;

	@Override
	protected void onStart() {
		
		super.onStart();
		
		if(isNeedToReboot){
			return;
		}
		String version=  android.os.Build.VERSION.SDK ;
		System.out.println(version);
		netinfo = cgr.getActiveNetworkInfo();
		System.out.println(netinfo);
		State mobile = cgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();

		State wifi = cgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(Service.WIFI_SERVICE);
		wifiManager.getWifiState();
		if(wifi == State.CONNECTED || wifi == State.CONNECTING){
			Util.dialogOnlyCommit(MainA.this, "请关闭WLAN后再使用", "关闭应用", "继续操作", MainA.this,"finishAndCloseWLAN", null);
		
			 
		      //打开WIFI网卡
			 Toast.makeText(this, "关闭WIFI网卡", Toast.LENGTH_SHORT).show();
		      //关闭WIFI网卡
		     wifiManager.setWifiEnabled(false); 
		      //获取网卡当前的状态
		}
		else if (mobile == State.CONNECTED || mobile == State.CONNECTING ) {

			L.l("+++++++++++++++++++++++++");
			 
			loadingText.setText(getString(R.string.vpnconnecting));
		
			this.loginChose();
			return;
		} else {
			if (netinfo == null || !netinfo.isConnectedOrConnecting()) {
				Util.dialog(MainA.this, "网络链接不存在", "关闭应用", "继续操作", MainA.this,
						"finish", null, MainA.this, "jumpToLoginPage", null, false);
			} else if (!netinfo.isAvailable()) {
				Util.dialog(MainA.this, "网络链接不存在", "关闭应用", "继续操作", MainA.this,
						"finish", null, MainA.this, "jumpToLoginPage", null, false);
			} else if (!netinfo.isConnected()) {
				Util.dialog(MainA.this, "网络链接不存在", "关闭应用", "继续操作", MainA.this,
						"finish", null, MainA.this, "jumpToLoginPage", null, false);
			}
			return;
			// 进入无线网络配置界面
		}
		
	}
	
	public void finishAndCloseWLAN(){
		finish();
		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.maina);
		setTitle(R.string.app_name);
		
		
		s_thisversion = getResources().getText(R.string.version).toString();
		s_preurl = getResources().getText(R.string.urlheader).toString();
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);// 获取sim卡信息
		cgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		//APN工具类实例化
		apnutility = new ApnUtility(this);
		
		getWindow().setFormat(PixelFormat.RGBA_8888);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

		TextView versionNumber = (TextView) findViewById(R.id.versionNumber);
		versionNumber.setText("Version " + getResources().getText(R.string.version).toString());
		
		String version=  android.os.Build.VERSION.SDK ;
		Util.Tag("固件版本号为 "+version, this.getApplicationContext());
		if(version.length()==1){
			version="0"+version;
		}
		if(version.compareTo("13")<=0&&tool!=null&&Util.checkSDCard())
		{
			try {
				Util.Tag("检测到CA卡，开始初始化", this.getApplicationContext());
				long lmsg =0;
				try{
					lmsg= tool.Initialize();
				}catch(java.lang.UnsatisfiedLinkError e){
					e.printStackTrace();
				}
				
				if (lmsg == 1L) {
					FileService fs = new FileService(this);
					try {
						String content = "公钥证书: " + tool.GetCertInfo(1, 8, "", 0)
								+ "\r\n 证书序列号：" + tool.GetCertInfo(1, 2, "", 0)
								+ "\r\n 证书DN: " + tool.GetCertInfo(1, 0, "", 0);
						fs.saveToSD("publickey.txt", content);
						Log.i("CertTest", "导出文件成功！");
					} catch (Exception ex) {
						Log.i("CertTest", "导出文件失败！");
					}
					
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
						String content = "原文:" + strSrcData + "\r\n 签名结果：" + signString
								+ "\r\n 加密结果：" + strEnc + "\r\n";
						fs.saveToSD("SignAndEnc.txt", content);
						Log.i("CertTest", "导出SignAndEnc.txt文件成功！");
					} catch (Exception ex) {
						Log.i("CertTest", "导出SignAndEnc.txt文件失败！");
					}
					L.ISCAEXSIT=true;
				} else {
					Util.Tag("初始化CA卡异常", this.getApplicationContext());
					Log.i("CertTest", "卡登录失败");
					L.ISCAEXSIT=false;
				}
			} catch (Exception e) {
				Util.Tag("初始化CA卡异常", this.getApplicationContext());
				Log.e("CertTest", "异常" + e.getMessage());
				L.ISCAEXSIT=false;
			}
			Util.Tag("初始化CA卡成功", this.getApplicationContext());
		}
		loadingText = (TextView) findViewById(R.id.loding_text);
		
		if(version.compareTo("13")>0){
			
			if(!this.installProcess()){
				isNeedToReboot=true;
				Log.d("MainA", "4.0以上机器运行情况");
				AlertDialog.Builder builder = new Builder(
						MainA.this);
				builder.setTitle("提示");
				builder.setMessage("请重新启动手机");
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
			}else{
				try{
					this.editMobileApn();
				}catch(java.lang.SecurityException s){
					
				}
			}
			
		}else{
			this.editMobileApn();
		}
		
		
		 
	}

	public boolean installProcess(){
		String dataPath = "/data/app/";
		String systemPath = "/system/app/";
		String systemAppFile = "net.htjs.mobile.nyoa-"+s_thisversion+".apk";
		
		String ipath=getPackageCodePath();
		Log.d("NYOAMainA", ipath);
		if(ipath != null && (ipath.startsWith(File.separator+"system") || ipath.startsWith("system"))){
			L.LocationToInstall=true;
			return true;
		}else if(ipath != null && (ipath.startsWith(File.separator+"data") || ipath.startsWith("data"))){
			Log.d("MainA", dataPath);
			File pathToDel = new File(systemPath);
			String[] files = pathToDel.list();
			boolean hasInstalled=false;
			for(String f:files){
				if(f.contains("net.htjs.mobile.nyoa")){
					Util.RootCmd(L.KeyStore.CMDRoot);
					Util.RootCmd("rm " + systemPath + f);
					Util.Tag("系统已经成功安装本应用 ", this.getApplicationContext());
				}
			}
			if(!hasInstalled){
				Util.RootCmd(L.KeyStore.CMDRoot);
				Util.RootCmd("touch "+ systemAppFile);
				Util.RootCmd("cat " + ipath + " > " + systemPath + systemAppFile);
				Util.Tag("需要重新启动服务来完成安装 ", this.getApplicationContext());
				return false;
			}
		}
		return true;
	}

	public void rebootCMD(){
		Util.RootCmd("reboot");
	}

	String s_thisversion=null;
	String s_preurl=null;
	public void updateNewVersion(){
		Util.Tag("开始检查更新 ", this.getApplicationContext());
		
		Myinfodata mi=new Myinfodata();
		final Myinfodata myinfo = mi.getmyinfodata();
		if (myinfo.NewVersion != null) {
			if (myinfo.NewVersion.compareTo(s_thisversion)>0) {
				Util.dialog(MainA.this, "新版本已发布，是否升级？", "立即升级", "以后再升级", MainA.this,
						"updateApp", new Object[]{myinfo}, MainA.this, "jumpToLoginPage", null, false);
				
			}else{
				loginChose();
			}
			 
		}else{
			loginChose();
		}

	}
	public boolean loginChose(){
		if(!L.ISCAEXSIT){
			this.jumpToLoginPage();

		}else{
			Util.Tag("使用CA登录 ", this.getApplicationContext());
			loginUseCa();
		}
		return true;
	}
	
	public void updateApp(Myinfodata myinfo){
	 
		Uri uri = Uri.parse(myinfo.DownUrl);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(it);
		MainA.super.finish();
	}
	public void jumpToTabPage() {
		new Handler().postDelayed(new Runnable() {
			public void run() {

				Intent mainIntent = new Intent(getApplication(),
						TabMainActivity.class);
				MainA.this.startActivity(mainIntent);
				MainA.super.finish();
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
			}
		}, 3500);
	}
	public void jumpToLoginPage() {
		
		new Handler().postDelayed(new Runnable() {
			public void run() {

				Intent mainIntent = new Intent(getApplication(),
						LoginActivityA.class);
				try {
					Thread.sleep(8500l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				MainA.this.startActivity(mainIntent);
				MainA.super.finish();
				overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);

			}

		}, 1000);
	}
	public void onPause() {
		super.onPause();
	}

	public void finish() {
		apnutility.StopYidongApn();
		super.finish();
	}

	public void onDestory() {

		super.onDestroy();
	}
	private void registerAPN(){
		NetworkChangeReceiver ncr = new NetworkChangeReceiver();
		IntentFilter upIntentFilter = new IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION);
		this.registerReceiver(ncr, upIntentFilter);// 网络状态监控
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
				apnutility.StopYidongApn();
				super.finish();
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
	private void onStateChanged(final Intent intent) {

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

		Util.dialogOnlyCommit(MainA.this, "需要重启手机才能正常使用", "关机", "确定", MainA.this,
				"finish", null);
	}
	static  String  retStr="-10000"; 
	static List<NameValuePair> params = new ArrayList<NameValuePair>(); 
	static boolean successLoginByCa=false;
	
	
	public void loginUseCa(){
	 
		String czryDm="songyunxiang";
		String signcert="MIIDejCCAuOgAwIBAgIQcSISp2a687f62BD/LhvhmTANBgkqhkiG9w0BAQUFADBFMQswCQYDVQQGEwJDTjELMAkGA1UECBMCSE4xCzAJBgNVBAcTAlpaMQ0wCwYDVQQKEwRITkNBMQ0wCwYDVQQDEwRITkNBMB4XDTEyMTAyNTAxMjcwMVoXDTE3MTAyNDAxMjcwMVowHjELMAkGA1UEBhMCQ04xDzANBgNVBAMeBnpGTjpsETCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAxYr4D+8afk4g9JZIUFntQ/gQzZ/O+1jhzhTvdZfKiHTIKlOdztKyYDgVCzfKygbjbJH13XVkHrEQ+Ja1Ry1Zpw4t+VQjeU+BTfiOuk6qCvtr18JAIeae9oGL6bSKrNEgh2SkKDWkIXczDjmA8w6yY/Nl61c5l8eZ5z6PYN4e1W8CAwEAAaOCAZAwggGMMB8GA1UdIwQYMBaAFHQC7Mga++RB59zuw3jkEb4VTXMjMB0GA1UdDgQWBBTyamHjxIr8yuriBDs/lprvozdrxjAOBgNVHQ8BAf8EBAMCA/gwDAYDVR0TBAUwAwEBADBHBgNVHSUEQDA+BggrBgEFBQcDAQYIKwYBBQUHAwIGCCsGAQUFBwMDBggrBgEFBQcDBAYIKwYBBQUHAwgGCisGAQQBgjcUAgIwgaoGA1UdHwSBojCBnzB1oHOgcaRvMG0xCzAJBgNVBAYTAkNOMQswCQYDVQQIEwJITjELMAkGA1UEBxMCWloxDTALBgNVBAoTBEhOQ0ExCjAIBgNVBAsTATExDDAKBgNVBAsTAzEwMzEMMAoGA1UECxMDY3JsMQ0wCwYDVQQDEwRjcmw1MCagJKAihiBodHRwOi8vMjE4LjI4LjE2LjcxL2NybC9jcmwxLmNybDATBgUqVgsHCwQKEwhNVVdFSU1JTjAhBgUqVgsHAQQYMRahFBMSNDEwMTAyMTk2MTExMTA3MTEwMA0GCSqGSIb3DQEBBQUAA4GBAJdz47JJKwfLxKrclh38z2MQ7HWlWuzU32oM/uAqFM3ud0jSahB1QnM6Z5ay4hL8bTI/JpfE8HUk0d1VkwTc8HAGBqvVuCsnylGjlAGwE9nz9rmwUTAqweptOSQ444xF8slY+Dl5LIq49jWzlNtdY/5NSbYnLUDT1FzStU/UwiUH";
		String signString="oyl5oOXROwo+iSUobClslaqbwEa1rK9t51fqrRO0Okngz5yMI9TLm+ucXdPXq2UhVeWCjnV6GpZiIbtYAC0C7JQndzcwA1Sp5TEhTNYv7UyYWnaUEwspwSjxwsRTUVGeldjkpfQB34LecCYGfc90YkI9w8WoqpxUCpdpm7GgmFI=";
		String strSrcData="rondomData_xiajinhu";
		
		params.clear();
		params.add(new BasicNameValuePair("signcert", signcert));  
		params.add(new BasicNameValuePair("randomsigndata", signString)); 
		params.add(new BasicNameValuePair("random", strSrcData)); 
		 
		final String czrydm=czryDm.toLowerCase();
		new Thread(new Runnable(){

			@Override
			public void run() {
				Object rtn=null;
				try {
					rtn=new Callable(){
						@Override
						public Object call() throws Exception {
							String t="";
							while(true){
								try {
									t=Util.getJsonFromUrl(L.CheckCAUrl, params);
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
				}catch(Exception e){
					e.printStackTrace();
					rtn="-1000";
				}
				L.l(rtn);
				retStr=(String)rtn;
				Integer retInt=-10000;
				try{
					retInt=new Integer(retStr.trim()) ;
				}catch(java.lang.NumberFormatException e){
					e.printStackTrace();
				}
				
				if(retInt!=null&&retInt>=0&&retInt<90){
					Util.Tag("验证CA有效性成功 ", MainA.this.getApplicationContext());
					HttpPost request = new HttpPost(L.loginUrlUseCA + "userId=" + czrydm);
					JSONObject param = new JSONObject();
					try {
						
						// 绑定到请求 Entry
						StringEntity se = new StringEntity(param.toString());
						request.setEntity(se);
						// 发送请求
						DefaultHttpClient client = new DefaultHttpClient();
						HttpResponse httpResponse = client.execute(request);
						
						// 得到应答的字符串，这也是一个 JSON 格式保存的数据
						String retSrc = EntityUtils.toString(httpResponse
								.getEntity());

						List<Cookie> cookies = client.getCookieStore()
								.getCookies();
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
								client1.getCookieStore()
										.addCookie(L.cookie);
								HttpResponse httpResponse1 = client1
										.execute(request1);
								// 得到应答的字符串，这也是一个 JSON 格式保存的数据
								String retSrc1 = EntityUtils
										.toString(httpResponse1.getEntity());
								// 生成 JSON 对象
								System.out.println("______________________" + retSrc1);
								JSONArray ja = new JSONArray(retSrc1.trim().replaceAll("msg", "").replaceAll("=", ""));
								for (int ii = 0; ii < ja.length(); ii++) {
									JSONObject jo = ja.getJSONObject(ii);
									L.notificationNum.put(jo.getString("mkxkmc"),jo.getString("count"));
								}
							} catch (JSONException e) {
								e.printStackTrace();
								//如果超时，不做任何操作
								if(L.C.isLoginTimeOut){
									return;
								}
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
								//如果超时，不做任何操作
								if(L.C.isLoginTimeOut){
									return;
								}
							} catch (ClientProtocolException e) {
								e.printStackTrace();
								//如果超时，不做任何操作
								if(L.C.isLoginTimeOut){
									return;
								}
							} catch (IOException e) {
								e.printStackTrace();
								//如果超时，不做任何操作
								if(L.C.isLoginTimeOut){
									return;
								}
							}
							successLoginByCa=true;
						} else  {
							AlertDialog.Builder builder = new Builder(
									MainA.this);
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
				}else{
					successLoginByCa=false;
				}
			}
		}).start(); 
		try {
			Thread.sleep(23000l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			if(successLoginByCa){
				this.jumpToTabPage();
			}else{
				this.jumpToLoginPage();
				Log.d("MAIN", "CA卡认证失败");
				AlertDialog.Builder builder = new Builder(
						MainA.this);
				builder.setTitle("提示");
				builder.setMessage("CA卡认证失败，不能使用系统");
				Dialog dialog = builder.create();
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick( DialogInterface dialog, int whichButton) {
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