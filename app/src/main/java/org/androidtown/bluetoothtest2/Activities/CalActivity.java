package org.androidtown.bluetoothtest2.Activities;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.androidtown.bluetoothtest2.Entities.DeviceInfo;
import org.androidtown.bluetoothtest2.R;

public class CalActivity extends Activity implements SensorEventListener {
        private DeviceInfo targetDevice;




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
    private String sendData;

    private double xcount[] = new double[50];
    private double ycount[] = new double[50];

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;
    private boolean bool = false;
    /****sensor variable****/

    private Button calistart, calisend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);

        //센서등록
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerormeterSensor != null){
            sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_UI);
        }
        //센서등록 끝

        targetDevice = (DeviceInfo)this.getIntent().getExtras().get("targetDevice");

        calistart = (Button)findViewById(R.id.calistart);
        calisend = (Button)findViewById(R.id.tomouseact);

        calisend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(flag == 1){
                    Log.d("Calibrate data", "send");

                    xmean = mean(xcount);
                    ymean = mean(ycount);
                    xsd = standardDeviation(xcount, 1);
                    ysd = standardDeviation(ycount, 1);
                    sendData = "calidata" + "," + xsd + "," + ysd + "," + xmean + "," + ymean;

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("targetDevice", targetDevice);
                    Bundle bundle2 = new Bundle();
                    bundle.putSerializable("caliData", sendData);

                    Intent intent = new Intent(CalActivity.this,MouseActivity.class);
                    intent.putExtras(bundle);
                    intent.putExtras(bundle2);
                    startActivity(intent);
                    flag = 0;
                    // 뒤로가기 했을경우 안나오도록 없애주기 >> finish!!
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "먼저 calistart버튼을 눌러 calibration을 진행해주세요",  Toast.LENGTH_SHORT).show();
                }
            }
        });
        calistart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "7초 후에 CALISEND버튼을 눌러주세요",  Toast.LENGTH_SHORT).show();
                flag = 1;
            }
        });

        /*Handler handler = new Handler();
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
        }, 2000);*/
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100 & flag == 1) {
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
}
