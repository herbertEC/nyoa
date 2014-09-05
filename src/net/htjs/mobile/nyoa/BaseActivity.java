package net.htjs.mobile.nyoa;

import java.util.List;

import net.htjs.mobile.nyoa.db.ConfigInfo;
import net.htjs.mobile.nyoa.db.DatabaseHandler;
import net.htjs.mobile.nyoa.util.L;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.apn.util.ApnUtility;

public class BaseActivity extends Activity {

	public static String pattern="0";//模式默认编号
	protected ApnUtility apnutility = null;//
	DatabaseHandler dbh;
	public void shutDown(){
    	System.exit(1);
    }
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
                        //设置窗口类型
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		dbh = new DatabaseHandler(this);
		apnutility = new ApnUtility(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStop() {
		super.onStop();
 
	}

	 protected String getVersionName() throws Exception
	 {
	           // 获取packagemanager的实例
	           PackageManager packageManager = getPackageManager();
	           // getPackageName()是你当前类的包名，0代表是获取版本信息
	           PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
	           String version = packInfo.versionName;
	           return version;
	 }
	/**
	 * 程序是否在前台运行
	 * 
	 * @return
	 */
	public boolean isAppOnForeground() {

		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		String packageName = getApplicationContext().getPackageName();

		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}

	// 编辑APN内容
	protected void editMobileApn() {
		if(!L.isUseAPN)return;
		int id = -1;
		Uri uri = Uri.parse("content://telephony/carriers");
		ContentResolver resolver = getContentResolver();
		Cursor c = resolver.query(uri, new String[] { "_id", "name", "apn","type" }, "apn like '%"+L.APNNAME+"%'", null, null);

		// 该项APN存在
		 
		if (c != null && c.moveToNext()) {
			id = c.getShort(c.getColumnIndex("_id"));
			String name = c.getString(c.getColumnIndex("name"));
			String apn = c.getString(c.getColumnIndex("apn"));
			String type=c.getString(c.getColumnIndex("type"));
			Log.v("APN", id + name + apn);
			 
			apnutility.setDefaultApn(id);
		} else {
			// 如果不存在该项APN则进行添加
			apnutility.setDefaultApn(apnutility.AddYidongApn());
		}
	}
	
	/**
	 * 选择皮肤
	 * @author Herbert
	 */
	public void partternChose(){
		
		
		try {
			dbh.onCreate(dbh.getWritableDatabase());
		} catch (Exception e) {
		}

		
		List<ConfigInfo> cis = dbh.getAllConfigInfos();
		ConfigInfo ConfigInfo = null;
		if (cis.size() > 0) {
			for (ConfigInfo ci : cis) {
				System.out.println(ci.toString());
			}
			ConfigInfo = cis.get(cis.size() - 1);
			pattern=ConfigInfo.pattern;
		}else{
			dbh.addConfigInfo(new ConfigInfo(0,"1","0"));
			pattern="0";
		}
	}
}