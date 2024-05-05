package com.example.compass;

import static android.app.PendingIntent.getActivity;
import static java.lang.Math.PI;
import static java.lang.Math.asin;
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
        manager.registerListener(new CompassListener(),
                manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(new CompassListener(),
                manager.getDefaultSensor(Sensor.TYPE_GRAVITY),
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


    private double[] gNorm = {0,0,0};
    private double[] mNorm = {0,0,0};
    private double mgLen[] = {0,0};


    private class CompassListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            onCalibProcess(event.sensor, event.values);
            ImageView iCompass = findViewById(R.id.iCompass);
            //            iCompass.setRotation(-(float)(Math.asin(vals[0])*180.f/PI));
            TextView tLevel, tMagnet, tGravity;

            tLevel = findViewById(R.id.tLevel);
            tMagnet = findViewById(R.id.tMagnet);
            tGravity = findViewById(R.id.tGravity);

            double len = event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2];
            len = sqrt(len);

            if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
                gNorm[0] = event.values[0]/len;
                gNorm[1] = event.values[1]/len;
                gNorm[2] = event.values[2]/len;
                mgLen[1] = len;
                tGravity.setText(String.format("G: %.2f %.2f %.2f",gNorm[0],gNorm[1],gNorm[2]));
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mNorm[0] = event.values[0]/len;
                mNorm[1] = event.values[1]/len;
                mNorm[2] = event.values[2]/len;
                mgLen[0] = len;
                tMagnet.setText(String.format("M: %.2f %.2f %.2f",mNorm[0],mNorm[1],mNorm[2]));
            }

            // todo UPDATE level
            double gxRoot = Math.sqrt(1-gNorm[0]*gNorm[0]);
            double gyRoot = Math.sqrt(1-gNorm[1]*gNorm[1]);
            double gx = gNorm[0];
            double gy = gNorm[1];
            double mx = mNorm[0];
            double my = mNorm[1];
            double mz = mNorm[2];

            double cos_a = gNorm[0]*mNorm[0] + gNorm[1]*mNorm[1] + gNorm[2]*mNorm[2];
            double m[] = {
                    (mx*gxRoot + gx*mz),
                    (-mx*gx*gy + my*gyRoot + mz*gy*gxRoot),
                    (-mx*gx*gyRoot - my*gy + mz*gxRoot*gyRoot)
            };
            // magnetic deviation
            tLevel.setText(String.format("%.2f %.2f %.2f",
                    Math.toDegrees(Math.acos(cos_a)),
                    mNorm[1] + Math.sin(Math.acos(cos_a) - PI/2 - Math.asin(gNorm[1])),
                    Math.sin(Math.acos(cos_a) - PI/2)
            ));
//            tLevel.setText(String.format("%.2f %.2f %.2f",
//                    m[0],
//                    m[1],
//                    m[2]
//            ));

            iCompass.setRotation((float)-Math.toDegrees(Math.asin(m[1])));

//            // gravity deviation
//            tLevel.setText(String.format("%.2f %.2f",
//                    Math.toDegrees(Math.asin(gNorm[0])),
//                    Math.toDegrees(Math.asin(gNorm[1]))
//            ));
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }
}