package helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBHelper {
    String TAG = "TAG SSH EXPLORER";

    private ArrayList<Map> servers = new ArrayList<>();
    private String xmlName;
    public DBHelper(Context ctx, String name) {
        XmlPullParser xml;
        FileInputStream fs;

        String fields[] = {"host", "port","login","pass"};

        xmlName = ctx.getApplicationInfo().dataDir + File.separator + name;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                String str;

                fs = new FileInputStream(xmlName);
                xml = Xml.newPullParser();
                xml.setInput(fs, "UTF-8");

                int evtType = xml.getEventType();
                while(evtType != XmlPullParser.END_DOCUMENT){
                    if (evtType == XmlPullParser.START_TAG){
                        if (xml.getName().equalsIgnoreCase("server")) {
                            if (xml.getAttributeCount() == 4) {

                                Map item = new HashMap<>();

                                for (int f = 0;f<fields.length;f++){
                                    str = xml.getAttributeValue("", fields[f]);
                                    if (str == null) str = "";
                                    item.put(fields[f], str);
                                }
                                servers.add(item);
                            }
                        }
                    }

                    evtType = xml.next();
                }
            }
        }catch (Exception e) {Log.e(TAG, e.toString());};
    }
    public void addItem(@NonNull Map item){
        servers.add(item);
        updateBase();
    }
    public Map getItem(int pos){ return servers.get(pos); }
    public int size(){ return servers.size(); }

    public void deleteItem(int pos)  {
        servers.remove(pos);
        updateBase();
    }
    public void editItem(int pos, @NonNull Map item){
        servers.set(pos, item);
        updateBase();
    }
    public void clear(){
        servers.clear();
        updateBase();
    }
    private void updateBase(){
        XmlSerializer xml;
        FileOutputStream fs;
        xml = Xml.newSerializer();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                File file = new File(xmlName);
                file.delete();

                fs = new FileOutputStream(file);

                xml.setOutput(fs, "UTF-8");
                xml.startDocument("UTF-8", true);
                xml.startTag("", "servers");

                Map item;

                for (int i=0;i<servers.size();i++){
                    item = servers.get(i);
                    xml.startTag("", "server");
                    xml.attribute("", "host",(String) item.get("host"));
                    xml.attribute("", "port",(String) item.get("port"));
                    xml.attribute("", "login",(String) item.get("login"));
                    xml.attribute("", "pass",(String) item.get("pass"));
                    xml.text((String) item.get("login"));
                    xml.endTag("","server");
                }
                xml.endTag("","servers");
                xml.endDocument();
                fs.close();
            }
        }catch (Exception e){ Log.e(TAG, e.toString());}
    }
}
