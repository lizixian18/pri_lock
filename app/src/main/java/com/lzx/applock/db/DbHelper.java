package com.lzx.applock.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xian on 2018/1/22.
 */

public class DbHelper extends SQLiteOpenHelper {
    private static final String name = "music_db";
    private static final int version = 7;
    private static volatile DbHelper instance;

    public DbHelper(Context context) {
        super(context, name, null, version);
    }

    /**
     * 播放列表
     */
    private final String CREATE_TABLE_APP_INFO = "create table "
            + DbConstants.TABLE_APP_INFO + " ( "
            + DbConstants.ID + " text, "
            + DbConstants.PACKAGENAME + " text, "
            + DbConstants.APPNAME + " text, "
            + DbConstants.APPTYPE + " text, "
            + DbConstants.ISLOCK + " text, "
            + DbConstants.ISRECOMMEND + " text, "
            + DbConstants.ISYSAPP + " text, "
            + DbConstants.ISETUNLOCK + " text);";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_APP_INFO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static DbHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DbHelper.class) {
                if (instance == null) {
                    instance = new DbHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }
}