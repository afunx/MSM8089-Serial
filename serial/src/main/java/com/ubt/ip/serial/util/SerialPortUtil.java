package com.ubt.ip.serial.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ubt.ip.serial.jni.SerialPortFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by afunx on 19/09/2017.
 */

public class SerialPortUtil {
    private static final String TAG = "SerialPortUtil";
    private SerialPortFile mSerialPortFile;
    //    /dev/ttyS1
//    /dev/ttyHSL1
    private final String mFileName = "/dev/ttyHSL1";
    private final int mBandrate;

    public SerialPortUtil(int bandrate) {
        mBandrate = bandrate;
    }

    public synchronized boolean isOpened() {
        return mSerialPortFile != null;
    }

    public synchronized void open() throws IOException {
        if (mSerialPortFile == null) {
            mSerialPortFile = new SerialPortFile(new File(mFileName), mBandrate, 0);
        }
    }

    public synchronized void close() {
        if (mSerialPortFile != null) {
            mSerialPortFile.close();
            mSerialPortFile = null;
        }
    }

    public void write(byte[] bytes) throws IOException {
        if (mSerialPortFile == null) {
            return;
        }
        if (bytes == null) {
            return;
        }
        OutputStream outputStream = mSerialPortFile.getOutputStream();
        if (outputStream != null) {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }

    public int read(byte[] buffer) throws IOException {
        if (mSerialPortFile == null || buffer == null) {
            return 0;
        }
        InputStream inputStream = mSerialPortFile.getInputStream();
        int available = inputStream == null ? 0 : inputStream.available();
        if (available > 0) {
            available = inputStream.read(buffer);
        }
        return available;
    }

    public int read(byte[] buffer, int offset) throws IOException {
        if (mSerialPortFile == null || buffer == null || offset >= buffer.length) {
            return 0;
        }
        InputStream inputStream = mSerialPortFile.getInputStream();
        int available = inputStream == null ? 0 : inputStream.available();
        if (available > 0) {
            available = inputStream.read(buffer, offset, buffer.length - offset);
        }
        return available;
    }

//    public synchronized int read(@NonNull final byte[] buffer, final int timeout) {
//        final SerialPortFile serialPortFile = mSerialPortFile;
//        if (serialPortFile == null) {
//            Log.e(TAG, "read() mSerialPortFile is null");
//            return 0;
//        }
//        final InputStream inputStream = serialPortFile.getInputStream();
//        int available = 0;
//        int time = 0;
//        if (inputStream == null) {
//            return 0;
//        } else {
//            try {
//                while (time < timeout) {
//                    available = inputStream.available();
//                    if (available != 0) {
//                        available = inputStream.read(buffer);
//                        return available;
//                    }
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    ++time;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return 0;
//    }

    public synchronized boolean write(@NonNull final byte[] buffer, int offset, int length) {
        if (mSerialPortFile == null) {
            Log.e(TAG, "write() mSerialPortFile is null");
            return false;
        }
        OutputStream outputStream = mSerialPortFile.getOutputStream();
        if (outputStream == null) {
            return false;
        } else {
            try {
                outputStream.write(buffer, offset, length);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
