package io.github.lh911002.seatview.seat;



public class Seat {
    /*座位状态*/
    public static class STATUS {
        /*走廊*/
        public static final int CORRIDOR = 0;
        /*可选*/
        public static final int SELECTABLE = 1;
        /*不可选*/
        public static final int UNSELECTABLE = 2;
        /*已选*/
        public static final int SELECTED = 3;


    }

    public int status;//0表示走廊，1可售，2不可售,3 是已点击
    public String id;

    public String rowName;
    public String columnName;

}