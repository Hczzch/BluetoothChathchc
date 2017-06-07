package com.vise.bluetoothchat.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.vise.basebluetooth.common.ChatConstant;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.bluetoothchat.R;
import com.vise.bluetoothchat.adapter.ChatAdapter;
import com.vise.bluetoothchat.common.AppConstant;
import com.vise.bluetoothchat.common.ChatSQLiteHelper;
import com.vise.bluetoothchat.mode.ChatInfo;
import com.vise.bluetoothchat.mode.FriendInfo;

import java.util.ArrayList;
import java.util.List;


public class ChatRecordActivity extends Activity {
    private ChatSQLiteHelper openHelper;
    private List<ChatInfo> mChatInfoList = new ArrayList<>();
    private FriendInfo mFriendInfo;
    private ChatAdapter mChatAdapter;
    private TextView mTitleTv;
    private ListView mChatMsgLv;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_record);
        initdb();   //在此处取得chat.db
        initWidget();
        readChat();
    }

    protected void initWidget() {
        mFriendInfo = this.getIntent().getParcelableExtra(AppConstant.FRIEND_INFO);
        mTitleTv = (TextView) findViewById(R.id.record_title);
        mChatMsgLv = (ListView) findViewById(R.id.record_listview);
        mChatAdapter = new ChatAdapter(ChatRecordActivity.this);
        mChatMsgLv.setAdapter(mChatAdapter);
    }

    private void readChat() {

        boolean _isComMeg = true;
        //获取数据库中的信息
        /*
        *   db.execSQL("CREATE TABLE chat ("+
                "chat_id INTEGER PRIMARY KEY,"+
                "friendInfo varchar(255), "+//朋友信息
                "message varchar(255) , "+//信息
                "sendTime varchar(255),  "+//发送时间
                "receiveTime varchar(64), "+//接收时间
                "isSend integer);");//是否是发送
        * */
//        SQLiteDatabase db=openHelper.getWritableDatabase();
        String sql = "select * from chat where friendmac=?";
        Cursor c = db.rawQuery(sql, new String[]{mFriendInfo.getDeviceAddress()});
        while (c.moveToNext()) {
            ChatInfo chatInfo = new ChatInfo();
            chatInfo.setChatId(c.getInt(0));
            chatInfo.setFriendInfo(mFriendInfo);
            String s = c.getString(2);
            /*
            * "BaseMessage{" +
                "msgType=" + msgType +
                ", msgContent='" + msgContent + '\'' +
                ", msgLength=" + msgLength +
                '}';
            * */
            String[] str = s.split(",");
            BaseMessage m = new BaseMessage();
            m.setMsgType(ChatConstant.VISE_COMMAND_TYPE_TEXT);
            m.setMsgContent(s);
            m.setMsgLength(s.length());
            chatInfo.setMessage(m);
            chatInfo.setSendTime(c.getString(3));
            chatInfo.setReceiveTime(c.getString(4));
            chatInfo.setSend(c.getInt(5) == 1 ? true : false);

            Log.e("数据库中读取的", m.getMsgContent());

            mChatInfoList.add(chatInfo);

        }
//        Log.e("读取数据库总记录长度",mChatInfoList.toString());
        mChatAdapter.setListAll(mChatInfoList);
        db.close();
    }


    private void initdb() {
        //准备数据库，存取聊天记录
        openHelper = new ChatSQLiteHelper(ChatRecordActivity.this, "chat.db", null, 1);
        db = openHelper.getReadableDatabase();
    }

//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK){
//            setResult(10086);
//        }
//        return false;
//    }
}