package com.example.sshfileexplorer.ui.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.adapters.ServerListAdapter;
import com.example.sshfileexplorer.ui.dialogs.ServerAddDialog;
import com.example.sshfileexplorer.ui.dialogs.YesNoDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import helpers.SSHHelper;
import services.SSHService;

public class MainActivity extends AppCompatActivity {

    String TAG = "TAG SSH EXPLORER";

    private ServerListAdapter srvListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // title bar

        try {
            ActionBar bar = getSupportActionBar();
//            bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.dialog_fg)));
            bar.setTitle("Select server");
        }catch (Exception e){
            // No action bar, nothing modify
        }
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // for background and corner support

        // "Add server" button

        FloatingActionButton btnAddServer = findViewById(R.id.btnAddServer);
        btnAddServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerAddDialog dialog = new ServerAddDialog(MainActivity.this);
                dialog.show(getSupportFragmentManager(), "");
            }
        });


        // Servers list

        srvListAdapter = new ServerListAdapter(this);


        // fixme {

//        PendingIntent it;


        Intent service = new Intent(this, SSHService.class);

        service.putExtra("response", createPendingResult(0, getIntent(), 0));    // add for this.onActivityResult()

        // }

        // todo read from base

        srvListAdapter.addItem("Opange PI", "192.168.80.134:22");
//        srvListAdapter.addItem("Banana PI", "192.142.23.65:23");
//        srvListAdapter.addItem("Ubuntu SSH", "192.142.23.25:23");

        ListView srvListView = findViewById(R.id.serversList);
        srvListView.setAdapter(srvListAdapter);
        srvListView.setOnItemClickListener((parent, view, position, id) -> {
            String[] list = (String[]) srvListAdapter.getItem(position);

            service.putExtra("host", "192.168.80.134");
            service.putExtra("port", 22);
            service.putExtra("timeout", 1000);

            service.putExtra("login", "user");  // todo need to add
            service.putExtra("pass", "1234");   // todo need to add

            service.putExtra("cmd", "ls -l --time-style=long-iso");
            startService(service);

            Log.d(TAG, String.format("%s (%s)", list[0], list[1]));

        });
        srvListAdapter.setOnRemoveListener((parent, view, position, id) -> {
            YesNoDialog dialog = new YesNoDialog();
            String[] list = (String[]) srvListAdapter.getItem(position);

            dialog.setTitle("Remove SSH server");
            dialog.setMessage("Do you want to remove \'" + list[0] + "\'?");

            dialog.setButtonYes("Yes", v -> {
                srvListAdapter.deleteItem(id);
                srvListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });
            dialog.setButtonNo("No", v -> dialog.dismiss());

            dialog.show(getSupportFragmentManager(), "");
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode){
            case 0:         // Done
                Log.i(TAG,"done");
                // todo show File explorer


                // todo open file system activity
            Intent intent = new Intent(this, FileExplorerActivity.class);
            startActivity(intent);
                break;
            case 1:         // Read data
                try {
                    SSHHelper.LSFile file = new SSHHelper.LSFile(data.getStringExtra("resp"));
                    Log.i(TAG, String.format("%s %d", file.getName(), file.getType()));
                } catch (Exception e) {}
                break;
            case -1:        // Server response error
            case -2:        // SSH error
                Log.e(TAG,String.format("%s", data.getStringExtra("resp")));
                break;
            default:        // Other error
                break;
        }
    }



    public void addServer(String name, String ip_addr, String ip_port){

        // todo check is exist
        // todo add server to base

        srvListAdapter.addItem(name, ip_addr + ":"+ip_port);
        srvListAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        Intent ssh = new Intent(this, SSHService.class);
        stopService(ssh);
        super.onDestroy();
    }
}