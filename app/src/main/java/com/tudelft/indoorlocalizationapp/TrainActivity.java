package com.tudelft.indoorlocalizationapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class TrainActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView txt_train;
    private WifiManager wifiManager;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    DatabaseClass DBClass;
    int new_samples=0;

    String cell;
    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        Intent intent = getIntent();
        cell = intent.getStringExtra("key");
        DBClass = new DatabaseClass(this);

        updateSamplesView(DBClass.getPopulatedColumns(cell));

        Button btn_train = (Button) findViewById(R.id.btn_train);
        btn_train.setOnClickListener(this);
        txt_train = (ListView) findViewById(R.id.text_train);
        TextView txt_train_title = findViewById(R.id.text_train_title);

        // Set Title with the appropriate cell name.
        txt_train_title.setText("Go to location " + cell + " and press <Scan> to detect WIFI signal");

        // Create an empty ListView
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        txt_train.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getApplicationContext().unregisterReceiver(wifiScanReceiver);
    }

    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_train) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast toast = Toast.makeText(getApplicationContext(), "Permission for WiFi scan not granted", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            if (runLocationPermissionCheck()) {
                // Set wifi manager.
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean success = wifiManager.startScan();
                if (!success) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Wifi Scan is not ready yet...", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    public void scanSuccess(){
        // Check if WiFi permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "Permission for WiFi scan not granted", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (runLocationPermissionCheck()) {
            // Store results in a list.
            List<ScanResult> scanResults = wifiManager.getScanResults();
            checkIfEmpty(scanResults);

            adapter.clear();
            int cur_measurement = DBClass.getPopulatedColumns(cell)+1;
            for (ScanResult scanResult : scanResults) {
                String str = "\n\tBSSID = " + scanResult.BSSID + "\n\tRSSI = " + scanResult.level + "dBm";
                // Print results
                adapter.add(str);
                // Save results in database
                boolean apAdded = DBClass.addData(scanResult.BSSID, scanResult.level, cell, ("M"+cur_measurement));
                if (!apAdded) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Problem writing in database", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            DBClass.increaseColumnCount(cell);
            updateSamplesView(DBClass.getPopulatedColumns(cell));
        }
    }

    public void checkIfEmpty(List<ScanResult> scanResults) {
        if (scanResults.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "No WiFi APs detected!", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            // Remove the background.
            findViewById(R.id.bg_train).setAlpha(0);
        }
    }
    public boolean runLocationPermissionCheck() {
        // Set location manager (Location is also necessary)
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast toast = Toast.makeText(getApplicationContext(), "Location Service is required for this action!", Toast.LENGTH_LONG);
            toast.show();
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
        return true;
    }

    void updateSamplesView(int samples){
        // Update the samples counter text
        TextView txt_count = findViewById(R.id.text_counter);
        txt_count.setText("Measurements: " + samples);
    }
}