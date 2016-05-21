package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
    private BroadcastReceiver mPairReceiver;

    private Boolean isSelected = false;

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog asyncDialog = new ProgressDialog(MenuActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("페어링 중..");

            mPairReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                        final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                        final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                        if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                            mouseBtn.setEnabled(true);

                        } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                            mouseBtn.setEnabled(false);
                        }
                    }
                }
            };
            IntentFilter paringFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            MenuActivity.this.registerReceiver(mPairReceiver, paringFilter);

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        connectBtn=(Button)findViewById(R.id.connectBtn); //버튼 조작을 위한 것
        mouseBtn=(Button)findViewById(R.id.mouseBtn);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSelected) {
                    Intent intent = new Intent(MenuActivity.this, ConnectActivity.class);
                    startActivityForResult(intent, CONNECT_ACTIVITY);
                }
                else{
                    connectBtn.setText("기기선택");
                    mouseBtn.setEnabled(false);
                    isSelected=false;
                }
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


//        mPairReceiver = new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//
//                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
//                    final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
//                    final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
//
//                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
//                        mouseBtn.setEnabled(true);
//
//                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
//                        mouseBtn.setEnabled(false);
//                    }
//
//                }
//            }
//        };
//        IntentFilter paringFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //어댑터 초기화
//        this.registerReceiver(mPairReceiver, paringFilter);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mPairReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // check if the result comes from the request to enable bluetooth
        if (requestCode == CONNECT_ACTIVITY)
            if (resultCode == RESULT_OK) {
                targetDevice = (DeviceInfo)intent.getExtras().get("deviceInfo");

                connectBtn.setText("연결해제");

                //선택한 기기가 이미 페어링되어 있으면
                BluetoothDevice tempBT = mBluetoothAdapter.getRemoteDevice(targetDevice.getAddress());
                if(tempBT.getBondState() == BluetoothDevice.BOND_BONDED){
                    mouseBtn.setEnabled(true);
                    isSelected = true;
                }
                else{
                    CheckTypesTask task = new CheckTypesTask();
                    task.execute();
                    isSelected = true;
                }


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
