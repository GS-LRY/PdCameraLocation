package com.tlkj.pdcameralocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amap.api.maps.model.LatLng;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseUtil {
	private MyHelper helper;

	public DatabaseUtil(Context context) {
		super();
		helper = new MyHelper(context);
	}

	public void dropTable(String tableName) {
		helper.getWritableDatabase().execSQL(
				"DROP TABLE IF EXISTS " + tableName);
	}

	public void InitLatlngData(InputStream in) {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.beginTransaction();
		String sqlUpdate = null;
		try {
			
			sqlUpdate = readTextFromSDcard(in);
			String[] s = sqlUpdate.split(";");
			Log.v("SLength", "s.length"+s.length);
			//int num = s.length;
			for (int i = 0; i < s.length-1; i++) {
				if (!TextUtils.isEmpty(s[i])) {
					db.execSQL(s[i]);
				}
			}
			db.setTransactionSuccessful();

			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	/**
	 * 按行读取txt
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	private String readTextFromSDcard(InputStream is) throws Exception {
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader bufferedReader = new BufferedReader(reader);
		StringBuffer buffer = new StringBuffer("");
		String str;
		while ((str = bufferedReader.readLine()) != null) {
			buffer.append(str);
			buffer.append("\n");
		}
		return buffer.toString();
	}

	/**
	 * 查询所有数据
	 */
	public ArrayList<LocalLatLng> queryAll_LatLngInformation() {
		SQLiteDatabase db = helper.getReadableDatabase();
		ArrayList<LocalLatLng> list = new ArrayList<LocalLatLng>();
		String sql = "select * from "+MyHelper.TABLE_NAME;
		Cursor cursor = db.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			LocalLatLng latlng = new LocalLatLng();
			latlng.setId(cursor.getInt(cursor.getColumnIndex("id")));
			latlng.setLat(cursor.getString(cursor
					.getColumnIndex("lat")));
			latlng.setLng(cursor.getString(cursor
					.getColumnIndex("lng")));
			latlng.setPac(cursor.getString(cursor.getColumnIndex("package")));
			latlng.setAuthId(cursor.getString(cursor.getColumnIndex("authId")));
			latlng.setBallMachineMainIp(cursor.getString(cursor.getColumnIndex("ballMachineMainIp")));
			latlng.setPlatformIp(cursor.getString(cursor.getColumnIndex("platformIp")));
			latlng.setDeviceAccessId(cursor.getString(cursor.getColumnIndex("deviceAccessId")));
			latlng.setSubnetMask(cursor.getString(cursor.getColumnIndex("subnetMask")));
			latlng.setGateWay(cursor.getString(cursor.getColumnIndex("gateWay")));
			latlng.setEntryNumber(cursor.getString(cursor.getColumnIndex("entryNumber")));
			latlng.setPublicLocus(cursor.getString(cursor.getColumnIndex("publicLocus")));
			latlng.setInstallLocation(cursor.getString(cursor.getColumnIndex("installLocation")));
			latlng.setAccessWay(cursor.getString(cursor.getColumnIndex("accessWay")));
			latlng.setNote(cursor.getString(cursor.getColumnIndex("note")));
			list.add(latlng);
			
		}
		db.close();
		return list;
	}
	
	/**
	 * 根据录入编号查询设备
	 */
	public LocalLatLng queryLatLngByEntryNumber(String entryNumber){
		SQLiteDatabase db = helper.getReadableDatabase();
		LocalLatLng latlng = null;
		String sql = "select * from "+MyHelper.TABLE_NAME +" where entryNumber = '"+entryNumber+"'";
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			latlng = new LocalLatLng();
			latlng.setId(cursor.getInt(cursor.getColumnIndex("id")));
			latlng.setLat(cursor.getString(cursor
					.getColumnIndex("lat")));
			latlng.setLng(cursor.getString(cursor
					.getColumnIndex("lng")));
			latlng.setPac(cursor.getString(cursor.getColumnIndex("package")));
			latlng.setAuthId(cursor.getString(cursor.getColumnIndex("authId")));
			latlng.setBallMachineMainIp(cursor.getString(cursor.getColumnIndex("ballMachineMainIp")));
			latlng.setPlatformIp(cursor.getString(cursor.getColumnIndex("platformIp")));
			latlng.setDeviceAccessId(cursor.getString(cursor.getColumnIndex("deviceAccessId")));
			latlng.setSubnetMask(cursor.getString(cursor.getColumnIndex("subnetMask")));
			latlng.setGateWay(cursor.getString(cursor.getColumnIndex("gateWay")));
			latlng.setEntryNumber(cursor.getString(cursor.getColumnIndex("entryNumber")));
			latlng.setPublicLocus(cursor.getString(cursor.getColumnIndex("publicLocus")));
			latlng.setInstallLocation(cursor.getString(cursor.getColumnIndex("installLocation")));
			latlng.setAccessWay(cursor.getString(cursor.getColumnIndex("accessWay")));
			latlng.setNote(cursor.getString(cursor.getColumnIndex("note")));
		}
		db.close();
		return latlng;
	}
}
