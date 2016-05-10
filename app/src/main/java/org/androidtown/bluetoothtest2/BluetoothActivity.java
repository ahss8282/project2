package org.androidtown.bluetoothtest2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothActivity extends Activity {
    private BluetoothSocket btSocket;
    private BluetoothAdapter mBTAdapter=BluetoothAdapter.getDefaultAdapter();
    private Button searchBtn;
    private IntentFilter bluetoothFilter;
    private ArrayAdapter<String> mNewDeviceArrayAdapter;

    private final UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ArrayList<String> deviceList = new ArrayList<String>();



    private final int REQUEST_ENABLE_BT = 1;
    BroadcastReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        searchBtn = (Button) findViewById(R.id.button3);

        IntentFilter bluetoothIntentFilter = new IntentFilter();
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TAG","씨발새끼얀");

                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    Log.d("TAG","개씨발새끼야");
                    BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if(searchedDevice.getBondState()!=BluetoothDevice.BOND_BONDED){ //페어링되어있지 않으면

                        Log.d("TAG","페어링되어있지 않음");
                        String address=searchedDevice.getAddress();
                        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                        Log.d("NAME: ",device.getName());
                        connectToDevice(device);
                    }
                }

            }
        };

        this.registerReceiver(mReceiver, bluetoothIntentFilter);
        Log.d("TAG","테스트");
    }

    public void searchOnclick(View view) {
        //step1: 블루투스 지원하는지 확인
//        if (mBTAdapter == null) {
//            return;
//        }
//
//        if (!mBTAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }

//        Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
//        if (pairedDevices.size() > 0) {
//            for (BluetoothDevice device : pairedDevices) {
//                Toast.makeText(this,device.getAddress(), Toast.LENGTH_SHORT).show();
//            }
//        }
        if (mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
        }
        //re-start discovery
        mBTAdapter.startDiscovery();

        Log.d("TAG", deviceList.size() + "");



        Toast.makeText(this, "ㅆ발", Toast.LENGTH_SHORT).show();

    }
    private void connectToDevice(BluetoothDevice device) {

        Log.d("MainActivity", "Enter connectToDevice()");


        // Create a connection to the device with the SPP UUID
        try {
            btSocket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
            Log.d("MainActivity", "InsecureRfCommSocket created");
        } catch (IOException e) {
            Log.d("MainActivity", "Unable to create InsecureRfCommSocket: " + e.getMessage());
            Toast.makeText(this, "Unable to open a serial socket with the device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Connect to the device
        try {
            btSocket.connect();
            Log.d("MainActivity", "Socket connected");
        } catch (IOException e) {
            Log.d("MainActivity", "Unable to connect the socket: " + e.getMessage());
            Toast.makeText(this, "Unable to connect to the device", Toast.LENGTH_SHORT).show();
            return;
        }



        Log.d("MainActivity", "Exit connectToDevice()");
    }
}
