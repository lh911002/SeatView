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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import io.github.lh911002.seatview.R;


/**
 * Created by athrun on 16/5/28.
 */
public class SeatView extends View {


    private SeatImages seatImages = null;
    private SeatViewConfig seatViewConfig = null;


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
    private String screenName = "";
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


    public void initSeatView(String screenName, SeatImages seatImages, List<SeatRow> seatMap) {
        this.screenName = screenName;
        this.seatImages = seatImages;
        seatViewConfig = new SeatViewConfig(getContext(), seatMap, getMeasuredHeight(),
                getMeasuredWidth());
        initPaint();
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
        rowNumPaint.setTextSize(seatViewConfig.barTextSize);


        centerTextPaint.setColor(Color.parseColor("#202020"));
        centerTextPaint.setTextAlign(Paint.Align.CENTER);
        centerTextPaint.setAntiAlias(true);
        centerTextPaint.setTextSize(seatViewConfig.centerTextSize);


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
        if (seatViewConfig == null || seatViewConfig.rowCount == 0) return;
        canvas.drawColor(getResources().getColor(R.color.seat_view_bg));
        drawSeat(canvas);
        drawScreen(canvas);
        drawCenterLine(canvas);
        drawLeftBar(canvas);
        drawThumbSeatView(canvas);
    }


    private void drawSeat(Canvas canvas) {
        for (int rowIndex = 0; rowIndex < seatViewConfig.rowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < seatViewConfig.columnCount; columnIndex++) {
                RectF seatRectF = seatViewConfig.getSeatRect(rowIndex, columnIndex);
                /*优化重绘效率*/
                if (seatRectF.right < seatViewConfig.windowRectF.left || seatRectF.left >
                        seatViewConfig.windowRectF.right || seatRectF.top > seatViewConfig
                        .windowRectF.bottom || seatRectF.bottom < seatViewConfig.windowRectF.top)
                    continue;

                Seat seatBean = seatViewConfig.seatArray[rowIndex][columnIndex];
                int seatStatus = seatBean.status;
                Bitmap drawBitmap = null;
                switch (seatStatus) {
                    case Seat.STATUS.SELECTABLE://可选
                        drawBitmap = seatImages.bgSeatAvail;
                        break;
                    case Seat.STATUS.SELECTED://已选
                        drawBitmap = seatImages.bgSeatSelected;
                        break;
                    case Seat.STATUS.UNSELECTABLE://已售
                        drawBitmap = seatImages.bgSeatLocked;
                        break;
                }
                if (drawBitmap != null) canvas.drawBitmap(drawBitmap, null, seatRectF, commonPaint);
            }
        }
    }

    private void drawLeftBar(Canvas canvas) {
        /*draw bg*/
        canvas.drawRoundRect(seatViewConfig.getLeftBarRect(), seatViewConfig.barWidth / 2,
                seatViewConfig.barWidth / 2, leftBarBgPaint);
        /*draw num*/
        for (int rowIndex = 0; rowIndex < seatViewConfig.rowCount; rowIndex++) {
            RectF rowNumRect = seatViewConfig.getRowNumRect(rowIndex);
            String rowNum = seatViewConfig.rowNames[rowIndex];
            if (!TextUtils.isEmpty(rowNum))
                canvas.drawText(rowNum, rowNumRect.centerX(), rowNumRect.centerY(), rowNumPaint);
        }
    }

    private void drawScreen(Canvas canvas) {

        canvas.drawPath(seatViewConfig.getScreenPath(), screenPaint);
    }

    private void drawCenterLine(Canvas canvas) {
        Path linePath = seatViewConfig.getCenterLinePath();
        canvas.drawPath(linePath, centerLinePaint);
        canvas.drawText(screenName + "  银幕", seatViewConfig.getScreenCenterX(), seatViewConfig
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
                double newSeatWidth = seatViewConfig.seatWidth + (fingerDistance -
                        lastFingerDistance) / 6d;
                seatViewConfig.setSeatWidth(new BigDecimal(newSeatWidth).floatValue(),
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
            if (seatViewConfig != null) {
                seatViewConfig.moveSeatView((int) distanceX, (int) distanceY);
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
            int[] clickedPosition = seatViewConfig.getClickedSeat(event.getX(), event.getY());
            if (clickedPosition != null && lastFingerDistance == Integer.MAX_VALUE) {
                int rowIndex = clickedPosition[0];
                int columnIndex = clickedPosition[1];
                Seat clickedSeat = seatViewConfig.seatArray[rowIndex][columnIndex];
                if (clickedSeat != null) {
                    checkOrUnCheckSeat(clickedSeat);
                }
                invalidate();
            } else {
                // click blank area
            }


            if (seatViewConfig.seatWidth < seatViewConfig.seatMaxWidth && lastFingerDistance ==
                    Integer.MAX_VALUE) {
                final float x = event.getX();
                final float y = event.getY();

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        float newSeatWidth = seatViewConfig.seatWidth + 8;
                        newSeatWidth = Math.min(newSeatWidth, seatViewConfig.seatMaxWidth);

                        seatViewConfig.setSeatWidth(newSeatWidth, x, y);
                        invalidate();
                        if (newSeatWidth >= seatViewConfig.seatMaxWidth) return;
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
        float left = -seatViewConfig.xOffset / seatViewConfig.virtualWidth * thumbWidth;
        left = Math.max(left, 0);
        float top = -seatViewConfig.yOffset / seatViewConfig.virtualHeight * thumbHeight;
        top = Math.max(top, 0);

        float height = thumbHeight * seatViewConfig.windowHeight / seatViewConfig.virtualHeight;
        height = Math.min(height, thumbHeight - top);
        float width = thumbWidth * seatViewConfig.windowWidth / seatViewConfig.virtualWidth;
        width = Math.min(width, thumbWidth - left);

        float bottom = top + height;
        float right = left + width;

        return new RectF(left, top, right, bottom);
    }

    private void drawThumbSeatView(Canvas canvas) {
        if (!fingerOnScreen) return;
        if (seatViewConfig.THUMB_HEIGHT <= 0 || seatViewConfig.THUMB_WIDTH <= 0) {
            return;
        }

        thumbSeatViewPaint.setColor(Color.parseColor("#B0000000"));
        thumbSeatViewPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, seatViewConfig.THUMB_WIDTH, seatViewConfig.THUMB_HEIGHT,
                thumbSeatViewPaint);

        for (int rowIndex = 0; rowIndex < seatViewConfig.rowCount; rowIndex++) {
            for (int columnIndex = 0; columnIndex < seatViewConfig.columnCount; columnIndex++) {
                Seat seatBean = seatViewConfig.seatArray[rowIndex][columnIndex];
                int seatStatus = seatBean.status;
                Bitmap drawBitmap = null;
                switch (seatStatus) {
                    case Seat.STATUS.SELECTABLE:
                        drawBitmap = seatImages.bgSeatAvail;

                        break;
                    case Seat.STATUS.SELECTED:
                        drawBitmap = seatImages.bgSeatSelected;
                        break;
                    case Seat.STATUS.UNSELECTABLE:
                        drawBitmap = seatImages.bgSeatLocked;
                        break;
                }
                if (drawBitmap != null)
                    canvas.drawBitmap(drawBitmap, null, getThumbSeatRect(seatViewConfig
                            .THUMB_SEAT_WIDTH, seatViewConfig.THUMB_SEAT_HEIGHT, seatViewConfig
                            .THUMB_GAP_INLINE, seatViewConfig.THUMB_GAP_NEWLINE, seatViewConfig
                            .THUMB_PADDING, rowIndex, columnIndex), commonPaint);
            }
        }

        thumbSeatViewPaint.setStyle(Paint.Style.STROKE);
        thumbSeatViewPaint.setStrokeWidth(3);
        thumbSeatViewPaint.setColor(Color.YELLOW);
        canvas.drawRect(getVisibleThumbRect(seatViewConfig.THUMB_WIDTH, seatViewConfig
                .THUMB_HEIGHT), thumbSeatViewPaint);
    }


    public HashMap<String, Seat> getSubmitSeats() {
        return submitSeats;
    }
}
