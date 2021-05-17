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

    public void progress(int num) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                FindForm.Mode=FindForm.CUR_MODE.PROGRESSING;
                int tmp=num,alpha = 0 ,beta = 0;
                System.out.println("num"+num );
                //progress bar 올라가는 상황 (물건과 가까워지는 상황)

                if(tmp < 0) {
                    tmp *= -1; // 양수화
                    tmp = (int)(tmp/0.2);
                    if (tmp <= 10) {
                        alpha = tmp / 2;
                        beta = 0;
                    } else {
                        alpha = 5;
                        beta = (tmp - 10) / 5;
                    }

                    for(int i = (circleProgressBar.getProgress()-90)/2; i <= alpha;i++) {
                        if(circleProgressBar.getProgress()+2 > 10) {
                            circleProgressBar.setProgress(10);
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circleProgressBar.setProgress(circleProgressBar.getProgress()+2);
                            }
                        });
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for(int j = (circleProgressBar.getProgress()-10)/5; j <= beta ;j++) {
                        if(circleProgressBar.getProgress()+5 > 100) {
                            circleProgressBar.setProgress(100);
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circleProgressBar.setProgress(circleProgressBar.getProgress()+5);
                            }
                        });
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    tmp = (int)(tmp/0.2);
                    if (tmp <= 10) {
                        alpha = tmp / 2;
                        beta = 0;
                    } else {
                        alpha = 5;
                        beta = (tmp - 10) / 5;
                    }
                    int proalpha= (circleProgressBar.getProgress()-90)/2 - alpha;
                    int probeta=(circleProgressBar.getProgress()-10)/5 - beta;
                    for(int j = beta; j <= probeta ;j++) {
                        if(circleProgressBar.getProgress()-5 <10) {
                            circleProgressBar.setProgress(10);
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circleProgressBar.setProgress(circleProgressBar.getProgress()-5);
                            }
                        });
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    for(int i = alpha; i <= proalpha;i++) {
                        if(circleProgressBar.getProgress()-2 < 0) {
                            circleProgressBar.setProgress(0);
                            break;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                circleProgressBar.setProgress(circleProgressBar.getProgress()-2);
                            }
                        });
                        try {
                            Thread.sleep(50);
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
