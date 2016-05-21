package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.androidtown.bluetoothtest2.BtAsyncTask;
import org.androidtown.bluetoothtest2.Constants.BluetoothConst;
import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;

import java.io.IOException;

public class MouseActivity extends Activity implements SensorEventListener {
    private BluetoothDevice bluetoothDevice;
    private BtAsyncTask btAsyncTask;
    private BluetoothSocket btSocket;
    private BluetoothAdapter btAdapter;
    private DeviceInfo targetDevice;

    private Button leftBtn;
    private Button rightBtn;
    private Button upWheelBtn;
    private Button downWheelBtn;
    private Button calisend;

    /****sensor variable****/
    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;
    private double xsd, ysd;
    private double xmean, ymean;
    private int flag = 0;
    private int count = 0;

    private static final int SHAKE_THRESHOLD = 800;
    private static final int DATA_X = 0;
    private static final int DATA_Y = 1;
    private static final int DATA_Z = 2;
    private double xcount[] = new double[50];
    private double ycount[] = new double[50];

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
        upWheelBtn = (Button)findViewById(R.id.upBtn);
        downWheelBtn = (Button)findViewById(R.id.downBtn);;
        calisend = (Button)findViewById(R.id.calisend);
        leftBtn.setBackgroundColor(1);
        rightBtn.setBackgroundColor(1);
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

        calisend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("Calibrate data", "send");

                xmean = mean(xcount);
                ymean = mean(ycount);
                xsd = standardDeviation(xcount, 1);
                ysd = standardDeviation(ycount, 1);
                btAsyncTask.sendCommand("calidata" + "," + xsd + "," + ysd + "," + xmean + "," + ymean);// mean과 sd 보내기
                flag = 1;
                //count = 100;// flag 바꾸기
            }
        });

        upWheelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MOUSE", "UP");
                btAsyncTask.sendCommand("upwheel");
            }
        });
        downWheelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MOUSE", "down");
                btAsyncTask.sendCommand("downwheel");
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
                //z = event.values[2];

                if(count < 50){
                    xcount[count] = event.values[0];
                    ycount[count] = event.values[1];
                    count++;
                }
                else{
                    count = 100;
                }

                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
                //흔들림의 정도에따라 이벤트를 넣게됨
                if (speed > SHAKE_THRESHOLD) {

                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                //lastZ = event.values[DATA_Z];
                //sensorText.setText(String.valueOf(event.values[0]) + "," + String.valueOf(event.values[1]) + "," + String.valueOf(event.values[2]));
                sensorText.setText(String.valueOf(event.values[0]) + "," + String.valueOf(event.values[1]));
                if (btAsyncTask != null && flag == 1){
                    btAsyncTask.sendCommand(sensorText.getText().toString());
                }
            }
        }
    }

    public static double mean(double[] array) {  // 산술 평균 구하기
        double sum = 0.0;

        for (int i = 0; i < array.length; i++)
            sum += array[i];

        return sum / array.length;
    }


    public static double standardDeviation(double[] array, int option) {
        if (array.length < 2) return Double.NaN;

        double sum = 0.0;
        double sd = 0.0;
        double diff;
        double meanValue = mean(array);

        for (int i = 0; i < array.length; i++) {
            diff = array[i] - meanValue;
            sum += diff * diff;
        }
        sd = Math.sqrt(sum / (array.length - option));

        return sd;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
