package net.htjs.mobile.nyoa.updateapk;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.http.client.ClientProtocolException;

import net.htjs.mobile.nyoa.util.L;
import net.htjs.mobile.nyoa.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class Myinfodata {

	public String NewVersion=null;
	public String DownUrl=null;
	
	public Myinfodata getmyinfodata(){
		final Myinfodata mid=new Myinfodata();
		
		try {
			JSONObject jo=Util.getJOFromString(Util.getJsonFromUrl(L.appUpdateUrl	, ""));
			if(jo!=null){
				mid.DownUrl=jo.getString(L.APP.CDOWNLOADURL);
				mid.NewVersion=jo.getString(L.APP.CVERSION);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}  
		return mid;
	}
}
