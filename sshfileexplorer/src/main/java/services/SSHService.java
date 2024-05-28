package services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SSHService extends Service {
    private JSch jsch;
    private ExecutorService executor;
    private Session session = null;
    String TAG = "TAG SSH EXPLORER";
    public SSHService() {}
    @Override
    public IBinder onBind(Intent intent) {return null;}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PendingIntent pi = intent.getParcelableExtra("response");
        String host;Integer port,timeout;
        String login, pass;
        try {
            host = intent.getStringExtra("host");
            port = intent.getIntExtra("port", 22);
            timeout = intent.getIntExtra("timeout", 30000);

            login = intent.getStringExtra("login");
            pass = intent.getStringExtra("pass");

            if (session == null) {
                session = jsch.getSession(login, host, port);
                session.setPassword(pass);
                session.setUserInfo(new SSHUserInfo());
            }

        }catch (Exception e){
            try {
                pi.send(this, -2, new Intent().putExtra("resp", e.toString()));
            }catch (PendingIntent.CanceledException err){
                Log.e(TAG, err.toString());
            }
            return super.onStartCommand(intent, flags, startId);
        }

        executor.execute(()->{
            try {
                if (session.isConnected() == false) {
                    session.connect(timeout);
                }

                ChannelExec channel = (ChannelExec)session.openChannel("exec"); // "shell", "exec", "x11", "sftp"

                final BufferedReader
                        readerInput = new BufferedReader(new InputStreamReader(channel.getInputStream())),
                        readerError = new BufferedReader(new InputStreamReader(channel.getErrStream()));

                channel.setCommand(intent.getStringExtra("cmd"));
                channel.connect();

                String line;

                // read response
                while((line = readerInput.readLine())!= null) {
                    pi.send(this, 1, new Intent().putExtra("resp", line));
                }
                // read error
                while((line = readerError.readLine())!= null) {
                    pi.send(this, -1, new Intent().putExtra("resp", line));
                }
                pi.send(this, 0, null);
            }catch (Exception e){
                try {
                    pi.send(this, -2, new Intent().putExtra("resp", e.toString()));
                }catch (PendingIntent.CanceledException err){
                    Log.e(TAG, err.toString());
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        jsch=new JSch();
        executor = Executors.newSingleThreadExecutor();

        Log.i(TAG,"onCreate SSHService");
    }

    private class SSHUserInfo implements UserInfo {
        public SSHUserInfo(){}
        @Override
        public String getPassphrase() {return null;}
        @Override
        public String getPassword() {return null;}
        @Override
        public boolean promptPassword(String s) {return true;}
        @Override
        public boolean promptPassphrase(String s) {return true;}
        @Override
        public boolean promptYesNo(String s) {return true;}
        @Override
        public void showMessage(String s) {}
    }
}