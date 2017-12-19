package com.moviepass.fragments;

import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.moviepass.R;
import com.moviepass.model.Movie;
import com.moviepass.model.Reservation;
import com.moviepass.network.RestClient;
import com.moviepass.responses.HistoryResponse;

import java.util.ArrayList;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryFragment extends BottomSheetDialogFragment {

    ArrayList<Movie> historyArrayList;
    ArrayList<Reservation> currentReservationItem;

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

        getHistory();
    }

    private void getHistory() {
        historyArrayList.clear();
//        progress.setVisibility(View.VISIBLE);

        RestClient.getAuthenticated().getReservations().enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
//                    progress.setVisibility(View.GONE);
                    HistoryResponse historyResponse = response.body();
                    historyArrayList.addAll(historyResponse.getHistory());

                    for (int i = 0; i < historyArrayList.size(); i++) {
                        Log.d(TAG, "history: " + historyResponse.getHistory().get(i).getTitle());
                    }
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {

            }
        });

    }

    private void getPendingReservation() {

        RestClient.getAuthenticated().getReservations().enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {

            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {

            }
        });
    }
}
