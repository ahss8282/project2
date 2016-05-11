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

        mybtn=(Button)findViewById(R.id.mybtn); //버튼 조작을 위한 것
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //어댑터 초기화

        //버튼 클릭 시 동작설정 (onclick 안에만 보면 됨)
        mybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                //페어링 된 디바이스 목록 얻기

                //디바이스 목록을 다이어로그 창에 추가
                String[] pairedDeviceNames = new String[pairedDevices.size()];
                int i = 0;
                for(BluetoothDevice pairedDevice : pairedDevices) {
                    pairedDeviceNames[i] = pairedDevice.getName();
                    i++;
                }

                //다이얼로그 세팅 후 출력
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
