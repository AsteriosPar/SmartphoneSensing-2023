package com.tudelft.indoorlocalizationapp;

import androidx.annotation.IntDef;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.Vector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    DatabaseClass db;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public static final int ACTIVITY_STANDING = 0;
    public static final int ACTIVITY_WALKING = 1;
    public static final int ACTIVITY_RUNNING = 2;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private float aX = 0;
    private float aY = 0;
    private float aZ = 0;
    private float gX = 0;
    private float gY = 0;
    private float gZ = 0;
    private String activity = "";
    private Vector<Double> collected_accel = new Vector<Double>();
    private Vector<Double> collected_gyro = new Vector<Double>();
    private int counter = 0;
    private TextView act;
    KNN knn = new KNN();

    double[][] jumping = {
            {5.772805407, 1.491408525},
            {15.29048816, 2.304816965},
            {8.571464033, 2.629734017},
            {18.28445243, 2.442568226},
            {15.57676771, 2.148983947},
            {14.84990087, 1.866227204},
            {12.16142341, 1.784260686},
            {12.53644211, 1.862997091},
            {15.81257614, 1.166125778},
            {14.02979772, 1.782919908},
            {12.06863498, 1.845124976},
            {18.98512681, 2.12048771},
            {13.61624604, 1.993468063},
            {12.3956547, 0.967673535},
            {2.329866165, 0.9733329}
    };

    double[][] walking = {
            {4.946991339, 0.207524172},
            {0.454493928, 0.289319332},
            {1.259789194, 0.222731654},
            {1.591158213, 0.286007604},
            {2.127408742, 0.316515039},
            {1.767162364, 0.36624297},
            {1.864520521, 0.266645219},
            {1.699551455, 0.288104774},
            {1.828309226, 0.465806036},
            {1.987639337, 0.618068084},
            {0.384925359, 0.478173615},
            {1.111020906, 0.336057085},
            {1.812560483, 0.398682757},
            {1.694532577, 0.361776654},
            {1.746755493, 0.258607312}
    };

    double[][] standing = {
            {5.065299773, 0.414664373},
            {0.175786553, 0.059679349},
            {0.125057164, 0.022229966},
            {0.140527446, 0.025016791},
            {0.211730351, 0.059875253},
            {0.106890466, 0.027881049},
            {0.19170376, 0.041778009},
            {0.123185943, 0.02150626},
            {0.105146775, 0.024174017},
            {0.121586046, 0.020777686},
            {0.118961951, 0.019198719},
            {0.084930598, 0.017143709},
            {0.103859401, 0.021365787},
            {0.125951807, 0.009910315},
            {0.06537188, 0.089638729}
    };

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

        act = (TextView) findViewById(R.id.activity_value);

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

        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            // No accelerometer!
        }

        // if the default gyroscope exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            // set gyroscope
            gyroscope = sensorManager
                    .getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.
            sensorManager.registerListener(this, gyroscope,
                    SensorManager.SENSOR_DELAY_UI);
        } else {
            // No gyroscope!
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getApplicationContext().unregisterReceiver(wifiScanReceiver);

        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        super.onResume();
        refreshSamples();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, gyroscope,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        currentX.setText("0.0");
//        currentY.setText("0.0");
//        currentZ.setText("0.0");

        if (event.sensor.getType() == 1) {
            // get the the x,y,z values of the accelerometer
            aX = event.values[0];
            aY = event.values[1];
            aZ = event.values[2];
//            // display the current x,y,z accelerometer values
//            currentX.setText(String.format("%.2f", aX));
//            currentY.setText(String.format("%.2f", aY));
//            currentZ.setText(String.format("%.2f", aZ));

            Vector<Double> v1 = new Vector<Double>();
            double accel = Math.sqrt(aX * aX + aY * aY + aZ * aZ);
            double gyro = Math.sqrt(gX * gX + gY * gY + gZ * gZ);
            collected_accel.add(accel);
            collected_gyro.add(gyro);
            counter = counter + 1;

            if (counter >= 10) {
                double testData[] = {calculateSD(collected_accel), calculateSD(collected_gyro)};
                int k = 3;
                int activity_detected = knn.classify(jumping, walking, standing, testData, k);
                collected_gyro.clear();
                collected_accel.clear();
                counter = 0;
                if (activity_detected == 1) {
                    act.setText("Jumping");
                }
                if (activity_detected == 2) {
                    act.setText("Walking");
                }
                if (activity_detected == 3) {
                    act.setText("Standing");
                }
            }
        }

        if (event.sensor.getType() == 4) {
            // get the the x,y,z values of the gyroscope
            gX = event.values[0];
            gY = event.values[1];
            gZ = event.values[2];
            // display the current x,y,z gyroscope values
//            gyro_x.setText(String.format("%.2f", gX));
//            gyro_y.setText(String.format("%.2f", gY));
//            gyro_z.setText(String.format("%.2f", gZ));
        }

//        String sensorName = String.valueOf(event.sensor.getType());
//        Log.d("Sensors ",sensorName);

    }

    public static double calculateSD(Vector<Double> data) {
        int n = data.size();
        double mean = 0.0;
        double sum = 0.0;
        double variance = 0.0;
        double stdDev = 0.0;

        // Calculate mean
        for (double value : data) {
            mean += value;
        }
        mean /= n;

        // Calculate variance
        for (double value : data) {
            sum += Math.pow((value - mean), 2);
        }
        variance = sum / (n - 1);

        // Calculate standard deviation
        stdDev = Math.sqrt(variance);

        return stdDev;
    }

    private void refreshSamples() {
        if (mPreferences.getInt("c1_samples", 0) == 0) {
            findViewById(R.id.error1).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.error1).setVisibility(View.GONE);
        }
        if (mPreferences.getInt("c2_samples", 0) == 0) {
            findViewById(R.id.error2).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.error2).setVisibility(View.GONE);
        }
        if (mPreferences.getInt("c3_samples", 0) == 0) {
            findViewById(R.id.error3).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.error3).setVisibility(View.GONE);
        }
        if (mPreferences.getInt("c4_samples", 0) == 0) {
            findViewById(R.id.error4).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.error4).setVisibility(View.GONE);
        }
    }

    private void darkenBlocks() {
        findViewById(R.id.block_C1).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C2).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C3).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C4).setBackgroundColor(Color.parseColor("#88000000"));
    }

    private void setBrightBlock(int id) {
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                makeToast("Permission for WiFi scan not granted");
                return;
            }
            if (runLocationPermissionCheck()) {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                boolean success = wifiManager.startScan();
                if (!success) {
                    makeToast("Wifi Scan is not ready yet");
                }
            }
        } else if (v.getId() == R.id.btn_delete) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Records")
                    .setMessage("Do you really want to delete all database records?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            makeToast("This function has been currently disabled");
//                            db.deleteAllData();
//                            mEditor.putInt("c1_samples", 0);
//                            mEditor.apply();
//                            mEditor.putInt("c2_samples", 0);
//                            mEditor.apply();
//                            mEditor.putInt("c3_samples", 0);
//                            mEditor.apply();
//                            mEditor.putInt("c4_samples", 0);
//                            mEditor.apply();
//                            darkenBlocks();
//                            Toast.makeText(getApplicationContext(), "Training data deleted", Toast.LENGTH_SHORT).show();
//                            refreshSamples();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        } else {
            String cell = ((TextView) v).getText().toString();
            Intent train = new Intent(MainActivity.this, TrainActivity.class);
            train.putExtra("key", cell); //Optional parameters
            MainActivity.this.startActivity(train);
        }
    }

    private void makeToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void applyKNN() {
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
        if (runLocationPermissionCheck()) {
            // Store results in a list.
            List<ScanResult> scanResults = wifiManager.getScanResults();
            checkIfEmpty(scanResults);
            for (ScanResult scanResult : scanResults) {
                scannedAPs.addElement(scanResult.BSSID);
            }
        }

//        PREPARING THE DISTANCE VECTOR PHASE
        for (String scan : scannedAPs) {
//            For every cell
            int pointer = 0;
            for (int i = 0; i < 4; i++) {
//                For every measurement
                for (int j = pointer; j < pointer + measurements_num[i]; j++) {
                    if (!db.checkAPExists(scan, table_names[i])) {
                        distance_sum[j]++;
                    } else if (db.isNull(scan, table_names[i], ("M" + (j - pointer)))) {
                        distance_sum[j]++;
                    }
                }
                pointer = pointer + measurements_num[i];
            }
        }
//        Passing the sums through a root would give us the euclidean distance but since we have either 0 or 1, it is redundant.

//        DETERMINING THE OUTPUT RESULT PHASE
        int[] id_table = new int[total_measurements];
        for (int i = 0; i < total_measurements; i++) {
            if (i < measurements_num[0]) {
                id_table[i] = 0;
            } else if (i < (measurements_num[0] + measurements_num[1])) {
                id_table[i] = 1;
            } else if (i < (measurements_num[0] + measurements_num[1] + measurements_num[2])) {
                id_table[i] = 2;
            } else {
                id_table[i] = 3;
            }
        }
//        Sort
        bubbleSort(distance_sum, id_table);
        int[] neighbours_counter = {0, 0, 0, 0};
        for (int id : id_table) {
            neighbours_counter[id]++;
            if (neighbours_counter[id] == K) {
                setBrightBlock(id);
                break;
            }
        }
    }

    private static void bubbleSort(int[] ap, int[] id) {
        boolean sorted = false;
        int temp;
        int temp2;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < ap.length - 1; i++) {
                if (ap[i] > ap[i + 1]) {
                    temp = ap[i];
                    ap[i] = ap[i + 1];
                    ap[i + 1] = temp;
                    temp2 = id[i];
                    id[i] = id[i + 1];
                    id[i + 1] = temp2;
                    sorted = false;
                }
            }
        }
    }

    private void checkIfEmpty(List<ScanResult> scanResults) {
        if (scanResults.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "No WiFi APs detected!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private boolean runLocationPermissionCheck() {
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

