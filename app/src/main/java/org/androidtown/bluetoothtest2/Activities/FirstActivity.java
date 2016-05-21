package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import org.androidtown.bluetoothtest2.R;

public class FirstActivity extends Activity {
    private BluetoothAdapter mBTAdapter=BluetoothAdapter.getDefaultAdapter();
    private final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (mBTAdapter == null) {
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

    }
    public void onStart() {
        super.onStart();

        if (!mBTAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            Intent intent = new Intent(FirstActivity.this, MenuActivity.class);
            startActivity(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode != Activity.RESULT_OK){
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
                else{
                    Intent intent = new Intent(FirstActivity.this, MenuActivity.class);
                    startActivity(intent);
                }
        }
    }
}