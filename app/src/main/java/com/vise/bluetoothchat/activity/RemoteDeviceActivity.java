package com.vise.bluetoothchat.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vise.basebluetooth.BluetoothChatHelper;
import com.vise.basebluetooth.DeviceInfo;
import com.vise.bluetoothchat.R;
import com.vise.bluetoothchat.common.AppConstant;
import com.vise.bluetoothchat.mode.FriendInfo;
import com.vise.bluetoothchat.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static android.R.attr.key;
import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;
import static android.content.ContentValues.TAG;
import static android.media.CamcorderProfile.get;


public class RemoteDeviceActivity extends Activity {

    private static final int FILE_SELECT_CODE = 9090;
    TextView tv1;
    FriendInfo f ;
    DeviceInfo selectDevice;
    ArrayList<DeviceInfo> pairedDevices = new ArrayList<>();
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitiviy_remote_device);
        BluetoothAdapter.getDefaultAdapter().getAddress();

        f =  this.getIntent().getParcelableExtra(AppConstant.FRIEND_INFO);
        tv1 = (TextView) findViewById(R.id.title2);
        tv1.setText("请从"+ f.getFriendNickName()+"的好友中选择:");



        pairedDevices = (ArrayList<DeviceInfo>) getIntent().getSerializableExtra("device2");

        ArrayAdapter<String> pairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.remote_device_name);
        ListView listview = (ListView) findViewById(R.id.remote_device_list);
        listview.setAdapter(pairedDevicesArrayAdapter);
        listview.setOnItemClickListener(mDeviceClickListener);
        if (pairedDevices.size() > 0) {
            for (DeviceInfo device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.a + "\n" + device.b);
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            selectDevice = pairedDevices.get(arg2);
            Log.d("选择的设备名",selectDevice.a);
            Log.d("选择的设备地址",selectDevice.b);
            showFileChooser();
        }
    };



    private void showFileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择需要发送的文件"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(RemoteDeviceActivity.this, "请安装文件管理器", Toast.LENGTH_SHORT)
                    .show();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);
                    File file = new File(path);
                    if (file.exists()) {
                        Log.d("选择的文件名", file.getName());
                        Log.d("文件路径", path);
                        //

                        Intent intent = new Intent();
                        intent.putExtra("fpath",path);
                        intent.putExtra("dname",selectDevice.a);
                        intent.putExtra("dadd",selectDevice.b);

                        setResult(RESULT_OK,intent);
                        Log.e("99","999出错？");
                        finish();
//                        Intent intent = new Intent();
//                        intent.setAction(Intent.ACTION_SEND);
//
//                        intent.setType("*/*");
//                        intent.setClassName("com.android.bluetooth"
//                                , "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
//                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
//                        startActivity(intent);

                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);


    }
}
