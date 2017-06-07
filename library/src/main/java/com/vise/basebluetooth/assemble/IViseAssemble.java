package com.vise.basebluetooth.assemble;



public interface IViseAssemble extends IBaseAssemble {
    /*设置数据长度*/
    void setDataLength(byte[] dataLength);

    /*设置协议版本*/
    void setProtocolVersion(byte protocolVersion);

    /*设置发送类型*/
    void setCommandType(byte commandType);
}
