package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.androidtown.bluetoothtest2.BtAsyncTask;
import org.androidtown.bluetoothtest2.Constants.BluetoothConst;
import org.androidtown.bluetoothtest2.Dialogs.SelectDeviceDialogListener;
import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;
import org.w3c.dom.Text;

import java.io.IOException;

public class MouseActivity extends Activity implements SensorEventListener {
    private BluetoothDevice bluetoothDevice;
    private BtAsyncTask btAsyncTask;
    private BluetoothSocket btSocket;
    private BluetoothAdapter btAdapter;
    private DeviceInfo targetDevice;

    private Button leftBtn;
    private Button rightBtn;

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
    private TextView sensorText;
    private boolean bool = false;
    /****sensor variable****/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse);

        //센서등록
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerormeterSensor != null){
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_UI);
        }
        //센서등록 끝

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //레이아웃 관련 설정
        sensorText = (TextView)findViewById(R.id.sensorText);
        leftBtn = (Button)findViewById(R.id.leftBtn);
        rightBtn = (Button)findViewById(R.id.rightBtn);

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MOUSE","LEFT");
                btAsyncTask.sendCommand("lclick");
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MOUSE","RIGHT");
                btAsyncTask.sendCommand("rclick");
            }
        });

        //레이아웃 관련 설정 끝

        targetDevice = (DeviceInfo)this.getIntent().getExtras().get("targetDevice");

        Log.d("MOUSE",targetDevice.getAddress());
        Log.d("MOUSE",targetDevice.getName());

        bluetoothDevice = btAdapter.getRemoteDevice(targetDevice.getAddress());
        connectToDevice(bluetoothDevice);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MOUSE ACTIVITY","DESTROY");

        sensorManager.unregisterListener(this);
        disconnectFromDevice();
    }


    private void connectToDevice(BluetoothDevice device) {

        Log.d("MOUSE ACTIVITY", "Enter connectToDevice()");

        // Create a connection to the device with the SPP UUID
        try {
            btSocket = device.createInsecureRfcommSocketToServiceRecord(BluetoothConst.SPP_UUID);
            Log.d("MOUSE ACTIVITY", "InsecureRfCommSocket created");
        } catch (IOException e) {
            Log.d("MOUSE ACTIVITY", "Unable to create InsecureRfCommSocket: " + e.getMessage());
            Toast.makeText(this, "Unable to open a serial socket with the device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Connect to the device
        try {
            btSocket.connect();
            Log.d("MOUSE ACTIVITY", "Socket connected");
        } catch (IOException e) {
            Log.d("MOUSE ACTIVITY", "Unable to connect the socket: " + e.getMessage());
            Toast.makeText(this, "Unable to connect to the device", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Connection successful, start the async task
        btAsyncTask = new BtAsyncTask(this, btSocket);
        btAsyncTask.execute();

        Log.d("MOUSE ACTIVITY", "AsyncTask executed");
        Log.d("MOUSE ACTIVITY", "Connection Success");
    }
    private void disconnectFromDevice() {

        Log.d("MOUSE ACTIVITY", "Enter disconnectFromDevice()");
        if(btAsyncTask!=null){
            btAsyncTask.cancel(true);
            btAsyncTask = null;

            // close the socket
            try {
                btSocket.close();
                Log.d("MOUSE ACTIVITY", "Socket closed");
            } catch (IOException e) {
                Log.d("MOUSE ACTIVITY", "Unable to close socket: " + e.getMessage());
                Toast.makeText(this, "Unable to disconnect from the device", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Log.d("MOUSE ACTIVITY", "AsyncTask stopped");
        sensorManager.unregisterListener(this);
        Log.d("MOUSE ACTIVITY", "Disconnection Success()");
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
                sensorText.setText(String.valueOf(event.values[0]) + "," + String.valueOf(event.values[1]) + "," + String.valueOf(event.values[2]));

                if(btAsyncTask!=null){
                    btAsyncTask.sendCommand(sensorText.getText().toString());
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
