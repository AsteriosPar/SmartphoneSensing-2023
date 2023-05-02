package com.tudelft.indoorlocalizationapp;

import androidx.annotation.IntDef;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final int ACTIVITY_STANDING = 0;
    public static final int ACTIVITY_WALKING = 1;
    public static final int ACTIVITY_RUNNING = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    void darkenBlocks() {
        findViewById(R.id.block_C1).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C2).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C3).setBackgroundColor(Color.parseColor("#88000000"));
        findViewById(R.id.block_C4).setBackgroundColor(Color.parseColor("#88000000"));
    }
    void setBrightBlock(ImageView imageview) {
        darkenBlocks();
        imageview.setBackgroundColor(Color.parseColor("#FFDD77"));
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
}

