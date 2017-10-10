package com.ubt.msm8909_serial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.ubt.msm8909_serial.test.TestExecutor4;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode==KeyEvent.KEYCODE_S) {
            testKeyCodeS();
        } else if(keyCode==KeyEvent.KEYCODE_E) {
            testKeyCodeE();
        }

        return super.onKeyUp(keyCode, event);
    }

    private void testKeyCodeE() {
        TestExecutor4.get().stop();
    }

    private void testKeyCodeS() {
        TestExecutor4.get().start();
    }
}
