package com.moviepass.fragments;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.model.Movie;
import com.moviepass.model.Reservation;
import com.moviepass.network.RestClient;
import com.moviepass.responses.ActiveReservationResponse;
import com.moviepass.responses.CancellationResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/31/17.
 */

public class PendingReservationFragment extends BottomSheetDialogFragment {

    ArrayList<Movie> historyArrayList;
    ArrayList<Reservation> currentReservationItem;

    ActiveReservationResponse reservationResponse;
    public static final String TAG = "found";

    View progress;
    TextView pendingReservationTitle, pendingReservationTheater, pendingReservationTime, pendingReservationCode, pendingResrvationCANCELBUTTON;
    SimpleDraweeView pendingPosterImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, rootView);


        progress = rootView.findViewById(R.id.progress);
        pendingReservationTitle = rootView.findViewById(R.id.PendingRes_Title);
        pendingReservationTheater = rootView.findViewById(R.id.PendingRes_Location);
        pendingReservationCode = rootView.findViewById(R.id.PendingRes_Code);
        pendingReservationTime = rootView.findViewById(R.id.PendingRes_Time);
        pendingPosterImage = rootView.findViewById(R.id.PendingRes_IMage);
        pendingResrvationCANCELBUTTON = rootView.findViewById(R.id.PEndingRes_Cancel);


        historyArrayList = new ArrayList<>();
        currentReservationItem = new ArrayList<>();

//        pendingResrvationCANCELBUTTON.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                RestClient.getAuthenticated().requestCancellation().enqueue(new Callback<CancellationResponse>() {
//                    @Override
//                    public void onResponse(Call<CancellationResponse> call, Response<CancellationResponse> response) {
//
//                    }
//
//                    @Override
//                    public void onFailure(Call<CancellationResponse> call, Throwable t) {
//
//                    }
//                });
//            }
//        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                Log.d(TAG, "pre if: ");

                if (response.body() != null && response.isSuccessful()) {
                    ActiveReservationResponse active = response.body();

                    pendingReservationTitle.setText(active.getTitle());
                    pendingReservationTheater.setText(active.getTheater());
                    String reservationTime = active.getShowtime().substring(11, 16);
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");

                    try {
                        Date date = sdf.parse(reservationTime);
                        Log.d(TAG, "onResponse: " + sdf.format(date));
                        pendingReservationTime.setText(sdf.format(date));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    Log.d(TAG, "title it: " + active.getTitle());
                    Log.d(TAG, "theater it: " + active.getTheater());
                    Log.d(TAG, "seat it: " + active.getSeat());
                    Log.d(TAG, "showtime it: " + active.getShowtime());
                }

            }

            @Override
            public void onFailure(Call<ActiveReservationResponse> call, Throwable t) {

            }
        });
    }
}