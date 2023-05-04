package com.tudelft.indoorlocalizationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class TrainActivity extends AppCompatActivity implements View.OnClickListener {

    private WifiManager wifiManager;
    private TextView txt_train;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        Intent intent = getIntent();
        String cell = intent.getStringExtra("key");

        Button btn_train = (Button) findViewById(R.id.btn_train);
        txt_train = (TextView) findViewById(R.id.text_train);
        TextView txt_train_title = findViewById(R.id.text_train_title);
        txt_train_title.setText("Go to location " + cell + " and press <Scan> to detect WIFI signal");

        btn_train.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Set wifi manager.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Start a wifi scan.
        wifiManager.startScan();
        // Store results in a list.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<ScanResult> scanResults = wifiManager.getScanResults();
        findViewById(R.id.bg_train).setAlpha(0);
        // Write results to a label
        for (ScanResult scanResult : scanResults) {
            txt_train.setText(txt_train.getText() + "\n\tBSSID = "
                    + scanResult.BSSID + "    RSSI = "
                    + scanResult.level + "dBm");
        }
    }
}