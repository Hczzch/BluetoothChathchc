package com.vise.basebluetooth.callback;

import com.vise.basebluetooth.common.State;


public interface IChatCallback<T> {
    void connectStateChange(State state);
    void writeData(T data, int type);
    void readData(T data, int type);
    void setDeviceName(String name);
    void showMessage(String message, int code);
    void showConnected();
}
