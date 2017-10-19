package com.ubt.msm8909_serial.test;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ubt.ip.serial.util.SerialPortUtil;

import java.io.IOException;
import java.util.Random;

import utils.HexUtils;

/**
 * Created by afunx on 19/10/2017.
 */

public class TestExecutor5 {

    private static final String TAG = "TestExecutor5";
    private static final int TWEENTY = 20;
    private static final int TEN = 10;

    private TestExecutor5() {
        mRequestsBytes[0] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xED};
        mRequestsBytes[1] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x02, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0xED};
        mRequestsBytes[2] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0xED};
        mRequestsBytes[3] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x04, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xED};
        mRequestsBytes[4] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x05, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0xED};
        mRequestsBytes[5] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0xED};
        mRequestsBytes[6] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x07, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0xED};
    }

    public static TestExecutor5 get() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final TestExecutor5 INSTANCE = new TestExecutor5();
    }

    private volatile boolean mIsStopped;

    private SerialPortUtil mSerialPortUtil = new SerialPortUtil(1000000);

    private final byte[][] mRequestsBytes = new byte[8][];

    private final byte[] mBuffer = new byte[128];

    private final byte[] mResponseBytes = new byte[128];

    private Context mContext;

    private final Random mRandom = new Random();

    private volatile boolean mIsFirst;

    public void setContext(Context context) {
        mContext = context;
    }

    public void start() {
        mIsStopped = false;
        mIsFirst = true;
        try {
            mSerialPortUtil.open();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "can't open serial port");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                while (!mIsStopped) {
                    int motorId = write();
                    read(motorId);
                }
            }
        }.start();
    }


    private int write() {
        Log.d(TAG, "write()");
        int motorId = mRandom.nextInt(7);
        final byte[] requestBytes = mRequestsBytes[motorId];
        try {
            mSerialPortUtil.write(requestBytes);
            Log.d(TAG, "write() " + HexUtils.bytes2HexString(requestBytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return motorId;
    }

    private int _read() {
        Log.d(TAG, "_read() entrance");
        int count = 0;
        try {
            count = mSerialPortUtil.read(mBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (count > 0) {
            Log.d(TAG, "_read() " + HexUtils.bytes2HexString(mBuffer, 0, count));
        } else {
            Log.d(TAG, "_read() dummy");
        }
        return count;
    }

    private void read(int motorId) {
        Log.d(TAG, "read()");
        final int readTimeout = 100;
        final long startTimestamp = System.currentTimeMillis();
        final int size = TWEENTY;
        int offset = 0;
        int time = 0;
        while (offset < size && time++ < readTimeout) {
            if (time > 1) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int count = _read();
                for (int i = 0; i < count; i++) {
                    mResponseBytes[offset + i] = mBuffer[i];
                }
                offset += count;
            }
        }
        if (offset < size - 1) {
            Log.e(TAG, "read() fail for not enough response");
        }

        for (int i = 0; i < TEN; i++) {
            if (mResponseBytes[i] != mRequestsBytes[motorId][i]) {
                Log.e(TAG, "read() fail for content error");
            }
        }

        if (mIsFirst && offset == size - 1) {
            mIsFirst = false;
        }

        final long consume = System.currentTimeMillis() - startTimestamp;
        Log.i(TAG, "read() consume: " + consume);
    }

    public void stop() {
        mIsStopped = true;
    }
}