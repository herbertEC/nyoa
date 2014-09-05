package net.htjs.mobile.nyoa;

import static xink.vpn.Constants.ACTION_VPN_CONNECTIVITY;
import static xink.vpn.Constants.BROADCAST_ERROR_CODE;
import static xink.vpn.Constants.BROADCAST_PROFILE_NAME;
import static xink.vpn.Constants.VPN_ERROR_NO_ERROR;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.apn.util.ApnUtility;

import xink.vpn.Utils;
import xink.vpn.VpnActor;
import xink.vpn.VpnProfileRepository;
import xink.vpn.wrapper.KeyStore;
import xink.vpn.wrapper.VpnProfile;
import xink.vpn.wrapper.VpnState;
import net.htjs.mobile.nyoa.gw.ListGwWebView;
import net.htjs.mobile.nyoa.gw.WebView4TabMain;
import net.htjs.mobile.nyoa.gw.ListGwWebView.downloadTask;
import net.htjs.mobile.nyoa.updateapk.Myinfodata;
import net.htjs.mobile.nyoa.util.FileService;
import net.htjs.mobile.nyoa.util.L;
import net.htjs.mobile.nyoa.util.Util;
import net.htjs.mobile.nyoa.util.UtilIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

public class TabMainActivity extends TabActivity implements
		OnCheckedChangeListener {
	//private Button cancel, refresh;
	public TabHost mHost;
	private RadioGroup radioderGroup;
	private static String TAG="tabmain";
	RadioButton radioButton;
	
	
	// VPN连接服务相关
	private BroadcastReceiver stateBroadcastReceiver;
	private VpnProfileRepository repository;
	private static VpnActor actor;
	VpnProfile vp;
	private KeyStore keyStore;
	private Runnable resumeAction;
	static VpnState memory;
	
	
	private ApnUtility apnutility = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tabhost);

		
		mHost = this.getTabHost();
		
		
		
		Intent intent = new Intent();
		Bundle bundle = new Bundle(); //
		//bundle.putString(L.ZT, "ZB");
		bundle.putString(L.LX, L.Nine.DZGG); //
		//bundle.putString(L.LX, L.Nine.GW);
		 
		intent.setClass(TabMainActivity.this, WebView4TabMain.class);
		intent.putExtras(bundle);
		//if ("0".equals(BaseActivity.pattern)) {

			mHost.addTab(mHost.newTabSpec("ONE").setIndicator("ONE")
					.setContent(new Intent(this, nineActivity.class)));
		/*} else {
			mHost.addTab(mHost.newTabSpec("ONE").setIndicator("ONE")
					.setContent(new Intent(this, ListViewMain.class)));
		}*/

		mHost.addTab(mHost.newTabSpec("TWO").setIndicator("TWO")
				.setContent(intent));

		
		radioderGroup = (RadioGroup) findViewById(R.id.main_radio);
		radioderGroup.setOnCheckedChangeListener(this);
		mHost.setCurrentTabByTag("ONE");
		radioButton=(RadioButton)findViewById(R.id.radio_button1); 
		radioButton.setChecked(true);

		
		apnutility = new ApnUtility(this);
	 
		if(L.isNotUseWIFI){
			mMainWifi=(WifiManager)getApplicationContext().getSystemService(Service.WIFI_SERVICE);
	        registerReceiversWIFI();
		}
	}
 
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio_button1:
			mHost.setCurrentTabByTag("ONE");
			break;
		case R.id.radio_button0:
			mHost.setCurrentTabByTag("TWO");
			break;
		default:
			break;
		}
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
		memory=L.VPNStatus;
		L.setVPNStatus(state);
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
				// redo this after unlock activity return
				connect(p);
			}
		};

		keyStore.unlock(this);
		return false;
	}
	public void finish4Cancel(){
		if(L.isNotUseWIFI)unregisterReceiver(mWifiStateReceiver);
		super.finish();
	}
	
	public void finish(){
		if(L.isNotUseWIFI)unregisterReceiver(mWifiStateReceiver);
		if(L.isUseAPN)apnutility.StopYidongApn();
		super.finish();
	}
	private void unregisterReceivers() {
		if (stateBroadcastReceiver != null) {
			unregisterReceiver(stateBroadcastReceiver);
		}
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if(L.isUseAPN)apnutility.StopYidongApn();
				super.finish();
				//Util.dialog(getApplicationContext(), "退出应用", "退出", "返回", TabMainActivity.class, "finish", null, TabMainActivity.class, null, null, false);
				break;
            default:
            	break;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
	WifiStateReceiver mWifiStateReceiver=null;
	public void registerReceiversWIFI(){
		 mWifiStateReceiver = new WifiStateReceiver();
	     registerReceiver(mWifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
	}
	
	int[] WIFI_STATES={0,1,2,3,4};
	class WifiStateReceiver extends BroadcastReceiver {     
		   public void onReceive(Context c, Intent intent) {
		      Bundle bundle = intent.getExtras();
		      int oldInt = bundle.getInt("previous_wifi_state");
		      int newInt = bundle.getInt("wifi_state");
		      String oldStr = (oldInt>=0 && oldInt<WIFI_STATES.length) ?WIFI_STATES[oldInt]+"" :"?";
		      String newStr = (newInt>=0 && oldInt<WIFI_STATES.length) ?WIFI_STATES[newInt]+"" :"?";
		      Log.e(TAG, "oldS="+oldStr+", newS="+newStr);
		      if(newInt==WifiManager.WIFI_STATE_DISABLED || newInt==WifiManager.WIFI_STATE_ENABLED) {
		    	  if(!L.isNotUseWIFI) onWifiStateChange();  // define this function elsewhere!
		      } 
		     }
		}
	WifiManager mMainWifi=null;
	private void onWifiStateChange() {
		if(L.isNotUseWIFI)return ;
        String ip_str = "";

        WifiInfo info = mMainWifi.getConnectionInfo();
        if(info != null) {
         int ipaddr = info.getIpAddress();
         ip_str = " (ip="+StringizeIp(ipaddr)+")";
        }
       

       if(mMainWifi.isWifiEnabled()==true)
    	   mMainWifi.setWifiEnabled(false);

	}
	public static String StringizeIp(int ip) {
		  int ip4 = (ip>>24) & 0x000000FF;
		  int ip3 = (ip>>16) & 0x000000FF;
		  int ip2 = (ip>> 8 )& 0x000000FF;
		  int ip1 = ip       & 0x000000FF;
		  return Integer.toString(ip1) + "." + ip2 + "." + ip3 + "." + ip4;
	}
	
	
}
