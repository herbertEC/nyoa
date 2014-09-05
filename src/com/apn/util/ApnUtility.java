package com.apn.util;

import java.util.ArrayList;
import java.util.HashMap;

import net.htjs.mobile.nyoa.util.L;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Apn基础类，封装关于Apn操作的常用方法
 * 
 * @Description: Apn基础类，封装关于Apn操作的常用方法
 * 
 * @FileName: ApnService.java
 * 
 * @Package com.apn.util
 * 
 * @Author Hanyonglu
 * 
 * @Date 2012-3-27 上午11:06:40
 * 
 * @Version V1.0
 */
public class ApnUtility {
	// 列表数据源
	protected  ArrayList<HashMap<String, String>> apn_list = null;

	// APN列表资源
	protected static Uri APN_LIST_URI = Uri
			.parse("content://telephony/carriers");

	// 默认APN资源
	protected static Uri PREFERRED_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");

	private String tag = "ApnUtility";// 标签

	private int OLD_APN_ID = -1, OLD_NETWORK_TYPE = -1, NEW_APN_ID = -1,
			EM_APN_ID = -1;

	private String NUMERIC = "";

	// hnydz.ha
	private String[] EM_APN = {  L.APN, L.APNNAME, "", "",
			"default" };

	// CMNET
	private String[] CM_APN = { "CMNET", "cmnet", "460", "02", "default,supl" };

	private ApnNode YIDONG_APN = null, YIDONG_OLD_APN = null,
			CHINAMOBILE_APN = null;

	// 网络类型
	private static final int NET_3G = 1, NET_WIFI = 2, NET_OTHER = -1;

	private Context context;

	public ApnUtility(Context context) {
		super();
		this.context = context;
	}

	/**
	 * 得到SIM卡上的信息
	 */
	protected void GetNumeric() {
		TelephonyManager iPhoneManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		NUMERIC = iPhoneManager.getSimOperator();
	}

	/**
	 * 初始化移动APN信息参数
	 */
	protected void InitYidongApn() {
		YIDONG_APN = new ApnNode();
		YIDONG_APN.setName(EM_APN[0]);
		YIDONG_APN.setApn(EM_APN[1]);
		YIDONG_APN.setType(EM_APN[4]);
	}
	public boolean isNyoa(String[] o){
		if(o.length<3)return false;
		if(EM_APN[0].equals(o[0])&&EM_APN[1].equals(o[1])&&EM_APN[4].equals(o[2])){
			return true;
		}
		return false;
	}
	/**
	 * 初始化默认的CMNET参数
	 */
	protected void InitCMApn() {
		GetNumeric();

		CHINAMOBILE_APN = new ApnNode();
		CHINAMOBILE_APN.setName(CM_APN[0]);
		CHINAMOBILE_APN.setApn(CM_APN[1]);
		CHINAMOBILE_APN.setType(CM_APN[4]);
		CHINAMOBILE_APN.setMcc(NUMERIC.substring(0, 3));
		CHINAMOBILE_APN.setMnc(NUMERIC.substring(3, NUMERIC.length()));
	}

	/**
	 * @Name: GetApnList
	 * @Description: 获得APN列表
	 * @param @return 设定文件
	 * @return ArrayList<HashMap<String,String>> 返回类型
	 * @throws
	 */
	public ArrayList<HashMap<String, String>> GetApnList() {
		apn_list = new ArrayList<HashMap<String, String>>();

		Cursor cr = context.getContentResolver().query(APN_LIST_URI, null,
				null, null, null);
		// */
		while (cr != null && cr.moveToNext()) {
			Log.v(tag,
					cr.getString(cr.getColumnIndex("_id")) + "  "
							+ cr.getString(cr.getColumnIndex("name")) + "  "
							+ cr.getString(cr.getColumnIndex("type")) + "  ");
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("id", cr.getString(cr.getColumnIndex("_id")));
			map.put("apn", cr.getString(cr.getColumnIndex("name")));
			map.put("name", cr.getString(cr.getColumnIndex("type")));
			apn_list.add(map);
		}

		if (apn_list.size() > 0) {
			return apn_list;
		} else {
			return null;
		}
	}

	/**
	 * 获得网络连接管理权
	 */
	private ConnectivityManager getConnectManager() {
		ConnectivityManager m_ConnectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		return m_ConnectivityManager;
	}

	/**
	 * 获得当前联网类型wifi or mobile
	 * 
	 * @return
	 */
	private int getNetWorkType() {
		if (getConnectManager() != null) {
			NetworkInfo networkInfo = getConnectManager()
					.getActiveNetworkInfo();
			if (networkInfo != null)
				return networkInfo.getType();
			return -1;
		} else {
			return -1;
		}
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return
	 */
	public int GetCurrentNetType() {
		int net_type = getNetWorkType();

		if (net_type == ConnectivityManager.TYPE_MOBILE) {
			return NET_3G;
		} else if (net_type == ConnectivityManager.TYPE_WIFI) {
			return NET_WIFI;
		}

		return NET_OTHER;
	}

	/**
	 * 要设置的APN是否与当前使用APN一致
	 * 
	 * @return
	 */
	public boolean IsCurrentYidongApn() {
		// 初始化移动APN选项信息
		InitYidongApn();
		YIDONG_OLD_APN = getDefaultAPN();

		if ((YIDONG_APN.getApn().equals(YIDONG_OLD_APN.getApn()))) {
			return true;
		}

		return false;
	}

	/**
	 * 获取当前使用的APN信息
	 * 
	 * @return
	 */
	public ApnNode getDefaultAPN() {
		String id = "";
		String apn = "";
		String name = "";
		String type = "";
		ApnNode apnNode = new ApnNode();
		Cursor mCursor = context.getContentResolver().query(PREFERRED_APN_URI,
				null, null, null, null);

		if (mCursor == null) {
			return null;
		}

		while (mCursor != null && mCursor.moveToNext()) {
			id = mCursor.getString(mCursor.getColumnIndex("_id"));
			name = mCursor.getString(mCursor.getColumnIndex("name"));
			apn = mCursor.getString(mCursor.getColumnIndex("apn"))
					.toLowerCase();
			type = mCursor.getString(mCursor.getColumnIndex("type"))
					.toLowerCase();
		}

		try {
			OLD_APN_ID = Integer.valueOf(id);
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(context, "请配置好APN列表！", Toast.LENGTH_LONG).show();
		}

		apnNode.setName(name);
		apnNode.setApn(apn);
		apnNode.setType(type);

		return apnNode;
	}

	/**
	 * 根据apnId将设置的APN选中
	 * 
	 * @param apnId
	 * @return
	 */
	public boolean setDefaultApn(int apnId) {
		boolean res = false;
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("apn_id", apnId);

		try {
			resolver.update(PREFERRED_APN_URI, values, null, null);
			Cursor c = resolver.query(PREFERRED_APN_URI, new String[] { "name",
					"apn" }, "_id=" + apnId, null, null);
			if (c != null) {
				res = true;
				c.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	/**
	 * 判断要设置的APN是否存在
	 * 
	 * @param apnNode
	 * @return
	 */
	public int IsYidongApnExisted(ApnNode apnNode) {
		int apnId = -1;
		Cursor mCursor = context.getContentResolver().query(APN_LIST_URI, null,
				"apn like '%hnydz.ha%'", null, null);

		while (mCursor != null && mCursor.moveToNext()) {
			apnId = mCursor.getShort(mCursor.getColumnIndex("_id"));
			String name = mCursor.getString(mCursor.getColumnIndex("name"));
			String apn = mCursor.getString(mCursor.getColumnIndex("apn"));
			String proxy = mCursor.getString(mCursor.getColumnIndex("proxy"));
			String type = mCursor.getString(mCursor.getColumnIndex("type"));

			if (apnNode.getName().equals(name)
					&& (apnNode.getApn().equals(apn))
					&& (apnNode.getName().equals(name))
					&& (apnNode.getType().equals(type))) {
				return apnId;
			} else {
				apnId = -1;
			}
		}

		return apnId;
	}

	/**
	 * 判断CMNET是否存在
	 * 
	 * @param apnNode
	 * @return
	 */
	public int IsCMApnExisted(ApnNode apnNode) {
		int apnId = -1;
		Cursor mCursor = context.getContentResolver().query(APN_LIST_URI, null,
				"apn like '%cmnet%' or apn like '%CMNET%'", null, null);

		// 如果不存在CMNET，则添加。
		if (mCursor == null) {
			addCmnetApn();
		}

		while (mCursor != null && mCursor.moveToNext()) {
			apnId = mCursor.getShort(mCursor.getColumnIndex("_id"));
			String name = mCursor.getString(mCursor.getColumnIndex("name"));
			String apn = mCursor.getString(mCursor.getColumnIndex("apn"));
			String proxy = mCursor.getString(mCursor.getColumnIndex("proxy"));
			String type = mCursor.getString(mCursor.getColumnIndex("type"));

			if ((apnNode.getApn().equals(apn))
					) {
				return apnId;
			} else {
				apnId = -1;
			}
		}

		return apnId;
	}

	/**
	 * 利用ContentProvider将添加的APN数据添加进入数据库
	 * 
	 * @return
	 */
	public int AddYidongApn() {
		int apnId = -1;
		GetNumeric();
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();

		values.put("name", EM_APN[0]);
		values.put("apn", EM_APN[1]);
		values.put("type", EM_APN[4]);
		values.put("numeric", NUMERIC);
		values.put("mcc", NUMERIC.substring(0, 3));
		Log.i("mcc", NUMERIC.substring(0, 3));
		values.put("mnc", NUMERIC.substring(3, NUMERIC.length()));
		Log.i("mnc", NUMERIC.substring(3, NUMERIC.length()));
		values.put("proxy", "");
		values.put("port", "");
		values.put("mmsproxy", "");
		values.put("mmsport", "");
		values.put("user", "");
		values.put("server", "");
		values.put("password", "");
		values.put("mmsc", "");

		Cursor c = null;

		try {
			Uri newRow = resolver.insert(APN_LIST_URI, values);
			if (newRow != null) {
				c = resolver.query(newRow, null, null, null, null);
				int idindex = c.getColumnIndex("_id");
				c.moveToFirst();
				apnId = c.getShort(idindex);
				Log.d("Robert", "New ID: " + apnId
						+ ": Inserting new APN succeeded!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (c != null)
			c.close();

		return apnId;

	}

	/**
	 * 删除指定的APN
	 * 
	 * @param id
	 * @return
	 */
	public int Delete_Apn(int id) {
		int deleteId = -1;
		ContentResolver resolver = context.getContentResolver();
		Uri deleteIdUri = ContentUris.withAppendedId(APN_LIST_URI, id);

		try {
			deleteId = resolver.delete(deleteIdUri, null, null);
		} catch (Exception e) {
			return deleteId;
		}

		return deleteId;
	}

	/**
	 * 转换APN状态 将CMNET切换为要设置的APN
	 */
	public void SwitchApn() {
		// 判断网络类型
		switch (GetCurrentNetType()) {
		case NET_3G:
			// 如果3G网络则切换APN网络类型
			if (!IsCurrentYidongApn()) {
				EM_APN_ID = IsYidongApnExisted(YIDONG_APN);

				if (EM_APN_ID == -1) {
					setDefaultApn(AddYidongApn());
				} else {
					setDefaultApn(EM_APN_ID);
				}
			}
			break;
		case NET_WIFI:
			// 如果是无线网络则转换为3G网络
			closeWifiNetwork();
			break;
		case NET_OTHER:
			// 如果是其他网络则转化为3G网络
			break;
		default:
			break;
		}
	}

	/**
	 * 关闭WIFI网络状态
	 */
	public void closeWifiNetwork() {
	}

	/**
	 * 关闭APN，并设置成CMNET
	 */
	public void StopYidongApn() {
		if (IsCurrentYidongApn()) {
			// 初始化CMNET
			InitCMApn();
			int i = IsCMApnExisted(CHINAMOBILE_APN);

			if (i != -1) {
				setDefaultApn(i);
			}
		}
	}

	/**
	 * 添加CMNET
	 * 
	 * @return
	 */
	public int addCmnetApn() {
		int apnId = -1;
		GetNumeric();
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();

		values.put("name", CM_APN[0]);
		values.put("apn", EM_APN[1]);
		values.put("type", EM_APN[4]);
		values.put("numeric", NUMERIC);
		values.put("mcc", NUMERIC.substring(0, 3));
		// Log.i("mcc", NUMERIC.substring(0, 3));
		values.put("mnc", NUMERIC.substring(3, NUMERIC.length()));
		// Log.i("mnc", NUMERIC.substring(3, NUMERIC.length()));
		values.put("proxy", "");
		values.put("port", "");
		values.put("mmsproxy", "");
		values.put("mmsport", "");
		values.put("user", "");
		values.put("server", "");
		values.put("password", "");
		values.put("mmsc", "");

		Cursor c = null;

		try {
			Uri newRow = resolver.insert(APN_LIST_URI, values);

			if (newRow != null) {
				c = resolver.query(newRow, null, null, null, null);
				int idindex = c.getColumnIndex("_id");
				c.moveToFirst();
				apnId = c.getShort(idindex);

				Log.d("Robert", "New ID: " + apnId
						+ ": Inserting new APN succeeded!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (c != null)
			c.close();

		return apnId;
	}
}