package com.example.vision01;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dinuscxj.progressbar.CircleProgressBar;

public class ProgressbarForm extends AppCompatActivity implements CircleProgressBar.ProgressFormatter{

    private static final String DEFAULT_PATTERN = "%d%%";

    public static CircleProgressBar circleProgressBar;

    public static ProgressbarForm test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_progressbar);

        circleProgressBar=findViewById(R.id.circle_progressbar);

        circleProgressBar.setProgress(0); //기본 0 설정
        circleProgressBar.setMax(100); //최대 설정

        test = this;

        //progress();
    }

    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }

    public void progress(int percent) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                FindForm.Mode=FindForm.CUR_MODE.PROGRESSING;

                //progress bar 올라가는 상황 (물건과 가까워지는 상황)
                if(percent < 0) {

                    int up = (int)(percent/(0.15) * -1);

                    if(circleProgressBar.getProgress() + up > 100) {
                        circleProgressBar.setProgress(100);
                        FindForm.Mode=FindForm.CUR_MODE.PROGRESS;
                        return;
                    }

                    for(int i = 0; i < up; i++) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circleProgressBar.setProgress(circleProgressBar.getProgress()+1);

                                if(circleProgressBar.getProgress() >= 100)
                                    circleProgressBar.setProgress(100);
                            }
                        });

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //progress bar 내려가는 상황 (물건과 멀어지는 상황)
                else if(percent > 0) {

                    int down = (int)(percent/(0.15));

                    if(circleProgressBar.getProgress() + down <= 0) {
                        circleProgressBar.setProgress(0);
                        FindForm.Mode=FindForm.CUR_MODE.PROGRESS;
                        return;
                    }

                    for(int i = 0; i < down; i++) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circleProgressBar.setProgress(circleProgressBar.getProgress()-1);

                                if(circleProgressBar.getProgress() <= 0)
                                    circleProgressBar.setProgress(0);
                            }
                        });

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                FindForm.Mode=FindForm.CUR_MODE.PROGRESS;
            }
        }).start();
    }
}

//                int up = 0, down = 0; //a, b는 퍼센트의 값
//
//                up = (int)(percent/(0.1) * -1);
//                //up = up-circleProgressBar.getProgress();
//
//                //progress bar 올라가는 상황 (물건과 가까워지는 상황)
//                if(up > 0) {
//
//                    for(int i = 0; i < up; i++) {
//                        final int progress = i;
//                        if(circleProgressBar.getProgress() == 100) break;
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                circleProgressBar.setProgress(circleProgressBar.getProgress()+1);
//                            }
//                        });
//
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//                //progress bar 내려가는 상황 (물건과 멀어지는 상황)
//                if(up < 0) {
//                    down = up * -1;
//
//                    //if(circleProgressBar.getProgress()-down<=0) {
//                    //    circleProgressBar.setProgress(0);
//                    //    FindForm.Mode=FindForm.CUR_MODE.PROGRESS;
//                    //    return;
//                    //}
//
//                    for(int i = 0; i < down; i++) {
//                        final int progress = i;
//                        if(circleProgressBar.getProgress() == 0) break;
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                circleProgressBar.setProgress(circleProgressBar.getProgress()-1);
//                            }
//                        });
//
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                int up = 0;
//
//                up = (int)(percent/(0.1) * -1);