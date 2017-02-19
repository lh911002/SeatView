package io.github.lh911002.seatview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.lh911002.seatview.seat.ISeatListener;
import io.github.lh911002.seatview.seat.Seat;
import io.github.lh911002.seatview.seat.SeatImages;
import io.github.lh911002.seatview.seat.SeatRow;
import io.github.lh911002.seatview.seat.SeatView;

public class MainActivity extends AppCompatActivity implements ISeatListener {

    private SeatView select_seat_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.select_seat_view = (SeatView) findViewById(R.id.select_seat_view);
        this.select_seat_view.setSeatClickListener(this);


        ViewTreeObserver vto = this.select_seat_view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                select_seat_view.initSeatView("Github", new SeatImages(getResources()),querySeatMap());
                select_seat_view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });


    }

    private List<SeatRow> querySeatMap() {
        List<SeatRow> seatRows = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            SeatRow seatRow = new SeatRow();
            seatRow.rowName = String.valueOf(i);
            List<Seat> seats = new ArrayList<>();
            for (int j = 0; j < 30; j++) {
                Seat seat = new Seat();
                if (i == 10) {
                    seat.status = Seat.STATUS.CORRIDOR;
                } else {
                    seat.status = Seat.STATUS.SELECTABLE;
                }
                seat.id = String.valueOf(seat);
                seats.add(seat);
            }
            seatRow.seats = seats;
            seatRows.add(seatRow);
        }
        return seatRows;
    }

    @Override
    public void releaseSeat(Seat canceledSeat) {
        Toast.makeText(this, "已选" + select_seat_view.getSubmitSeats().size() + "个座位", Toast
                .LENGTH_SHORT).show();
    }

    @Override
    public void lockSeat(Seat selectedSeat) {
        Toast.makeText(this, "已选" + select_seat_view.getSubmitSeats().size() + "个座位", Toast
                .LENGTH_SHORT).show();
    }
}
