package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;

public class CalActivity extends Activity {
        private DeviceInfo targetDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        targetDevice = (DeviceInfo)this.getIntent().getExtras().get("targetDevice");



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //Calibrate 작업 들어갈곳

                //Calibrate 끝

                Bundle bundle = new Bundle();
                bundle.putSerializable("targetDevice", targetDevice);

                Intent intent = new Intent(CalActivity.this,MouseActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                // 뒤로가기 했을경우 안나오도록 없애주기 >> finish!!
                finish();
            }
        }, 2000);
    }
}
