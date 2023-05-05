package com.tudelft.indoorlocalizationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    DatabaseClass DBClass;
    public String cell;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        Intent intent = getIntent();
        cell = intent.getStringExtra("key");

        Button btn_train = (Button) findViewById(R.id.btn_train);
        txt_train = (ListView) findViewById(R.id.text_train);
        TextView txt_train_title = findViewById(R.id.text_train_title);

        // Set Title with the appropriate cell name.
        txt_train_title.setText("Go to location " + cell + " and press <Scan> to detect WIFI signal");

        // Create an empty ListView
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        txt_train.setAdapter(adapter);

        // Connect button to listener
        btn_train.setOnClickListener(this);
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
    @Override
    public void onClick(View v) {
        // Set wifi manager.
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Check if WiFi permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "Permission for WiFi scan not granted", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (runLocationPermissionCheck()) {
            // Initialize Local Database
            DBClass = new DatabaseClass(this);
            // Start a wifi scan.
            wifiManager.startScan();
            // Store results in a list.
            List<ScanResult> scanResults = wifiManager.getScanResults();
            checkIfEmpty(scanResults);


            // Write results to a label
            adapter.clear();
            for (ScanResult scanResult : scanResults) {
                String str = "\n\tBSSID = " + scanResult.BSSID + "\n\tRSSI = " + scanResult.level + "dBm";
                // Print results
                adapter.add(str);
                // Save results in database
                boolean apAdded = DBClass.addData(scanResult.BSSID, scanResult.level, cell);
                if (!apAdded) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Problem writing in database", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        }
    }
}