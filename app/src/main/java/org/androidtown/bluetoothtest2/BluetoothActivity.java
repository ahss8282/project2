package org.androidtown.bluetoothtest2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothActivity extends Activity implements SensorEventListener {
    private BluetoothSocket btSocket;
    private BluetoothAdapter mBTAdapter=BluetoothAdapter.getDefaultAdapter();
    private Button searchBtn;
    private IntentFilter bluetoothFilter;
    private ArrayAdapter<String> mNewDeviceArrayAdapter;

    private BtAsyncTask btAsyncTask;
    private Button Rbtn;

    private final UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ArrayList<String> deviceList = new ArrayList<String>();

    private TextView SensorText;

    private final int REQUEST_ENABLE_BT = 1;
    BroadcastReceiver mReceiver;

    /****sensor variable****/
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;

    private static final int SHAKE_THRESHOLD = 800;
    private static final int DATA_X = 0;
    private static final int DATA_Y = 1;
    private static final int DATA_Z = 2;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;
    private TextView myText;
    private boolean bool = false;
    /****sensor variable****/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        searchBtn = (Button) findViewById(R.id.button3);

        IntentFilter bluetoothIntentFilter = new IntentFilter();
        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        SensorText = (TextView)findViewById(R.id.SensorText);
        Log.d("TAG","센서등록");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerormeterSensor != null){
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_UI);
            // 센서 매니저 등록
        }
        Log.d("TAG","센서등록완료");

        Rbtn = (Button)findViewById(R.id.Rbtn);

        Rbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btAsyncTask.sendCommand("rclick");
            }
        });
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {


                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){

                    BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    if(searchedDevice.getBondState()!=BluetoothDevice.BOND_BONDED){ //페어링되어있지 않으면

                        Log.d("TAG","페어링되어있지 않음");
                        String address=searchedDevice.getAddress();
                        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);
                        Log.d("NAME: ",device.getName());
                        Log.d("ADDRESS: ",device.getAddress());
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
        bool=true;
        // Connection successful, start the async task
        btAsyncTask = new BtAsyncTask(this, btSocket);
        btAsyncTask.execute();
        Log.d("MainActivity", "AsyncTask executed");


        Log.d("MainActivity", "Exit connectToDevice()");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
                //흔들림의 정도에따라 이벤트를 넣게됨
                if (speed > SHAKE_THRESHOLD) {

                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
                SensorText.setText(String.valueOf(event.values[0]) + "," + String.valueOf(event.values[1]) + "," + String.valueOf(event.values[2]));


                if(bool == true){
                    btAsyncTask.sendCommand(SensorText.getText().toString());
                }

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
