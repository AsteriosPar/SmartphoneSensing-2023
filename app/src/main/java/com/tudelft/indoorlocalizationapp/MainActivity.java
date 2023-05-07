package com.tudelft.indoorlocalizationapp;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    DatabaseClass db;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public static final int ACTIVITY_STANDING = 0;
    public static final int ACTIVITY_WALKING = 1;
    public static final int ACTIVITY_RUNNING = 2;

    private WifiManager wifiManager;
    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                applyKNN();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        refreshSamples();

        TextView txt1 = findViewById(R.id.textC1);
        txt1.setOnClickListener(this);
        TextView txt2 = findViewById(R.id.textC2);
        txt2.setOnClickListener(this);
        TextView txt3 = findViewById(R.id.textC3);
        txt3.setOnClickListener(this);
        TextView txt4 = findViewById(R.id.textC4);
        txt4.setOnClickListener(this);
        Button btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
        ImageView btn_delete = findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);

        db = new DatabaseClass(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        getApplicationContext().unregisterReceiver(wifiScanReceiver);
    }

    public void onResume() {
        super.onResume();
        refreshSamples();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
    }

    void refreshSamples(){
        if (mPreferences.getInt("c1_samples", 0) == 0) {
            findViewById(R.id.error1).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.error1).setVisibility(View.GONE);
        }
        if (mPreferences.getInt("c2_samples", 0) == 0) {
            findViewById(R.id.error2).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.error2).setVisibility(View.GONE);
        }
        if (mPreferences.getInt("c3_samples", 0) == 0) {
            findViewById(R.id.error3).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.error3).setVisibility(View.GONE);
        }
        if (mPreferences.getInt("c4_samples", 0) == 0) {
            findViewById(R.id.error4).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.error4).setVisibility(View.GONE);
        }
    }

    void darkenBlocks() {
        findViewById(R.id.block_C1).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C2).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C3).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C4).setBackgroundColor(Color.parseColor("#88000000"));
    }
    void setBrightBlock(int id) {
        darkenBlocks();
        switch (id) {
            case 0:
                findViewById(R.id.block_C1).setBackgroundColor(Color.parseColor("#FFDD77"));
                break;
            case 1:
                findViewById(R.id.block_C2).setBackgroundColor(Color.parseColor("#FFDD77"));
                break;
            case 2:
                findViewById(R.id.block_C3).setBackgroundColor(Color.parseColor("#FFDD77"));
                break;
            default:
                findViewById(R.id.block_C4).setBackgroundColor(Color.parseColor("#FFDD77"));
        }
    }
    void printActivity(int user_activity){
        TextView txt_act = findViewById(R.id.txt_activity);
        switch (user_activity) {
            case ACTIVITY_STANDING: {
                txt_act.setText("Standing");
                break;
            }
            case ACTIVITY_WALKING: {
                txt_act.setText("Walking");
                break;
            }
            case ACTIVITY_RUNNING: {
                txt_act.setText("Running");
                break;
            }
            default: {
                txt_act.setText("Press START.");
            }
        }
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            boolean success = wifiManager.startScan();
            if (!success) {
                Toast toast = Toast.makeText(getApplicationContext(), "Wifi Scan is not ready yet...", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else if (v.getId() == R.id.btn_delete){
            db.deleteAllData();
            mEditor.putInt("c1_samples", 0);
            mEditor.apply();
            mEditor.putInt("c2_samples", 0);
            mEditor.apply();
            mEditor.putInt("c3_samples", 0);
            mEditor.apply();
            mEditor.putInt("c4_samples", 0);
            mEditor.apply();
            darkenBlocks();
            Toast.makeText(getApplicationContext(), "Training data deleted", Toast.LENGTH_SHORT).show();
            refreshSamples();
        }
        else {
            String cell = ((TextView) v).getText().toString();
            Intent train = new Intent(MainActivity.this, TrainActivity.class);
            train.putExtra("key", cell); //Optional parameters
            MainActivity.this.startActivity(train);
        }
    }

    public void applyKNN(){
        int K = 2;
        int[] measurements_num = {
                mPreferences.getInt("c1_samples", 0),
                mPreferences.getInt("c2_samples", 0),
                mPreferences.getInt("c3_samples", 0),
                mPreferences.getInt("c4_samples", 0)
        };
        int total_measurements = measurements_num[0] + measurements_num[1] + measurements_num[2] + measurements_num[3];
        String[] table_names = {"C1", "C2", "C3", "C4"};
        Vector<String> scannedAPs = new Vector<String>();
        int[] distance_sum = new int[total_measurements];
        Arrays.fill(distance_sum, 0);

//        SCAN CURRENT WIFI PHASE
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "Permission for WiFi scan not granted", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (runLocationPermissionCheck()) {
            // Store results in a list.
            List<ScanResult> scanResults = wifiManager.getScanResults();
            checkIfEmpty(scanResults);
            for (ScanResult scanResult : scanResults) {
                scannedAPs.addElement(scanResult.BSSID);
            }
        }

//        PREPARING THE DISTANCE VECTOR PHASE
        for (String scan: scannedAPs) {
//            For every cell
            int pointer = 0;
            for (int i=0;i<4;i++) {
//                For every measurement
                for (int j=pointer; j<pointer+measurements_num[i]; j++) {
                    if (!db.checkAPExists(scan, table_names[i])) {
                        distance_sum[j]++;
                    } else if (db.isNull(scan, table_names[i], ("M" + (j-pointer)))) {
                        distance_sum[j]++;
                    }
                }
                pointer = pointer + measurements_num[i];
            }
        }
//        Passing the sums through a root would give us the euclidean distance but since we have either 0 or 1, it is redundant.

//        DETERMINING THE OUTPUT RESULT PHASE
        int[] id_table = new int[total_measurements];
        for (int i=0;i<total_measurements;i++){
            if (i<measurements_num[0]) {
                id_table[i] = 0;
            }
            else if (i<(measurements_num[0] + measurements_num[1])) {
                id_table[i] = 1;
            }
            else if (i<(measurements_num[0] + measurements_num[1] + measurements_num[2])) {
                id_table[i] = 2;
            }
            else {
                id_table[i] = 3;
            }
        }
//        Sort
        bubbleSort(distance_sum, id_table);
        int[] neighbours_counter = {0, 0, 0, 0};
        for (int id: id_table){
            neighbours_counter[id]++;
            if (neighbours_counter[id] == K){
                setBrightBlock(id);
                break;
            }
        }
    }

    public static void bubbleSort(int[] ap, int[] id) {
        boolean sorted = false;
        int temp;
        int temp2;
        while(!sorted) {
            sorted = true;
            for (int i = 0; i < ap.length - 1; i++) {
                if (ap[i] > ap[i+1]) {
                    temp = ap[i];
                    ap[i] = ap[i+1];
                    ap[i+1] = temp;
                    temp2 = id[i];
                    id[i] = id[i+1];
                    id[i+1] = temp2;
                    sorted = false;
                }
            }
        }
    }

    public void checkIfEmpty(List<ScanResult> scanResults) {
        if (scanResults.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "No WiFi APs detected!", Toast.LENGTH_SHORT);
            toast.show();
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
}

