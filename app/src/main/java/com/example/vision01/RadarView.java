package com.example.vision01;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;


public class RadarView extends View {
    private final int POINT_ARRAY_SIZE = 40;

    private int fps = 2000;
    private boolean showCircles = true;
    public static RadarView radarView;
    float alpha = 0;
    float target_alpha=90;
    float r1 = 0;
    Point latestPoint[] = new Point[POINT_ARRAY_SIZE];
    Paint latestPaint[] = new Paint[POINT_ARRAY_SIZE];
    Paint paintBackgroundCircle, paintCircle;

    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //돌아가는 원
        Paint localPaint = new Paint();
        localPaint.setColor(Color.WHITE);
        localPaint.setAntiAlias(true);
        localPaint.setStyle(Paint.Style.FILL);
        localPaint.setStrokeWidth(3.0F);
        //localPaint.setAlpha(0); //투명도관련


        //배경화면의 원
        Paint local2Paint = new Paint();
        local2Paint.setColor(Color.WHITE);
        local2Paint.setAntiAlias(true);
        local2Paint.setStrokeWidth(3.0F);
        local2Paint.setStyle(Paint.Style.STROKE);

        int alpha_step = 255 / POINT_ARRAY_SIZE;

        for (int i=0; i < latestPaint.length; i++) {
            latestPaint[i] = new Paint(localPaint);
            latestPaint[i].setAlpha(255 - (alpha_step));  //i*alpha_step : 점점 색깔 옅어지는 꼬리 역할
        }

        local2Paint.setAlpha(200 - (alpha_step));
        paintBackgroundCircle = local2Paint;
        paintCircle = localPaint;
        radarView = this;
    }

    android.os.Handler mHandler = new android.os.Handler();
    Runnable mTick = new Runnable() {
        @Override
        public void run() {
            invalidate();
            mHandler.postDelayed(mTick, 1000 / fps);
        }
    };

    public void startAnimation() {
        mHandler.removeCallbacks(mTick);
        mHandler.post(mTick);
    }

    public void stopAnimation() {
        mHandler.removeCallbacks(mTick);
    }

    public void setFrameRate(int fps) { this.fps = fps; }
    public int getFrameRate() { return this.fps; };

    public void setShowCircles(boolean showCircles) { this.showCircles = showCircles; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int r = Math.min(width, height);
        int i = r / 2;
        int j = i - 40;
        Paint localPaint = latestPaint[0]; // GREEN
        Paint pp = paintBackgroundCircle;

        if (showCircles) {
            canvas.drawCircle(i, i, j, pp);
            //canvas.drawCircle(i, i, j, localPaint);
            canvas.drawCircle(i, i, j * 3 / 4, pp);
            //canvas.drawCircle(i, i, j >> 1, localPaint);
            //canvas.drawCircle(i, i, j >> 2, localPaint);
        }
        /*
        alpha -= 2;
        if (alpha < -360) alpha = 0;
        double angle = Math.toRadians(alpha);
        int offsetX =  (int) (i + (float)((i-40) * Math.cos(angle)));
        int offsetY = (int) (i - (float)((i-40) * Math.sin(angle)));
         */

        float alpha_tmp=alpha;
        boolean checkFirst = true;
        //x y 좌표를 기준으로 위로 원을그리고 아래로 원을그린다. 이때 가운데 x y 좌표로 다시 찾아가기 위한 값

        if(alpha < 0) alpha = 360-2;
        if(alpha >= 360) alpha = 0;
        float a;
        a=target_alpha - alpha;

        if(-180 <= a && a < 0 ){ //시계방향
            alpha -= 2;
        }
        else if(180 <= a && a <360){
            alpha -= 2;
        }
        else if( a==0){
            //아무것도 하지않음
        }
        else if(-360 < a && a < -180){  //반시계방향
            alpha += 2;
        }
        else if(0 < a && a < 180){
            alpha += 2;
        }

        for(int x=0; x<POINT_ARRAY_SIZE; x++){
            double angle = Math.toRadians(alpha_tmp);
            int offsetX =  (int) (i + (float)((i-40) * Math.cos(angle)));
            int offsetY = (int) (i - (float)((i-40) * Math.sin(angle)));
            latestPoint[x]= new Point(offsetX, offsetY);
            if(x > POINT_ARRAY_SIZE/2){
                if(checkFirst) {
                    alpha_tmp=alpha;
                    checkFirst=false;
                }
                alpha_tmp -= 2;
            }else {
                alpha_tmp += 2;
            }
            //latestPoint[0] 가 중심
        }

        for (int x = 0; x < POINT_ARRAY_SIZE; x++) {
            Point point = latestPoint[x];
            if (point != null) {
                canvas.drawCircle(point.x, point.y, 40, paintCircle);
            }
        }

    }
}