package com.example.sshfileexplorer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.adapters.FileListAdapter;
import com.example.sshfileexplorer.ui.dialogs.YesNoDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import services.SFTPService;

public class FileExplorerActivity extends AppCompatActivity {
    String TAG = "TAG SSH EXPLORER";
    private FileListAdapter listAdapter;
    private Intent srvSFTP;

    private final int REQ_CONNECT=0;
    private final int REQ_LS=1;
    private final int REQ_CD=2;
    private final int REQ_PWD=3;
    private final int REQ_GET=4;

    private String storageDir;
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

        // Storage dirtectory

        storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                + File.separator
                + getApplicationInfo().loadLabel(getPackageManager()).toString();

        // connect to server

        Intent intent = this.getIntent();
        srvSFTP = new Intent(this, SFTPService.class);
        srvSFTP.putExtra("host", intent.getStringExtra("host"));
        srvSFTP.putExtra("port", intent.getStringExtra("port"));
        srvSFTP.putExtra("login", intent.getStringExtra("login"));
        srvSFTP.putExtra("pass", intent.getStringExtra("pass"));

        srvSFTP.putExtra("callback", createPendingResult(REQ_CONNECT, getIntent(), 0));
        startService(srvSFTP);

        // UI init

        // Logout button

        ImageButton btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> {
            YesNoDialog dialog = new YesNoDialog();
            dialog.setTitle("Remove SSH server");
            dialog.setMessage("Disconnect?");

            dialog.setButtonYes("Yes", view -> {
                stopService(new Intent(this, SFTPService.class));
                this.finish();
                dialog.dismiss();
            });
            dialog.setButtonNo("No", view -> dialog.dismiss());
            dialog.show(getSupportFragmentManager(), "");
        });

        // Download button

        listAdapter = new FileListAdapter(this);
        listAdapter.setOnDownloadListener((parent, view, position, id)->{
            FileListAdapter.FileItem file = (FileListAdapter.FileItem)listAdapter.getItem(position);
            if (file.getType() == 'd')  return;
            cmdGET(file.getName());
        });

        ListView list = findViewById(R.id.filesList);
        list.setAdapter(listAdapter);

        // Select event

        list.setOnItemClickListener((parent, view, position, id)->{
            FileListAdapter.FileItem file = (FileListAdapter.FileItem)listAdapter.getItem(position);
            if (file.getType() == 'd') {
                cmdCD(file.getName());
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, String.format("onItemLongClick %d",position));
                return false;
            }
        });

        // Back button pressed process
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { cmdCD(".."); }
        });
    }
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, SFTPService.class));
        super.onDestroy();
    }

    private void cmdLS(){
        srvSFTP.putExtra("callback", createPendingResult(REQ_LS, getIntent(), 0));
        srvSFTP.putExtra("path", ".");
        srvSFTP.putExtra("cmd", "ls");
        startService(srvSFTP);
    }
    private void cmdCD(String path){
        srvSFTP.putExtra("callback", createPendingResult(REQ_CD, getIntent(), 0));
        srvSFTP.putExtra("path", path);
        srvSFTP.putExtra("cmd", "cd");
        startService(srvSFTP);
    }
    private void cmdPWD(){
        srvSFTP.putExtra("callback", createPendingResult(REQ_PWD, getIntent(), 0));
        srvSFTP.putExtra("cmd", "pwd");
        startService(srvSFTP);
    }
    private void cmdGET(String name){

        // Create storage directory

        File file = new File(storageDir);
        if (file.exists() == false){
            if (file.mkdir() == false){
                showMsg("Error");
                return;
            }
        }
        String srcDir = ((TextView)findViewById(R.id.filesTitle)).getText().toString();

        srvSFTP.putExtra("callback", createPendingResult(REQ_GET, getIntent(), 0));
        srvSFTP.putExtra("from", srcDir + File.separator + name);
        srvSFTP.putExtra("to", storageDir + File.separator + name);
        srvSFTP.putExtra("cmd", "get");
        startService(srvSFTP);
    }

    private void showMsg(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private void showMsg(String msg, Boolean err){
        if (err){
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == SFTPService.RSP_OK){
            switch (requestCode){
                case REQ_CONNECT:
                case REQ_CD:
                    cmdLS();
                    cmdPWD();
                    break;

                case REQ_PWD:
                    TextView title = findViewById(R.id.filesTitle);
                    title.setText(data.getStringExtra("rsp"));
                    break;
                case REQ_LS:
                    String[] arr = data.getStringArrayExtra("rsp");
                    listAdapter.clear();

                    // create items
                    ArrayList<FileListAdapter.FileItem> files = new ArrayList<>();
                    ArrayList<FileListAdapter.FileItem> dirs = new ArrayList<>();

                    class FileItem implements FileListAdapter.FileItem{
                        private String[] file;
                        private String[] date;
                        FileItem (String[] f, String[] d){ file = f; date = d;}
                        @Override public String getName() { return file[0]; }
                        @Override public int getSize() { return Integer.parseInt(file[1]); }
                        @Override public char getType() { return file[2].toCharArray()[0];}
                        @Override public String getDate() { return date[2]+" " + date[1]+" " + date[5] +" " + date[3]; }
                    }

                    for (Iterator it = Arrays.stream(arr).iterator();it.hasNext();){
                        String[] file = ((String)it.next()).split(SFTPService.ls_separator);
                        String[] date = file[3].split(" ");
                        if (file[0].toCharArray()[0] == '.') continue;

                        if (file[2].toCharArray()[0] == 'd'){   // dir
                            dirs.add(new FileItem(file, date));
                        }else if (file[2].toCharArray()[0] == '-'){   // file
                            files.add(new FileItem(file, date));
                        }
                        /*
                            d (directory)
                            c (character device)
                            l (symlink)
                            p (named pipe)
                            s (socket)
                            b (block device)
                            D (door, not common on Linux systems, but has been ported)
                        */
                    }

                    // Sort items
                    Collections.sort(dirs,(o1, o2) ->  o1.getName().compareTo(o2.getName()));
                    Collections.sort(files,(o1, o2) ->  o1.getName().compareTo(o2.getName()));

                    // Add to view
                    for (Iterator it = dirs.iterator();it.hasNext();){
                        listAdapter.addItem((FileListAdapter.FileItem)it.next());
                    }
                    for (Iterator it = files.iterator();it.hasNext();){
                        listAdapter.addItem((FileListAdapter.FileItem)it.next());
                    }
                    listAdapter.notifyDataSetChanged();
                    break;
                case REQ_GET:
                    if (data.getIntExtra("state", -1) == SFTPService.STATE_LOAD){
                        Log.w(TAG, String.format("load %d", data.getIntExtra("progress", 0) ));
                    }else if (data.getIntExtra("state", -1) == SFTPService.STATE_CANCEL){
                        showMsg(data.getStringExtra("rsp") + " cancel");
                    }else if (data.getIntExtra("state", -1) == SFTPService.STATE_DONE){
                        showMsg(data.getStringExtra("rsp") + " download");
                    }
                    break;
                default:
                    break;
            }
        } else if (resultCode == SFTPService.RSP_CONN_ERR){
            showMsg("conn: "+data.getStringExtra("rsp"),true);
            if (data.getStringExtra("rsp").charAt(0) == '3'){   // if permission denied
                cmdCD("..");
            }
        }else if (resultCode == SFTPService.RSP_CMD_ERR){
            showMsg("cmd: "+data.getStringExtra("rsp"), true);
        }else {
            showMsg("unknown error", true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
