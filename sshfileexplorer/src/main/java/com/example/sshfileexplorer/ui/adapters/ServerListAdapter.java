package com.example.sshfileexplorer.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.sshfileexplorer.R;

import java.util.Map;

import helpers.DBHelper;

public class ServerListAdapter extends BaseAdapter {
    public interface OnListener {void onEvent(AdapterView<?> parent, View view, int position);}
    String TAG = "TAG SSH EXPLORER";

    private DBHelper db;
    private LayoutInflater lInflater;
    private OnListener removeListener = null;
    private OnListener editListener = null;

    public ServerListAdapter(Context ctx){
        lInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        db = new DBHelper(ctx, "servers.xml");
    }
    @Override
    public int getCount() {return db.size(); }
    @Override
    public Map getItem(int position) { return db.getItem(position); }
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
                if (editListener != null) {
                    editListener.onEvent((AdapterView) parent, v, position);
                }
            });

            // Remove item listener
            view.findViewById(R.id.bSrvItemDel).setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onEvent((AdapterView) parent, v, position);
                }
            });
        }

        // Update item data on the View
        Map item = db.getItem(position);

        if (item.size() > 0) {
            ((TextView) view.findViewById(R.id.tServerLogin)).setText(((String) item.get("host")).toString());
            ((TextView) view.findViewById(R.id.tServerIP)).setText(((String) item.get("login")).toString());
        }else{
            Log.e(TAG, "size error");
        }
        return view;
    }
    public void addItem(Map item){ db.addItem(item); }
    public void editItem(int pos, Map item){db.editItem(pos, item);};
    public void deleteItem(long id){ db.deleteItem((int)id); }
    public void clear(){db.clear();}
    public void setOnRemoveListener(OnListener listener){
        removeListener = listener;
    }
    public void setOnEditListener(OnListener listener){
        editListener = listener;
    }
}


