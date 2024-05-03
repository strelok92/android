package com.example.compass;

import static android.app.PendingIntent.getActivity;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Sensor compass;
    private MainActivityDialog dialogClear;
    private MainActivityDialog dialogCalib;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init dialogs
        dialogClear = new MainActivityDialog(MainActivityDialog.TYPE_CLEAR);
        dialogCalib = new MainActivityDialog(MainActivityDialog.TYPE_CALIB);

        dialogClear.setOnDialogListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE){
                    // TODO: add code for clear both azimuths from file system

                }
            }
        });
        dialogCalib.setOnDialogListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE){
                    // TODO: add code for running calibration process
                    onCalibStart();
                }
            }
        });

        // Init buttons
        Button btn;
        btn = findViewById(R.id.bClear);
        btn.setOnClickListener(this);
        btn = findViewById(R.id.bAlarm);
        btn.setOnClickListener(this);
        btn = findViewById(R.id.bCalib);
        btn.setOnClickListener(this);
        btn = findViewById(R.id.bAzimuth);
        btn.setOnClickListener(this);


        SensorManager manager = (SensorManager)getSystemService(SENSOR_SERVICE);
//        compass = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        manager.registerListener(new CompassListener(),
//                manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private final int CALIB_CNT = 500;
    private int calibCntr = CALIB_CNT+1;
    private XmlSerializer xml=null;
    private FileOutputStream fs=null;
    private void onCalibStart(){
        if (xml == null){
            xml = Xml.newSerializer();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    fs = new FileOutputStream(new File(getApplicationInfo().dataDir + File.separator+ "files" +File.separator + "magnetometer.xml"));
                }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                xml = null;
                return;
            }
        }
        try {
            xml.setOutput(fs, "UTF-8");
            xml.startDocument("UTF-8", true);
            xml.startTag("", "magnetometer");
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            xml = null;
            return;
        }
        calibCntr = 0;
        Log.i("calib", "start");
    }
    private void onCalibProcess(Sensor sensor, float[] values){
        if (calibCntr < CALIB_CNT) {
            calibCntr++;
            try {
                xml.startTag("", "sensor");
                xml.attribute("", "x", String.format("%.3f", values[0]).replace(',', '.'));
                xml.attribute("", "y", String.format("%.3f", values[1]).replace(',', '.'));
                xml.attribute("", "z", String.format("%.3f", values[2]).replace(',', '.'));
                xml.endTag("","sensor");
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
            }

//            TextView tLevel;
//            tLevel = findViewById(R.id.tLevel);
//            tLevel.setText(String.format("%d", calibCntr));
        }

        if (calibCntr == CALIB_CNT) {
            calibCntr++;
            onCalibEnd();
        }
    }
    private void onCalibEnd()  {
        try {
            xml.endTag("", "magnetometer");
            xml.endDocument();
            xml.flush();
            Log.i("calib", "calib done");
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        // Vibration for calib done indication
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(200, -1));
        }else{
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
        }
        Toast.makeText(getApplicationContext(), "Calib done", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bClear){
            dialogClear.show(getSupportFragmentManager(), "");
        } else if (v.getId() == R.id.bAlarm){
            // TODO: add code for save alarm azimuth on file system
        } else if (v.getId() == R.id.bAzimuth){
            // TODO: add code for save current azimuth on file system
        } else if (v.getId() == R.id.bCalib){
            dialogCalib.show(getSupportFragmentManager(), "");
        }
    }



    private class CompassListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            onCalibProcess(event.sensor, event.values);
//            TextView tCompass;
            TextView tLevel;
            tLevel = findViewById(R.id.tLevel);

            ImageView iCompass = findViewById(R.id.iCompass);
//            tCompass = findViewById(R.id.tCompass);

            float[] rotV = new float[16];
            float[] orientV = new float[3];

            SensorManager.getRotationMatrixFromVector(rotV, event.values);
            SensorManager.getOrientation(rotV, orientV);

            float[] vals = {event.values[0],event.values[1], event.values[2]};
//            float mod = event.values[0]*event.values[0] + event.values[1]*event.values[1];
            double len = vals[0]*vals[0]+vals[1]*vals[1]+vals[2]*vals[2];
            len = sqrt(len);
            vals[0]/=len;
            vals[1]/=len;
            vals[2]/=len;
            tLevel.setText(String.format("%.2f %.2f %.2f",vals[0], vals[1],vals[2]));

            iCompass.setRotation(-(float)(Math.asin(vals[0])*180.f/PI));
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }
}