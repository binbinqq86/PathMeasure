package com.binbin.pathmeasure;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

/**
 * Created by -- on 2016/11/24.
 */

public class CircleView extends View {
    private static final String TAG="tianbin";

    private PathMeasure mPathMeasure;
    private Paint mPaint;
    private Path mPath;
    private float[] mCurrentPosition = new float[2];

    private int height=500,width=500;

    private ArrayList<Float> allLength=new ArrayList<>();
    private ValueAnimator valueAnimator;

    public CircleView(Context context) {
        this(context,null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4);
        mPaint.setColor(Color.RED);

        mPath = new Path();
//        mPath.addCircle(300,300,200, Path.Direction.CCW);


        float radius = width / 3;
        float radius2=width/6;
        float w1= (float) (width/2-radius/(Math.sqrt(2)));
        float w2= (float) (width/2+radius/(Math.sqrt(2)));
        float h= (float) (height/2-radius/(Math.sqrt(2)));

        /*绘制圆弧*/
        RectF oval = new RectF(width/2 - radius, height/2-radius, width/2
                + radius, height/2 + radius);  //用于定义的圆弧的形状和大小的界限
        mPath.addArc(oval,344,212);
       /*耳朵处的圆心坐标为：(w1,h),(w2.h)*/
        /*耳朵所在的矩形的范围为：w1-getWidth()/16;*/
        RectF oval2=new RectF(w1-radius2,h-radius2,w1+radius2,h+radius2);
        RectF ovale=new RectF(w2-radius2,h-radius2,w2+radius2,h+radius2);
        mPath.addArc(oval2,121,208);
        mPath.addArc(oval,254,32);
        mPath.addArc(ovale,211,208);

        mPathMeasure = new PathMeasure(mPath, false);
        mCurrentPosition = new float[2];

        do {
            allLength.add(mPathMeasure.getLength());
        }while (mPathMeasure.nextContour());
        resetPathMeasure();
    }

    public void resetPathMeasure(){
        mPathMeasure.setPath(mPath, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);

        //绘制对应目标（动画开启后才进行绘制）
        if(valueAnimator!=null){
            canvas.drawCircle(mCurrentPosition[0], mCurrentPosition[1], 10, mPaint);
        }
    }

    /**
     * 开启路径动画
     * @param verlocity 速度
     * @param isRepeat 是否循环
     */
    public void startPlay(final float verlocity,final boolean isRepeat){
        if(valueAnimator!=null){
            valueAnimator.cancel();
            resetPathMeasure();
        }
        startPathAnim(verlocity,isRepeat);
    }

    private void startPathAnim(final float verlocity,final boolean isRepeat) {
        final float length=mPathMeasure.getLength();
        final long duration= (long) (length/verlocity);
        // 0 － getLength()
        valueAnimator = ValueAnimator.ofFloat(0, length);
        Log.e(TAG, "measure length = " + length+"###"+duration);
        valueAnimator.setDuration(duration);
        // 减速插值器
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                Log.e(TAG, "value = " + value);
                // 获取当前点坐标封装到mCurrentPosition
                mPathMeasure.getPosTan(value, mCurrentPosition, null);
                postInvalidate();
                if(allLength.contains(value)){
                    if(mPathMeasure.nextContour()){
                        //不是最后一个
                        startPathAnim(verlocity,isRepeat);
                    }else{
                        //结束的时候
                        resetPathMeasure();
                        if(isRepeat){
                            startPathAnim(verlocity,isRepeat);
                        }
                    }
                }
            }
        });
        valueAnimator.start();

    }
}
