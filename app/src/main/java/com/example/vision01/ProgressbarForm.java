package com.example.vision01;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dinuscxj.progressbar.CircleProgressBar;

public class ProgressbarForm extends AppCompatActivity implements CircleProgressBar.ProgressFormatter{

    private static final String DEFAULT_PATTERN = "%d%%";

    public static CircleProgressBar circleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_progressbar);

        circleProgressBar=findViewById(R.id.circle_progressbar);

        circleProgressBar.setProgress(0); //기본 0 설정
        circleProgressBar.setMax(100); //최대 설정

        //progress();
    }

    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }

    public void progress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 100; i++) {
                    final int percent = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            circleProgressBar.setProgress(percent);
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            circleProgressBar.setProgress(0);
                        }
                    });
                }
            }
        }).start();
    }
}
