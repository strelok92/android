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
    public static final int RSP_SERVER_INIT_ERROR = -4;
    public static final int RSP_SERVER_CONNECT_ERROR = -3;
    public static final int RSP_SERVER_CMD_ERROR = -2;
    public static final int RSP_SERVER_CMD_NACK = -1;
    public static final int RSP_SERVER_CMD_DONE = 0;
    public static final int RSP_SERVER_CMD_ACK = 1;

    private JSch jsch;
    private ExecutorService executor;
    private Session session = null;
    String TAG = "TAG SSH EXPLORER";
    public SSHService() {}
    @Override
    public IBinder onBind(Intent intent) {return null;}

    private ChannelExec channel = null;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PendingIntent pi = intent.getParcelableExtra("response");

        // Create session if not created
        if (session == null) {
            String host;
            Integer port;
            String login, pass;

            host = intent.getStringExtra("host");
            port = intent.getIntExtra("port", 22);

            login = intent.getStringExtra("login");
            pass = intent.getStringExtra("pass");

            try {
                session = jsch.getSession(login, host, port);
                session.setPassword(pass);
                session.setUserInfo(new SSHUserInfo());
            }catch (Exception se){
                Log.e(TAG, se.toString());
                try{
                    pi.send(this, RSP_SERVER_INIT_ERROR, new Intent().putExtra("resp", "server init error"));
                }catch (Exception e){
                    Log.e(TAG, e.toString());
                }
                return super.onStartCommand(intent, flags, startId);
            }
        }

        // perform connect

        executor.execute(()->{
            // Open session
            try {
                if (session.isConnected() == false) {
                    Log.i(TAG, "Open session..");
                    Integer timeout = intent.getIntExtra("timeout", 30000);
                    session.connect(timeout);
                }
            }catch (Exception e){
                Log.e(TAG, e.toString());
                try{
                    pi.send(this, RSP_SERVER_CONNECT_ERROR, new Intent().putExtra("resp", "server connect error"));
                }catch (Exception pe){
                    Log.e(TAG, pe.toString());
                }
                return;
            }

            // Send command

            String cmd = intent.getStringExtra("cmd");
            if (cmd == null) return;

            Log.d(TAG, cmd);

            try {
                if (channel == null){
                    channel = (ChannelExec) session.openChannel("exec"); // "shell", "exec", "x11", "sftp"
                }

                final BufferedReader
                        readerInput = new BufferedReader(new InputStreamReader(channel.getInputStream())),
                        readerError = new BufferedReader(new InputStreamReader(channel.getErrStream()));

                String line;

                if (channel.isConnected()){
                    Log.i(TAG, "CONNECTED");
                }

                channel.setCommand(cmd);
                channel.connect();

                // read response
                while((line = readerInput.readLine())!= null) {
                    pi.send(this, RSP_SERVER_CMD_ACK, new Intent().putExtra("resp", line));
                }
                // read error
                while((line = readerError.readLine())!= null) {
                    pi.send(this, RSP_SERVER_CMD_NACK, new Intent().putExtra("resp", line));
                    Log.e(TAG,line );
                }
                pi.send(this, RSP_SERVER_CMD_DONE, null);

            }catch (Exception e){
                Log.e(TAG, e.toString());
                try{
                    pi.send(this, RSP_SERVER_CMD_ERROR, new Intent().putExtra("resp", "server command error"));
                }catch (Exception pe){
                    Log.e(TAG, pe.toString());
                }
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        jsch = new JSch();
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