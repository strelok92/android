package services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.os.IBinder;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UserInfo;

import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SFTPService extends Service {
    public static final int RSP_CMD_ERR = -2;
    public static final int RSP_CONN_ERR = -1;
    public static final int RSP_OK = 0;

    public static final int STATE_DONE = 0;
    public static final int STATE_CANCEL = 1;
    public static final int STATE_LOAD = 2;

    public static final String ls_separator = "/";

    private final String TAG = "TAG SSH EXPLORER";

    private JSch jsch;
    private ExecutorService executor;

    private Session session = null;
    private ChannelSftp sftp = null;

    public SFTPService() {}
    @Override public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
        jsch = new JSch();
        executor = Executors.newSingleThreadExecutor();
//        executor = Executors.newCachedThreadPool();
        // todo may be not Single thread, pool add?
    }
    @Override
    public void onDestroy() {
        // todo need to check this process, may be remove it
        if (sftp != null){
            if (sftp.isConnected()){
                sftp.disconnect();
            }
        }
        if (session != null){
            if (session.isConnected()){
                session.disconnect();
            }
        }
        super.onDestroy();
    }
    private void sendResponse(PendingIntent pi, int code, String msg){
        if (pi == null) return;
        try{
            pi.send(this, code, new Intent().putExtra("rsp", msg));
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }
    private void sendResponse(PendingIntent pi, int code, String[] msg){
        if (pi == null) return;
        try{
            pi.send(this, code, new Intent().putExtra("rsp", msg));
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    private Boolean openSession(PendingIntent pi, Intent intent){
        // Session create
        if (session == null) {
            try {
                session = jsch.getSession(
                        intent.getStringExtra("login"),
                        intent.getStringExtra("host"),
                        intent.getIntExtra("port", 22)
                );
                session.setPassword(intent.getStringExtra("pass"));
                session.setUserInfo(new SSHUserInfo());
            }catch (Exception e){
                Log.e(TAG, e.toString());
                sendResponse(pi, RSP_CONN_ERR, "session open error");
                return false;
            }
        }

        // Session connect
        try {
            if (session.isConnected() == false) {
                session.connect(intent.getIntExtra("timeout", 30000));
            }
            if (session.isConnected()){
                return true;
            }
        }catch (Exception e){
            Log.e(TAG, e.toString());
            sendResponse(pi, RSP_CONN_ERR, "session connect error");
        }
        return false;
    }
    private void download(PendingIntent pi, Intent intent){
        String from = intent.getStringExtra("from");
        String to = intent.getStringExtra("to");
        try {
            ChannelSftp ch = (ChannelSftp) session.openChannel("sftp");
            if (ch != null){
                ch.connect(10000);
                if (ch.isConnected()) {

//                    intent.putExtra("state", STATE_LOAD);

                    DownloadMonitor mon = new DownloadMonitor(pi, intent);
                    ch.get(from, to, mon);


//                    ch.get(from, to, new SftpProgressMonitor() {
//                        private long fullSize = 0;
//                        private long loadedSize = 0;
//                        private int prevPrc = 0;
//
//                        private Intent intent_out = intent;
//                        @Override public void init(int i, String s, String s1, long l) { fullSize = l; }
//                        @Override public void end() {}
//                        @Override public boolean count(long l) {
//                            loadedSize += l;
//                            int currPrc = (int)(100*loadedSize/fullSize);
//                            if (((currPrc % 1) == 0) && (currPrc != prevPrc)){
//                                prevPrc = currPrc;
////                                intent.putExtra("progress", 14);
//                                new Intent().putExtra("progress", 14);
//                                sendResponse(pi, RSP_OK,from);
//                            }
//                            return true;
//                        }
//                    });
                    ch.disconnect();
                    intent.putExtra("state", STATE_DONE);
                    sendResponse(pi, RSP_OK,from);
                    return;
                }
            }
        }catch (Exception e){ Log.e(TAG, e.toString()); }
        sendResponse(pi, RSP_CONN_ERR, "download error");
    }
    
    private class DownloadMonitor implements  SftpProgressMonitor{
        private long fullSize = 0;
        private long loadedSize = 0;
        private int prevPrc = 0;

        private Intent intent;
        private PendingIntent pi;
        private boolean isCanceled;
        DownloadMonitor(PendingIntent pendi,Intent in){
            intent = in;
            pi=pendi;
            isCanceled = false;
        }
        @Override public void init(int i, String s, String s1, long l) { fullSize = l; }
        @Override public void end() {

        }
        @Override public boolean count(long l) {
            loadedSize += l;
            int currPrc = (int)(100*loadedSize/fullSize);
            if (((currPrc % 1) == 0) && (currPrc != prevPrc)){
                prevPrc = currPrc;
//                intent.putExtra("state", STATE_LOAD);
//                intent.putExtra("progress", 14);

                Intent i = new Intent();
                i.putExtra("state", STATE_LOAD);
                sendResponse(pi, RSP_OK,"file");
            }
            return (isCanceled == false);
        }
    }
    
    private void explorer(String cmd, PendingIntent pi, Intent intent){

        // Channel open
        try {
            if (sftp == null){
                sftp = (ChannelSftp)session.openChannel("sftp");
                if (sftp == null){
                    sendResponse(pi, RSP_CONN_ERR, "channel open error");
                    return;
                }
            }
            if (sftp.isConnected() == false){
                sftp.connect(10000);
                if (sftp.isConnected() == false){
                    sendResponse(pi, RSP_CONN_ERR, "channel connect error");
                    return;
                }
            }

            // Channel process
            String path = intent.getStringExtra("path");
            String oldName = intent.getStringExtra("old");
            String newName = intent.getStringExtra("new");

            String msg = "";
            int code = RSP_OK;

            if (cmd.equals("cd")) {
                sftp.cd(path);
            }  else if (cmd.equals("mkdir")) {
                sftp.mkdir(path);
            } else if (cmd.equals("rm")) {
                sftp.rm(path);
            } else if (cmd.equals("rmdir")) {
                sftp.rmdir(path);
            } else if (cmd.equals("rename")) {
                sftp.rename(oldName, newName);
            } else if (cmd.equals("home")) {
                msg = sftp.getHome();
            } else if (cmd.equals("pwd")) {
                msg = sftp.pwd();
            }else if (cmd.equals("ls")) {
                    Vector<ChannelSftp.LsEntry> files = sftp.ls(path);

                    String ls[] = new String[files.size()];
                    int num = 0;

                    for (Iterator it = files.iterator();it.hasNext();){
                        ChannelSftp.LsEntry file = (ChannelSftp.LsEntry)it.next();
                        SftpATTRS attr = file.getAttrs();
                        ls[num++] = file.getFilename()
                                + ls_separator + String.format("%d",attr.getSize())
                                + ls_separator + attr.getPermissionsString()
                                + ls_separator + attr.getMtimeString();
                    }
                    sendResponse(pi, RSP_OK, ls);
                    return;
            }else {
                msg = "command error";
                code = RSP_CMD_ERR;
            }
            sendResponse(pi, code, msg);
        }catch (Exception e){ Log.e(TAG, e.toString() + ", cmd="+cmd); sendResponse(pi, RSP_CONN_ERR, e.toString());}
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PendingIntent pi = intent.getParcelableExtra("callback");

        executor.execute(()->{

            // Session
            if (openSession(pi, intent) == false){
                return;
            }

            // execute command
            String cmd = intent.getStringExtra("cmd");
            if (cmd == null){
                sendResponse(pi, RSP_OK, "");
                return;
            }

            // Channels
            if (cmd.equals("get")){
                download(pi, intent);
            }else{
                explorer(cmd, pi, intent);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    private class SSHUserInfo implements UserInfo {
        public SSHUserInfo(){}
        @Override public String getPassphrase() {return null;}
        @Override public String getPassword() {return null;}
        @Override public boolean promptPassword(String s) {return true;}
        @Override public boolean promptPassphrase(String s) {return true;}
        @Override public boolean promptYesNo(String s) {return true;}
        @Override public void showMessage(String s) {}
    }
}