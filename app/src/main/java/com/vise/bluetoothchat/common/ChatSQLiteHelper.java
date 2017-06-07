package com.vise.bluetoothchat.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2017/5/31 0031.
 */
public class ChatSQLiteHelper extends SQLiteOpenHelper {
    //调用父类构造器
    public ChatSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                            int version) {
        super(context, name, factory, version);
    }

    /**
     * 当数据库首次创建时执行该方法，一般将创建表等初始化操作放在该方法中执行.
     * 重写onCreate方法，调用execSQL方法创建表
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE chat ("+
                "chat_id varchar(64),"+
                "friendmac varchar(64) ,"+
                "message varchar(255) , "+//信息
                "sendTime varchar(255),  "+//发送时间
                "receiveTime varchar(64) PRIMARY KEY, "+//接收时间
                "isSend integer);");//是否是发送
    }
    /*
    *   private int chatId;
    private FriendInfo friendInfo;
    private BaseMessage message;
    private boolean isSend;
    private String sendTime;
    private String receiveTime;
*/

    //当打开数据库时传入的版本号与当前的版本号不同时会调用该方法
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}