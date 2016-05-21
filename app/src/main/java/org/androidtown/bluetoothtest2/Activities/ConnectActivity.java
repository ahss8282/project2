package org.androidtown.bluetoothtest2.Activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ListView;

import org.androidtown.bluetoothtest2.Adapters.BluetoothListAdapter;
import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;

import java.util.ArrayList;
import java.util.Set;

public class ConnectActivity extends Activity {
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver mReceiver;
    private IntentFilter bluetoothIntentFilter = new IntentFilter();

    private BluetoothDevice bluetoothDevice;
    private DeviceInfo deviceInfo;

    private ArrayList<BluetoothDevice> deviceList;
    private BluetoothListAdapter adapter;
    private ListView pairedListView;
    private ListView searchedListView;

    private Bundle extras = new Bundle();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        deviceList = new ArrayList<BluetoothDevice>();

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        searchedListView = (ListView) findViewById(R.id.listview1);

        bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice searchedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(searchedDevice.getBondState()!=BluetoothDevice.BOND_BONDED){ //페어링되어있지 않으면
                        Log.d("CONNECT","기기찾음");
                        String address=searchedDevice.getAddress();
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

                        deviceList.add(device);
                        adapter.notifyDataSetChanged(); //리스트뷰 갱신
                    }
                }
            }
        };


        this.registerReceiver(mReceiver, bluetoothIntentFilter);

        int hasPermission = ActivityCompat.checkSelfPermission(ConnectActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        Log.d("TEXT",hasPermission+"");


        hasPermission = ActivityCompat.checkSelfPermission(ConnectActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        Log.d("TEXT",hasPermission+"");


        ActivityCompat.requestPermissions(ConnectActivity.this,
                new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION },
                0);

        ActivityCompat.requestPermissions(ConnectActivity.this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION },
                0);

        ActivityCompat.requestPermissions(ConnectActivity.this,
                new String[]{
                        android.Manifest.permission.BLUETOOTH_ADMIN },
                0);

        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        //re-start discovery
        bluetoothAdapter.startDiscovery();
        setDeviceList();
        setList();
    }


    public void setList() {
        Log.d("connect", "리스트뷰 시작");
        adapter = new BluetoothListAdapter(this, deviceList);
        searchedListView.setAdapter(adapter);

//        searchedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (bluetoothAdapter.isDiscovering()){
//                    bluetoothAdapter.cancelDiscovery();
//                }
//                bluetoothDevice = (BluetoothDevice)adapter.getItem(position);
//
//                Log.d("CONNECT","선택된 기기: "+bluetoothDevice.getName()+","+bluetoothDevice.getAddress());
//
//
//
//
//            }
//        });

    }

    private void setDeviceList(){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        String[] pairedDeviceNames = new String[pairedDevices.size()];
        int i = 0;
        for(BluetoothDevice pairedDevice : pairedDevices) {
            deviceList.add(pairedDevice);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }
}
