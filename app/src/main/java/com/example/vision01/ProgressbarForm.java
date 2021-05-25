package com.example.vision01;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dinuscxj.progressbar.CircleProgressBar;

public class ProgressbarForm extends AppCompatActivity implements CircleProgressBar.ProgressFormatter{

    private static final String DEFAULT_PATTERN = ".";

    public static CircleProgressBar circleProgressBar;

    public static ProgressbarForm test;

    Button button_complete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_progressbar);

        circleProgressBar=findViewById(R.id.circle_progressbar);

        button_complete = (Button) findViewById(R.id.button_complete);

        //찾기 완료 버튼을 클릭하면
        button_complete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        test = this;
    }

    void showDialog() {
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(ProgressbarForm.this)
                .setTitle("물건 찾기")
                .setMessage("물건을 찾으셨습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), DeviceListForm.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), FindForm.class);
                        startActivity(intent);
                    }
                })
                .setNeutralButton("취소", null);

        AlertDialog msgDlg = msgBuilder.create();
        msgDlg.show();
    }

    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }

    // %가 느리게올라가는거
    public void progress(double percent) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                FindForm.Mode=FindForm.CUR_MODE.PROGRESSING;

                //progress bar 올라가는 상황 (물건과 가까워지는 상황)
                if(percent < 0) {

                    double up = (percent/(0.17) * -1);

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

                    double down = (percent/(0.17));

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