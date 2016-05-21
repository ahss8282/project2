package org.androidtown.bluetoothtest2.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class BluetoothListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Activity mActivity;
    private ArrayList<BluetoothDevice> devices;




    public BluetoothListAdapter(Activity activity, ArrayList devices){
        this.mActivity = activity;
        this.devices = devices;

        mInflater =(LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView ==null){
            int res = 0;
            res = R.layout.activity_bluetooth_list_adapter;
            convertView = mInflater.inflate(res,parent,false);
        }

        TextView name = (TextView)convertView.findViewById(R.id.vi_title);
        TextView address = (TextView)convertView.findViewById(R.id.vi_content);
        TextView isPaired =(TextView)convertView.findViewById(R.id.vi_paired);

        LinearLayout layout_view =  (LinearLayout)convertView.findViewById(R.id.vi_view);

        name.setText(devices.get(position).getName());
        address.setText(devices.get(position).getAddress());
        isPaired.setText(devices.get(position).getBondState()+"");

        layout_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TEXT",devices.get(position).getAddress());

                BluetoothDevice bluetoothDevice = (BluetoothDevice)devices.get(position);
                DeviceInfo info = new DeviceInfo(bluetoothDevice.getName(),bluetoothDevice.getAddress(),bluetoothDevice.getBondState());

                //페어링안되어있으면 페어링을 시도한다
                if(bluetoothDevice.getBondState() != bluetoothDevice.BOND_BONDED)
                try {
                    Method method = bluetoothDevice.getClass().getMethod("createBond", (Class[]) null);
                    method.invoke(bluetoothDevice, (Object[]) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bundle extras = new Bundle();

                extras.putSerializable("deviceInfo",info);
                Intent intent =new Intent();
                intent.putExtras(extras);

                mActivity.setResult(mActivity.RESULT_OK,intent);
                mActivity.finish();
            }
        });


        return convertView;
    }
}
