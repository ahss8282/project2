package org.androidtown.bluetoothtest2.Entities;

import java.io.Serializable;

/**
 * Created by 재민 on 2016-05-15.
 */
public class DeviceInfo implements Serializable {
    private String Name;
    private String address;

    public DeviceInfo(String name, String address) {
        Name = name;
        this.address = address;
    }

    public String getName() {
        return Name;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
