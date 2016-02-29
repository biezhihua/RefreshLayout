package com.bzh.refresh;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int TRANSITION_ANIM_DURATION = 300;
    public static final float TRANSITION_START_VAL = 0.0f;
    public static final float TRANSITION_END_VAL = 1.0f;

    Handler mHandler = new Handler();
    float mProgress = 0.0f;
    private RefreshView refreshView;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setProgress();

            if (mProgress <= 1.0f) {
                mHandler.postDelayed(this, 30);
            } else {
                Toast.makeText(MainActivity.this, "结束了", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void setProgress() {
        mProgress += 0.01;
        refreshView.setTransitionProgress(mProgress);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshView = (RefreshView) findViewById(R.id.refreshView);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshView.resetValues();
                mProgress = 0.0f;
                mHandler.post(runnable);
            }
        });
    }
}
