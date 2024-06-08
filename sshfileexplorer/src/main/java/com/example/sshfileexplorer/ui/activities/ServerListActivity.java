package com.example.sshfileexplorer.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sshfileexplorer.R;
import com.example.sshfileexplorer.ui.adapters.ServerListAdapter;
import com.example.sshfileexplorer.ui.dialogs.ServerDialog;
import com.example.sshfileexplorer.ui.dialogs.YesNoDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

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

            this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // for background and corner support

            // "Add server" button

            FloatingActionButton btnAddServer = findViewById(R.id.btnAddServer);
            btnAddServer.setOnClickListener(v -> {
                ServerDialog dialog = new ServerDialog();
                dialog.setButtonConnect("Connect", (view)->{
                    addServer((String)dialog.get(ServerDialog.HOST),
                            (String)dialog.get(ServerDialog.PORT),
                            (String)dialog.get(ServerDialog.LOGIN),
                            (String)dialog.get(ServerDialog.PASS),
                            (Boolean) dialog.get(ServerDialog.PASS_SAVE), true);
                    dialog.dismiss();
                });
                dialog.setButtonAccept("Save", (view)->{
                    addServer((String)dialog.get(ServerDialog.HOST),
                            (String)dialog.get(ServerDialog.PORT),
                            (String)dialog.get(ServerDialog.LOGIN),
                            (String)dialog.get(ServerDialog.PASS),
                            (Boolean) dialog.get(ServerDialog.PASS_SAVE), false);
                    dialog.dismiss();
                });
                dialog.show(getSupportFragmentManager(), "");
            });

            // Servers list
            srvListAdapter = new ServerListAdapter(this);
            ListView srvListView = findViewById(R.id.serversList);
            srvListView.setAdapter(srvListAdapter);
            srvListView.setOnItemClickListener((parent, view, position, id) -> {
                try {
                    Map item = srvListAdapter.getItem(position);
                    connect(
                            (String) item.get("host"),
                            (String) item.get("port"),
                            (String) item.get("login"),
                            (String) item.get("pass")
                    );
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
            srvListAdapter.setOnEditListener((parent, view, position) -> {
                ServerDialog dialog = new ServerDialog();

                // filling dialog data

                dialog.setTitle("Edit SSH server");
                try {
                    Map item = srvListAdapter.getItem(position);

                    dialog.set(ServerDialog.HOST, item.get("host"));
                    dialog.set(ServerDialog.PORT, item.get("port"));
                    dialog.set(ServerDialog.LOGIN,item.get("login"));
                    dialog.set(ServerDialog.PASS, item.get("pass"));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }

                // filling dialog response

                dialog.setButtonConnect("Connect", (v)->{
                        connect(
                                (String)dialog.get(ServerDialog.HOST),
                                (String)dialog.get(ServerDialog.PORT),
                                (String)dialog.get(ServerDialog.LOGIN),
                                (String)dialog.get(ServerDialog.PASS)
                        );
                    dialog.dismiss();

                });
                dialog.setButtonAccept("Cancel", (v)->dialog.dismiss());
                dialog.show(getSupportFragmentManager(), "");
            });
            srvListAdapter.setOnRemoveListener((parent, view, position) -> {
                YesNoDialog dialog = new YesNoDialog();
                String msg = "Do you want to remove \n";
                try {
                    Map item = srvListAdapter.getItem(position);
                    msg += (String) item.get("login");
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                msg += " ?";

                dialog.setTitle("Remove SSH server");
                dialog.setMessage(msg);

                dialog.setButtonYes("Yes", v -> {
                    srvListAdapter.deleteItem(position);
                    srvListAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                });
                dialog.setButtonNo("No", v -> dialog.dismiss());

                dialog.show(getSupportFragmentManager(), "");
            });
    }

    private void addServer(String ip_addr, String ip_port, String login, String pass, Boolean save, Boolean connect){
        Map item = new HashMap<>();

            item.put("host", ip_addr);
            item.put("port", ip_port);
            item.put("login", login);
            if (save) {
                item.put("pass", pass);
            } else {
                item.put("pass", "");
            }
            srvListAdapter.addItem(item);
            srvListAdapter.notifyDataSetChanged();

            if (connect) {
                this.connect(ip_addr, ip_port, login, pass);
            }
    }
    @Override
    protected void onDestroy() {
        stopService(new Intent(this, SSHService.class));
        super.onDestroy();
    }

    private void connect(@NonNull String host, @NonNull String port, @NonNull String login, @NonNull String pass){
        Log.d(TAG, String.format("connect to '%s:%s' (%s, %s)", host, port, login, pass));

        if (pass.length() == 0){
            Toast.makeText(this, "Password not entry!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent ssh = new Intent(this, SSHService.class);
        ssh.putExtra("host", host);
        ssh.putExtra("port", Integer.parseInt(port));
        ssh.putExtra("timeout", 1000);
        ssh.putExtra("login", login);
        ssh.putExtra("pass", pass);
        startService(ssh);

        // Run file explorer activity and connect to remote SSH server
        startActivity(new Intent(this, FileExplorerActivity.class));
    }
}