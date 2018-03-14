package com.mobile.fragments;

import android.app.Fragment;
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
import com.mobile.activities.ConfirmationActivity;
import com.mobile.network.RestClient;
import com.mobile.requests.CancellationRequest;
import com.mobile.responses.CancellationResponse;
import com.moviepass.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 9/1/17.
 */

public class ProfileCancellationFragment extends Fragment {

    MaterialSpinner spinnerCancelReason;
    EditText cancelComments;
    Button buttonCancel;
    View progress;
    ImageView cancelBack;
    String cancelReasons;
    long cancelSubscriptionReason;
    CancellationResponse cancellationResponse;

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

        spinnerCancelReason.setItems("Reason for Cancellation", "Price", "Theater selection", "Ease of use", "Lack of use", "Other");

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
                    Toast.makeText(getActivity(), "Please make a selection", Toast.LENGTH_SHORT).show();
                } else {
                    buttonCancel.setEnabled(true);
                }


            }
        });

        cancelBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                cancelFlow();
            }
        });


    }

    public void onResume() {
        super.onResume();

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
                    Toast.makeText(getActivity(), "Cancellation successful", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        Toast.makeText(getActivity(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {

                    }
                }
            }


            @Override
            public void onFailure(Call<CancellationResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Server Error; Try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
