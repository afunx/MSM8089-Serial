package com.ubt.msm8909_serial.test;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.ubt.ip.serial.util.SerialPortUtil;

import java.io.IOException;

import utils.HexUtils;

/**
 * Created by afunx on 19/10/2017.
 */

public class TestExecutor6 {

    private static final String TAG = "TestExecutor6";
    private static final int TEN = 10;
    private static final int TWEENTY = 20;
    private boolean mIsStopped;
    private SerialPortUtil mSerialPortUtil = new SerialPortUtil(1000000);

    private TestExecutor6() {
        mRequestsBytes[0] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0xED};
        mRequestsBytes[1] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x02, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0xED};
        mRequestsBytes[2] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0xED};
        mRequestsBytes[3] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x04, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x07, (byte) 0xED};
        mRequestsBytes[4] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x05, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0xED};
        mRequestsBytes[5] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0xED};
        mRequestsBytes[6] = new byte[]{(byte) 0xFA, (byte) 0xAF, (byte) 0x07, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0xED};
    }

    public static TestExecutor6 get() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final TestExecutor6 INSTANCE = new TestExecutor6();
    }

    private Activity mActivity;
    private final byte[][] mRequestsBytes = new byte[7][];
    private final byte[] mBuffer = new byte[128];

    private final byte[] mResponseBytes = new byte[128];

    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    private void showToast(final String message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                mIsStopped = false;
                try {
                    mSerialPortUtil.open();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "can't open serial port");
                    showToast("串口打开失败！");
                    return;
                }
                boolean isSuc = true;
                for (int motorId = 0; isSuc && motorId < 7; motorId++) {
                    isSuc = test(motorId);
                }
                if (isSuc) {
                    showToast("串口舵机测试成功!");
                } else {
                    showToast("串口舵机测试失败!");
                }
            }
        }.start();
    }

    private boolean test(int motorId) {
        send(motorId);
        boolean isSuc = read(motorId);
        return isSuc;
    }

    private boolean read(int motorId) {
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
            }
            int count = _read();
            for (int i = 0; i < count; i++) {
                mResponseBytes[offset + i] = mBuffer[i];
            }
            offset += count;
        }
        if (offset < size - 1) {
            Log.e(TAG, "read() fail for not enough response");
            return false;
        }

        for (int i = 0; i < TEN; i++) {
            if (mResponseBytes[i] != mRequestsBytes[motorId][i]) {
                Log.e(TAG, "read() fail for content error");
                return false;
            }
        }

        if (mResponseBytes[TEN] != (byte) 0xFA) {
            Log.e(TAG, "read() fail for content error 111111");
            return false;
        }
        if (mResponseBytes[TEN + 1] != (byte) 0xAF) {
            Log.e(TAG, "read() fail for content error 222222");
            return false;
        }
        if (mResponseBytes[TEN + 2] != (byte) (motorId + 1)) {
            Log.e(TAG, "read() fail for content error 333333");
            return false;
        }
        if (mResponseBytes[TEN + 9] != (byte) 0xED) {
            Log.e(TAG, "read() fail for content error 444444");
            return false;
        }

        final long consume = System.currentTimeMillis() - startTimestamp;
        Log.i(TAG, "read() consume: " + consume);
        return true;
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

    private void send(int motorId) {
        final byte[] requestBytes = mRequestsBytes[motorId];
        try {
            mSerialPortUtil.write(requestBytes);
            Log.d(TAG, "write() " + HexUtils.bytes2HexString(requestBytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}