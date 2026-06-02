package com.example.hellofirst;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView welcomeText = findViewById(R.id.welcomeText);
        TextView systemInfo = findViewById(R.id.systemInfo);

        welcomeText.setText("欢迎使用 HelloFirst!");

        String sysInfo = "设备型号: " + Build.MODEL + "\n"
                + "厂商: " + Build.MANUFACTURER + "\n"
                + "Android 版本: " + Build.VERSION.RELEASE + "\n"
                + "API 级别: " + Build.VERSION.SDK_INT + "\n"
                + "品牌: " + Build.BRAND + "\n"
                + "产品: " + Build.PRODUCT;

        systemInfo.setText(sysInfo);
    }
}
