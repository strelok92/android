package com.example.sshfileexplorer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.adapters.FileListAdapter;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.security.auth.callback.Callback;

import helpers.SSHHelper;
import services.SSHService;

public class FileExplorerActivity extends AppCompatActivity {
    String TAG = "TAG SSH EXPLORER";
    private FileListAdapter listAdapter;
    private Intent service;

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

        service = new Intent(this, SSHService.class);
        service.putExtra("response", createPendingResult(0, getIntent(), 0));    // add for this.onActivityResult()

        // Init adapter
        listAdapter = new FileListAdapter(this);
        listAdapter.setOnDownloadListener((parent, view, position, id)->{
            FileListAdapter adapter = (FileListAdapter)parent.getAdapter();
            SSHHelper.LSFile file = (SSHHelper.LSFile)adapter.getItem(position);
//            Log.i(TAG, String.format("File %d download", position));
            service.putExtra("cmd", "cd "+file.getName());
            startService(service);
        });

        ListView list = findViewById(R.id.filesList);
        list.setAdapter(listAdapter);
        list.setOnItemClickListener((parent, view, position, id)->{

            FileListAdapter adapter = (FileListAdapter)parent.getAdapter();
            SSHHelper.LSFile file = (SSHHelper.LSFile)adapter.getItem(position);

//            service.putExtra("response", createPendingResult(1, getIntent(), 0));
//            service.putExtra("cmd", "cd "+file.getName());
            service.putExtra("cmd", "ls -l --time-style=long-iso");
//            service.putExtra("cmd", "ls ");
            startService(service);
            adapter.clear();
//            service.putExtra("response", createPendingResult(0, getIntent(), 0));
//            service.putExtra("cmd", "ls -l --time-style=long-iso");
//            startService(service);
        });

//        listAdapter.addItem(); // todo need to add '..'

        // Read file list from SSH
        service.putExtra("cmd", "ls -l --time-style=long-iso");
        startService(service);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, String.format("onActivityResult %d %d", requestCode, resultCode));

        switch (resultCode){
            case SSHService.RSP_SERVER_CMD_DONE:         // Done
                listAdapter.notifyDataSetChanged();
                break;
            case SSHService.RSP_SERVER_CMD_ACK:         // Read data
                try {
                    listAdapter.addItem(new SSHHelper.LSFile(data.getStringExtra("resp")));
                } catch (Exception e) {}
                break;
            case SSHService.RSP_SERVER_CMD_NACK:        // SSH error
//                Toast.makeText(getApplicationContext(), data.getStringExtra("resp"), Toast.LENGTH_LONG);
                // todo not working
                break;
            default:        // Other error
                break;
        }
    }
}
