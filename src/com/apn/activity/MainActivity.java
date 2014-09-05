package com.apn.activity;

import java.util.ArrayList;
import java.util.HashMap;

import net.htjs.mobile.nyoa.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.apn.corner.CornerListView;
import com.apn.util.ApnUtility;

/**
 * Android开发之APN网络切换
 * 
 * @Description: Android开发之APN网络切换
 * 
 * @FileName: MainActivity.java
 * 
 * @Package com.apn.demo
 * 
 * @Author Hanyonglu
 * 
 * @Date 2012-3-27 上午10:55:53
 * 
 * @Version V1.0
 */
public class MainActivity extends Activity {
	private CornerListView cornerListView = null;
	private ArrayList<HashMap<String, String>> mapList = null;
	private SimpleAdapter simpleAdapter = null;
	private ApnUtility apnutility = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 设置窗口特征
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting_apn);

		apnutility = new ApnUtility(this);

		simpleAdapter = new SimpleAdapter(this, getDataSource(),
				R.layout.simple_list_item_1, new String[] { "item_title",
						"item_value" }, new int[] { R.id.item_title });

		cornerListView = (CornerListView) findViewById(R.id.apn_list);
		cornerListView.setAdapter(simpleAdapter);
		cornerListView.setOnItemClickListener(new OnItemListSelectedListener());
	}

	// 设置列表数据
	public ArrayList<HashMap<String, String>> getDataSource() {
		mapList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> map1 = new HashMap<String, String>();
		map1.put("item_title", "设置APN选项");
		HashMap<String, String> map2 = new HashMap<String, String>();
		map2.put("item_title", "编辑APN内容");
		mapList.add(map1);
		mapList.add(map2);

		return mapList;
	}

	// ListView事件监听器
	class OnItemListSelectedListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			switch (position) {
			case 0:
				openApnActivity();
				break;
			case 1:
				editMobileApn();
				break;
			}
		}
	}

	// 设置APN选项
	private void openApnActivity() {
		Intent intent = new Intent(Settings.ACTION_APN_SETTINGS);
		startActivity(intent);
	}

	// 编辑APN内容
	private void editMobileApn() {
		int id = -1;
		Uri uri = Uri.parse("content://telephony/carriers");
		ContentResolver resolver = getContentResolver();
		Cursor c = resolver.query(uri, new String[] { "_id", "name", "apn" },
				"apn like '%hnydz.ha%'", null, null);

		// 该项APN存在
		if (c != null && c.moveToNext()) {
			id = c.getShort(c.getColumnIndex("_id"));
			String name = c.getString(c.getColumnIndex("name"));
			String apn = c.getString(c.getColumnIndex("apn"));

			Log.v("APN", id + name + apn);

			Uri uri1 = Uri.parse("content://telephony/carriers/" + id);

			Intent intent = new Intent(Intent.ACTION_EDIT, uri1);
			startActivity(intent);
			apnutility.setDefaultApn(id);
		} else {
			// 如果不存在该项APN则进行添加
			apnutility.setDefaultApn(apnutility.AddYidongApn());
			Toast.makeText(getApplicationContext(), "再次点击APN内容即可编辑！",
					Toast.LENGTH_LONG).show();
		}
	}
}