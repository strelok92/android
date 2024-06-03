package com.example.sshfileexplorer.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
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

import services.SSHService;

public class ServerListActivity extends AppCompatActivity {

    String TAG = "TAG SSH EXPLORER";

    private ServerListAdapter srvListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_server_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // title bar
//        try {
//            ActionBar bar = getSupportActionBar();
////            bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.dialog_fg)));
//            bar.setTitle("Select server");
//        }catch (Exception e){
//            // No action bar, nothing modify
//        }
//
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // for background and corner support

        // "Add server" button

        FloatingActionButton btnAddServer = findViewById(R.id.btnAddServer);
        btnAddServer.setOnClickListener(v -> {
            ServerAddDialog dialog = new ServerAddDialog(ServerListActivity.this);
            dialog.show(getSupportFragmentManager(), "");
        });

        // Servers list
        Intent service = new Intent(this, SSHService.class);

        srvListAdapter = new ServerListAdapter(this);

        // todo read from base
        srvListAdapter.addItem("user", "192.168.168.134");

        ListView srvListView = findViewById(R.id.serversList);
        srvListView.setAdapter(srvListAdapter);
        srvListView.setOnItemClickListener((parent, view, position, id) -> {
            String[] list = (String[]) srvListAdapter.getItem(position);

            Log.d(TAG, String.format("connect to %s (%s)", list[0], list[1]));

            service.putExtra("host", "192.168.147.134");
            service.putExtra("port", 22);
            service.putExtra("timeout", 1000);

            service.putExtra("login", "user");  // todo need to add
            service.putExtra("pass", "1234");   // todo need to add
            startService(service);

            // Run file explorer activity and connect to remote SSH server
            startActivity(new Intent(this, FileExplorerActivity.class));
        });
        srvListAdapter.setOnEditListener((parent, view, position, id) -> {
            ServerAddDialog dialog = new ServerAddDialog(ServerListActivity.this);
//            dialog.
            // fixme filling dialog data + add API
            dialog.show(getSupportFragmentManager(), "");
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