package com.jesen.cod.jetpackvideo.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.libcommon.utils.PixUtils;

public class RecordView extends View implements View.OnLongClickListener, View.OnClickListener {

    private static final int PROGRESS_INTERVAL = 100;
    private final int radius;
    private final int progressWidth;
    private final int progressColor;
    private final int fillColor;
    private final int maxDuration;

    private final Paint mPaint;
    private final Paint progressPaint;
    private int progressMaxValue;
    private boolean isRecording = false;
    // 当前进度值
    private int curProgressValue;

    // 录制开始时间
    private long startRecordTime;
    private OnRecordListener mRecordListener;

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RecordView(Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, defStyleRes);
        radius = typedArray.getDimensionPixelOffset(R.styleable.RecordView_radius, 0);
        progressWidth = typedArray.getDimensionPixelOffset(R.styleable.RecordView_progress_width, PixUtils.dp2px(3));
        progressColor = typedArray.getColor(R.styleable.RecordView_progress_color, Color.BLUE);
        fillColor = typedArray.getColor(R.styleable.RecordView_fill_color, Color.WHITE);
        maxDuration = typedArray.getInteger(R.styleable.RecordView_duration, 60);

        setMaxDuration(maxDuration);

        typedArray.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(fillColor);
        mPaint.setStyle(Paint.Style.FILL);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressWidth);

        Handler handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                curProgressValue++;
                postInvalidate();
                if (curProgressValue <= progressMaxValue){
                    sendEmptyMessageDelayed(0, PROGRESS_INTERVAL);
                }else {
                    finishRecord();
                }
            }
        };

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    isRecording = true;
                    startRecordTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(0);
                }else if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    long now = System.currentTimeMillis();
                    if (now - startRecordTime > ViewConfiguration.getLongPressTimeout()){
                        finishRecord();
                    }
                    handler.removeCallbacksAndMessages(null);
                    isRecording = false;
                    startRecordTime = 0;
                    curProgressValue = 0;
                    postInvalidate();
                }
                return false;
            }
        });

        setOnClickListener(this::onClick);
        setOnLongClickListener(this::onLongClick);
    }

    private void finishRecord() {
        if (mRecordListener != null){
            mRecordListener.onFinish();
        }
    }

    private void setMaxDuration(int maxDuration) {
        this.progressMaxValue = maxDuration * 1000 / PROGRESS_INTERVAL;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (isRecording) {
            canvas.drawCircle(width / 2, height / 2, width / 2, mPaint);
            int left = 0;
            int top = 0;
            int right = width;
            int bottom = height;
            float sweepAngle = (curProgressValue / progressMaxValue) * 360;
            canvas.drawArc(left, top, right, bottom, -90, sweepAngle, false, progressPaint);
        } else {
            canvas.drawCircle(width / 2, height / 2, radius, mPaint);
        }
    }

    @Override
    public void onClick(View view) {
        if (mRecordListener != null){
            mRecordListener.onClick();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (mRecordListener != null){
            mRecordListener.onLongClick();
        }
        return true;
    }

    public void setOnRecordListener(OnRecordListener listener){
        mRecordListener = listener;
    }

    public interface OnRecordListener{
        void onClick();
        void onLongClick();
        void onFinish();
    }
}
