package helpers;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class DBHelper {
    String TAG = "TAG SSH EXPLORER";
    private String dbName;
    private SQLiteDatabase db = null;
    public DBHelper(Context ctx, String name){
        dbName = ctx.getApplicationInfo().dataDir + File.separator + dbName;
    }
    public void open(){
        if (db.isOpen()){ return;}

        try {
            // Open DB
            db = SQLiteDatabase.openOrCreateDatabase(dbName, null);
           // Create table
            if (db.isOpen()) {
                db.execSQL("create table if not exists servers (id integer, host text, port integer, login text, pass text)");
            }
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    public int addItem(String host, Integer port, String login, String pass){
        int id = -1;
        if (db.isOpen()){

            Cursor query = db.rawQuery("select * from servers", null);

            id = query.getCount() + 1;
            if (pass == null) pass = "";

            db.execSQL(String.format("insert into servers values (%d, %s, %d, %s, %s)", id, host, port, login, pass));
        }
        return id;
    }

    public void deleteItem(int id){
        if (db.isOpen()){
            db.execSQL(String.format("delete from servers where id like %d", id));
        }
    }

    public void getItem(int id){
        if (db.isOpen()){
            // Read
            Cursor query = db.rawQuery("select * from servers", null);

//            Log.i(TAG, String.format("%d", query.getCount()));
//
//            while (query.moveToNext()){
//                String name = query.getString(0);
//                int age = query.getInt(1);
//                Log.i(TAG, String.format("%s, %d", name, age));
//            }
//

            query.close();
        }
    }

    public void close(){
        if (db.isOpen()){
            db.close();
        }
    }
}
