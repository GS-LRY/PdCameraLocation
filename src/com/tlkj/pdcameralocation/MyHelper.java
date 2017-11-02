package com.tlkj.pdcameralocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyHelper extends SQLiteOpenHelper {
	private static String DB_NAME = "LatLngLocation.db";// 数据库名称
	public static String TABLE_NAME = "pdwt";// 经纬度表名
	public static String LATLNG_NAME = "location";// 定位经纬度表

	private Context mContext;
	
	public MyHelper(Context context) {
		super(context, DB_NAME, null, 1);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create table
		String sql = "CREATE TABLE IF NOT EXISTS "
				+ TABLE_NAME + " ( "
				+ "id INTEGER PRIMARY KEY," + "authId TEXT,platformIp TEXT,deviceAccessId TEXT,ballMachineMainIp TEXT,subnetMask TEXT,gateWay TEXT,entryNumber TEXT,publicLocus TEXT,installLocation TEXT,lat TEXT,lng TEXT,accessWay TEXT,note TEXT,package TEXT"
				+ ");";
		db.execSQL(sql);
		
		db.execSQL("CREATE TABLE IF NOT EXISTS "+LATLNG_NAME+ " ( " 
				+"lat TEXT,lng TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		onCreate(db);
	}

	/**
     * 读取数据库文件（.sql），并执行sql语句
     * */
	private void executeAssetsSQL(SQLiteDatabase db, String schemaName){
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(mContext.getAssets().open(com.tlkj.pdcameralocation.Configuration.DB_PATH+"/"+schemaName)));
			String line;
			String buffer = "";
			while((line = in.readLine())!=null){
				buffer+=line;
				if(line.trim().endsWith(";")){
					db.execSQL(buffer.replace(";", ""));
					buffer = "";
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
