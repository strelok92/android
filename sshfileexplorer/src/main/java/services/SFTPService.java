package services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.jcraft.jsch.JSch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SFTPService extends Service {
    private JSch jsch;
    private ExecutorService executor;
    public SFTPService() {
    }
    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        jsch = new JSch();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}