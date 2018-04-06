/*
 * SqlConnection.java
 * 
 * author : rahmatul.hidayat@gmail.com
 * 2014
 */
package com.tampir.jlast.utils;
/*
 * SqlConnection.java
 *
 * author : rahmatul.hidayat@gmail.com
 * 2014
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SqlConnection {
	private static String DB;
	private static String TB_CONTENT;
	private static SQLiteDatabase dbshared;

	private Context context;
	private SQLiteDatabase db;
	private String C_CATEGORY = encrypt_value("a").replaceAll("=","");
	private String C_CONTENT = encrypt_value("b").replaceAll("=","");
	private String C_DT = encrypt_value("c").replaceAll("=","");

	public String encrypt_value(String value){
		String key = General.md5(General.K_RC4(),true);
		String encrypted = RC4.Encrypt(key,value+"");
		byte[] data = null;
		try {
			data = encrypted.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String encode = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);
		return encode;
	}
	public String decrypt_value(String value){
		byte[] decode = android.util.Base64.decode(value, android.util.Base64.DEFAULT);
		String data = "";
		try {
			data = new String(decode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String key = General.md5(General.K_RC4(),true);
		String decript = RC4.Decrypt(key,data);
		return decript;
	}
	public SqlConnection(Context context){
		this.context=context;
		this.DB=encrypt_value(General.K_DB()).replaceAll("=","");
		this.TB_CONTENT=encrypt_value("CONTENT").replaceAll("=","");

		if (dbshared==null){
			ODataHelper DB = new ODataHelper(this.context);
			this.db=DB.getWritableDatabase();
			dbshared=this.db;
		}else{
			this.db=dbshared;
		}
	}

	public void setContent (String content, String category){
		if (content!=null) {
			setContent(content,category,getCurrentDate());
		}
	}
	public void setContent (String content, String category, String dateContent){
		if (content!=null) {
			String sql = "INSERT INTO " + TB_CONTENT + " ("+C_CONTENT+","+C_CATEGORY+","+C_DT+") VALUES ('" + encrypt_value(content) + "','" + category + "','" + encrypt_value(dateContent) + "')";
			this.db.execSQL(sql);
		}
	}
	public void removeContentAll(String category){
		this.db.delete(TB_CONTENT,C_CATEGORY + "='" + category + "'",null);
	}
	public void removeContentBeforeAll(String category){
		this.db.delete(TB_CONTENT,C_CATEGORY + "='" + category + "' AND " + C_DT + "<>'"+encrypt_value(getCurrentDate())+"'",null);
	}
	public void removeContentAllStartWith(String category){
		this.db.delete(TB_CONTENT,C_CATEGORY + " LIKE '" + category + "%'",null);
	}
	public void removeContentBeforeAllStartWith(String category){
		this.db.delete(TB_CONTENT,C_CATEGORY + " LIKE '" + category + "%' AND " + C_DT + "<>'"+encrypt_value(getCurrentDate())+"'",null);
	}
	public void removeContent(String category, String dateContent){
		this.db.delete(TB_CONTENT,C_CATEGORY + "='" + category + "' AND " + C_DT + "='" + encrypt_value(dateContent)+ "'",null);
	}
	public String getContent (String category){
		return getContent(category,getCurrentDate());
	}
	public String getContent (String category, String dateContent){
		String ret = null;
		Cursor cursor = this.db.query(TB_CONTENT, new String[] {C_CONTENT},C_CATEGORY + "='" + category + "' AND " + C_DT + "='"+encrypt_value(dateContent)+"'", null, null, null, null);
		if (cursor.moveToFirst()){
			ret=decrypt_value(cursor.getString(0));
		}
		cursor.close();
		return ret;
	}
	public List getContentStartWith (String category){
		return getContentStartWith(category,getCurrentDate());
	}
	public List getContentStartWith (String category, String dateContent){
		List ret = new ArrayList();
		Cursor cursor = this.db.query(TB_CONTENT, new String[] {C_CONTENT},C_CATEGORY + " LIKE '" + category + "%' AND " + C_DT + "='"+encrypt_value(dateContent)+"'", null, null, null, null);
		if (cursor.moveToFirst()) {
			int cursorSize = cursor.getCount();
			for (int i=0;i<cursorSize;i++) {
				ret.add(decrypt_value(cursor.getString(0)));
				cursor.moveToNext();
			}
		}
		cursor.close();
		return ret;
	}

	public List getContentAll (String category){
		List ret = new ArrayList();
		Cursor cursor = this.db.query(TB_CONTENT, new String[] {C_CONTENT},C_CATEGORY + " = '" + category + "'", null, null, null, null);
		if (cursor.moveToFirst()) {
			int cursorSize = cursor.getCount();
			for (int i=0;i<cursorSize;i++) {
				ret.add(decrypt_value(cursor.getString(0)));
				cursor.moveToNext();
			}
		}
		cursor.close();
		return ret;
	}
	private String getCurrentDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(new Date());
	}

	private class ODataHelper extends SQLiteOpenHelper {
		ODataHelper(Context context){
			super(context,DB,null, General.V_DB());
		}
		@Override
		public void onCreate(SQLiteDatabase db){
			db.execSQL("CREATE TABLE " + TB_CONTENT + "(" + C_CATEGORY + " varchar(20)," + C_CONTENT + " text, "+ C_DT +" varchar(32))");
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TB_CONTENT);
			onCreate(db);
		}
	}
}

