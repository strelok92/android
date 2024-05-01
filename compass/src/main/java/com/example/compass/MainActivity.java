package com.example.compass;

import static android.app.PendingIntent.getActivity;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
                    // Vibration for calib done indication
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(200, -1));
                    }else{
                        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(200);
                    }
                    Toast.makeText(getApplicationContext(), "Calib done", Toast.LENGTH_SHORT).show();
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


//        DrawView draw = new DrawView(getApplicationContext());
//        DrawView iAzimuth = findViewById(R.id.vAzimuth);
//
//        iAzimuth.setBackgroundColor(0xFFFF0000);


//        CompassListener listener = new CompassListener();

        SensorManager manager = (SensorManager)getSystemService(SENSOR_SERVICE);
//        compass = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        manager.registerListener(new CompassListener(),
                manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
//                manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//                manager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR),
//                TYPE_GEOMAGNETIC_ROTATION_VECTOR
                SensorManager.SENSOR_DELAY_NORMAL);


//        TextView tCompass = findViewById(R.id.tCompass);
//        tCompass.setText("<-");
//        tCompass.setRotation(90.1f);

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
//            TextView tCompass;
            TextView tLevel;
            tLevel = findViewById(R.id.tLevel);

            ImageView iCompass = findViewById(R.id.iCompass);
//            tCompass = findViewById(R.id.tCompass);

            float[] rotV = new float[16];
            float[] orientV = new float[3];

            SensorManager.getRotationMatrixFromVector(rotV, event.values);
            SensorManager.getOrientation(rotV, orientV);


            float mod = event.values[0]*event.values[0] + event.values[1]*event.values[1];
            mod = (float)sqrt((double)mod);
//            tLevel.setText(String.format("%.2f %.2f ",event.values[0]/mod, event.values[1]/mod));
            tLevel.setText(String.format("%.2f %.2f %.2f",orientV[0], orientV[1], orientV[2]));


//            tCompass.setRotation(-(float)(orientV[0]*180.f/PI));
            iCompass.setRotation(-(float)(orientV[0]*180.f/PI));
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    class DrawView extends View{

        public DrawView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            canvas.drawColor(0xFF0000FF);
        }
    }
}