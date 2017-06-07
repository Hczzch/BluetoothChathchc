package com.vise.bluetoothchat.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.vise.basebluetooth.BluetoothChatHelper;
import com.vise.basebluetooth.CommandHelper;
import com.vise.basebluetooth.DeviceInfo;
import com.vise.basebluetooth.callback.IChatCallback;
import com.vise.basebluetooth.common.ChatConstant;
import com.vise.basebluetooth.common.State;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.basebluetooth.mode.FileMessage;
import com.vise.basebluetooth.utils.HexUtil;
import com.vise.bluetoothchat.R;
import com.vise.bluetoothchat.adapter.ChatAdapter;
import com.vise.bluetoothchat.common.AppConstant;
import com.vise.bluetoothchat.common.ChatSQLiteHelper;

import com.vise.bluetoothchat.mode.ChatInfo;
import com.vise.bluetoothchat.mode.FriendInfo;
import com.vise.bluetoothchat.utils.FileUtils;
import com.vise.common_base.manager.AppManager;
import com.vise.common_base.utils.ToastUtil;
import com.vise.common_utils.log.LogUtils;
import com.vise.common_utils.utils.character.DateTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import cn.qqtheme.framework.picker.FilePicker;
import cn.qqtheme.framework.util.StorageUtils;

import static android.R.attr.onClick;
import static android.R.attr.path;
import static android.R.id.message;

public class ChatActivity extends BaseChatActivity implements EmojiconsFragment.OnEmojiconBackspaceClickedListener,
        EmojiconGridFragment.OnEmojiconClickedListener {

    private static final  int FILE_SELECT_CODEq = 100;
    private TextView mTitleTv;
    private ListView mChatMsgLv;
    private ImageButton mMsgFaceIb;
    private ImageButton mMsgAddIb;
    private Button geButton;
    private EditText mMsgEditEt;
    private ImageButton mMsgSendIb;
    private FrameLayout mEmojiconFl;
    private ProgressDialog mProgressDialog;
    private ChatAdapter mChatAdapter;
    private FriendInfo mFriendInfo;
    private List<ChatInfo> mChatInfoList = new ArrayList<>();
    private BluetoothChatHelper mBluetoothChatHelper;
    private boolean mIsSendFile = false;
    private File mSendFile;
    private String mFilePath;
    private ChatSQLiteHelper openHelper;
    private SQLiteDatabase db;
    private String myAddress;

    private List<DeviceInfo> mDeviceInfo = new ArrayList<DeviceInfo>();
//    private FileTransportThread fileThread;
//    private BluetoothSocket bs;

    private IChatCallback<byte[]> chatCallback = new IChatCallback<byte[]>() {
        @Override
        public void connectStateChange(State state) {
            LogUtils.i("connectStateChange:"+state.getCode());
            if(state == State.STATE_CONNECTED){
                if (mProgressDialog != null) {
                    mProgressDialog.hide();
                }
                if(mFriendInfo != null){
                    mTitleTv.setText(mFriendInfo.getFriendNickName()+"("+getString(R.string.device_online)+")");
                }
                ToastUtil.showToast(mContext, getString(R.string.connect_friend_success));
            }
        }

        @Override
        public void writeData(byte[] data, int type) {
            if(data == null){
                LogUtils.e("writeData is Null or Empty!");
                return;
            }
            LogUtils.i("writeData:"+HexUtil.encodeHexStr(data));
        }

        @Override
        public void readData(byte[] data, int type) {
            if(data == null){
                LogUtils.e("readData is Null or Empty!");
                return;
            }

//            LogUtils.i("readData:"+HexUtil.encodeHexStr(data));
            try {
               String con = new String(data);
                Log.d("聊天时打印",con);
               JSONArray jsonArray= new JSONArray(con);
                for (int i=0;i<jsonArray.length();i++)//接受到的设备信息
                {
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    DeviceInfo deviceInfo=new DeviceInfo(jsonObject.getString("a"),jsonObject.getString("b"));

                    if(deviceInfo.b.equals(myAddress)){
                        Log.d("自己Mac","因为重复了");
                    }else{
                        mDeviceInfo.add(deviceInfo);
                    }
                }
                Log.d("列表长度",""+mDeviceInfo.size());
//                Gson gs = new Gson();
//                DeviceInfo i = gs.fromJson(con,DeviceInfo.class);
//                mDeviceInfo.add(i);


//                Set<BluetoothDevice> set1 = new Gson().fromJson(con,new TypeToken<Set<BluetoothDevice>>(){}.getType());


//                JsonObject jsonObject=new Gson().
//                if (jsonObject.isJsonObject())
//                {
//
//                }
            } catch (JSONException e) {//接受到的普通信息
                BaseMessage message = null;
                try {
                    message = CommandHelper.unpackData(data);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                if(message.getMsgContent().contains("qwertyuiop")){
                        String []s=message.getMsgContent().split("-qwertyuiop-");
                        String fname3= s[0];
                        String dname3=s[1];
                        ToastUtil.showToast(mContext,"请将"+fname3+"发送给："+dname3);
                        File sendfile=new File("/storage/emulated/0/bluetooth/"+fname3);
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("*/*");
                        sharingIntent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sendfile));
                        startActivityForResult(sharingIntent, 30);



                }else{

                ChatInfo chatInfo = new ChatInfo();
                chatInfo.setMessage(message);
                chatInfo.setReceiveTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
                chatInfo.setSend(false);
                chatInfo.setFriendInfo(mFriendInfo);
                Log.e("接受到的信息",chatInfo.toString());


                //存入数据库
                savechatInfoIntoSQL(chatInfo);
                mChatInfoList.add(chatInfo);
                mChatAdapter.setListAll(mChatInfoList);
                }
            }
        }
      public void savechatInfoIntoSQL(ChatInfo chatInfo)
      {
          /**
           db.execSQL("CREATE TABLE chat ("+
           "chat_id varchar(64),"+
           "friendmac varchar(64) ,"+
           "message varchar(255) , "+//信息
           "sendTime varchar(255),  "+//发送时间
           "receiveTime varchar(64) PRIMARY KEY, "+//接收时间
           "isSend integer);");//是否是发送
           */


          //getContentResolver().insert(DataChangeProvider.CONTENT_URI, con);  //保存到本地sqlite
          db = openHelper.getReadableDatabase();
          ContentValues values = new ContentValues();
          values.put("chat_id",chatInfo.getChatId());
          values.put("friendmac",mFriendInfo.getDeviceAddress());
          values.put("message",chatInfo.getMessage().getMsgContent().toString());
          values.put("sendTime",chatInfo.getSendTime());
          values.put("receiveTime",chatInfo.getReceiveTime());
          values.put("isSend",chatInfo.isSend()?1:0);
//          Log.e("sqlite写入成功",chatInfo.getMessage().getMsgContent().toString());
          db.insert("chat",null,values);
          db.close();
      }
        @Override
        public void setDeviceName(String name) {
            LogUtils.i("setDeviceName:"+name);
        }

        @Override
        public void showMessage(String message, int code) {
            if (!isFinishing()) {
                return;
            }
            LogUtils.i("showMessage:"+message);
            if (mProgressDialog != null) {
                mProgressDialog.hide();
            }
            ToastUtil.showToast(mContext, getString(R.string.connect_friend_fail));
        }

        @Override
        public void showConnected() {
            if(mBluetoothChatHelper.getState() == State.STATE_CONNECTED){
//                bs = mBluetoothChatHelper.getConnectedThread().getBTSocket();
                Log.d("BT","已连接");
                //打开文件传输线程
//                fileThread = new FileTransportThread(bs);
//                fileThread.start();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_chat);
        initdb();
        myAddress=BluetoothAdapter.getDefaultAdapter().getAddress();
        Log.d("自己的Mac地址",myAddress);
        Log.d("初始化数据库","success");
    }

    @Override
    protected void initWidget() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initdb();
        Log.e("init","初始化数据库");
        mTitleTv = (TextView) findViewById(R.id.title);
        mChatMsgLv = (ListView) findViewById(R.id.chat_msg_show_list);
        mMsgFaceIb = (ImageButton) findViewById(R.id.chat_msg_face);
        mMsgAddIb = (ImageButton) findViewById(R.id.chat_msg_add);
        mMsgEditEt = (EditText) findViewById(R.id.chat_msg_edit);
        mMsgSendIb = (ImageButton) findViewById(R.id.chat_msg_send);
        mEmojiconFl = (FrameLayout) findViewById(R.id.chat_emojicons);
        mProgressDialog = new ProgressDialog(mContext);
        geButton = (Button) findViewById(R.id.ge_button);
    }

    @Override
    protected void initData() {
        mFriendInfo = this.getIntent().getParcelableExtra(AppConstant.FRIEND_INFO);
        Log.d("friend",mFriendInfo.toString());
        if (mFriendInfo == null) {
            return;
        }
        if(mFriendInfo.isOnline()){
            mTitleTv.setText(mFriendInfo.getFriendNickName()+"("+getString(R.string.device_online)+")");
        } else{
            mTitleTv.setText(mFriendInfo.getFriendNickName()+"("+getString(R.string.device_offline)+")");
        }
        //读取数据库中的记录


        mChatAdapter = new ChatAdapter(mContext);
//        readChat();
        mChatMsgLv.setAdapter(mChatAdapter);

        mBluetoothChatHelper = new BluetoothChatHelper(chatCallback);       //此处
        mProgressDialog.setMessage(getString(R.string.connect_friend_loading));
        if(!isFinishing() && !mProgressDialog.isShowing()){
            mProgressDialog.show();
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothChatHelper.connect(mFriendInfo.getBluetoothDevice(), false);
            }
        }, 3000);
        //??????
//        bs = mBluetoothChatHelper.getConnectedThread().getBTSocket();
    }

    @Override
    protected void initEvent() {
        mMsgFaceIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmojiconFl.getVisibility() == View.GONE){
                    hideSoftInput();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mEmojiconFl.setVisibility(View.VISIBLE);
                            setEmojiconFragment(false);
                        }
                    }, 100);
                } else{
                    mEmojiconFl.setVisibility(View.GONE);
                }
            }
        });

        mMsgAddIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        mMsgSendIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMsgEditEt.getText() != null && mMsgEditEt.getText().toString().trim().length() > 0){
                    sendMessage((mMsgEditEt.getText().toString()));
                } else{
                    ToastUtil.showToast(mContext, getString(R.string.send_msg_isEmpty));
                }
            }
        });
        mMsgEditEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmojiconFl.setVisibility(View.GONE);
            }
        });
        geButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putParcelable(AppConstant.FRIEND_INFO, mFriendInfo);
                intent.putExtras(bundle);
                intent.putExtra("device2",(Serializable) mDeviceInfo);
                intent.setClass(ChatActivity.this, RemoteDeviceActivity.class);

                Log.d("1",mDeviceInfo.size()+"");
                startActivityForResult(intent,99);


            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothChatHelper != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mBluetoothChatHelper.getState() == State.STATE_NONE) {
                // Start the Bluetooth chat services
                mBluetoothChatHelper.start(false);
            }

        }
    }

    private void sendMessage(String msg) {
        ChatInfo chatInfo = new ChatInfo();
        FriendInfo friendInfo = new FriendInfo();
        friendInfo.setBluetoothDevice(mBluetoothChatHelper.getAdapter().getRemoteDevice(mBluetoothChatHelper.getAdapter().getAddress()));
        friendInfo.setOnline(true);
        friendInfo.setFriendNickName(mBluetoothChatHelper.getAdapter().getName());
        friendInfo.setIdentificationName(mBluetoothChatHelper.getAdapter().getName());
        friendInfo.setDeviceAddress(mBluetoothChatHelper.getAdapter().getAddress());
        chatInfo.setFriendInfo(friendInfo);
        chatInfo.setSend(true);
        chatInfo.setSendTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
        chatInfo.setReceiveTime(chatInfo.getSendTime());
        BaseMessage message = null;
        if(mIsSendFile){
            message = new FileMessage();
            message.setMsgType(ChatConstant.VISE_COMMAND_TYPE_FILE);
            message.setMsgContent(mMsgEditEt.getText().toString());
            message.setMsgLength(mMsgEditEt.getText().length());
            if(mSendFile != null){
                ((FileMessage)message).setFileLength((int) mSendFile.length());
                ((FileMessage)message).setFileName(mSendFile.getName());
            }
            if(mFilePath != null){
                ((FileMessage)message).setFileNameLength(mFilePath.length());
            }
        } else{
            message = new BaseMessage();
            message.setMsgType(ChatConstant.VISE_COMMAND_TYPE_TEXT);
            message.setMsgContent(msg);
            message.setMsgLength(mMsgEditEt.getText().length());
        }
        if(message.getMsgContent().contains("qwertyuiop")){

        }else {
            chatInfo.setMessage(message);
            mChatInfoList.add(chatInfo);
            mChatAdapter.setListAll(mChatInfoList);
            mMsgEditEt.setText("");
        }
        try {
            if(mIsSendFile && mSendFile != null){
                mBluetoothChatHelper.write(CommandHelper.packFile(mSendFile));
                mIsSendFile = false;
                //调用系统程序发送文件
                String uri = "file://" + mFilePath;
//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                sharingIntent.setType("*/*");
//                sharingIntent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
//                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mSendFile));
//                startActivityForResult(sharingIntent, 1);
            } else {
                if(message.getMsgContent().contains("qwertyuiop")){

                }else{
                SQLiteDatabase db=openHelper.getReadableDatabase();
                ContentValues values = new ContentValues();

                values.put("chat_id",chatInfo.getChatId());
                values.put("friendmac",mFriendInfo.getDeviceAddress());
                values.put("message",message.getMsgContent());
                values.put("sendTime",chatInfo.getSendTime());
                values.put("receiveTime",chatInfo.getReceiveTime());
                values.put("isSend",chatInfo.isSend()?1:0);
                db.insert("chat",null,values);
                db.close();
                Log.e("发送的消息已存入数据库",message.getMsgContent());
                }
                mBluetoothChatHelper.write(CommandHelper.packMsg(message.getMsgContent()));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if(mBluetoothChatHelper != null){
            mBluetoothChatHelper.stop();
            mBluetoothChatHelper = null;
        }
        super.onDestroy();
    }

    private void hideSoftInput(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_emojicons, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mMsgEditEt);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mMsgEditEt, emojicon);
    }
    private void showFileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择需要发送的文件"),
                    FILE_SELECT_CODEq);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            ToastUtil.showToast(mContext,"请安装文件管理器");
        }
    }
    File file2=null;
    String dname="";
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {

        switch (requestCode) {
            case FILE_SELECT_CODEq:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Log.e("文件选择完毕！","成功！");
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);
//                    sendfile();         //!!!!!!
                    Log.d("文件路径",path);
                    File file = new File(path);
                    if (file.exists()) {
//
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("*/*");
                        sharingIntent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        startActivityForResult(sharingIntent, 1);


                    }
                    else{
                        Log.d("文件不存在","错误信息");
                    }
                }
                break;
            case 99:
                if(resultCode == RESULT_OK){
                String fpath = data.getStringExtra("fpath");
                dname = data.getStringExtra("dname");
                String dadd=data.getStringExtra("dadd");
                file2 = new File(fpath);
                if(file2.exists()) {

                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("*/*");
                    sharingIntent.setComponent(new ComponentName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity"));
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file2));
                    startActivityForResult(sharingIntent, 98);


                }
                }
                break;
            case 98:
                if(file2.exists()) {
                    sendMessage(file2.getName() + "-qwertyuiop-" + dname);

                }else{
                    Log.d("file2","不存在");
                }
                break;
            case 30 :
                ToastUtil.showToast(mContext,"已向目的设备传输");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
//    private void readChat(){
//
//        boolean _isComMeg = true;
//        //获取数据库中的信息
//        /*
//        *   db.execSQL("CREATE TABLE chat ("+
//                "chat_id INTEGER PRIMARY KEY,"+
//                "friendInfo varchar(255), "+//朋友信息
//                "message varchar(255) , "+//信息
//                "sendTime varchar(255),  "+//发送时间
//                "receiveTime varchar(64), "+//接收时间
//                "isSend integer);");//是否是发送
//        * */
//        SQLiteDatabase db=openHelper.getWritableDatabase();
//        String sql="select * from chat where friendmac=?";
//        Cursor c = db.rawQuery(sql,new String[]{mFriendInfo.getDeviceAddress()});
//        while(c.moveToNext()){
//            ChatInfo chatInfo=new ChatInfo();
//            chatInfo.setChatId(c.getInt(0));
//            chatInfo.setFriendInfo(mFriendInfo);
//            String s=c.getString(2);
//            /*
//            * "BaseMessage{" +
//                "msgType=" + msgType +
//                ", msgContent='" + msgContent + '\'' +
//                ", msgLength=" + msgLength +
//                '}';
//            * */
//            String[] str=s.split(",");
//            BaseMessage m=new BaseMessage();
//            m.setMsgType(ChatConstant.VISE_COMMAND_TYPE_TEXT);
//            m.setMsgContent(s);
//            m.setMsgLength(s.length());
//            chatInfo.setMessage(m);
//            chatInfo.setSendTime(c.getString(3));
//            chatInfo.setReceiveTime(c.getString(4));
//            chatInfo.setSend(c.getInt(5)==1?true:false);
//
//            Log.e("数据库中读取的",m.getMsgContent());
//
//            mChatInfoList.add(chatInfo);
//
//        }
////        Log.e("读取数据库总记录长度",mChatInfoList.toString());
//        mChatAdapter.setListAll(mChatInfoList);
//        db.close();
//    }
    private void initdb()
    {
        //准备数据库，存取聊天记录
        openHelper=new ChatSQLiteHelper(ChatActivity.this,"chat.db",null,1) ;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.chat_record){
//            ToastUtil.showToast(mContext, getString(R.string.menu_share));
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable(AppConstant.FRIEND_INFO, mFriendInfo);
            intent.putExtras(bundle);
            intent.setClass(ChatActivity.this,ChatRecordActivity.class);
            startActivity(intent);
            ToastUtil.showToast(mContext,"打开聊天记录");
            return true;
        }
        if(id == R.id.record_delete){
            Log.e("A++++",mFriendInfo.getDeviceAddress());
            db = openHelper.getReadableDatabase();
            db.delete("chat","friendmac = ?", new String[]{mFriendInfo.getDeviceAddress()});
            db.close();
//            String s= "DELETE * FROM chat WHERE friendmac = "+mFriendInfo.getDeviceAddress();
//            Log.e("BBB",s);
//            db.execSQL(s);
        }


        return super.onOptionsItemSelected(item);
    }

//    public void fileTrans(File file) {
//        fileThread.sendFile(file);
//    }
}
