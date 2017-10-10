package com.ubt.ip.serial.jni;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class SerialPortFile {

    private static final String TAG = "SerialPortFile";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public static void doCmds(List<String> cmds) throws Exception {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(process.getOutputStream());

        for (String tmpCmd : cmds) {
            os.writeBytes(tmpCmd+"\n");
        }

        os.writeBytes("exit\n");
        os.flush();
        os.close();

        int ret = process.waitFor();
        Log.e("afunx", "ret: " + ret);
    }

    public SerialPortFile(File device, int baudrate, int flags) throws SecurityException, IOException {
		/* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            /* Missing read/write permission, trying to chmod the file */
            Process su;
            su = Runtime.getRuntime().exec("/system/bin/su");
            String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                    + "exit\n";
            su.getOutputStream().write(cmd.getBytes());
            try {
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    Log.e(TAG, "device can't be opened");
                    throw new IOException();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException(device.getAbsolutePath());
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);
    public native void close();
    static {
        System.loadLibrary("serial_port");
    }
}