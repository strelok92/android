package com.example.sshfileexplorer.ui.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FileExplorerActivity extends AppCompatActivity {
    private final String SH_TYPE_SESSION = "session";
    private final String SH_TYPE_SHELL = "shell";
    private final String SH_TYPE_EXEC = "exec";
    private final String SH_TYPE_X11 = "x11";
    private final String SH_TYPE_SFTP = "sftp";
    private final String SH_TYPE_DIRECT_TCPIP = "direct-tcpip";
    private final String SH_TYPE_FORWARDED_TCPIP = "forwarded-tcpip";
    String TAG = "TAG SSH EXPLORER";

    private class SSHUserInfo implements UserInfo {
        @Override
        public String getPassphrase() {
            Log.d(TAG,String.format("getPassphrase"));

            String pass = "1234";
            return pass;
        }

        @Override
        public String getPassword() {
            Log.d(TAG,String.format("getPassword"));
            String pass = "1234";
            return pass;
        }

        @Override
        public boolean promptPassword(String s) {
            Log.d(TAG,String.format("promptPassword '%s'",s));
            return true;
        }

        @Override
        public boolean promptPassphrase(String s) {
            Log.d(TAG,String.format("promptPassphrase '%s'",s));
            return false;
        }
        @Override
        public boolean promptYesNo(String s) {
            Log.d(TAG,String.format("promptYesNo '%s'",s));
//                            return false;
            return true;
        }

        @Override
        public void showMessage(String s) {
            Log.d(TAG,String.format("showMessage '%s'", s));

        }
    }

    private void ssh(){
                        // SSH connect
                try{
                    JSch jsch=new JSch();
                    String user = "user";
                    String host = "192.168.135.134";
                    String pass = "1234";
                    Session session = jsch.getSession(user, host, 22);
                    session.setPassword(pass);
                    session.setUserInfo(new SSHUserInfo());

                    session.connect(1000);


                    String command = "ls -d";


                    ChannelExec channel = (ChannelExec)session.openChannel(SH_TYPE_EXEC);


//                    channel.setCommand(command);
//                    channel.setInputStream(null);
//
                    final InputStream
                         in = channel.getInputStream(),
                            err = channel.getErrStream();
//
//

                    final BufferedReader
                            readerIn = new BufferedReader(new InputStreamReader(in)),
                            readerEr = new BufferedReader(new InputStreamReader(err));

                    channel.setCommand("ls -l");
                    channel.connect();

                    // > drwxr-xr-x 2 user user 4096 May 15 20:34 Desktop
                    // > -rw-r--r-- 1 user user    5 May 17 19:17 tmp.txt
                    // PARSE:
                    //       d         rwx   r-x   r-x    2    user  user 4096  May 15 20:34  Desktop
                    // (d)dir/(-)file ouner group other links ouner group size    date/time     name


                    String line;
                    while ((line = readerIn.readLine()) != null) {
                        Log.d(TAG,String.format("%s",line));
                    }
                    while ((line = readerEr.readLine()) != null) {
                        Log.e(TAG,String.format("%s",line));
                    }

                    Log.i(TAG,"SSH done");
                }catch (Exception e){
                    Log.e(TAG, e.toString());
                }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_file_explorer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String names[] = {"..", "home", "readme.md"};
        // todo for test only!
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

       ListView list = findViewById(R.id.filesList);
        list.setAdapter(adapter);


        // run ssh

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> ssh());

    }
}

