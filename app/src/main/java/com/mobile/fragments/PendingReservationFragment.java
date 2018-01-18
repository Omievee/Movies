package com.mobile.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.model.Movie;
import com.mobile.model.Reservation;
import com.mobile.network.RestClient;
import com.mobile.responses.ActiveReservationResponse;
import com.moviepass.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by omievee
 */

public class PendingReservationFragment extends BottomSheetDialogFragment {

    ArrayList<Movie> historyArrayList;
    ArrayList<Reservation> currentReservationItem;

    ActiveReservationResponse reservationResponse;
    public static final String TAG = "found";

    View progress;
    TextView pendingReservationTitle, pendingReservationTheater, pendingReservationTime, pendingReservationCode;

    Button pendingResrvationCANCELBUTTON;
    SimpleDraweeView pendingPosterImage;
    LinearLayout pendingLayout, noPending;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pendingreservation, container, false);
        ButterKnife.bind(this, rootView);


        progress = rootView.findViewById(R.id.progress);
        pendingReservationTitle = rootView.findViewById(R.id.PendingRes_Title);
        pendingReservationTheater = rootView.findViewById(R.id.PendingRes_Location);
        pendingReservationCode = rootView.findViewById(R.id.PendingRes_Code);
        pendingReservationTime = rootView.findViewById(R.id.PendingRes_Time);
        pendingPosterImage = rootView.findViewById(R.id.PendingRes_IMage);
        pendingResrvationCANCELBUTTON = rootView.findViewById(R.id.PEndingRes_Cancel);
        pendingLayout = rootView.findViewById(R.id.Pending_Data);
        noPending = rootView.findViewById(R.id.NoPending);


        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress.setVisibility(View.VISIBLE);


        pendingResrvationCANCELBUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }

        });

        getPendingReservation();


    }


    private void getPendingReservation() {
        RestClient.getAuthenticated().getLast().enqueue(new Callback<ActiveReservationResponse>() {
            @Override
            public void onResponse(Call<ActiveReservationResponse> call, Response<ActiveReservationResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    ActiveReservationResponse active = response.body();
                    progress.setVisibility(View.GONE);

                    Log.d(TAG, "onResponse: " + active.toString());

                    if(active.getTitle() != null && active.getTheater() != null && active.getShowtime() != null) {
                        pendingLayout.setVisibility(View.VISIBLE);
                        noPending.setVisibility(View.GONE);

                        pendingReservationTitle.setText(active.getTitle());
                        pendingReservationTheater.setText(active.getTheater());
                        String reservationTime = active.getShowtime().substring(11, 16);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                        try {
                            Date date = sdf.parse(reservationTime);
                            pendingReservationTime.setText(sdf.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }else {
                        pendingLayout.setVisibility(View.GONE);
                        noPending.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG, "title : " + active.getTitle());
                    Log.d(TAG, "theater : " + active.getTheater());
                    Log.d(TAG, "seat : " + active.getSeat());
                    Log.d(TAG, "showtime : " + active.getShowtime());
                    Log.d(TAG, "eticket : " + active.geteTicket());

                }

            }

            @Override
            public void onFailure(Call<ActiveReservationResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server error; Try again", Toast.LENGTH_SHORT).show();

            }
        });
    }
}