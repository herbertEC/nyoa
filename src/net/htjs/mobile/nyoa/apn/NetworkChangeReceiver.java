package net.htjs.mobile.nyoa.apn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkChangeReceiver  extends BroadcastReceiver {
	NetCheck netCheck=new NetCheck();
	 public void onReceive(Context context, Intent intent) {
		 Log.e("NetworkChangeReceiver", "onReceive");
		 ConnectivityManager conManager= (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            NetworkInfo info = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            String apn = StringUtils.null2String(info.getExtraInfo());
            if (!"nyszf.ha".equals(apn.toLowerCase())) {
            	netCheck.checkNetworkInfo(context);
            }
        }
    }
}
