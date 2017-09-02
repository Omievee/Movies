package com.moviepass.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.moviepass.R;
import com.moviepass.network.RestClient;
import com.moviepass.requests.CancellationRequest;
import com.moviepass.responses.CancellationResponse;
import com.moviepass.responses.UserInfoResponse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 9/1/17.
 */

public class ProfileCancellationFragment extends Fragment {

    RelativeLayout relativeLayout;
    Spinner spinnerCancelReason;
    EditText cancelComments;
    Button buttonCancel;
    View progress;

    ArrayAdapter<CharSequence> cancelAdapter;
    private HashMap<String, Integer> cancelMap;
    final String nextBillingDate = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_cancellation, container, false);
        ButterKnife.bind(this, rootView);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Cancel Account");

        spinnerCancelReason = ButterKnife.findById(rootView, R.id.spinnerCancelReason);
        cancelComments = ButterKnife.findById(rootView, R.id.cancelComments);
        buttonCancel = ButterKnife.findById(rootView, R.id.buttonCancel);
        progress = ButterKnife.findById(rootView, R.id.progress);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        buttonCancel.setOnClickListener(onActionButtonClick);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private View.OnClickListener onActionButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            progress.setVisibility(View.VISIBLE);
            cancelFlow();
        }
    };

    public void onResume() {
        super.onResume();

        cancelAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.cancel_reasons, android.R.layout.simple_spinner_item);
        cancelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCancelReason.setAdapter(cancelAdapter);
    }

    public void cancelFlow() {
        RestClient.getAuthenticated().getUserData(RestClient.userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                UserInfoResponse userInfoUpdateResponse = response.body();
                if (userInfoUpdateResponse != null) {
                    String nextBillingDate = userInfoUpdateResponse.getNextBillingDate();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                    try {
                        Date date = format.parse(nextBillingDate);

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        cal.add(Calendar.DATE, -1);
                        Date lastDay = cal.getTime();
                        format = new SimpleDateFormat("MM/dd/yyyy");
                        nextBillingDate = format.format(lastDay);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    final String lastActiveDay = nextBillingDate.toString();

                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

                    View layout = View.inflate(getActivity(), R.layout.dialog_generic, null);

                    alert.setView(layout);
                    alert.setTitle(R.string.fragment_profile_cancellation_fragment_header);
                    alert.setMessage("The last day your account will remain active is on: " + lastActiveDay);

                    alert.setPositiveButton("Cancel Membership", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //get date & format to pass
                            Calendar c = Calendar.getInstance();
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            String requestDate = df.format(c.getTime());

                            //map of cancel reasons
                            String cancelReason = spinnerCancelReason.getSelectedItem().toString();

                            String[] cancelReasons = getResources().getStringArray(R.array.cancel_reasons);
                            int[] cancelReasonCodes = getResources().getIntArray(R.array.cancel_reason_codes);

                            HashMap<String, Integer> myMap = new HashMap<String, Integer>();
                            for (int i = 0; i < cancelReasons.length; i++) {
                                myMap.put(cancelReasons[i], cancelReasonCodes[i]);
                            }

                            Integer cancellationReason = myMap.get(cancelReason);

                            //cancellation comment
                            String cancellationComment = cancelComments.getText().toString();

                            //cancellation request
                            CancellationRequest request = new CancellationRequest(requestDate, cancellationReason, cancellationComment);

                            progress.setVisibility(View.VISIBLE);
                            buttonCancel.setEnabled(false);

                            RestClient.getAuthenticated().requestCancellation(request).enqueue(new Callback<CancellationResponse>() {
                                @Override
                                public void onResponse(Call<CancellationResponse> call, Response<CancellationResponse> response) {
                                    CancellationResponse cancellationResponse = response.body();
                                    progress.setVisibility(View.GONE);

                                    if (cancellationResponse != null && response.isSuccessful()) {
                                        if (cancellationResponse.getMessage().equals("You have already canceled your account")) {

                                            makeSnackbar(cancellationResponse.getMessage());
                                        } else {
                                            AlertDialog.Builder alertConfirmation = new AlertDialog.Builder(getActivity());
                                            View layout = View.inflate(getActivity(), R.layout.dialog_generic, null);

                                            alertConfirmation.setView(layout);
                                            alertConfirmation.setTitle("Cancellation Confirmation");
                                            alertConfirmation.setMessage("Your account has been canceled and the last day you can use MoviePass will be on: " + lastActiveDay + ".");
                                            alertConfirmation.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    buttonCancel.setEnabled(true);
                                                }
                                            });
                                            AlertDialog dialogConfirmation = alertConfirmation.create();
                                            dialogConfirmation.show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<CancellationResponse> call, Throwable t) {
                                    progress.setVisibility(View.GONE);
                                    buttonCancel.setEnabled(true);
                                    makeSnackbar(t.getMessage().toString());
                                }
                            });
                        }
                    });
                    alert.setNegativeButton("Keep Membership", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progress.setVisibility(View.GONE);
                        }
                    });
                    AlertDialog dialog = alert.create();
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {

            }
        });
    }

    public void makeSnackbar(String message) {
        final Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
