package com.okreduce.compiler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class StreamUtil {
    public static final String readFileString(File file){
        if(file==null||file.isDirectory()){
            return null;
        }
        FileInputStream input=null;
        ByteArrayOutputStream output=null;
        try {
            input = new FileInputStream(file);
            output=new ByteArrayOutputStream();
            byte[] bys=new byte[1024];
            int i=0;
            while ((i=input.read(bys))!=-1){
                output.write(bys,0,i);
            }
            output.flush();
            return new String(output.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
