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

//                int up = 0, down = 0; //a, b는 퍼센트의 값
//
//                up = (int)(percent/(0.1) * -1);
//                //up = up-circleProgressBar.getProgress();
//
//                //progress bar 올라가는 상황 (물건과 가까워지는 상황)
//                if(up > 0) {
//
//                    /*
//                    if(circleProgressBar.getProgress()+up>100) {
//                        circleProgressBar.setProgress(100);
//                        FindForm.Mode=FindForm.CUR_MODE.PROGRESS;
//                        return;
//                    }
//                    */
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

                switch(num) {
                    case 1:
                        for (int i = 0; i <= 5; i++) {

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
                        }
                        break;
                    case 2:
                        for (int i = 6; i <= 10; i++) {

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
                        }
                        break;

                    case 3:
                        for (int i = 11; i <= 18; i++) {

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
                        }
                        break;

                    case 4:
                        for (int i = 19; i <= 27; i++) {

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

//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    circleProgressBar.setProgress(0);
//                                }
//                            });
                        }
                        break;
                    case 5:
                        for (int i = 28; i <= 48; i++) {

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
                        }
                        break;
                    case 6:
                        for (int i = 49; i <= 70; i++) {

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
                        }
                        break;
                    case 7:
                        for (int i = 71; i <= 90; i++) {

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
                        }
                        break;
                    case 8:
                        for (int i = 91; i <= 100; i++) {

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
                        }
                        break;
                    case 9:
                        for (int i = 90; i <= 94; i++) {

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
                        }
                        break;
                    case 10:
                        for (int i = 95; i <= 100; i++) {

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
                        }
                        break;

                }



                FindForm.Mode=FindForm.CUR_MODE.PROGRESS;
            }
        }).start();
    }
}
