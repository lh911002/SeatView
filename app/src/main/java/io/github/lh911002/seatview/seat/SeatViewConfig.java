package io.github.lh911002.seatview.seat;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.List;

import io.github.lh911002.seatview.R;

/**
 * Created by athrun on 16/5/28.
 */
public class SeatViewConfig {

    public Seat[][] seatArray = new Seat[50][100];
    public String[] rowNames = new String[50];

    public RectF windowRectF;
    /*view window config*/
    public float windowHeight;
    public float windowWidth;

    /*seat config*/
    public float virtualHeight;
    public float virtualWidth;
    public int rowCount;
    public int columnCount;

    public static final float SEAT_WIDTH_HEIGHT_RATIO = 1.2f;
    public static final float SEAT_INLINE_GAP_WIDTH_RATIO = 0.265f;
    public static final float SEAT_NEWLINE_GAP_WIDTH_RATIO = 0.304f;

    public float seatWidth = 0;
    public float seatHeight = 0;
    public float seatInlineGap = 0;
    public float seatNewlineGap = 0;

    public float padding;


    /*left row num bar about*/
    public float barWidth;
    public float barMarginLeft;
    public float barTextSize;

    /*screen about*/
    public float screenWidth;
    public float screenHeight;
    public float centerTextSize;


    /**
     * 座位最小高度
     */
    public float seatMinHeight = 0;

    /**
     * 进入后的默认高度
     */
    public float seatDefaultHeight = 0;

    /**
     * 座位最大高度
     */
    public float seatMaxHeight = 0;
    /**
     * 座位最小宽度
     */
    public float seatMinWidth = 0;
    /**
     * 座位最大宽度
     */
    public float seatMaxWidth = 0;

    /**
     * 进入后的默认高度
     */
    public float seatDefaultWidth = 0;

    public float xOffset = 0;
    public float yOffset = 0;
    public float yOffsetDefault;

    /*thumb about*/
    public float THUMB_WIDTH;
    public float THUMB_HEIGHT;
    public float THUMB_SEAT_WIDTH;
    public float THUMB_SEAT_HEIGHT;
    public float THUMB_GAP_INLINE;
    public float THUMB_GAP_NEWLINE;
    public float THUMB_PADDING;


    public SeatViewConfig(Context context, List<SeatRow> seatDatas, int windowHeight, int windowWidth) {
        initSize(context, windowHeight, windowWidth);
        initSeatArray(seatDatas);

        float seatColumnCount = 2 + (4f * columnCount - 1) / 3f;
        seatMinWidth = windowWidth / seatColumnCount;
        seatMinHeight = seatMinWidth / SEAT_WIDTH_HEIGHT_RATIO;

        seatDefaultWidth = Math.max(seatDefaultWidth, seatMinWidth);
        seatDefaultHeight = seatDefaultWidth / SEAT_WIDTH_HEIGHT_RATIO;

        seatWidth = seatDefaultWidth;
        seatHeight = seatDefaultHeight;

        seatInlineGap = seatWidth * SEAT_INLINE_GAP_WIDTH_RATIO;
        seatNewlineGap = seatWidth * SEAT_NEWLINE_GAP_WIDTH_RATIO;
        padding = seatWidth;


        virtualWidth = columnCount * (seatWidth + seatInlineGap) - seatInlineGap + padding * 2;
        virtualHeight = rowCount * (seatHeight + seatNewlineGap) - seatNewlineGap + padding * 2;

        screenWidth = windowWidth * 0.55f;
        screenHeight = screenWidth / 8;

        xOffset = (windowWidth - virtualWidth) / 2;
        yOffsetDefault = screenHeight;
        yOffset = yOffsetDefault;

        initThumbSize();
    }

    private void initSize(Context context, int windowHeight, int windowWidth) {
        seatMaxWidth = context.getResources()
                .getDimensionPixelSize(R.dimen.seat_max_height);
        seatMaxHeight = seatMaxWidth / SEAT_WIDTH_HEIGHT_RATIO;
        seatDefaultWidth = context.getResources()
                .getDimensionPixelSize(R.dimen.seat_min_height);
        seatDefaultHeight = seatDefaultWidth / SEAT_WIDTH_HEIGHT_RATIO;
        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

        barWidth = seatDefaultWidth;
        barMarginLeft = barWidth;
        barTextSize = context.getResources().getDimensionPixelSize(R.dimen.text_size_tiny);
        centerTextSize = context.getResources().getDimensionPixelSize(R.dimen.text_size_small);

        windowRectF = new RectF(0f, 0f, windowWidth, windowHeight);
    }

    private void initSeatArray(List<SeatRow> seatDatas) {
        if (seatDatas != null) {
            rowCount = seatDatas.size();
            for (int rowIndex = 0; rowIndex < seatDatas.size(); rowIndex++) {
                SeatRow rowSeatListBean = seatDatas.get(rowIndex);
                rowNames[rowIndex] = rowSeatListBean.rowName;
                if (rowSeatListBean != null && rowSeatListBean.seats != null) {
                    for (int columnIndex = 0; columnIndex < rowSeatListBean.seats.size(); columnIndex++) {
                        rowSeatListBean.seats.get(columnIndex).rowName = rowSeatListBean.rowName;
                        seatArray[rowIndex][columnIndex] = rowSeatListBean.seats.get(columnIndex);
                    }
                    columnCount = Math.max(columnCount, rowSeatListBean.seats.size());
                }
            }
        }
    }


    private void initThumbSize() {
        THUMB_WIDTH = windowWidth * 0.35f;

        float seatColumnCount = columnCount + SeatViewConfig.SEAT_INLINE_GAP_WIDTH_RATIO * (columnCount - 1) + 2;//padding = seatwidth
        THUMB_SEAT_WIDTH = (THUMB_WIDTH / seatColumnCount);
        THUMB_SEAT_HEIGHT = THUMB_SEAT_WIDTH / SeatViewConfig.SEAT_WIDTH_HEIGHT_RATIO;
        THUMB_GAP_INLINE = THUMB_SEAT_WIDTH * SeatViewConfig.SEAT_INLINE_GAP_WIDTH_RATIO;
        THUMB_GAP_NEWLINE = THUMB_SEAT_WIDTH * SeatViewConfig.SEAT_NEWLINE_GAP_WIDTH_RATIO;
        THUMB_PADDING = THUMB_SEAT_WIDTH;

        THUMB_HEIGHT = THUMB_SEAT_HEIGHT * rowCount + THUMB_GAP_NEWLINE * (rowCount - 1) + THUMB_PADDING * 2;

    }

    /**
     * get draw position of seats
     *
     * @param rowIndex    start from 0
     * @param columnIndex start from 0
     * @return
     */
    public RectF getSeatRect(int rowIndex, int columnIndex) {
        float left = padding + columnIndex * (seatInlineGap + seatWidth);
        float right = left + seatWidth;
        float top = padding + rowIndex * (seatNewlineGap + seatHeight);
        float bottom = top + seatHeight;
        RectF drawRect = new RectF(left, top, right, bottom);
        drawRect.offset(xOffset, yOffset);
        return drawRect;
    }

    /**
     * get draw rect of left row num bar
     *
     * @return
     */
    public RectF getLeftBarRect() {
        float left = barMarginLeft;
        float top = padding - barWidth / 2;
        float right = left + barWidth;
        float bottom = top + virtualHeight - padding * 2 + barWidth;
        RectF drawRect = new RectF(left, top, right, bottom);
        drawRect.offset(0, yOffset);
        return drawRect;
    }


    /**
     * get draw rect of row num text
     *
     * @param rowIndex
     * @return
     */
    public RectF getRowNumRect(int rowIndex) {
        float left = barMarginLeft;
        float top = padding + rowIndex * (seatHeight + seatNewlineGap) + seatHeight / 2;
        float right = left + barWidth;
        float bottom = top + barTextSize;
        RectF drawRect = new RectF(left, top, right, bottom);
        drawRect.offset(0, yOffset);
        return drawRect;

    }

    /**
     * get screen center x
     *
     * @return
     */
    public float getScreenCenterX() {
        float left = virtualWidth / 2 - screenWidth / 2 + xOffset;
        float right = left + screenWidth;
        return (left + right) / 2f;
    }

    /**
     * get screen draw path
     *
     * @return
     */
    public Path getScreenPath() {
        Path screenPath = new Path();
        float centerX = getScreenCenterX();
        screenPath.moveTo(centerX - screenWidth / 2, 0);
        screenPath.lineTo(centerX - screenWidth / 2 + 0.03f * windowWidth, screenHeight);
        screenPath.lineTo(centerX + screenWidth / 2 - 0.03f * windowWidth, screenHeight);
        screenPath.lineTo(centerX + screenWidth / 2, 0);
        screenPath.close();
        return screenPath;
    }


    /**
     * get draw path of center line
     *
     * @return
     */
    public Path getCenterLinePath() {
        Path linePath = new Path();
        linePath.moveTo(virtualWidth / 2 + xOffset, screenHeight);
        linePath.lineTo(virtualWidth / 2 + xOffset, virtualHeight - padding + yOffset);
        return linePath;
    }


    public int[] getClickedSeat(float touchX, float touchY) {
        /*virtual and touch convert */
        int[] position = new int[2];
        float virtualX = touchX - xOffset;
        float virtualY = touchY - yOffset;
        if (virtualX < padding || virtualX > (virtualWidth - padding)
                || virtualY < padding || virtualY > (virtualHeight - padding)) {
            //touch outsize of the seats
            return null;
        } else {
            int rowIndex = (int) Math.floor((virtualY - padding + seatNewlineGap / 2) / (seatHeight + seatNewlineGap));
            int columnIndex = (int) Math.floor((virtualX - padding + seatInlineGap / 2) / (seatWidth + seatInlineGap));
            position[0] = rowIndex;
            position[1] = columnIndex;
            if (rowIndex >= 0 && rowIndex < rowCount && columnIndex >= 0 && columnIndex < columnCount) {
                return position;
            } else {
                return null;
            }

        }
    }

    public void moveSeatView(float moveX, float moveY) {

        float newXOffset = xOffset - moveX;
        float newYOffset = yOffset - moveY;

        RectF seatViewRect = new RectF(newXOffset, newYOffset, newXOffset + virtualWidth, newYOffset + virtualHeight);
        RectF windowRect = new RectF(0, 0, windowWidth, windowHeight);


        if (moveX < 0) {
            //move right
            if (seatViewRect.left < windowRect.left) {
                this.xOffset -= moveX;
            }
        } else {
            if (seatViewRect.right > windowRect.right) {
                this.xOffset -= moveX;

            }
        }

        if (moveY < 0) {
            //move down
            if (seatViewRect.top < windowRect.top || (seatViewRect.top - windowRect.top) < getHeadSpace()) {
                this.yOffset -= moveY;
            }
        } else {
            if (seatViewRect.bottom > windowRect.bottom) {
                this.yOffset -= moveY;
            }
        }


    }

    public void setSeatWidth(float newSeatWidth, float touchX, float touchY) {
        float newSeatHeight = newSeatWidth / SEAT_WIDTH_HEIGHT_RATIO;
        if (newSeatWidth <= seatMinWidth || newSeatHeight <= seatMinHeight
                || newSeatWidth >= seatMaxWidth || newSeatHeight >= seatMaxHeight) {
            return;
        }

        if (newSeatWidth < seatWidth) {
            touchX = windowWidth / 2;
            touchY = windowHeight / 2;
        }

        double virtualX = touchX - xOffset;
        double virtualY = touchY - yOffset;
        double ratioX = virtualX / virtualWidth;
        double ratioY = virtualY / virtualHeight;

        seatWidth = newSeatWidth;
        seatHeight = seatWidth / SEAT_WIDTH_HEIGHT_RATIO;
        seatInlineGap = seatWidth * SEAT_INLINE_GAP_WIDTH_RATIO;
        seatNewlineGap = seatWidth * SEAT_NEWLINE_GAP_WIDTH_RATIO;
        padding = seatWidth;

        virtualWidth = columnCount * (seatWidth + seatInlineGap) - seatInlineGap + padding * 2;
        virtualHeight = rowCount * (seatHeight + seatNewlineGap) - seatNewlineGap + padding * 2;

        xOffset = touchX - (int) (virtualWidth * ratioX);
        yOffset = touchY - (int) (virtualHeight * ratioY);
    }

    public float getHeadSpace() {
        return screenHeight;
    }

    public Seat getSeat(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < rowCount && columnIndex >= 0 && columnIndex < columnCount) {
            return seatArray[rowIndex][columnIndex];
        }
        return null;
    }
}
