package com.wxl.lockview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class PointView extends View {
    private static final String TAG = "PointView";

    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_RIGHT = 1;
    public static final int STATUS_WRONG = 2;

    private int[] colors;
    private int type;
    private int mPointRadius;
    private int mCircleRadius;
    private Paint mPaint;
    private int mPathWidth;

    private int mWidth;

    private float circleCenterX;
    private float circleCenterY;

    private int index;

    public PointView(Context context) {
        this(context,null);
    }

    public PointView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PointView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        type = STATUS_DEFAULT;
        colors = new int[3];
        colors[0] = Color.GRAY;
        colors[1] = Color.GREEN;
        colors[2] = Color.RED;

        mWidth = 200;
        mPointRadius = 30;
        mPathWidth = 2;


        mPaint = new Paint();
        mPaint.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //由于当前view是由父view 创建、当前view完全有width属性控制匡高
        if (width == 0){
            width = mWidth;
        }else {
            mWidth = width;
            height = width;
        }
        mCircleRadius = mWidth / 2;
        mPointRadius = mCircleRadius * 2 / 5;//固定的圆点的半径比圆环半径小30;
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(colors[type]);
        //画小点
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mWidth / 2 ,mWidth / 2 , mPointRadius,mPaint);
        if (type != STATUS_DEFAULT) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2);
            canvas.drawCircle(mWidth / 2, mWidth / 2, mCircleRadius-4, mPaint);
        }
    }

    public void setType(int type){
        this.type = type;
        invalidate();
    }

    public void setColors(int[] colors){
        this.colors = colors;
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
//                setType(STATUS_RIGHT);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setCircleCenterX(float circleCenterX) {
        this.circleCenterX = circleCenterX;
    }

    public void setCircleCenterY(float circleCenterY) {
        this.circleCenterY = circleCenterY;
    }

    public float getCircleCenterX() {
        return circleCenterX;
    }

    public float getCircleCenterY() {
        return circleCenterY;
    }

    public int getmWidth() {
        return mWidth;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getmPointRadius() {
        return mPointRadius;
    }

    public void setmPointRadius(int mPointRadius) {
        this.mPointRadius = mPointRadius;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
