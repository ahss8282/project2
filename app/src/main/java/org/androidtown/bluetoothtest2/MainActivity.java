package org.androidtown.bluetoothtest2;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity implements SelectDeviceDialogListener, SensorEventListener {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket btSocket;
    private BtAsyncTask btAsyncTask;

    private boolean connected;

    private Button bt1;
    private Button bt2;

    private Button ssbutton;
    private Button searchBtn;
    private Menu actionsMenu;

    private final UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final int REQUEST_ENABLE_BT = 1;

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

    /**
     * APP INITIALIZATION
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

        // get the bluetooth adapter - bluetooth adapter 초기화
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // check if the device has bluetooth capabilities
        // if not, display a toast message and close the app
        if (mBluetoothAdapter == null) {

            Toast.makeText(this, "This app requires a bluetooth capable phone",
                    Toast.LENGTH_SHORT).show();
            finish();
        }


    }




    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        // check if bluetooth is enabled
        // if not, ask the user to enable it using an Intent
        if (!mBluetoothAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerormeterSensor != null){
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_UI);
            // 센서 매니저 등록
        }

        // init variables and GUI controls
        connected = false;
        bt1 = (Button)findViewById(R.id.bt1);
        bt2 = (Button)findViewById(R.id.bt2);
        myText = (TextView) findViewById(R.id.myText);
        ssbutton = (Button)findViewById(R.id.ssbutton);

        bt1.setEnabled(false);
        bt2.setEnabled(false);
        ssbutton.setEnabled(false);

        searchBtn = (Button)findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,BluetoothActivity.class);
                startActivity(intent);
            }
        });
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Save the menu variable
        actionsMenu = menu;

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        return super.onCreateOptionsMenu(menu);
    }


    /**
     * INTENTS
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // check if the result comes from the request to enable bluetooth
        if (requestCode == REQUEST_ENABLE_BT)

            // the request was not successful? display a toast message and close
            // the app
            if (resultCode != RESULT_OK) {
                Toast.makeText(this, "This app requires bluetooth",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**** sensorevents implement func************/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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
                myText.setText(String.valueOf(event.values[0]) + "," + String.valueOf(event.values[1]) + "," + String.valueOf(event.values[2]));
                if(bool == true){
                    btAsyncTask.sendCommand(myText.getText().toString());
                }

            }

        }
    }
    /***************imp func end*****************/

    /*******************mouse touch event ****************/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //터치 이벤트는 그 뭐냐 버튼같이 추가적인 부품들이 없는곳에 대한 터치만 작용되는듯 하다.
        final int action = event.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN: // 버튼에서는 작동없이 계산을 통해 이벤트를 발생시킬까?
                // 처음 터치가 눌러졌을 때
                //x = event.getX();
                //y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // 터치가 눌린 상태에서 움직일 때
                if(bool == true){
                    btAsyncTask.sendCommand("Dragged"); // left button long clicked + 변수 추가해서 연결전 전송하는 일이 없도록 하기
                }

                //x = event.getX();
                //y = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                // 터치가 떼어졌을 때
                //x = event.getX();
                //y = event.getY();
                break;
            default :
                break;
        }
        /*********************multi touch******************************/
/*
        switch(action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // 처음 터치가 눌러졌을 때
                break;
            case MotionEvent.ACTION_MOVE:
                // 터치가 눌린 상태에서 움직일 때
                break;
            case MotionEvent.ACTION_UP:
                // 터치가 떼어졌을 때
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 터치가 두 개 이상일 때 눌러졌을 때
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                //x = getX(pointerIndex);
                //y = getY(pointerIndex);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 터치가 두 개 이상일 때 떼어졌을 때
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                break;
            default :
                break;
        }
*/
        /*********************multi touch end *************************/

        return true;
    }

    /*******************mouse touch event ****************/


    /**
     * GUI EVENTS
     */

    // Respond to click on BtOnOff button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_btonoff:

                // If we're not connected, create and show the dialog with the paired devices
                if(!connected) {

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

                // if we're connected, disconnect
                else {
                    disconnectFromDevice();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /************click ssbutton****************/
    //음 그 연결이 되기전에 전송하는걸 방지하기 위한 bool 변수를 스위칭 하는 함수
    public void onssButtonClicked(View v){
        if(bool == false){
            bool = true;
            //break;
        }
        else{
            bool = false;
            //break;
        }
        //btAsyncTask.sendCommand("lclick");
    }

    public void onButton2Clicked(View v){
        btAsyncTask.sendCommand("lpress");
    }
    public void onButton3Clicked(View v){
        btAsyncTask.sendCommand("lrelease");
    }
    /************end***************************/

    // Click on BT1 or BT2
    public void btClick(View view) {

        switch (view.getId()) {

            case R.id.bt1:
                //bool = true;
                btAsyncTask.sendCommand("lclick");
                break;

            case R.id.bt2:
                //bool = false;
                btAsyncTask.sendCommand("rclick");
                break;
        }
    }

    @Override
    public void onChoosingPairedDevice(String deviceName) {

        connectToDevice(deviceName);
    }


    /**
     * LOGIC
     */

    // Bluetooth connection
    private void connectToDevice(String deviceName) {

        Log.d("MainActivity", "Enter connectToDevice()");

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;
        for(BluetoothDevice pairedDevice : pairedDevices)
            if(pairedDevice.getName().equals(deviceName)) {
                targetDevice = pairedDevice;
                break;
            }

        // If the device was not found, toast an error and return
        if(targetDevice == null) {
            Log.d("MainActivity", "No device found with name " + deviceName);
            Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a connection to the device with the SPP UUID
        try {
            btSocket = targetDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
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

        // Connection successful, start the async task
        btAsyncTask = new BtAsyncTask(this, btSocket);
        btAsyncTask.execute();
        Log.d("MainActivity", "AsyncTask executed");

        // update GUI
        connected = true;
        bt1.setEnabled(true);
        bt2.setEnabled(true);
        ssbutton.setEnabled(true);
        actionsMenu.findItem(R.id.action_btonoff).setIcon(R.drawable.button_off);
        Log.d("MainActivity", "GUI updated");

        Log.d("MainActivity", "Exit connectToDevice()");
    }

    private void disconnectFromDevice() {

        Log.d("MainActivity", "Enter disconnectFromDevice()");

        // stop the async task
        btAsyncTask.cancel(true);
        Log.d("MainActivity", "AsyncTask stopped");

        /******리스너 해제*****/
        if (sensorManager != null)
            sensorManager.unregisterListener(this);

        // close the socket
        try {
            btSocket.close();
            Log.d("MainActivity", "Socket closed");
        } catch (IOException e) {
            Log.d("MainActivity", "Unable to close socket: " + e.getMessage());
            Toast.makeText(this, "Unable to disconnect from the device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disconnection successful, update GUI
        connected = false;
        bt1.setEnabled(false);
        bt2.setEnabled(false);
        ssbutton.setEnabled(false);
        actionsMenu.findItem(R.id.action_btonoff).setIcon(R.drawable.button_on);
        ((TextView)findViewById(R.id.tvResponse)).setText("");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerormeterSensor != null){
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_UI);
            // 센서 매니저 등록
        }
        Log.d("MainActivity", "GUI updated");

        Log.d("MainActivity", "Exit disconnectFromDevice()");
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }
    }
}