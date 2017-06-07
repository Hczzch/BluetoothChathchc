package com.vise.basebluetooth;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class DeviceInfo implements Serializable {
    public String a="";
    public String b="";

    public DeviceInfo(String a, String b){

        this.a = a;
        this.b = b;
    }

}
