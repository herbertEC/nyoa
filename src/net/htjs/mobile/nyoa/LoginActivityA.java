package net.htjs.mobile.nyoa;

import static xink.vpn.Constants.ACTION_VPN_CONNECTIVITY;
import static xink.vpn.Constants.BROADCAST_ERROR_CODE;
import static xink.vpn.Constants.BROADCAST_PROFILE_NAME;
import static xink.vpn.Constants.VPN_ERROR_NO_ERROR;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import net.htjs.mobile.nyoa.db.DatabaseHandler;
import net.htjs.mobile.nyoa.db.UserInfo;
import net.htjs.mobile.nyoa.updateapk.Myinfodata;
import net.htjs.mobile.nyoa.util.L;
import net.htjs.mobile.nyoa.util.Util;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import xink.vpn.Utils;
import xink.vpn.VpnActor;
import xink.vpn.VpnProfileRepository;
import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.apn.util.ApnUtility;

public class LoginActivityA extends Activity {
	private Button login, vpnconnect;
	EditText userN;
	EditText pssW;
	CheckBox recorduserinfo;
	
	
	private static String TAG = "LoginActivity";
	DatabaseHandler dbh;
	static boolean isRecordUserInfo = false;

	// VPN连接服务相关
	private BroadcastReceiver stateBroadcastReceiver;
	private VpnProfileRepository repository;
	private static VpnActor actor;
	VpnProfile vp;
	private KeyStore keyStore;
	private Runnable resumeAction;
	static VpnState memory;

	// APN相关
	private ApnUtility apnutility = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.logina);

		userN = (EditText) this.findViewById(R.id.username_edit);
		pssW = (EditText) this.findViewById(R.id.password_edit);
	
		// APN相关
		apnutility = new ApnUtility(this);
		
		dbh = new DatabaseHandler(this);
		if (!dbh.userTableIsExist()) {
			try {
				dbh.onCreate(dbh.getWritableDatabase());
			} catch (Exception e) {

			}
		}
		
		List<UserInfo> uis = dbh.getAllUserInfos();
		UserInfo userinfo = null;
		if (uis.size() != 0) {
			for (UserInfo ui : uis) {
				System.out.println(ui.toString());
			}
			userinfo = uis.get(uis.size() - 1);
		}
		String userId="";
		String password="";
		if (userinfo == null) {
			userN.setText(userId);
			pssW.setText(userId);
		} else {
			userId=userinfo.getName();
			password=userinfo.getPassword();
			userN.setText(userId);
			pssW.setText(password);
		} 
		recorduserinfo = (CheckBox) findViewById(R.id.recorduserinfo);
		if(!"".equals(password)){
			recorduserinfo.setChecked(true);
		}else{
			recorduserinfo.setChecked(false);
		}
		recorduserinfo .setOnCheckedChangeListener(new
		   CompoundButton.OnCheckedChangeListener() { 
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { //
					if (isChecked) { 
						isRecordUserInfo = true; 
						} else { 
							isRecordUserInfo = false;
						}
					}
			});

		vpnconnect = (Button) this.findViewById(R.id.vpntoggle_button);
		vpnconnect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (L.VPNStatus.equals(VpnState.CONNECTED)
						|| (VpnState.IDLE.equals(memory) && L.VPNStatus
								.equals(VpnState.IDLE))) {
					vpnconnect.setText(getString(R.string.disconnecting));
					//disconnect();
				} else {
					vpnconnect.setText(getString(R.string.connecting));
					connect(vp);
				}
			}
		});
		vpnconnect.setVisibility(View.INVISIBLE);
		login = (Button) this.findViewById(R.id.signin_button);
		/*
		 * login.setOnTouchListener(new OnTouchListener() {
		 * 
		 * public boolean onTouch(View v, MotionEvent event) { if
		 * (event.getAction() == MotionEvent.ACTION_DOWN) { // 更改为按下时的背景图片
		 * v.setBackgroundResource(R.drawable.photo_press); } else if
		 * (event.getAction() == MotionEvent.ACTION_UP) { // 改为抬起时的图片
		 * v.setBackgroundResource(R.drawable.photo_ok); } return false; } });
		 */
		login.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/*if (!(L.VPNStatus.equals(VpnState.CONNECTED) || L.VPNStatus
						.equals(VpnState.IDLE))) {
					
					 * Intent mainIntent = new Intent(getApplication(),
					 * VpnSettings.class);
					 * LoginActivity.this.startActivity(mainIntent);
					 

					connect(vp);
					return;
				}
				 disconnect();*/
				L.C.loginFlag = false;
				L.C.startLoginTime = System.currentTimeMillis();
				L.C.isLoginTimeOut = false;
				final ProgressDialog pdialog = ProgressDialog.show(
						LoginActivityA.this, "", "登录中...", true);
				new Thread(new Runnable() {

					public void run() {

						Looper.prepare();
						while (true) {
							if (L.C.loginFlag) {
								break;
							}
							long timeNow = System.currentTimeMillis();
							if (timeNow - L.C.startLoginTime > L.C.MAXTIMETOLOGIN) {
								pdialog.dismiss();
								AlertDialog.Builder builder = new Builder(
										LoginActivityA.this);

								builder.setTitle("提示");
								builder.setIcon(R.drawable.logonyoa);
								builder.setMessage("连接超时");
								Dialog dialog = builder.create();
								builder.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												// 这里添加点击确定后的逻辑
												L.C.isLoginTimeOut = true;// 确定连接超时
												dialog.dismiss();
											}
										});

								builder.create().show();
								break;
							} else {
								try {
									Thread.sleep(1000l);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						Looper.loop();
					}

				}).start();
				new Thread(new Runnable() {

					public void run() {
						Looper.prepare();
						
						HttpPost request = new HttpPost(L.loginUrl + "userId="
								+ userN.getText().toString().trim()
								+ "&password="
								+ pssW.getText().toString().trim());
						// 先封装一个 JSON 对象 /server/3g/login4Mobile.jsp
						JSONObject param = new JSONObject();
						try {
							param.put("userId", userN.getText().toString()
									.trim());
							param.put("password", pssW.getText().toString()
									.trim());
							// 绑定到请求 Entry
							StringEntity se = new StringEntity(param.toString());
							request.setEntity(se);
							// 发送请求
							DefaultHttpClient client = new DefaultHttpClient();
							HttpResponse httpResponse = client.execute(request);
							L.C.loginFlag = true;

							// 如果超时，不做任何操作
							if (L.C.isLoginTimeOut) {
								return;
							}
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
							isRecordUserInfo=recorduserinfo.isChecked();
							// 生成 JSON 对象
							System.out.println(retSrc);
							JSONObject i = new JSONObject(retSrc.trim());
							if ("1".equals(i.get("result"))) {
								
								pdialog.dismiss();
								L.LoginInfo.userName = userN.getText()
										.toString().trim();
								L.LoginInfo.password = pssW.getText()
										.toString().trim();
								
								if (isRecordUserInfo) { 
									  if (dbh.getUserInfo(L.LoginInfo.userName) != null)
									  { 
										  	updateUserInfo(L.LoginInfo.userName,L.LoginInfo.password);
									  } else {
											recordUserInfo(L.LoginInfo.userName, L.LoginInfo.password); 
									  } 
								} else {
									  dbh.deleteUserInfo(); 
									  recordUserInfo(L.LoginInfo.userName,"");
								}
								jumpPage();
							} else if ("4".equals(i.get("result"))) {
								Util.dialogOnlyCommit(LoginActivityA.this, "非法访问", "确定", "取消", null, null, null);
							} else if ("5".equals(i.get("result"))) {
								Util.dialogOnlyCommit(LoginActivityA.this, "账户异常", "确定", "取消", null, null, null);
							} else if ("0".equals(i.get("result"))) {
								Util.dialogOnlyCommit(LoginActivityA.this, "账户被禁用", "确定", "取消", null, null, null);
							} else if ("3".equals(i.get("result"))) {
								Util.dialogOnlyCommit(LoginActivityA.this, "用户名无效", "确定", "取消", null, null, null);
							} else {
								Util.dialogOnlyCommit(LoginActivityA.this, "密码错误", "确定", "取消", null, null, null);
							}
							L.C.loginFlag = true;
							
							// 如果超时，不做任何操作
							if (L.C.isLoginTimeOut) {
								return;
							}
						} catch (JSONException e) {
							Util.dialogOnlyCommit(LoginActivityA.this, "登录失败", "确定", "取消", null, null, null);
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						pdialog.dismiss();
						login.setClickable(true);
						Looper.loop();
					}

				}).start();
			}
		});
	}
	public void updateApp(Myinfodata myinfo){
		 
		Uri uri = Uri.parse(myinfo.DownUrl);
		Intent it = new Intent(
				Intent.ACTION_VIEW, uri);
		startActivity(it);
	}
	private void connect(final VpnProfile p) {
		if (unlockKeyStoreIfNeeded(p)) {
			actor.connect(p);
		}
	}

	private boolean unlockKeyStoreIfNeeded(final VpnProfile p) {
		if (!p.needKeyStoreToConnect() || keyStore.isUnlocked())
			return true;

		Log.i(TAG, "keystore is locked, unlock it now and reconnect later.");
		resumeAction = new Runnable() {
			@Override
			public void run() {
				connect(p);
			}
		};

		keyStore.unlock(this);
		return false;
	}

	private void disconnect() {
		L.setVPNStatus(VpnState.CANCELLED);
		actor.disconnect();
	}

	private void registerReceivers() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_VPN_CONNECTIVITY);
		stateBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, final Intent intent) {
				String action = intent.getAction();

				if (ACTION_VPN_CONNECTIVITY.equals(action)) {
					onStateChanged(intent);
				} else {
					Log.d(TAG, "VPNSettings receiver ignores intent:" + intent); //$NON-NLS-1$
				}
			}
		};
		registerReceiver(stateBroadcastReceiver, filter);
	}

	private void onStateChanged(final Intent intent) {

		final String profileName = intent
				.getStringExtra(BROADCAST_PROFILE_NAME);
		final VpnState state = Utils.extractVpnState(intent);
		final int err = intent.getIntExtra(BROADCAST_ERROR_CODE,
				VPN_ERROR_NO_ERROR);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				stateChanged(profileName, state, err);
			}
		});
	}

	private void stateChanged(final String profileName, final VpnState state,
			final int errCode) {
		VpnProfile p = repository.getProfileByName(profileName);

		memory = L.VPNStatus;
		L.setVPNStatus(state);
		if (state.equals(VpnState.CONNECTED)
				|| (memory.equals(VpnState.IDLE) && state.equals(VpnState.IDLE))) {
			login.setClickable(true);
			vpnconnect.setText(getString(R.string.cancel));

		} else {
			login.setClickable(false);
			if (state.equals(VpnState.CONNECTING)) {
				vpnconnect.setText(getString(R.string.connecting));
			} else {
				vpnconnect.setText(getString(R.string.connect));
				connect(vp);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void unregisterReceivers() {
		if (stateBroadcastReceiver != null) {
			unregisterReceiver(stateBroadcastReceiver);
		}
	}

	public boolean jumpPage() {
		Intent mainIntent = new Intent(getApplication(), ListViewMain.class);
		try {
			Thread.sleep(300l);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		LoginActivityA.this.startActivity(mainIntent);
		LoginActivityA.super.finish();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		return true;
	}

	public boolean recordUserInfo(String userName, String password) {

		dbh.addUserInfo(new UserInfo().setName(userName).setPassword(password));

		return true;
	}

	/**
	 * 
	 * @param userName
	 * @param password
	 * @return
	 * @author Herbert
	 */
	public boolean updateUserInfo(String userName, String password) {

		dbh.updateUserInfo(new UserInfo().setName(userName).setPassword( password));
		return true;
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

	public void finish() {
 
		if(L.isUseAPN)apnutility.StopYidongApn();
		super.finish();
	}
}