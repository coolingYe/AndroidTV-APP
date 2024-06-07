package com.zee.device.base.model;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.zee.device.base.db.DatabaseSettings;

import java.io.Serializable;

public class DeviceInfo implements Serializable {
    public long _id;
    public String sn;
    public String mac;
    public String name;
    public String ip;
    public int port;
    public int extraOne;
    public String extraTwo;
    public long saveTime;
    public String describe;
    public int status = 0;

    public DeviceInfo() {
    }

    @SuppressLint("Range")
    public DeviceInfo(Cursor cursor){
        _id = cursor.getLong(cursor.getColumnIndex(DatabaseSettings.DeviceInfo._ID));
        sn = cursor.getString(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.SN));
        mac = cursor.getString(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.MAC));
        name = cursor.getString(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.NAME));
        ip = cursor.getString(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.IP));
        port = cursor.getInt(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.PORT));
        extraOne = cursor.getInt(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.EXTRA_ONE));
        extraTwo = cursor.getString(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.EXTRA_TWO));
        saveTime = cursor.getLong(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.SAVE_TIME));
        describe = cursor.getString(cursor.getColumnIndex(DatabaseSettings.DeviceInfo.DESC));
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "_id=" + _id +
                ", sn='" + sn + '\'' +
                ", mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", extraOne=" + extraOne +
                ", extraTwo='" + extraTwo + '\'' +
                ", saveTime=" + saveTime +
                ", describe='" + describe + '\'' +
                ", status=" + status +
                '}';
    }
}
