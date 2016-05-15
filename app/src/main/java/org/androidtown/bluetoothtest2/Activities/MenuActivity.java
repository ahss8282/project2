package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.androidtown.bluetoothtest2.Constants.BluetoothConst;
import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;

import java.io.IOException;

public class MenuActivity extends Activity{
    private BluetoothSocket btSocket; //블루투스 소켓

    private Button mouseBtn;
    private Button connectBtn;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private DeviceInfo targetDevice;

    private static final int CONNECT_ACTIVITY = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        connectBtn=(Button)findViewById(R.id.connectBtn); //버튼 조작을 위한 것
        mouseBtn=(Button)findViewById(R.id.mouseBtn);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this,ConnectActivity.class);
                startActivityForResult(intent,CONNECT_ACTIVITY);
            }
        });

        mouseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("targetDevice",targetDevice);

                Intent intent = new Intent(MenuActivity.this,MouseActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //어댑터 초기화
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // check if the result comes from the request to enable bluetooth
        if (requestCode == CONNECT_ACTIVITY)
            if (resultCode == RESULT_OK) {
                mouseBtn.setEnabled(true);
                targetDevice = (DeviceInfo)intent.getExtras().get("deviceInfo");
            }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void connectToDevice(BluetoothDevice device) {

        Log.d("MainActivity", "Enter connectToDevice()");

        // Create a connection to the device with the SPP UUID
        try {
            btSocket = device.createInsecureRfcommSocketToServiceRecord(BluetoothConst.SPP_UUID);
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



        Log.d("MainActivity", "AsyncTask executed");
        Log.d("MainActivity", "Exit connectToDevice()");
    }





//    @Override
//    public void onChoosingPairedDevice(String deviceName) {
//        Log.d("DEVICE INFO: ",deviceName);
//    }
}
