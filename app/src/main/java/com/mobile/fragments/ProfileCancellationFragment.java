package com.mobile.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.mobile.Constants;
import com.mobile.UserPreferences;
import com.mobile.helpers.LogUtils;
import com.mobile.network.RestClient;
import com.mobile.requests.CancellationRequest;
import com.mobile.responses.CancellationResponse;
import com.mobile.responses.UserInfoResponse;
import com.mobile.widgets.MaterialSpinnerSpinnerView;
import com.moviepass.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 9/1/17.
 */

public class ProfileCancellationFragment extends MPFragment {

    MaterialSpinner spinnerCancelReason;
    EditText cancelComments;
    Button buttonCancel;
    View progress;
    ImageView cancelBack;
    String cancelReasons;
    long cancelSubscriptionReason;
    CancellationResponse cancellationResponse;
    Activity myActivity;
    Context myContext;
    private UserInfoResponse userInfoResponse;
    private String billingDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_profile_cancelation, container, false);
        ButterKnife.bind(this, rootView);


        spinnerCancelReason = rootView.findViewById(R.id.SPINNER);
        buttonCancel = rootView.findViewById(R.id.cancelbutton);
        cancelBack = rootView.findViewById(R.id.cancelBack);
        progress = rootView.findViewById(R.id.progress);
        cancelComments = rootView.findViewById(R.id.CancelComments);
        buttonCancel.setEnabled(false);
        spinnerCancelReason
                .setAdapter(new MaterialSpinnerAdapter<String>(getActivity(), Arrays.asList("Reason for Cancellation", "Price", "Theater selection", "Ease of use", "Lack of use", "Other")) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        MaterialSpinnerSpinnerView view = new MaterialSpinnerSpinnerView(parent.getContext());
                        view.bind(getItemText(position));
                        return view;
                    }
                });
        loadUserInfo();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerCancelReason.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                cancelReasons = (String) view.getItems().get(position);
                if (cancelReasons.equals("Reason for Cancellation")) {
                    buttonCancel.setEnabled(false);
                    Toast.makeText(myActivity, "Please make a selection", Toast.LENGTH_SHORT).show();
                } else {
                    buttonCancel.setEnabled(true);
                }


            }
        });

        cancelBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myActivity.onBackPressed();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancellationConfirmationDialog();
            }
        });


    }

    public void onResume() {
        super.onResume();

    }

    public void showCancellationConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(spinnerCancelReason.getContext(), R.style.CUSTOM_ALERT);
        String message;
        if (billingDate != null)
            message = "You account will remain active until " + billingDate + " (paid through date).";
        else
            message = "Are you sure you want to cancel your membership?";
        builder.setMessage(message)
                .setTitle("Cancel Membership")
                .setPositiveButton("Cancel Membership", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progress.setVisibility(View.VISIBLE);
                        cancelFlow();
                    }
                })
                .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create();
        builder.show();
    }


    public void cancelFlow() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String requestDate = df.format(c.getTime());

        String cancelReason = spinnerCancelReason.getText().toString();
        switch (cancelReason) {
            case "Price":
                cancelSubscriptionReason = 1;
                break;
            case "Theater selection":
                cancelSubscriptionReason = 2;
                break;
            case "Ease of use":
                cancelSubscriptionReason = 3;
                break;
            case "Lack of use":
                cancelSubscriptionReason = 4;
                break;
            case "Other":
                cancelSubscriptionReason = 7;
                break;
            default:
                cancelSubscriptionReason = 8;
                break;
        }
        String angryComments = cancelComments.getText().toString();
        CancellationRequest request = new CancellationRequest(requestDate, cancelSubscriptionReason, angryComments);
        RestClient.getAuthenticated().requestCancellation(request).enqueue(new Callback<CancellationResponse>() {
            @Override
            public void onResponse(Call<CancellationResponse> call, Response<CancellationResponse> response) {
                cancellationResponse = response.body();
                progress.setVisibility(View.GONE);
                if (cancellationResponse != null && response.isSuccessful()) {
                    Toast.makeText(myActivity, "Cancellation successful", Toast.LENGTH_SHORT).show();
                    myActivity.onBackPressed();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(myActivity, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {

                    }
                }
            }


            @Override
            public void onFailure(Call<CancellationResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(myActivity, "Server Error; Try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        int userId = UserPreferences.INSTANCE.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {
                    if (userInfoResponse.getNextBillingDate().equals("")) {
                    } else {
                        billingDate = (userInfoResponse.getNextBillingDate());
                    }
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                LogUtils.newLog(Constants.TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
        myActivity = getActivity();
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }
}
