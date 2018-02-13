package com.mobile.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.ChangedMindRequest;
import com.mobile.responses.ActiveReservationResponse;
import com.mobile.responses.ChangedMindResponse;
import com.moviepass.R;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by omievee
 */

public class PendingReservationFragment extends Fragment {

    public static final String TAG = "found";
    int reservation;
    View progress;
    TextView pendingReservationTitle, confirmedText, pendingReservationTheater, pendingReservationTime, pendingReservationCode, message, ifAsked;
    FrameLayout frame;
    Button pendingResrvationCANCELBUTTON;
    ImageView pendingPosterImage;
    LinearLayout noPending;
    RelativeLayout pendingLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pendingreservation, container, false);
        ButterKnife.bind(this, rootView);


        progress = rootView.findViewById(R.id.progress);
        pendingReservationTitle = rootView.findViewById(R.id.PendingRes_Title);
        pendingReservationTheater = rootView.findViewById(R.id.PendingRes_Location);
        pendingReservationCode = rootView.findViewById(R.id.PendingRes_Code);
        pendingReservationTime = rootView.findViewById(R.id.PendingRes_Time);
        confirmedText = rootView.findViewById(R.id.PendingConrimCode);
        pendingPosterImage = rootView.findViewById(R.id.PendingRes_IMage);
        pendingResrvationCANCELBUTTON = rootView.findViewById(R.id.PEndingRes_Cancel);
        pendingLayout = rootView.findViewById(R.id.Pending_Data);
        noPending = rootView.findViewById(R.id.NoPending);
        message = rootView.findViewById(R.id.CONFIRMED_READY_MESSAGE);
        ifAsked = rootView.findViewById(R.id.ifAsked);
        frame = rootView.findViewById(R.id.Frame);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress.setVisibility(View.VISIBLE);
        pendingResrvationCANCELBUTTON.setVisibility(View.GONE);
        pendingResrvationCANCELBUTTON.setOnClickListener(v -> cancelPending());

        getPendingReservation();


    }


    private void getPendingReservation() {
        RestClient.getAuthenticated().last().enqueue(new Callback<ActiveReservationResponse>() {
            @Override
            public void onResponse(Call<ActiveReservationResponse> call, Response<ActiveReservationResponse> response) {
                if (response.body() != null && response.isSuccessful()) {
                    ActiveReservationResponse active = response.body();
                    progress.setVisibility(View.GONE);
                    if (active.getTitle() != null && active.getTheater() != null && active.getShowtime() != null) {
                        pendingLayout.setVisibility(View.VISIBLE);
                        noPending.setVisibility(View.GONE);
                        pendingResrvationCANCELBUTTON.setVisibility(View.VISIBLE);
                        pendingLayout.setVisibility(View.VISIBLE);


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

                        if (active.getRedemption_code() != null) {
                            message.setText("Here is your redemption code");
                            ifAsked.setVisibility(View.GONE);
                            pendingReservationCode.setText(active.getRedemption_code());
                            frame.setVisibility(View.GONE);
                            pendingResrvationCANCELBUTTON.setVisibility(View.GONE);
                        } else {
                            message.setText("You can now use your MoviePass card to purchase yoru ticket");
                            pendingReservationCode.setText(active.getZip());
                        }

                    } else {
                        pendingLayout.setVisibility(View.GONE);
                        noPending.setVisibility(View.VISIBLE);
                        pendingResrvationCANCELBUTTON.setVisibility(View.GONE);
                    }
                    reservation = active.getReservation().getId();
                } else {
                    progress.setVisibility(View.GONE);
                    pendingLayout.setVisibility(View.GONE);
                    noPending.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<ActiveReservationResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server error; Try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelPending() {
        progress.setVisibility(View.VISIBLE);
        ChangedMindRequest request = new ChangedMindRequest(reservation);
        RestClient.getAuthenticated().changedMind(request).enqueue(new RestCallback<ChangedMindResponse>() {
            @Override
            public void onResponse(Call<ChangedMindResponse> call, Response<ChangedMindResponse> response) {
                ChangedMindResponse responseBody = response.body();
                progress.setVisibility(View.GONE);

                if (responseBody != null && responseBody.getMessage().matches("Failed to cancel reservation: You have already purchased your ticket.")) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.d("jObjError", "jObjError: " + jObjError.getString("message"));

                        Toast.makeText(getActivity(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                    }
                } else if (responseBody != null && responseBody.getMessage().matches("Failed to cancel reservation: You do not have a pending reservation.")) {
                } else if (responseBody != null && response.isSuccessful()) {
                    Toast.makeText(getActivity(), responseBody.getMessage(), Toast.LENGTH_LONG).show();
                    getActivity().finish();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.d("jObjError", "jObjError: " + jObjError.getString("message"));
                        Toast.makeText(getActivity(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        getActivity().finish();
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void failure(RestError restError) {
                progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), restError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}