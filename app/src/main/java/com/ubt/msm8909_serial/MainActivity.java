package com.ubt.msm8909_serial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.ubt.msm8909_serial.test.TestExecutor4;
import com.ubt.msm8909_serial.test.TestExecutor5;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtnStart;
    private Button mBtnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);

        mBtnStart.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);

        mBtnStart.setEnabled(true);
        mBtnStop.setEnabled(false);

        TestExecutor5.get().setContext(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_S) {
            testKeyCodeS();
        } else if (keyCode == KeyEvent.KEYCODE_E) {
            testKeyCodeE();
        }

        return super.onKeyUp(keyCode, event);
    }

    private void testKeyCodeE() {
        TestExecutor5.get().stop();
    }

    private void testKeyCodeS() {
        TestExecutor5.get().start();
    }

    @Override
    public void onClick(View view) {
        if (view == mBtnStart) {
            start();
        } else if(view == mBtnStop) {
            stop();
        }
    }

    private void start() {
        mBtnStart.setEnabled(false);
        mBtnStop.setEnabled(true);
        TestExecutor5.get().start();
    }

    private void stop() {
        mBtnStart.setEnabled(true);
        mBtnStop.setEnabled(false);
        TestExecutor5.get().stop();
    }
}
