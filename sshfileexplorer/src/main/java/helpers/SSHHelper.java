package helpers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import services.SSHService;

@Deprecated
public class SSHHelper {
    public interface Listener{void onListener(int cmd, int code, Object data);}
    public interface InitListener{void onInit(String path);}
    String TAG = "TAG SSH EXPLORER";
    private static final Integer CMD_PWD = 0;
    public static final Integer CMD_LS = 1;
    public static final Integer CMD_CD = 2;

    public static final Integer CODE_COMPLETE = 0;
    public static final Integer CODE_DATA = 1;
    public static final Integer CODE_ERROR = -1;
    private String path=null;
    private Listener listener = null;
    private InitListener init;
    private Intent service;
    private Activity activity;
    public SSHHelper(Activity act, InitListener init){
        this.init = init;
        activity = act;
        service = new Intent(activity, SSHService.class);
    }

    public void start(){
        service.putExtra("response", activity.createPendingResult(CMD_PWD, activity.getIntent(), 0));
        service.putExtra("cmd", "pwd");
        activity.startService(service);
    }
    public void stop(){
        activity.stopService(service);
    }

    public void ls(){
        service.putExtra("response", activity.createPendingResult(CMD_LS, activity.getIntent(), 0));
        service.putExtra("cmd", "ls " + path + " -l --time-style=long-iso");
        activity.startService(service);
    }
    public void cd(String dir){
        service.putExtra("response", activity.createPendingResult(CMD_CD, activity.getIntent(), 0));
        if (path.equals("/") == false) {
            service.putExtra("cmd", "cd " + path + "/" + dir + " && pwd");
        }else{
            service.putExtra("cmd", "cd " + "/" + dir + " && pwd");
        }
        activity.startService(service);
    }
    public void setOnListener(Listener listener){
        this.listener = listener;
    }
    public void onResult(int req, int rsp, Intent data){
        // Init
        if (req == CMD_PWD){
            if (rsp == SSHService.RSP_SERVER_CMD_ACK) {
                path = data.getStringExtra("resp");
                if (init != null) init.onInit(path);
            }
        }
        // Check on error
        if (listener == null) return;
        if (rsp == SSHService.RSP_SERVER_CMD_NACK) {
            listener.onListener(CMD_LS, CODE_ERROR, data.getStringExtra("resp"));
            return;
        }
        // Commands
        if (req == CMD_LS){
            if (rsp == SSHService.RSP_SERVER_CMD_ACK) {
                listener.onListener(CMD_LS, CODE_DATA, data.getStringExtra("resp"));
            } else if (rsp == SSHService.RSP_SERVER_CMD_DONE) {
                listener.onListener(CMD_LS, CODE_COMPLETE, null);
            }
        }else if (req == CMD_CD){
            if (rsp == SSHService.RSP_SERVER_CMD_ACK) {
                path = data.getStringExtra("resp");
                listener.onListener(CMD_CD, CODE_DATA, "");
            } else if (rsp == SSHService.RSP_SERVER_CMD_DONE) {
                listener.onListener(CMD_CD, CODE_COMPLETE, "");
            }
        }
    }
    public String getPath(){ return path; }
    public void getFile(String file){
        service.putExtra("cmd", "get");
        service.putExtra("file", file);
        activity.startService(service);
    }

    public static class LSFile{
        //                    // > drwxr-xr-x 2 user user 4096 May 15 20:34 Desktop
//                    // > -rw-r--r-- 1 user user    5 2024-05-23 20:17 tmp.txt
//                    // PARSE:
//                    //       d         rwx   r-x   r-x    2    user  user 4096  2024-05-23 20:34  Desktop
//                    // (d)dir/(-)file owner group other links owner group size    date/time        name
        public static final int TYPE_DIR = 0;
        public static final int TYPE_FILE = 1;
        private int type;
        private int size;
        private String date, time, owner, name;

        public LSFile(@NonNull String ld) throws Exception {
            // Prepare data array
            ArrayList<String> list = new ArrayList(Arrays.asList(ld.split(" ")));
            for (Iterator<String> it = list.iterator(); it.hasNext();){
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

            owner = list.get(2);
            size = Integer.parseInt(list.get(4));
            date = list.get(5);
            time = list.get(6);
            name = list.get(7);

        }

        public int getType(){return type;}
        public int getSize(){return size;}
        public String getName() {return name;}
        public String getDate() {return date;}
        public String getTime() {return time;}
        public String getDateTime() {return date+" " +time;}
    }
}
