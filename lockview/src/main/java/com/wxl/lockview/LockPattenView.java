package com.wxl.lockview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LockPattenView extends ViewGroup {

    private static final String TAG = "LockPattenView";
    public static final int FIRST_CREATE_SUCCESS = 0;
    public static final int RE_CREATE_SUCCESS = 1;
    public static final int CREATE_FAILED = 2;
    public static final int UNLOCK_FAILED = 3;
    public static final int UNLOCK_SUCCESS = 4;
    public static final int DRAWING = 5;
    public static final int RE_CREATE_FAILED = 6;


    public static final int FAILURE_REASON_PASSWORD_SHORT = 0;
    public static final int FAILURE_REASON_RECREATE_INCORRECT = 1;


    public static final int TYPE_CREATE = 1;
    public static final int TYPE_UNLOCK = 2;

    public int lockPattenViewType;

    public int status;

    //此view的宽高
    public int mWidth,mHeight;
    //点集合即子view
    public List<PointView> mPointViews;
    //外部设置的子view的个数
    public int mChildrenCount;
    //外部设置的每一行的个数
    public int mCountPerLine;
    //默认点与点之间的间隙
    public float mDefaultSpace;
    //行数
    public int mRowCount;
    public float mCircleWidth;
    public int mRightColor;
    public int mWrongColor;
    public int mDefaultColor;

    private Paint mPaint;


    private float mTouchDownPointX;
    private float mTouchDownPointY;
    private float mTouchPointX;
    private float mTouchPointY;

    //已经划过的点集合
    private List<PointView> addPointViews;
    private Set<PointView> addPointViewsSet;

    private OnUnlockListener mOnUnlockListener;
    private OnCreateGesturePasswordListener mOnCreateGesturePasswordListener;

    private String password;

    public Context mContext;
    public LockPattenView(Context context) {
        this(context,null);
    }

    public LockPattenView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LockPattenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LockPattenView);
        mChildrenCount = typedArray.getInt(R.styleable.LockPattenView_pointCount,9);
        mCountPerLine = typedArray.getInt(R.styleable.LockPattenView_pointCountPerLine,3);
        mDefaultSpace = typedArray.getDimension(R.styleable.LockPattenView_pointSpace,Utils.dp2px(30));
        mCircleWidth = typedArray.getDimension(R.styleable.LockPattenView_circleWidth,Utils.dp2px(80));
        mRightColor = typedArray.getColor(R.styleable.LockPattenView_rightColor, Color.GREEN);
        mWrongColor = typedArray.getColor(R.styleable.LockPattenView_wrongColor, Color.RED);
        mDefaultColor = typedArray.getColor(R.styleable.LockPattenView_defaultColor, Color.GRAY);
        init();
        typedArray.recycle();


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LockPattenView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(){
        mPointViews = new ArrayList<>(mChildrenCount);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        addPointViewsSet = new HashSet<>(mChildrenCount);
        addPointViews = new ArrayList<>(mChildrenCount);
        PointView pointView = null;
        int index = 0;
        int[] colors = {mDefaultColor,mRightColor,mWrongColor};
        while (mPointViews.size() < mChildrenCount){
            pointView = new PointView(mContext);
            pointView.setIndex(index);
            pointView.setColors(colors);
            mPointViews.add(pointView);
            addView(pointView);
            index ++;
        }
        //防止在写布局文件的时候、人为再在当前ViewGroup中添加子view
        Log.d(TAG, "init: mChildrenCount is "+mChildrenCount);


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        Log.d(TAG, "onLayout:===optimize=== start time"+ System.currentTimeMillis());
        l = getPaddingLeft();
        t = getPaddingTop();
        int currentLine = 0;
        PointView pointView;
        for (int index = 0 ; index < mChildrenCount ; index ++){
            pointView = ((PointView) getChildAt(index));
            Log.d(TAG, "onLayout: pointView.getMeasuredWidth()="+pointView.getMeasuredWidth());
            Log.d(TAG, "onLayout: pointView.getWidth()="+pointView.getWidth());
            l = getPaddingLeft() + (index % mCountPerLine) * (pointView.getMeasuredWidth() + Utils.upInt(mDefaultSpace));

            t = getPaddingTop()+currentLine*(pointView.getMeasuredHeight()+ Utils.upInt(mDefaultSpace));
            Log.d(TAG, "onLayout: l="+l);
            Log.d(TAG, "onLayout: t="+t);
            pointView.layout(l,t,pointView.getMeasuredWidth() + l , pointView.getMeasuredHeight() +t);



            if (index % mCountPerLine == (mCountPerLine - 1)){//下一个要换行
                currentLine ++;
            }
        }


        Log.d(TAG, "onLayout:===optimize=== end time"+ System.currentTimeMillis());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG, "onMeasure:===optimize=== start time"+ System.currentTimeMillis());
        if (getChildCount()>mPointViews.size()){
            try {
                throw new Exception("this LockPattenView layout should not has extra child view");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mCountPerLine <= 0 || mChildrenCount <= 0 || getChildCount() == 0){
            setMeasuredDimension(0,0);
            return;
        }

//        measureChildren(widthMeasureSpec,heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            //设置每个子view的宽高
            setPerPointViewSize(mCircleWidth);
            measureChildren(widthMeasureSpec,heightMeasureSpec);
            //宽
            width = (mPointViews.get(0).getMeasuredWidth()) * mCountPerLine + Utils.upInt(mDefaultSpace)*(mCountPerLine - 1);
            width += getPaddingLeft() + getPaddingRight();
        }

        if (widthMode == MeasureSpec.EXACTLY) {//如果父控件宽高、mDefaultSpace有可能失效
            setPerPointViewSize(mCircleWidth);
            measureChildren(widthMeasureSpec,heightMeasureSpec);
            if (width - getPaddingLeft() - getPaddingRight() >= (mPointViews.get(0).getMeasuredWidth() * mCountPerLine + Utils.upInt(mDefaultSpace)*(mCountPerLine - 1))){
                width = (mPointViews.get(0).getMeasuredWidth() * mCountPerLine + Utils.upInt(mDefaultSpace)*(mCountPerLine - 1)) + getPaddingLeft()+getPaddingRight();
            }else {
                int canUseWidth = width - getPaddingRight() - getPaddingLeft();
                if (mPointViews.get(0).getMeasuredWidth() * mCountPerLine <= canUseWidth){
                    int spaceCanUseWidth = canUseWidth - mPointViews.get(0).getMeasuredWidth() * mCountPerLine;
                    mDefaultSpace = spaceCanUseWidth / (mCountPerLine - 1);
                }else {//子view的宽高将失效并且没有间隙即mDefaultSpace = 0;
                    mDefaultSpace = 0;
                    setPerPointViewSize(width / mCountPerLine);
                    measureChildren(widthMeasureSpec,heightMeasureSpec);
                }
            }
        }



        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED){
            //高
            mRowCount = mChildrenCount / mCountPerLine;
            if (mChildrenCount % mCountPerLine != 0){
                mRowCount += 1;
            }
            height = mPointViews.get(0).getMeasuredHeight() * mRowCount +Utils.upInt(mDefaultSpace) * (mRowCount - 1);
            height += getPaddingBottom()+getPaddingTop();
        }

        if (heightMode == MeasureSpec.EXACTLY){

            //高
            mRowCount = mChildrenCount / mCountPerLine;
            if (mChildrenCount % mCountPerLine != 0){
                mRowCount += 1;
            }
            int childrenViewHeight = mPointViews.get(0).getMeasuredHeight() * mRowCount +Utils.upInt(mDefaultSpace) * (mRowCount - 1);
            int canUseHeight = height - getPaddingTop() - getPaddingBottom();
            if (canUseHeight >= childrenViewHeight){
                height = childrenViewHeight + getPaddingTop()+getPaddingBottom();
            }else {//可使用的空间小于子view高度总和
                if (mPointViews.get(0).getMeasuredHeight() * mRowCount <= canUseHeight){//除去空隙子view高度和小于可用空间
                    int spaceCanUseHeight = canUseHeight - mPointViews.get(0).getMeasuredHeight() * mRowCount;
                    mDefaultSpace = spaceCanUseHeight / (mRowCount - 1);
                }else {//子view的宽高将失效并且没有间隙即mDefaultSpace = 0;
                    mDefaultSpace = 0;
                    setPerPointViewSize(height / mRowCount);
                    measureChildren(widthMeasureSpec,heightMeasureSpec);
                    //再根据子view的宽度重新计算宽度当前view的宽度
                    width = (mPointViews.get(0).getMeasuredWidth() * mCountPerLine + Utils.upInt(mDefaultSpace)*(mCountPerLine - 1)) + getPaddingLeft()+getPaddingRight();

                }
            }
        }
        Log.d(TAG, "onMeasure: rowCount="+mRowCount);
        Log.d(TAG, "onMeasure: width="+width);
        Log.d(TAG, "onMeasure: height="+height);
        mHeight = height;
        mWidth = width;
        Log.d(TAG, "onMeasure:===optimize=== end time"+ System.currentTimeMillis());
        setMeasuredDimension(width,height);
    }

    private void setPerPointViewSize(float size){
        LayoutParams lp = null;
        for (PointView pointView:mPointViews){
            lp = pointView.getLayoutParams();
            lp.width = lp.height = ((int) size);
            pointView.setLayoutParams(lp);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                reset();
                mTouchDownPointX = event.getX();
                mTouchDownPointY = event.getY();
                checkTouchDownPointHitPointView(mTouchDownPointX,mTouchDownPointY);
                status = DRAWING;
            case MotionEvent.ACTION_MOVE:
                mTouchPointX = event.getX();
                mTouchPointY = event.getY();
                checkTouchDownPointHitPointView(mTouchPointX, mTouchPointY);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                complete();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0 ; i < addPointViews.size() ; i ++) {
            mPaint.setStrokeWidth(Math.min(addPointViews.get(0).getmPointRadius(), 25));
            switch (status) {
                case DRAWING:
                    mPaint.setColor(mRightColor);
                    if (i == addPointViews.size() - 1) {
                        canvas.drawLine(addPointViews.get(i).getCircleCenterX(), addPointViews.get(i).getCircleCenterY(), mTouchPointX, mTouchPointY, mPaint);
                    }
                    break;
                case CREATE_FAILED:
                case RE_CREATE_FAILED:
                case UNLOCK_FAILED:
                    mPaint.setColor(mWrongColor);
                    addPointViews.get(i).setType(PointView.STATUS_WRONG);
                    break;
                case FIRST_CREATE_SUCCESS:
                case RE_CREATE_SUCCESS:
                case UNLOCK_SUCCESS:
                    mPaint.setColor(mRightColor);
                    addPointViews.get(i).setType(PointView.STATUS_RIGHT);

                    break;
            }
            if (i != addPointViews.size() - 1) {
                canvas.drawLine(addPointViews.get(i).getCircleCenterX(), addPointViews.get(i).getCircleCenterY(), addPointViews.get(i + 1).getCircleCenterX(), addPointViews.get(i + 1).getCircleCenterY(), mPaint);
            }
        }
    }



    private void checkTouchDownPointHitPointView(float downX, float downY){
        float rowHeight = mHeight / mRowCount;
        float nextRowHeight = 0;
        int pointDownLineIndex = 0;
        for (int i = 0 ; i < mRowCount ; i ++){//搜索触碰点在第几行
            nextRowHeight = (i+1)*rowHeight;
            if (nextRowHeight > mHeight){
                nextRowHeight = mHeight;
            }
            if (downY >i * rowHeight && downY < nextRowHeight){
                pointDownLineIndex = i;
                break;
            }
        }

        Log.d(TAG, "checkTouchDownPointHitPointView: pointDownLineIndex="+pointDownLineIndex);

        float columnWidth = mWidth / mCountPerLine;
        float nextColumnWidth = 0;
        int pointDownColumnIndex = 0;
        for (int pointViewIndex = 0 ; pointViewIndex < mCountPerLine ; pointViewIndex ++){
            nextColumnWidth = (pointViewIndex + 1) * columnWidth;
            if (nextColumnWidth > mWidth){
                nextColumnWidth = mWidth;
            }
            if (downX > pointViewIndex * columnWidth && downX < nextColumnWidth){
                pointDownColumnIndex = pointViewIndex;
                break;
            }
        }

        Log.d(TAG, "checkTouchDownPointHitPointView: pointDownColumnIndex"+pointDownColumnIndex);


        //以上为找到触摸点位于整个九宫格的第几格，获取当前格的pointView对象
        int index = pointDownLineIndex * mCountPerLine + pointDownColumnIndex;
        if (index >= mPointViews.size()){
            return;
        }
        PointView pointView = mPointViews.get(index);
        //获取当前点的半径
        float radius = pointView.getWidth() / 2;
        //获取圆心点的坐标
        float circleHeartPointX = pointView.getLeft() + radius;
        float circleHeartPointY = pointView.getTop() + radius;

        if (downX < pointView.getRight() && downX > pointView.getLeft() && downY < pointView.getBottom() && downY > pointView.getTop()){
            pointView.setType(PointView.STATUS_RIGHT);
            pointView.setCircleCenterX(circleHeartPointX);
            pointView.setCircleCenterY(circleHeartPointY);
            if (addPointViewsSet.add(pointView)){
                addPointViews.add(pointView);
            }
        }
    }

    private void complete(){
        StringBuilder sb = new StringBuilder();
        for (PointView p : addPointViews){
            sb.append(p.getIndex());
        }


        switch (lockPattenViewType) {
            case TYPE_CREATE:
                createPassword(sb.toString());
                invalidate();
                break;
            case TYPE_UNLOCK:
                unlock(sb.toString());
                invalidate();
                break;
            default:
                try {
                    throw new Exception("you should call method setLockPattenViewType() to indicate this view is create password or unlock");
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }


    private void createPassword(String pwd){
        if (addPointViews.size() < 4){
            status = CREATE_FAILED;
            if (mOnCreateGesturePasswordListener != null){
                mOnCreateGesturePasswordListener.onCreateFailed(FAILURE_REASON_PASSWORD_SHORT);
            }
            return;
        }else if (TextUtils.isEmpty(password)){//第一次
            status = FIRST_CREATE_SUCCESS;
            if (mOnCreateGesturePasswordListener != null){
                mOnCreateGesturePasswordListener.onFirstCreateSuccess(pwd);
            }
            password = pwd;
            return;
        }else if (!TextUtils.isEmpty(password) && password.equals(pwd)){//第二次创建
            status = RE_CREATE_SUCCESS;
            if (mOnCreateGesturePasswordListener != null){
                mOnCreateGesturePasswordListener.onSecondCreateSuccess(pwd);
            }
            return;
        }else if (!TextUtils.isEmpty(password) && !password.equals(pwd)){//与第一次不一致
            status = RE_CREATE_FAILED;
            if (mOnCreateGesturePasswordListener != null){
                mOnCreateGesturePasswordListener.onCreateFailed(FAILURE_REASON_RECREATE_INCORRECT);
            }
            return;
        }
    }

    private void unlock(String pwd){
        if (addPointViews.size() < 4){
            status = UNLOCK_FAILED;
            if (mOnUnlockListener != null){
                mOnUnlockListener.onError(FAILURE_REASON_PASSWORD_SHORT);
            }
            return;
        }else{
            status = UNLOCK_SUCCESS;
            if (mOnUnlockListener != null){
                mOnUnlockListener.onUnlock(pwd);
            }
            return;
        }
    }

    public void reset(){
        for (PointView p:addPointViews){
            p.setType(PointView.STATUS_DEFAULT);
        }
        addPointViews.clear();
        addPointViewsSet.clear();
    }

    public void setOnCreateGesturePasswordListener(OnCreateGesturePasswordListener onCreateGesturePasswordListener){
        this.mOnCreateGesturePasswordListener = onCreateGesturePasswordListener;
    }

    public void setOnUnlockListener(OnUnlockListener onUnlockListener){
        this.mOnUnlockListener = onUnlockListener;
    }

    public LockPattenView setLockPattenViewType(int lockPattenViewType) {
        this.lockPattenViewType = lockPattenViewType;
        return this;
    }
}
