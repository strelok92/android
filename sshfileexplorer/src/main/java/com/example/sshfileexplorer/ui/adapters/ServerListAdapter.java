package com.example.sshfileexplorer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.sshfileexplorer.R;

import java.util.ArrayList;

public class ServerListAdapter extends BaseAdapter {
    public interface OnListener {void onEvent(AdapterView<?> parent, View view, int position, long id);}
    String TAG = "TAG SSH EXPLORER";

    private LayoutInflater lInflater;
    private ArrayList<String[]> srvList;
    private OnListener removeListener = null;
    private OnListener editListener = null;

    public ServerListAdapter(Context ctx){
        lInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        srvList = new ArrayList<>();
    }
    @Override
    public int getCount() {return srvList.size();}
    @Override
    public Object getItem(int position) {return srvList.get(position);}
    @Override
    public long getItemId(int position) {return position;}
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Create View

        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.fragment_item_server, parent, false);

            // Item select listener
            view.setOnClickListener(v -> {
                AdapterView.OnItemClickListener listener = ((AdapterView) parent).getOnItemClickListener();
                if (listener != null) {
                    listener.onItemClick((AdapterView) parent, v, position, position);
                }
            });
            // Edit item listener
            view.findViewById(R.id.bSrvItemEdit).setOnClickListener(v -> {
                if (editListener != null){
                    editListener.onEvent((AdapterView) parent, v, position, position);
                }
            });

            // Remove item listener
            view.findViewById(R.id.bSrvItemDel).setOnClickListener(v -> {
                if (removeListener != null){
                    removeListener.onEvent((AdapterView) parent, v, position, position);
                }
            });
        }

        // Update item data on the View
        String[] item = srvList.get(position);

        ((TextView)view.findViewById(R.id.tServerLogin)).setText(item[0]);
        ((TextView)view.findViewById(R.id.tServerIP)).setText(item[1]);

        return view;
    }
    public void addItem(@NonNull String title,@NonNull String data){srvList.add(new String[]{title, data});}
    public void deleteItem(long id){srvList.remove((int)id);}
    public void clear(){srvList.clear();}
    public void setOnRemoveListener(OnListener listener){
        removeListener = listener;
    }
    public void setOnEditListener(OnListener listener){
        editListener = listener;
    }
}


