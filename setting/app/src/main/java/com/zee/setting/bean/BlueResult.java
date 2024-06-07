package com.zee.setting.bean;

import android.bluetooth.BluetoothDevice;

public class BlueResult {
    public BluetoothDevice bluetoothDevice;
    public short rssi;
    public boolean isPaired;
    public boolean isConnected;
    public int type;


    public BlueResult(BluetoothDevice bluetoothDevice, short rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }

    public BlueResult(BluetoothDevice bluetoothDevice, short rssi, boolean isPaired,int type) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.isPaired = isPaired;
        this.type=type;
    }

    public BlueResult(BluetoothDevice bluetoothDevice, short rssi, boolean isPaired, boolean isConnected,int type) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.isPaired = isPaired;
        this.isConnected = isConnected;
        this.type=type;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public boolean isPaired() {
        return isPaired;
    }

    public void setPaired(boolean paired) {
        isPaired = paired;
    }

    public short getRssi() {
        return rssi;
    }

    public void setRssi(short rssi) {
        this.rssi = rssi;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
