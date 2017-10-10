package com.ubt.msm8909_serial.test;

import android.util.Log;

import com.ubt.ip.serial.util.SerialPortUtil;

import java.io.IOException;

import utils.HexUtils;

/**
 * Created by afunx on 10/10/2017.
 */

public class TestExecutor4 {

    private static final String TAG = "TestExecutor4";

    private TestExecutor4() {
    }

    public static TestExecutor4 get() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final TestExecutor4 INSTANCE = new TestExecutor4();
    }

    private volatile boolean mIsStopped;

    private final byte[] mRequestBytes = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a};

    private final byte[] mBuffer = new byte[128];

    private final byte[] mResponseBytes = new byte[128];

    private SerialPortUtil mSerialPortUtil = new SerialPortUtil(1000000);

    public void start() {

        mIsStopped = false;
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

                int totalCount = 0;
                int failCount = 0;
                boolean isFailed;

                while (!mIsStopped) {

                    isFailed = false;
                    ++totalCount;

                    boolean isWriteSuc = write();
                    if (!isWriteSuc) {
                        Log.e(TAG, "write() fail");
                        isFailed = true;
//                        mIsStopped = true;
                    }
                    boolean isReadSuc = read();
                    if (!isReadSuc) {
                        Log.e(TAG, "read() fail");
                        isFailed = true;
//                        mIsStopped = true;
                    }

                    if (totalCount % 1000 == 1 || isFailed) {
                        if (isFailed) {
                            ++failCount;
                        }
                        Log.e(TAG, "totalCount: " + totalCount + ", failCount: " + failCount);
                    }
                }
            }
        }.start();
        write();
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

    private boolean read() {
        final int readTimeout = 100;
        final long startTimestamp = System.currentTimeMillis();
        int offset = 0;
        int time = 0;
        while (offset < mRequestBytes.length && time++ < readTimeout) {
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
        if (offset < mRequestBytes.length) {
            Log.e(TAG, "read() fail for not enough response");
            return false;
        }

        for (int i=0;i<mRequestBytes.length;i++) {
            if (mResponseBytes[i] != mRequestBytes[i]) {
                Log.e(TAG, "read() fail for content error");
                return false;
            }
        }

        final long consume = System.currentTimeMillis() - startTimestamp;
        Log.i(TAG, "read() consume: " + consume);
        return true;
    }

    private boolean write() {
        boolean isSuc = false;
        try {
            mSerialPortUtil.write(mRequestBytes);
            Log.d(TAG, "write() " + HexUtils.bytes2HexString(mRequestBytes));
            isSuc = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isSuc;
    }

    public void stop() {
        mIsStopped = true;
    }
}