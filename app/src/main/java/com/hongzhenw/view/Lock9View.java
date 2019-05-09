package com.hongzhenw.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.hongzhenw.androidlock9view.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 2019/5/6
 */
public class Lock9View extends View {

    private final String aTag = Lock9View.class.getSimpleName();

    // 测量相关
    private int mMeasuredWidth;                         // 测量宽度
    private int mMeasuredHeight;                        // 测量高度
    // 宽高相关
    private int mCirleRadius;                           // 圆半径
    private int mCircleMaxWidth;                        // 每个圆圈最大宽度
    // 位置相关
    private int mStartX;                                // 屏幕中心X
    private int mStartY;                                // 屏幕中心Y
    // 数据相关
    private List<CircleBean> mCircleViews;              // 每个大圆的集合
    private List<CircleBean> mPassword;                 // 密码
    // 绘制相关
    private Paint mPaint;                               // 每个大圆大画笔
    private Paint mLinePaint;                           // 画线轨迹的画笔
    private Path mPath;                                 // 不连接圆的轨迹
    private Path mPathTemp;                             // 连接圆之间的轨迹
    // 状态常量
    private final int STATUS_DEFAULT = 0;                // 未选中
    private final int STATUS_FAILED = 1;                // 失败
    private final int STATUS_SUCCESS = 2;               // 成功
    private final int STATUS_SELECTED = 3;              // 已选中
    // 标记相关
    private boolean isMove;                             // 是否已触摸
    private int mPassWordLength;                        // 密码长度
    // 颜色相关
    private int mCircleColorFailed;                     // 圆圈&连接线失败颜色
    private int mCircleColorSuccess;                    // 圆圈&连接线成功颜色
    private int mCircleColorDefault;                    // 圆圈默认颜色
    private int mCircleColorSelected;                   // 圆圈&连接线选中颜色
    // 回调相关
    private ILock9ViewListener mListener;               // 手势结果回调

    {
        mPath = new Path();
        mPathTemp = new Path();
        mPassword = new ArrayList<>();
        mCircleViews = new ArrayList<>();
    }

    public Lock9View(Context context) {
        this(context, null);
    }

    public Lock9View(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Lock9View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    /**
     * 初始化XML配置资源
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Lock9View, defStyleAttr, 0);
        // 圆圈&连接线失败颜色
        mCircleColorFailed = array.getColor(R.styleable.Lock9View_circleColorFailed, Color.RED);
        // 圆圈&连接线成功颜色
        mCircleColorSuccess = array.getColor(R.styleable.Lock9View_circleColorSuccess, Color.GREEN);
        // 圆圈默认颜色
        mCircleColorDefault = array.getColor(R.styleable.Lock9View_circleColorDefault, Color.GRAY);
        // 圆圈&连接线选中颜色
        mCircleColorSelected = array.getColor(R.styleable.Lock9View_circleColorSelected, Color.GRAY);
        // 连接的宽度
        int lineWidth = (int) array.getDimension(R.styleable.Lock9View_lineWidth, dp2Px(1));
        // 密码长度
        mPassWordLength = array.getInteger(R.styleable.Lock9View_passwordLength, 4);
        // 圆圈的宽度
        int circleLineWidth = (int) array.getDimension(R.styleable.Lock9View_circleLineWidth, dp2Px(5));
        // 画笔设置
        mPaint = getPaint(lineWidth, mCircleColorDefault);
        mLinePaint = getPaint(circleLineWidth, mCircleColorDefault);
        array.recycle();
    }

    /**
     * 九宫格监听事件
     */
    public void setLock9ViewListener(ILock9ViewListener listener) {
        this.mListener = listener;
    }

    /**
     * 重置所有View
     */
    public void reset() {
        mPath.reset();
        mPathTemp.reset();
        mPassword.clear();
        mPaint.setColor(getPaintColor(STATUS_DEFAULT));
        mLinePaint.setColor(getPaintColor(STATUS_DEFAULT));
        for (int i = 0; i < mCircleViews.size(); i++) {
            CircleBean bean = mCircleViews.get(i);
            bean.status = STATUS_DEFAULT;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 测量的宽高
        mMeasuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        mMeasuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        // 每个圆圈最大宽度
        if (mMeasuredWidth < mMeasuredHeight) {
            mCircleMaxWidth = mMeasuredWidth / 3;
            mCirleRadius = mCircleMaxWidth / 4;

            mStartY = mMeasuredHeight / 2 - mCircleMaxWidth / 2 * 3;
            mStartX = 0;
        } else {
            mCircleMaxWidth = mMeasuredHeight / 3;
            mCirleRadius = mCircleMaxWidth / 4;

            mStartX = mMeasuredWidth / 2 - mCircleMaxWidth / 2 * 3;
            mStartY = 0;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            int row = i % 3;
            int column = i / 3;
            int x = mStartX + row * mCircleMaxWidth + mCircleMaxWidth / 2;
            int y = mStartY + column * mCircleMaxWidth + mCircleMaxWidth / 2;
            mCircleViews.add(new CircleBean(x, y, i, STATUS_DEFAULT));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mCircleViews.size(); i++) {
            drawCirle(canvas, mCircleViews.get(i));
        }
        canvas.drawPath(mPath, mLinePaint);
    }

    /**
     * 画圆
     */
    private void drawCirle(Canvas canvas, CircleBean bean) {
        // 画笔样式
        if (bean.status != STATUS_DEFAULT) {
            mPaint.setStyle(Paint.Style.FILL);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
        }
        // 画笔颜色
        mPaint.setColor(getPaintColor(bean.status));
        // 大圆
        canvas.drawCircle(bean.x, bean.y, mCirleRadius, mPaint);
        // 小圆
        canvas.drawCircle(bean.x, bean.y, mCirleRadius / 3, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int currX = (int) event.getX();
        int currY = (int) event.getY();
        CircleBean bean = isCircleRect(currX, currY);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (bean != null) {
                    reset();
                    mPassword.add(bean);
                    bean.status = STATUS_SELECTED;
                    mPathTemp.moveTo(bean.x, bean.y);
                    isMove = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMove) {
                    mPath.reset();
                    mPath.addPath(mPathTemp);
                    mPath.lineTo(currX, currY);
                    if (bean != null) {
                        mPassword.add(bean);
                        bean.status = STATUS_SELECTED;
                        mPathTemp.lineTo(bean.x, bean.y);
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isMove = false;
                mPath.reset();
                mPath.addPath(mPathTemp);
                if (mListener != null && mPassword.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mPassword.size(); i++) {
                        sb.append(mPassword.get(i).i);
                    }
                    // 正在设置密码
                    if (mListener.isSettingPassWord()) {
                        if (sb.length() < mPassWordLength) {
                            resetColor(STATUS_FAILED);
                            mListener.onFailed(sb.length());
                        } else {
                            resetColor(STATUS_SUCCESS);
                            mListener.onSuccess(sb.toString());
                        }
                    }
                    // 对比密码
                    else if (mListener.comparePassWord(sb.toString())) {
                        // 成功
                        resetColor(STATUS_SUCCESS);
                        mListener.onSuccess(sb.toString());
                    } else {
                        // 失败
                        resetColor(STATUS_FAILED);
                        mListener.onFailed(sb.length());
                    }
                    mPassword.clear();
                }
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 获取画笔颜色
     *
     * @param status 触摸的不通状态
     * @return 在触摸的不同状态下的画笔颜色
     */
    private int getPaintColor(int status) {
        int color = mCircleColorDefault;
        switch (status) {
            case STATUS_DEFAULT:
                color = mCircleColorDefault;
                break;
            case STATUS_FAILED:
                color = mCircleColorFailed;
                break;
            case STATUS_SUCCESS:
                color = mCircleColorSuccess;
                break;
            case STATUS_SELECTED:
                color = mCircleColorSelected;
                break;
            default:
                break;
        }
        return color;
    }

    /**
     * 获取画笔
     *
     * @param width 获取绘制画笔
     * @return 根据线宽、颜色，返回画笔
     */
    private Paint getPaint(int width, int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    /**
     * 重置画笔、线条轨迹颜色
     *
     * @param status 触摸的不通状态
     */
    private void resetColor(int status) {
        mPaint.setColor(getPaintColor(status));
        mLinePaint.setColor(getPaintColor(status));
        for (int i = 0; i < mPassword.size(); i++) {
            mPassword.get(i).status = status;
        }
    }

    /**
     * 是否在圆圈范围内
     *
     * @param x 触摸的X坐标
     * @param y 触摸的Y坐标
     */
    private CircleBean isCircleRect(int x, int y) {
        for (int i = 0; i < mCircleViews.size(); i++) {
            CircleBean bean = mCircleViews.get(i);
            if ((x - bean.x) * (x - bean.x) + (y - bean.y) * (y - bean.y) <= mCirleRadius * mCirleRadius) {
                if (bean.status != STATUS_SELECTED) {
                    return bean;
                }
            }
        }
        return null;
    }

    /**
     * DP 转 PX
     *
     * @param dp DP值
     * @return DP转成PX的值
     */
    private int dp2Px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    interface ILock9ViewListener {
        // 正在设置密码
        boolean isSettingPassWord();

        // 失败
        void onFailed(int passwordLength);

        // 成功
        void onSuccess(String password);

        // 对比密码
        boolean comparePassWord(String password);
    }


    private class CircleBean {

        private int x;
        private int y;
        private int i;
        private int status;

        public CircleBean(int x, int y, int i, int status) {
            this.x = x;
            this.y = y;
            this.i = i;
            this.status = status;
        }
    }
}
