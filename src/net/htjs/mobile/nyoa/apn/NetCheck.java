package net.htjs.mobile.nyoa.apn;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

public class NetCheck {
	public static String APN="nyszf.ha";
	public static final Uri APN_URI = Uri.parse("content://telephony/carriers");
	public static final Uri CURRENT_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");
	public static String getCurrentAPNFromSetting(ContentResolver resolver) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(CURRENT_APN_URI, null, null, null, null);
            String curApnId = null;
            String apnName1=null;
            if (cursor != null && cursor.moveToFirst()) {
                curApnId = cursor.getString(cursor.getColumnIndex("_id"));
                apnName1 = cursor.getString(cursor.getColumnIndex("apn"));
            }
            if(cursor != null){
                cursor.close();
            }
            Log.e("NetCheck getCurrentAPNFromSetting","curApnId:"+curApnId+" apnName1:"+apnName1);
            if (curApnId != null) {
                cursor = resolver.query(APN_URI, null, " _id = ?", new String[]{curApnId}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String apnName = cursor.getString(cursor.getColumnIndex("apn"));
                    return apnName;
                }
            } 

        } catch (SQLException e) {
            Log.e("NetCheck getCurrentAPNFromSetting",e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        } 

        return null;
}
	public static int updateCurrentAPN(ContentResolver resolver, String newAPN) {
        Cursor cursor = null;
        try {
            //get new apn id from list
            cursor = resolver.query(APN_URI, null, " apn = ? and current = 1", new String[]{newAPN.toLowerCase()}, null);
            String apnId = null;
            if (cursor != null && cursor.moveToFirst()) {
                apnId = cursor.getString(cursor.getColumnIndex("_id"));
            }
            if(cursor != null){
                cursor.close();
            }
            Log.e("NetCheck updateCurrentAPN","apnId:"+apnId);
            //set new apn id as chosen one
            if (apnId != null) {
                ContentValues values = new ContentValues();
                values.put("apn_id", apnId);
                resolver.update(CURRENT_APN_URI, values, null, null);
            } else {
                //apn id not found, return 0.
                return 0;
            }
        } catch (SQLException e) {
        	Log.e("NetCheck updateCurrentAPN",e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        } 

        //update success
        return 1;
} 

	public boolean checkNetworkInfo(Context c) {
		boolean ret=false;
		ConnectivityManager conManager= (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean internet=conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        String oldAPN = StringUtils.null2String(info.getExtraInfo());
        String oldSQLAPN=StringUtils.null2String(getCurrentAPNFromSetting(c.getContentResolver()));

        Log.e("NetCheck checkNetworkInfo","oldAPN:"+oldAPN+" oldSQLAPN:"+oldSQLAPN);
        if (internet==false||!APN.equals(oldAPN.toLowerCase())||!APN.equals(oldSQLAPN.toLowerCase())) {
            if("cmwap".equals(oldAPN.toLowerCase())&&APN.equals(oldSQLAPN.toLowerCase())){
            	updateCurrentAPN(c.getContentResolver(), "cmwap");
            	try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            updateCurrentAPN(c.getContentResolver(), "nyszf.ha");
            try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            ret=true;
        }
        return ret; 

	}
}