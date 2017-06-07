package com.vise.basebluetooth.callback;


public interface IPairCallback {
    void unBonded();
    void bonding();
    void bonded();
    void bondFail();
}
