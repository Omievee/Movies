package com.mobile.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import com.helpshift.support.Log;
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

import com.mobile.model.E_Ticket;
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
    TextView noCurrentRes, pendingTitle, pendingLocal, pendingTime, pendingSeat, confirmCode, zip;
    FrameLayout frame;
    Button pendingResrvationCANCELBUTTON;
    RelativeLayout pendingData, StandardTicket, ETicket;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_pending, container, false);
        ButterKnife.bind(this, rootView);


        progress = rootView.findViewById(R.id.progress);
        noCurrentRes = rootView.findViewById(R.id.NO_Current_Res);
        pendingTitle = rootView.findViewById(R.id.PendingRes_Title);
        pendingLocal = rootView.findViewById(R.id.PendingRes_Location);
        pendingTime = rootView.findViewById(R.id.PendingRes_Time);
        pendingSeat = rootView.findViewById(R.id.PendingRes_Seat);
        StandardTicket = rootView.findViewById(R.id.STANDARD_TICKET);
        ETicket = rootView.findViewById(R.id.E_TICKET);
        confirmCode = rootView.findViewById(R.id.ConfirmCode);
        pendingResrvationCANCELBUTTON = rootView.findViewById(R.id.PEndingRes_Cancel);
        zip = rootView.findViewById(R.id.PendingZip);
        pendingData = rootView.findViewById(R.id.PENDING_DATA);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress.setVisibility(View.VISIBLE);
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


                    if (active.getTitle() != null && active.getTheater() != null) {
                        pendingData.setVisibility(View.VISIBLE);

                        reservation = active.getReservation().getId();

                        String reservationTime = active.getShowtime().substring(11, 16);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                        try {
                            Date date = sdf.parse(reservationTime);
                            pendingTime.setText(sdf.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        pendingTitle.setText(active.getTitle());
                        pendingLocal.setText(active.getTheater());

                        if (!active.getE_ticket().getRedemption_code().equals("")) {
                            ETicket.setVisibility(View.VISIBLE);

                            confirmCode.setText(active.getE_ticket().getRedemption_code());

                            if (!active.getE_ticket().getSeat().equals("")) {
                                pendingSeat.setVisibility(View.VISIBLE);
                                pendingSeat.setText("Seat: " + active.getE_ticket().getSeat());
                            }


                        } else {
                            StandardTicket.setVisibility(View.VISIBLE);
                            zip.setText(active.getZip());
                        }
                    } else {
                        noCurrentRes.setVisibility(View.VISIBLE);
                    }

                } else {
                    noCurrentRes.setVisibility(View.VISIBLE);

                    Log.d(TAG, "else: ");
                    progress.setVisibility(View.GONE);

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