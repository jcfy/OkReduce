package com.okreduce.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StreamUtil {
    public static String readString(InputStream input) {
        byte[] bytes = readStream(input);
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }

    public static byte[] readStream(InputStream input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] datas = new byte[1024];
        int i;
        try {
            while ((i = input.read(datas)) != -1) {
                output.write(datas, 0, i);
            }
            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(input, output);
        }
        return null;
    }

    public static void outputStream(InputStream input, OutputStream output) {
        byte[] datas = new byte[1024];
        int i;
        try {
            while ((i = input.read(datas)) != -1) {
                output.write(datas, 0, i);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(input, output);
        }
    }

    public static void closeStream(InputStream input, OutputStream output) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (output != null) {
            try {
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
