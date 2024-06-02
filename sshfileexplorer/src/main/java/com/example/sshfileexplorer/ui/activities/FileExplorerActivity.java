package com.example.sshfileexplorer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.adapters.FileListAdapter;

import helpers.SSHHelper;

public class FileExplorerActivity extends AppCompatActivity {
    String TAG = "TAG SSH EXPLORER";
    private FileListAdapter listAdapter;
    private SSHHelper ssh;

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

        ssh = new SSHHelper(this, (path)->{ssh.ls();});

        ssh.setOnListener((cmd, code, data)->{
            if (code == SSHHelper.CODE_ERROR){
                // fixme not working
                //  Toast.makeText(getApplicationContext(), data.toString(), Toast.LENGTH_LONG);
                return;
            }
            if (cmd == SSHHelper.CMD_LS){
                if (code == SSHHelper.CODE_COMPLETE){
                    listAdapter.notifyDataSetChanged();

                }else if (code == SSHHelper.CODE_DATA) {
                    try {
                        listAdapter.addItem(new SSHHelper.LSFile(data.toString()));
                    }catch (Exception e){}
                }
            }
            if ((cmd == SSHHelper.CMD_CD) && (code == SSHHelper.CODE_COMPLETE)){
                ssh.ls();
            }
        });

        // Init adapter
        listAdapter = new FileListAdapter(this);
//        listAdapter.setOnDownloadListener((parent, view, position, id)->{
//            FileListAdapter adapter = (FileListAdapter)parent.getAdapter();
//            SSHHelper.LSFile file = (SSHHelper.LSFile)adapter.getItem(position);
//        });

        ListView list = findViewById(R.id.filesList);
        list.setAdapter(listAdapter);

        list.setOnItemClickListener((parent, view, position, id)->{
            SSHHelper.LSFile file = (SSHHelper.LSFile)listAdapter.getItem(position);

            if (file.getType() == SSHHelper.LSFile.TYPE_DIR) {
                listAdapter.clear();
                ssh.cd(file.getName());
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                listAdapter.clear();
                ssh.cd("..");
            }
        });
        ssh.start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ssh.onResult(requestCode, resultCode, data);
    }
}
