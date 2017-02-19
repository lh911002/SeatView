package io.github.lh911002.seatview.seat;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import io.github.lh911002.seatview.R;


/**
 * Created by athrun on 16/5/29.
 */
public class SeatImages {
    public Bitmap bgSeatAvail;
    public Bitmap bgSeatLocked;
    public Bitmap bgSeatSelected;

    public SeatImages(Resources resources) {
        bgSeatAvail = BitmapFactory.decodeResource(resources,R.mipmap.movies_seat_normal);
        bgSeatLocked = BitmapFactory.decodeResource(resources,R.mipmap.movies_seat_lock);
        bgSeatSelected = BitmapFactory.decodeResource(resources,R.mipmap.movies_seat_checked);
    }
}
