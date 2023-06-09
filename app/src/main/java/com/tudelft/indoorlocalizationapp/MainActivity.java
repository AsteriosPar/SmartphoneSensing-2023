package com.tudelft.indoorlocalizationapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static final int CELLS_NUM = 20;
    private static final int H = 3;
    DatabaseClass db;
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
    private double[] prior = new double[CELLS_NUM];
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
                applyBayesian();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Set listeners to all buttons
        TextView[] textViews = new TextView[CELLS_NUM];
        for (int j = 0; j < CELLS_NUM; j++) {
            String txt = "textC"+(j+1);
            int txtView = getResources().getIdentifier(txt, "id", getPackageName());
            textViews[j] = ((TextView) findViewById(txtView));
            textViews[j].setOnClickListener(this);
        }
        Button btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);
        ImageView btn_delete = findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);
        ImageView btn_uni = findViewById(R.id.btn_uni);
        btn_uni.setOnClickListener(this);
        act = (TextView) findViewById(R.id.activity_value);

        db = new DatabaseClass(this);
        refreshWarningIcons();

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
        refreshWarningIcons();
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
//                            makeToast("This function has been currently disabled");
                            db.deleteAllData();
                            darkenBlocks();
                            makeToast("Training data deleted");
                            refreshWarningIcons();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
        }
        else if (v.getId() == R.id.btn_uni) {
            makeToast("The first 16 cells are reserved for indoor localization in TU Delft Building 28 (2nd floor)");
        }
        else {
            String cell = ((TextView) v).getText().toString();
            Intent train = new Intent(MainActivity.this, TrainActivity.class);
            train.putExtra("key", cell); //Optional parameters
            MainActivity.this.startActivity(train);
        }
    }

    private void refreshWarningIcons() {
        for (int i=1;i<CELLS_NUM+1;i++){
            String error = "error" + i;
            int errorId = getResources().getIdentifier(error, "id", getPackageName());
            if (db.getPopulatedColumns("C"+i)==0){
                findViewById(errorId).setVisibility(View.VISIBLE);
            }
            else {
                findViewById(errorId).setVisibility(View.GONE);
            }
        }
    }

    private void darkenBlocks() {
        for (int j = 1; j < CELLS_NUM+1; j++) {
            String img = "block_C"+j;
            int imgView = getResources().getIdentifier(img, "id", getPackageName());
            findViewById(imgView).setBackgroundColor(Color.parseColor("#88000000"));
        }
    }

    private void setBrightBlock(int id) {
        darkenBlocks();
        String name = "block_C"+(id+1);
        int view = getResources().getIdentifier(name, "id", getPackageName());
        findViewById(view).setBackgroundColor(Color.parseColor("#FFDD77"));
    }

    private void makeToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

    private boolean runLocationPermissionCheck() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast toast = Toast.makeText(getApplicationContext(), "Location Service is required for this action!", Toast.LENGTH_LONG);
            toast.show();
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent);
        }
        return true;
    }

    private void applyBayesian() {
//        for (int ms=0;ms<35;ms++) {
        int[] measurements_num = new int[CELLS_NUM];
        int total_measurements = 0;
        for (int i = 0; i < CELLS_NUM; i++) {
            measurements_num[i] = db.getPopulatedColumns("C" + (i + 1));
            total_measurements += measurements_num[i];
        }
        Vector<String> scannedAPs = new Vector<String>();
        Vector<Integer> scannedRSSs = new Vector<Integer>();
//        We should renew the prior everytime we press the button
        Arrays.fill(prior, 1.0 / CELLS_NUM);


//        1) Gather data
    if (runLocationPermissionCheck()) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            makeToast("Permission for WiFi scan not granted");
            return;
        }
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {
            scannedAPs.addElement(scanResult.BSSID);
            scannedRSSs.addElement(scanResult.level);
        }
    }


//            Test
//            int indexC = 16;
//            int indexM = ms;
//            Cursor column = db.getColumnData(indexC, indexM);
//            Cursor ap_column = db.getColumnData(indexC, 0);
//            column.moveToFirst();
//            do {
//                scannedRSSs.add(column.getInt(0));
//            } while (column.moveToNext());
//            ap_column.moveToFirst();
//            do {
//                scannedAPs.add(ap_column.getString(0));
//            } while (ap_column.moveToNext());


//        2) Make histogram of the training data (1 table per access point)
//        NOTE: The k variable indicates the different H slots, not the measurement values
//                      _ _
//                    _| | |
//                   | | | |_
//            _______| | | | |_______
//          0         h h h h        -100


        double[][][] norm_dist = new double[scannedAPs.size()][CELLS_NUM][2];
        double zero = 0;
        for (double[][] norm : norm_dist) {
            for (double[] doubles : norm) {
                Arrays.fill(doubles, zero);
            }
        }

        for (int i = 0; i < scannedAPs.size(); i++) {
            for (int j = 0; j < CELLS_NUM; j++) {
//                Mean of normal distribution
                double sum = 0;
                int count = 0;
                if (db.checkAPExists(scannedAPs.get(i), "C" + (j + 1))) {
                    for (int k = 0; k < measurements_num[j]; k++) {
                        int value = db.getData(scannedAPs.get(i), "C" + (j + 1), "M" + (k + 1));
//                        We get '1' in case the value is NULL
//                        We can never measure a signal with less than -100db but added the check for safety
                        if (0 > value && value > -100) {
//                            calculate the mean for the normal distribution
                            sum += value;
                            count++;
                        }
                    }
                }
                if (count != 0) {
                    norm_dist[i][j][0] = sum / count;
                }
            }
        }

//        3) Prepare Gaussian parameters (std_dev)
        for (int i = 0; i < scannedAPs.size(); i++) {
            for (int j = 0; j < CELLS_NUM; j++) {
                double dist = 0;
                if (db.checkAPExists(scannedAPs.get(i), "C" + (j + 1))) {
                    for (int k = 0; k < measurements_num[j]; k++) {
                        int value = db.getData(scannedAPs.get(i), "C" + (j + 1), "M" + (k + 1));
                        if (0 > value && value > -100) {
                            dist += Math.pow((value - norm_dist[i][j][0]), 2.0);
                        }
                    }
                }
                norm_dist[i][j][1] = Math.sqrt((dist / measurements_num[j]));
            }
        }


//          4) Iterate through Access points until stop condition is met
        double max_prior = 0;
        int max_index = 0;
        boolean isConverged = false;
        for (int apIndex = 0; apIndex < scannedAPs.size(); apIndex++) {
            if ((max_prior > 0.95)) {
                    setBrightBlock(max_index);
                    makeToast("Iterations: " + apIndex);
//                makeToast("Iteration: " + ms + ", C"+(max_index+1));

                isConverged = true;
                break;
            }
//            TODO: Place a second condition
//            Run the posterior probability calculation
            posterior_calculation(norm_dist, scannedRSSs, apIndex);

//             Max of prior
            max_prior = 0;
            max_index = 0;
            for (int j = 0; j < CELLS_NUM; j++) {
                if (prior[j] > max_prior) {
                    max_prior = prior[j];
                    max_index = j;
                }
            }
        }
        if (!isConverged) {
            makeToast("Not converged");
//            makeToast("Iteration: " + ms + ", C"+(max_index+1));
                setBrightBlock(max_index);
        }
    }
//    }

    private void posterior_calculation(double[][][] norm_dist, Vector<Integer> scannedRSSs, int i){
//        3) Find posterior (multiply histogram matrix with priors and divide with normalization factor) -> This should be a vector where the probabilities of all cells should add up to 1
//        Max of each row : Currently using max as the mean of the histogram
        double[] conditionalProbability = new double[CELLS_NUM];
        int element = scannedRSSs.get(i);

        // Find the conditional probability and the normalization factor
        double normalization_factor = 0;
        for(int j = 0; j < CELLS_NUM; j++){
            if (norm_dist[i][j][1]!=0 && norm_dist[i][j][0]!=0){
                double exponent = -0.5 * Math.pow((element - norm_dist[i][j][0]) / norm_dist[i][j][1], 2);
                double denominator = norm_dist[i][j][1] * Math.sqrt(2 * Math.PI);
                conditionalProbability[j] = Math.exp(exponent) / denominator;
                normalization_factor += conditionalProbability[j]*prior[j];
            }
        }
        // Find the posterior
        for (int j = 0; j < CELLS_NUM; j++) {
            prior[j] = conditionalProbability[j]*prior[j]/normalization_factor;
        }
    }
}

