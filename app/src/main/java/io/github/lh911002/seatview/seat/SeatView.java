package io.github.lh911002.seatview.seat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import io.github.lh911002.seatview.R;


/**
 * Created by athrun on 16/5/28.
 */
public class SeatView extends View {


    private SeatImages mSeatImages = null;
    private SeatViewConfig mSeatViewConfig = null;


    private Paint commonPaint = new Paint();
    private Paint rowNumPaint = new Paint();
    private Paint screenPaint = new Paint();
    private Paint leftBarBgPaint = new Paint();
    private Paint centerTextPaint = new Paint();
    private Paint centerLinePaint = new Paint();
    private Paint thumbSeatViewPaint = new Paint();

    private float scaleCenterX = Float.MAX_VALUE;
    private float scaleCenterY = Float.MAX_VALUE;
    private double lastFingerDistance = Integer.MAX_VALUE;
    private boolean fingerOnScreen = false;

    private ISeatListener seatClickListener;
    private String mScreenName = "";
    private HashMap<String, Seat> submitSeats = new HashMap<>();


    public SeatView(Context context) {
        super(context);
    }

    public SeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void initSeatView(final String screenName, final SeatImages seatImages, final List<SeatRow> seatMap) {

        ViewTreeObserver vto = getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mScreenName = screenName;
                mSeatImages = seatImages;
                mSeatViewConfig = new SeatViewConfig(getContext(), seatMap, getMeasuredHeight(),
                        getMeasuredWidth());
                initPaint();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }


    private void initPaint() {
        commonPaint.setAntiAlias(true);
        screenPaint.setColor(Color.parseColor("#E5E5E5"));
        screenPaint.setStyle(Paint.Style.FILL);
        screenPaint.setAntiAlias(true);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(12);
        screenPaint.setPathEffect(cornerPathEffect);

        leftBarBgPaint.setColor(Color.parseColor("#9F9F9F"));
        leftBarBgPaint.setStyle(Paint.Style.FILL);
        leftBarBgPaint.setAntiAlias(true);
        leftBarBgPaint.setAlpha(200);

        rowNumPaint.setColor(Color.WHITE);
        rowNumPaint.setTextAlign(Paint.Align.CENTER);
        rowNumPaint.setAntiAlias(true);
        rowNumPaint.setTextSize(mSeatViewConfig.barTextSize);


        centerTextPaint.setColor(Color.parseColor("#202020"));
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPaint.setAntiAlias(true);
        centerTextPaint.setTextSize(mSeatViewConfig.centerTextSize);


        centerLinePaint.setStyle(Paint.Style.STROKE);
        centerLinePaint.setStrokeWidth(0.8f);
        PathEffect effects = new DashPathEffect(new float[]{5, 5, 5, 5}, 1f);
        centerLinePaint.setPathEffect(effects);
        centerLinePaint.setAntiAlias(true);
        centerLinePaint.setColor(getResources().getColor(R.color.seat_view_line));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSeatViewConfig == null || mSeatViewConfig.rowCount == 0) return;
        canvas.drawColor(getResources().getColor(R.color.seat_view_bg));
        drawSeat(canvas);
        drawScreen(canvas);
        drawCenterLine(canvas);
        drawLeftBar(canvas);
        drawThumbSeatView(canvas);
    }


    private void drawSeat(Canvas canvas) {
        for (int rowIndex = 0; rowIndex < mSeatViewConfig.rowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < mSeatViewConfig.columnCount; columnIndex++) {
                RectF seatRectF = mSeatViewConfig.getSeatRect(rowIndex, columnIndex);
                /*优化重绘效率*/
                if (seatRectF.right < mSeatViewConfig.windowRectF.left || seatRectF.left >
                        mSeatViewConfig.windowRectF.right || seatRectF.top > mSeatViewConfig
                        .windowRectF.bottom || seatRectF.bottom < mSeatViewConfig.windowRectF.top)
                    continue;

                Seat seatBean = mSeatViewConfig.seatArray[rowIndex][columnIndex];
                int seatStatus = seatBean.status;
                Bitmap drawBitmap = null;
                switch (seatStatus) {
                    case Seat.STATUS.SELECTABLE://可选
                        drawBitmap = mSeatImages.bgSeatAvail;
                        break;
                    case Seat.STATUS.SELECTED://已选
                        drawBitmap = mSeatImages.bgSeatSelected;
                        break;
                    case Seat.STATUS.UNSELECTABLE://已售
                        drawBitmap = mSeatImages.bgSeatLocked;
                        break;
                }
                if (drawBitmap != null) canvas.drawBitmap(drawBitmap, null, seatRectF, commonPaint);
            }
        }
    }

    private void drawLeftBar(Canvas canvas) {
        /*draw bg*/
        canvas.drawRoundRect(mSeatViewConfig.getLeftBarRect(), mSeatViewConfig.barWidth / 2,
                mSeatViewConfig.barWidth / 2, leftBarBgPaint);
        /*draw num*/
        for (int rowIndex = 0; rowIndex < mSeatViewConfig.rowCount; rowIndex++) {
            RectF rowNumRect = mSeatViewConfig.getRowNumRect(rowIndex);
            String rowNum = mSeatViewConfig.rowNames[rowIndex];
            if (!TextUtils.isEmpty(rowNum))
                canvas.drawText(rowNum, rowNumRect.centerX(), rowNumRect.centerY(), rowNumPaint);
        }
    }

    private void drawScreen(Canvas canvas) {

        canvas.drawPath(mSeatViewConfig.getScreenPath(), screenPaint);
    }

    private void drawCenterLine(Canvas canvas) {
        Path linePath = mSeatViewConfig.getCenterLinePath();
        canvas.drawPath(linePath, centerLinePaint);
        canvas.drawText(mScreenName + "  银幕", mSeatViewConfig.getScreenCenterX(), mSeatViewConfig
                .screenHeight / 2 + centerTextPaint.getTextSize() / 2 - 4, centerTextPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        restStatusByAction(event);
        if (event.getPointerCount() == 1) {
            gestureDetector.onTouchEvent(event);
        } else {
            float deltaX = event.getX(0) - event.getX(1);
            float deltaY = event.getY(0) - event.getY(1);
            double fingerDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (lastFingerDistance != Integer.MAX_VALUE && scaleCenterX != Float.MAX_VALUE) {
                double newSeatWidth = mSeatViewConfig.seatWidth + (fingerDistance -
                        lastFingerDistance) / 6d;
                mSeatViewConfig.setSeatWidth(new BigDecimal(newSeatWidth).floatValue(),
                        scaleCenterX, scaleCenterY);
                invalidate();
            }
            lastFingerDistance = fingerDistance;
        }
        return true;
    }


    private Runnable hideThumbViewRunnable = new Runnable() {
        @Override
        public void run() {
            if (!fingerOnScreen) invalidate();
        }
    };

    private void restStatusByAction(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            fingerOnScreen = true;
            removeCallbacks(hideThumbViewRunnable);
        } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
            if (scaleCenterX == Float.MAX_VALUE) {
                scaleCenterX = event.getX(0) / 2 + event.getX(1) / 2;
                scaleCenterY = event.getY(0) / 2 + event.getY(1) / 2;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            fingerOnScreen = false;
            lastFingerDistance = Integer.MAX_VALUE;
            postDelayed(hideThumbViewRunnable, 1000);
        } else if (action == MotionEvent.ACTION_POINTER_UP) {
            scaleCenterX = Float.MAX_VALUE;
            scaleCenterY = Float.MAX_VALUE;
        }
    }

    private GestureDetector gestureDetector = new GestureDetector(getContext(), new
            GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mSeatViewConfig != null) {
                mSeatViewConfig.moveSeatView((int) distanceX, (int) distanceY);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            int[] clickedPosition = mSeatViewConfig.getClickedSeat(event.getX(), event.getY());
            if (clickedPosition != null && lastFingerDistance == Integer.MAX_VALUE) {
                int rowIndex = clickedPosition[0];
                int columnIndex = clickedPosition[1];
                Seat clickedSeat = mSeatViewConfig.seatArray[rowIndex][columnIndex];
                if (clickedSeat != null) {
                    checkOrUnCheckSeat(clickedSeat);
                }
                invalidate();
            } else {
                // click blank area
            }


            if (mSeatViewConfig.seatWidth < mSeatViewConfig.seatMaxWidth && lastFingerDistance ==
                    Integer.MAX_VALUE) {
                final float x = event.getX();
                final float y = event.getY();

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        float newSeatWidth = mSeatViewConfig.seatWidth + 8;
                        newSeatWidth = Math.min(newSeatWidth, mSeatViewConfig.seatMaxWidth);

                        mSeatViewConfig.setSeatWidth(newSeatWidth, x, y);
                        invalidate();
                        if (newSeatWidth >= mSeatViewConfig.seatMaxWidth) return;
                        postDelayed(this, 10);
                    }
                }, 20);

            }
            return super.onSingleTapUp(event);
        }
    });

    /**
     * 选座或者取消选座
     *
     * @param clickedSeat
     * @return 是否进行过操作
     */
    private boolean checkOrUnCheckSeat(Seat clickedSeat) {
        if (clickedSeat == null) return false;
        if (clickedSeat.status == Seat.STATUS.SELECTABLE) {
            clickedSeat.status = Seat.STATUS.SELECTED;
            submitSeats.put(clickedSeat.id, clickedSeat);
            if (seatClickListener != null) {
                seatClickListener.lockSeat(clickedSeat);
            }
        } else if (clickedSeat.status == Seat.STATUS.SELECTED) {
            clickedSeat.status = Seat.STATUS.SELECTABLE;
            submitSeats.remove(clickedSeat.id);
            if (seatClickListener != null) {
                seatClickListener.releaseSeat(clickedSeat);
            }
            return true;
        }
        return false;
    }


    public ISeatListener getSeatClickListener() {
        return seatClickListener;
    }

    public void setSeatClickListener(ISeatListener seatClickListener) {
        this.seatClickListener = seatClickListener;
    }

    private RectF getThumbSeatRect(float seatWidth, float seatHeight, float seatGapInLine, float
            seatGapNewLine, float padding, int rowIndex, int columnIndex) {
        float top = padding + rowIndex * (seatHeight + seatGapNewLine);
        float bottom = top + seatHeight;
        float left = padding + columnIndex * (seatWidth + seatGapInLine);
        float right = left + seatWidth;
        return new RectF(left, top, right, bottom);
    }

    private RectF getVisibleThumbRect(float thumbWidth, float thumbHeight) {
        float left = -mSeatViewConfig.xOffset / mSeatViewConfig.virtualWidth * thumbWidth;
        left = Math.max(left, 0);
        float top = -mSeatViewConfig.yOffset / mSeatViewConfig.virtualHeight * thumbHeight;
        top = Math.max(top, 0);

        float height = thumbHeight * mSeatViewConfig.windowHeight / mSeatViewConfig.virtualHeight;
        height = Math.min(height, thumbHeight - top);
        float width = thumbWidth * mSeatViewConfig.windowWidth / mSeatViewConfig.virtualWidth;
        width = Math.min(width, thumbWidth - left);

        float bottom = top + height;
        float right = left + width;

        return new RectF(left, top, right, bottom);
    }

    private void drawThumbSeatView(Canvas canvas) {
        if (!fingerOnScreen) return;
        if (mSeatViewConfig.THUMB_HEIGHT <= 0 || mSeatViewConfig.THUMB_WIDTH <= 0) {
            return;
        }

        thumbSeatViewPaint.setColor(Color.parseColor("#B0000000"));
        thumbSeatViewPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, mSeatViewConfig.THUMB_WIDTH, mSeatViewConfig.THUMB_HEIGHT,
                thumbSeatViewPaint);

        for (int rowIndex = 0; rowIndex < mSeatViewConfig.rowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < mSeatViewConfig.columnCount; columnIndex++) {
                Seat seatBean = mSeatViewConfig.seatArray[rowIndex][columnIndex];
                int seatStatus = seatBean.status;
                Bitmap drawBitmap = null;
                switch (seatStatus) {
                    case Seat.STATUS.SELECTABLE:
                        drawBitmap = mSeatImages.bgSeatAvail;

                        break;
                    case Seat.STATUS.SELECTED:
                        drawBitmap = mSeatImages.bgSeatSelected;
                        break;
                    case Seat.STATUS.UNSELECTABLE:
                        drawBitmap = mSeatImages.bgSeatLocked;
                        break;
                }
                if (drawBitmap != null)
                    canvas.drawBitmap(drawBitmap, null, getThumbSeatRect(mSeatViewConfig
                            .THUMB_SEAT_WIDTH, mSeatViewConfig.THUMB_SEAT_HEIGHT, mSeatViewConfig
                            .THUMB_GAP_INLINE, mSeatViewConfig.THUMB_GAP_NEWLINE, mSeatViewConfig
                            .THUMB_PADDING, rowIndex, columnIndex), commonPaint);
            }
        }

        thumbSeatViewPaint.setStyle(Paint.Style.STROKE);
        thumbSeatViewPaint.setStrokeWidth(3);
        thumbSeatViewPaint.setColor(Color.YELLOW);
        canvas.drawRect(getVisibleThumbRect(mSeatViewConfig.THUMB_WIDTH, mSeatViewConfig
                .THUMB_HEIGHT), thumbSeatViewPaint);
    }


    public HashMap<String, Seat> getSubmitSeats() {
        return submitSeats;
    }
}
