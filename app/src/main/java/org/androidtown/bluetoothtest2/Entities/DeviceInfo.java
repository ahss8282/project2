package org.androidtown.bluetoothtest2.Entities;

import java.io.Serializable;

/**
 * Created by 재민 on 2016-05-15.
 */
public class DeviceInfo implements Serializable {
    private String name;
    private String address;
    private int bondState;

    public DeviceInfo(String name, String address,int bondState) {
        this.name = name;
        this.address = address;
        this.bondState = bondState;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

}
