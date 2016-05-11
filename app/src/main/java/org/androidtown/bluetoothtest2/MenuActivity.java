package org.androidtown.bluetoothtest2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Set;

public class MenuActivity extends Activity implements SelectDeviceDialogListener{
    private Button mybtn;

    private BluetoothAdapter mBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mybtn=(Button)findViewById(R.id.mybtn);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //어댑터 초기화


        mybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                String[] pairedDeviceNames = new String[pairedDevices.size()];
                int i = 0;
                for(BluetoothDevice pairedDevice : pairedDevices) {
                    pairedDeviceNames[i] = pairedDevice.getName();
                    i++;
                }

                SelectDeviceDialog selectDeviceDialog = SelectDeviceDialog.newInstance(pairedDeviceNames);
                selectDeviceDialog.show(getFragmentManager(), "selectDeviceDialog");
            }
        });
    }

    @Override
    public void onChoosingPairedDevice(String deviceName) {
        Log.d("DEVICE INFO: ",deviceName);
    }
}
