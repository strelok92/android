package helpers;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class SSHHelper {
    public static class LSFile{
        //                    // > drwxr-xr-x 2 user user 4096 May 15 20:34 Desktop
//                    // > -rw-r--r-- 1 user user    5 2024-05-23 20:17 tmp.txt
//                    // PARSE:
//                    //       d         rwx   r-x   r-x    2    user  user 4096  2024-05-23 20:34  Desktop
//                    // (d)dir/(-)file owner group other links owner group size    date/time        name
        public final int TYPE_DIR = 0;
        public final int TYPE_FILE = 1;
        private int type;
        private int size;
        private String date, time, owner, name;

        public LSFile(@NonNull String ld) throws Exception {
            // Prepare data array
            ArrayList<String> list = new ArrayList(Arrays.asList(ld.split(" ")));
            for (Iterator<String> it = list.iterator(); it.hasNext();){
                String next = it.next();
                if (next.equals("")) it.remove();
            }

            // Parse data array

            // Type
            char fType = list.get(0).charAt(0);
            if (fType == 'd'){
                type = TYPE_DIR;
            }else if (fType == '-'){
                type = TYPE_FILE;
            }else{
                throw new Exception("ld parse error!");
            }

            // Permissions
            String permission = list.get(0);

            owner = list.get(2);
            size = Integer.parseInt(list.get(4));
            date = list.get(5);
            time = list.get(6);
            name = list.get(7);

        }

        public int getType(){return type;}
        public int getSize(){return size;}
        public String getName() {return name;}
        public String getDate() {return date;}
        public String getTime() {return time;}
        public String getDateTime() {return date+" " +time;}

        //  todo add getPermissions                  file.getOwner(); // String []

    }
}
