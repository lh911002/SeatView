package io.github.lh911002.seatview.seat;

/**
 * Created by athrun on 17/2/18.
 */

public interface ISeatListener {

    void releaseSeat(Seat canceledSeat);

    void lockSeat(Seat selectedSeat);
}
