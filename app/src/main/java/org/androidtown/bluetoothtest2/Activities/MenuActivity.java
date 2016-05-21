package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;

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
        mouseBtn.setBackgroundColor(Color.BLACK);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSelected) {
                    Intent intent = new Intent(MenuActivity.this, ConnectActivity.class);
                    startActivityForResult(intent, CONNECT_ACTIVITY);
                } else {
                    connectBtn.setText("기기선택");
                    connectBtn.setBackgroundColor(Color.parseColor(("#ef4444")));
                    mouseBtn.setBackgroundColor(Color.BLACK);
                    mouseBtn.setEnabled(false);
                    isSelected = false;
                }
            }
        });

        mouseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("targetDevice", targetDevice);

                Intent intent = new Intent(MenuActivity.this, CalActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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

                connectBtn.setText("기기선택 취소");
                connectBtn.setBackgroundColor(Color.BLUE);

                //선택한 기기가 이미 페어링되어 있으면
                BluetoothDevice tempBT = mBluetoothAdapter.getRemoteDevice(targetDevice.getAddress());
                if(tempBT.getBondState() == BluetoothDevice.BOND_BONDED){
                    mouseBtn.setEnabled(true);
                    mouseBtn.setBackgroundColor(Color.parseColor("#449def"));
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setMessage("정말 종료하시겠습니까?");

        d.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // process전체 종료
                moveTaskToBack(true);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        d.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        d.show();


    }


}
