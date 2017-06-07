package com.vise.basebluetooth.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.google.gson.Gson;
import com.vise.basebluetooth.BluetoothChatHelper;
import com.vise.basebluetooth.DeviceInfo;
import com.vise.basebluetooth.common.ChatConstant;
import com.vise.basebluetooth.utils.BleLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;


public class ConnectedThread extends Thread {

    private final BluetoothChatHelper mHelper;
    private final BluetoothSocket mSocket;
    private final InputStream mInStream;
    private final OutputStream mOutStream;

    public BluetoothSocket getBTSocket(){
        return mSocket;
    }
    public ConnectedThread(BluetoothChatHelper bluetoothChatHelper, BluetoothSocket socket, String socketType) {
        BleLog.i("create ConnectedThread: " + socketType);
        mHelper = bluetoothChatHelper;
        mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            BleLog.e("temp sockets not created", e);
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
        init();
    }

    public void run() {
        BleLog.i("BEGIN mConnectedThread");
        int bytes;
        byte[] buffer = new byte[1024];

        // Keep listening to the InputStream while connected
        while (true) {
            try {
                bytes = mInStream.read(buffer);
                byte[] data = new byte[bytes];

//                String str = new String(data,"GB2312");
//                JSONObject jsonObject= null;
//                Log.d("接收方str打印",str);
//                try {
//                    jsonObject = new JSONObject(str);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                System.arraycopy(buffer, 0, data, 0, data.length);
                mHelper.getHandler().obtainMessage(ChatConstant.MESSAGE_READ, bytes, -1, data).sendToTarget();
            } catch (IOException e) {
                BleLog.e("disconnected", e);
                mHelper.start(false);
                break;
            }
        }
    }

    public void write(byte[] buffer) {
        if(mSocket.isConnected()){
            try {
                mOutStream.write(buffer);
                try {
                    String str = new String(buffer);
                    JSONObject jsonObject = new  JSONObject(str);
                    Log.d("发送方json打印",jsonObject.toString());
                } catch (JSONException e) {
                    mHelper.getHandler().obtainMessage(ChatConstant.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
                }


            } catch (IOException e) {
                BleLog.e("Exception during write", e);
            }
        }
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            BleLog.e("close() of connect socket failed", e);
        }
    }
    private void init(){
        Set<BluetoothDevice> info= BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (info.size() > 0) {
            int i=0;
            JSONArray  jsonArray = new JSONArray();
            for (BluetoothDevice device : info) {

                String name = device.getName();
                String add = device.getAddress();
                DeviceInfo di = new DeviceInfo(name, add);
                Log.d("jack","取得"+ (++i) + "个" );
                Gson gs = new Gson();
                String str = gs.toJson(di);
                try {
                    JSONObject js = new JSONObject(str);
                    jsonArray.put(js);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("此次取得的东西",str);


//                try {
//                    sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            write(jsonArray.toString().getBytes());
            Log.i("初始时打印所有设备信息", jsonArray.toString());
        }
    }
}
