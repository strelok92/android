package com.example.sshfileexplorer.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
//                    String host = "192.168.135.134";
                    String host = "192.168.1.51";
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

                    channel.setCommand("ls -l --time-style=long-iso");
//                    channel.connect();

                    // > drwxr-xr-x 2 user user 4096 May 15 20:34 Desktop
                    // > -rw-r--r-- 1 user user    5 2024-05-23 20:17 tmp.txt
                    // PARSE:
                    //       d         rwx   r-x   r-x    2    user  user 4096  2024-05-23 20:34  Desktop
                    // (d)dir/(-)file owner group other links owner group size    date/time        name

                    class FileProp{
                        public final int TYPE_DIR = 0;
                        public final int TYPE_FILE = 1;
                        private int type;
                        private int size;
                        private String date, time, owner;

                        FileProp(@NonNull String ld) throws Exception {
                            // Prepare data array
                            ArrayList<String> list = new ArrayList(Arrays.asList(ld.split(" ")));
                            for (Iterator<String> it = list.iterator();it.hasNext();){
                                String next = it.next();
                                if (next.equals("")) it.remove();
                            }

                            // Parse data array

                            // Type
                            char fType = list.get(0).charAt(0);
                            if (fType == 'd'){
                                type = TYPE_DIR;
                            }else if (fType == '-'){
                                type = TYPE_FILE;
                            }else{
                                throw new Exception("ld parse error!");
                            }

                            // Permissions
                            String permission = list.get(0);
                            // ouner
                            permission.charAt();
                            // group

                            // other

                            // Size
                            size = Integer.parseInt(list.get(4));

                            // Date/Time
                            date = list.get(5);
                            time = list.get(6);
                            owner = list.get(7);

                        }

                        public int getType(){return type;}
                        public int getSize(){return size;}
                        public String getOwner() {return owner;}
                        public String getDate() {return date;}
                        public String getTime() {return time;}
                        public String getDateTime() {return date+" " +time;}


                        //  todo add getPermissions                  file.getOwner(); // String []

                    }

//                    try {
                        FileProp file = new FileProp("-rw-r--r-- 1 user user    5 2024-05-23 20:17 tmp.txt");

                        int type = file.getType();
                        int size = file.getSize();
                        String name = file.getOwner();
                        String date = file.getDate();
                        String time = file.getTime();
                        String datetime = file.getDateTime();


                    Log.w(TAG,String.format("type %d size %d", type, size));
                        Log.w(TAG,name);
                    Log.w(TAG,date);
                    Log.w(TAG,time);
                    Log.w(TAG,datetime);




//                    }catch (Exception e){
//                        Log.e(TAG, e.toString());
//                    }

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

