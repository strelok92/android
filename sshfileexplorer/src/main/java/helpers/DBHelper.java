package helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DBHelper {
    String TAG = "TAG SSH EXPLORER";
    private SQLiteDatabase db;
    public DBHelper(Context ctx, String name){
        // Open DB
        try {
            db = SQLiteDatabase.openOrCreateDatabase(
                    ctx.getApplicationInfo().dataDir + File.separator + name,
                    null
            );
            if (db.isOpen()) {
                db.execSQL("create table if not exists servers (id integer, host text, port integer, login text, pass text)");
            }
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    @SuppressLint("Range")
    public int addItem(@NonNull Map item){
        String host = (String)item.get("host");
        Integer port = (Integer)item.get("port");
        String login = (String)item.get("login");
        String pass = (String)item.get("pass");
        if (pass == null) pass = "";
        int id = -1;
        if (db.isOpen()){
            Cursor query = db.rawQuery("select * from servers", null);
            id = query.getCount();
            if (pass == null) pass = "";
            db.execSQL(String.format("insert into servers values (%d, '%s', %d, '%s', '%s')", id, host, port, login, pass));
        }
        return id;
    }
    public int getId(int pos){
        int ret = -1;
        if (db.isOpen()) {
            Cursor query = db.rawQuery("select * from servers ", null);
            if (query.getCount() <= pos){
                return ret;
            }
            try{
                query.moveToPosition(pos);
                ret = query.getInt(0);
            }catch (Exception e){ Log.e(TAG, e.toString()); }
            query.close();
        }
        return ret;
    }
    public Map getItemPos(int pos){
        Map item = new HashMap();

        if (db.isOpen()) {
            Cursor query = db.rawQuery("select * from servers ", null);
            try {
                query.move(pos);
                item.put("host", query.getString(1));
                item.put("port", query.getInt(2));
                item.put("login", query.getString(3));
                item.put("pass", query.getString(4));
            } catch (Exception e) { Log.e(TAG, e.toString()); }
            query.close();
        }
        return item;
    }
    public Map getItemId(int id){
        Map item = new HashMap();

        if (db.isOpen()) {
            Cursor query = db.rawQuery(String.format("select * from servers where id=%d", id), null);

            if (query.getCount() != 1){
                query.close();
                return item;
            }

            try {
                query.moveToFirst();
                item.put("host", query.getString(1));
                item.put("port", query.getInt(2));
                item.put("login", query.getString(3));
                item.put("pass", query.getString(4));
            } catch (Exception e) { Log.e(TAG, e.toString()); }
            query.close();
        }
        return item;
    }
    public void deleteItemId(int id)  {
        if (db.isOpen()){
            db.delete("servers", String.format("id=%d", id), null);
        }
    }
    public int size(){
        int ret = 0;
        if (db.isOpen()){
            Cursor query = db.rawQuery("select * from servers", null);
            ret = query.getCount();
            query.close();
        }
        return ret;
    }

    public void clear(){
        if (db.isOpen()){
            db.delete("servers", null, null);
        }
    }
}
