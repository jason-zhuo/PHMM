package Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by jasonzhuo on 2016/8/4.
 */
public class MyFileWriter {
    String path;

    public MyFileWriter(String path) {
        this.path = path;
    }

    public void WriteArrayToFile(ArrayList<?> List){
        StringBuilder builder  = new StringBuilder();
        for (Object str: List){
            String tmp = str.toString();
            builder.append(tmp+" ");
        }
        //builder.append(" ");
        WriteResultToFile(builder.toString());
    }
    public void WriteResultToFile(String msg) {
        File resultfile = new File(path);
        try {
            BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(resultfile,
                    true));
            bw.write(msg);
            bw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
