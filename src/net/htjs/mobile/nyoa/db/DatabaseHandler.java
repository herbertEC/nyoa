package net.htjs.mobile.nyoa.db;

import java.util.ArrayList;
import java.util.List;

import net.htjs.mobile.nyoa.util.L;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class DatabaseHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "UserInfosManager";

	private static final String TABLE_UserInfoS = "UserInfos";

	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_PH_NO = "phone_number";
	private static final String KEY_PASS_WORD = "password";

	public static class CONFIGINFO{
		public static String TABLENAME="configinfo";
		public static String ID="id";
		public static String ISRECORD="isrecord";
		public static String PATTERN="pattern";
	}
	
	public static class VPN{
		public static String TABLENAME="vpnnyoa";
		public static String ID="id";
		public static String VpnState="VpnState";		
		public static String username="username";
		public static String password="password";
		public static String VpnState1="isrecord";
	}
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	@Override 
    public void onOpen(SQLiteDatabase db){

        super.onOpen(db);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		if(!db.isOpen()){
			onOpen(db);
		}
		String CREATE_UserInfoS_TABLE = "CREATE TABLE " + TABLE_UserInfoS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
				+ KEY_PH_NO + " TEXT," + KEY_PASS_WORD + " TEXT" + ")";
		String CREATE_ConfigInfo_TABLE="CREATE TABLE " + CONFIGINFO.TABLENAME + "("
		+ CONFIGINFO.ID + " INTEGER PRIMARY KEY," + CONFIGINFO.ISRECORD + " TEXT," + CONFIGINFO.PATTERN+" TEXT" + ")";
		
		if(!this.userTableIsExist()) 
			db.execSQL(CREATE_UserInfoS_TABLE);
		if(this.configTableIsExist()){
			if(this.isColumnExist(CONFIGINFO.TABLENAME, CONFIGINFO.PATTERN)
					&&this.isColumnExist(CONFIGINFO.TABLENAME, CONFIGINFO.ISRECORD)){
				 
			}else{
				db.execSQL("DROP TABLE IF EXISTS " + CONFIGINFO.TABLENAME);
				db.execSQL(CREATE_ConfigInfo_TABLE);
			}
		}else{
			db.execSQL(CREATE_ConfigInfo_TABLE);
		}
		
		L.l("  insert   iiii    " + "0");
	}

	public boolean userTableIsExist() {
		return this.tableIsExist(TABLE_UserInfoS);
	}
	public boolean configTableIsExist() {
		return this.tableIsExist(CONFIGINFO.TABLENAME);
	}
	public boolean tableIsExist(String tableName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = this.getReadableDatabase();
			String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"
					+ tableName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (cursor!=null&&!cursor.isClosed()) {
				cursor.close();
			}
		}
		
		return result;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		db.execSQL("DROP TABLE IF EXISTS " + TABLE_UserInfoS);
		db.execSQL("DROP TABLE IF EXISTS " + CONFIGINFO.TABLENAME);
		onCreate(db);
	}

	
    public boolean isColumnExist(String tableName,String columnName){
               boolean result = false;
               if(tableName == null){
                       return false;
               }
              
               try {
            	   SQLiteDatabase db = this.getReadableDatabase();
                   Cursor cursor = null;
                   String sql = "select count(1) as c from sqlite_master where type ='table' and name ='"+tableName.trim()+"' and sql like '%"+ columnName.trim() +"%'" ;
                   cursor = db.rawQuery(sql, null);
                   if(cursor.moveToNext()){
                           int count = cursor.getInt(0);
                           if(count>0){
                                   result = true;
                           }
                   }
                        
                   cursor.close();
               } catch (Exception e) {
                      e.printStackTrace();
               }               
               return result;
       }
	           
	/**
	 * All CRUD操作
	 */

	public void addUserInfo(UserInfo UserInfo) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, System.currentTimeMillis());
		values.put(KEY_NAME, UserInfo.getName()); // UserInfo Name
		values.put(KEY_PH_NO, UserInfo.getPhoneNumber()); // UserInfo Phone
		values.put(KEY_PASS_WORD, UserInfo.getPassword());

		db.insert(TABLE_UserInfoS, null, values);
		L.l("  insert   iiii    " + UserInfo.toString());
		db.close();
	}

	public UserInfo getUserInfo(String name) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_UserInfoS, new String[] { KEY_ID,
				KEY_NAME, KEY_PH_NO, KEY_PASS_WORD }, KEY_NAME + "=?",
				new String[] { name }, null, null, null, null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
		} else {
			if (!cursor.isClosed()) {
				cursor.close();
			}
			db.close();
			return null;
		}

		UserInfo UserInfo = new UserInfo(cursor.getInt(0), cursor.getString(1),
				cursor.getString(2), cursor.getString(3));
		if (!cursor.isClosed()) {
			cursor.close();
		}
		db.close();
		return UserInfo;
	}
	public void addConfigInfo(ConfigInfo ci) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(CONFIGINFO.ID, System.currentTimeMillis());
		values.put(CONFIGINFO.ISRECORD, ci.isRecord); // UserInfo Name
		values.put(CONFIGINFO.PATTERN, ci.pattern); // UserInfo Phone
		
		db.insert(CONFIGINFO.TABLENAME, null, values);
		L.l("  insert   iiii    " + ci.toString());
		db.close();
	}
	public ConfigInfo getConfigInfo(String name) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(CONFIGINFO.TABLENAME, new String[] { CONFIGINFO.ID,
				CONFIGINFO.ISRECORD }, CONFIGINFO.ISRECORD + "=?",
				new String[] { name }, null, null, null, null);
		if (cursor.getCount() != 0) {
			cursor.moveToFirst();
		} else {
			if (!cursor.isClosed()) {
				cursor.close();
			}
			db.close();
			return null;
		}
		ConfigInfo rtn=new ConfigInfo(cursor.getInt(0),cursor.getString(1), cursor.getString(1));
		 
		if (!cursor.isClosed()) {
			cursor.close();
		}
		db.close();
		return rtn;
	}
	UserInfo getUserInfo(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_UserInfoS, new String[] { KEY_ID,
				KEY_NAME, KEY_PH_NO, KEY_PASS_WORD }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		UserInfo UserInfo = new UserInfo(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
		return UserInfo;
	}

	public List<UserInfo> getAllUserInfos() {
		List<UserInfo> UserInfoList = new ArrayList<UserInfo>();
		String selectQuery = "SELECT  * FROM " + TABLE_UserInfoS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				UserInfo UserInfo = new UserInfo();
				UserInfo.setID((cursor.getInt(0)));
				UserInfo.setName(cursor.getString(1));
				UserInfo.setPhoneNumber(cursor.getString(2));
				UserInfo.setPassword(cursor.getString(3));
				UserInfoList.add(UserInfo);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return UserInfoList;
	}
	public List<ConfigInfo> getAllConfigInfos() {
		List<ConfigInfo> ConfigInfoList = new ArrayList<ConfigInfo>();
		String selectQuery = "SELECT  * FROM " + CONFIGINFO.TABLENAME;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.moveToFirst()) {
			do {
				ConfigInfo ConfigInfo = new ConfigInfo(cursor.getInt(0),cursor.getString(1),cursor.getString(2));
				ConfigInfoList.add(ConfigInfo);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return ConfigInfoList;
	}
	public int updateUserInfo(UserInfo UserInfo) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_NAME, UserInfo.getName());
		values.put(KEY_PH_NO, UserInfo.getPhoneNumber());
		values.put(KEY_PASS_WORD, UserInfo.getPassword());
		return db.update(TABLE_UserInfoS, values, KEY_NAME + " = ?",
				new String[] { String.valueOf(UserInfo.getName()) });
	}

	public void deleteUserInfo(UserInfo UserInfo) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_UserInfoS, "",
				new String[] { String.valueOf(UserInfo.getID()) });
		db.close();
	}

	public void deleteUserInfo() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_UserInfoS, null, null);
		db.close();
	}

	public int getUserInfosCount() {
		String countQuery = "SELECT  * FROM " + TABLE_UserInfoS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		return cursor.getCount();
	}

}
