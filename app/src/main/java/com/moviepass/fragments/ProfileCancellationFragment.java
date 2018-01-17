package com.moviepass.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.network.RestClient;
import com.moviepass.requests.CancellationRequest;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.ButterKnife;

/**
 * Created by anubis on 9/1/17.
 */

public class ProfileCancellationFragment extends Fragment {

    MaterialSpinner spinnerCancelReason;
    EditText cancelComments;
    Button buttonCancel;
    View progress;

    HashMap<String, Integer> mapReasons;
    ImageView cancelBack;
    String cancelReasons;
    private HashMap<String, Integer> cancelMap;
    final String nextBillingDate = null;
    long cancelSubscriptionReason;


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


        Log.d(Constants.TAG, "onViewCreated: " + RestClient.userId);

        spinnerCancelReason.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {

                cancelReasons = (String) view.getItems().get(position);
                if (cancelReasons.equals("Reason for Cancellation")) {
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

        String[] cancelReasons = (String[]) spinnerCancelReason.getItems().toArray();
        int[] cancelCodes = getResources().getIntArray(R.array.cancel_reason_codes);
        mapReasons = new HashMap<>();

        Log.d(Constants.TAG, "cancelFlow: " + cancelReasons);
        Log.d(Constants.TAG, "cancelFlow: " + cancelCodes);


        for (int i = 0; i < cancelReasons.length - 1; i++) {
            for (int j = 0; j < cancelCodes.length - 1; j++) {
                mapReasons.put(cancelReasons[i], cancelCodes[j]);
                Log.d(Constants.TAG, "cancelFlow: " + mapReasons.get(i));

            }
        }

        String angryComments = cancelComments.getText().toString();

//
        CancellationRequest request = new CancellationRequest(requestDate, cancelSubscriptionReason, angryComments);
//        RestClient.getAuthenticated().requestCancellation(request).enqueue(new Callback<CancellationResponse>() {
//            @Override
//            public void onResponse(Call<CancellationResponse> call, Response<CancellationResponse> response) {
//                CancellationResponse cancellationResponse = response.body();
//                progress.setVisibility(View.GONE);
//                if (cancellationResponse != null && response.isSuccessful()) {
//                    if (cancellationResponse.getMessage().equals("You have already canceled your account")) {
//                        Toast.makeText(getActivity(), "This account has already been canceled", Toast.LENGTH_SHORT).show();
//                    }
//                    Toast.makeText(getActivity(), "Cancellation successful", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//
//            @Override
//            public void onFailure(Call<CancellationResponse> call, Throwable t) {
//
//            }
//        });

//        RestClient.getAuthenticated().getUserData(RestClient.userId).enqueue(new Callback<UserInfoResponse>() {
//            @Override
//            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
//                UserInfoResponse userInfoUpdateResponse = response.body();
//                if (userInfoUpdateResponse != null) {
//                    String nextBillingDate = userInfoUpdateResponse.getNextBillingDate();
//                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//
//                    try {
//                        Date date = format.parse(nextBillingDate);
//
//                        Calendar cal = Calendar.getInstance();
//                        cal.setTime(date);
//                        cal.add(Calendar.DATE, -1);
//                        Date lastDay = cal.getTime();
//                        format = new SimpleDateFormat("MM/dd/yyyy");
//                        nextBillingDate = format.format(lastDay);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                    final String lastActiveDay = nextBillingDate.toString();
//
//                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//
//                    View layout = View.inflate(getActivity(), R.layout.dialog_generic, null);
//
//                    alert.setView(layout);
//                    alert.setTitle(R.string.fragment_profile_cancellation_fragment_header);
//                    alert.setMessage("The last day your account will remain active is on: " + lastActiveDay);
//
//                    alert.setPositiveButton("Cancel Membership", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                            //get date & format to pass
//                            Calendar c = Calendar.getInstance();
//                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//                            String requestDate = df.format(c.getTime());
//
//                            //map of cancel reasons
//                            String cancelReason = spinnerCancelReason.getSelectedItem().toString();
//
//                            String[] cancelReasons = getResources().getStringArray(R.array.cancel_reasons);
//                            int[] cancelReasonCodes = getResources().getIntArray(R.array.cancel_reason_codes);
//
//                            HashMap<String, Integer> myMap = new HashMap<String, Integer>();
//                            for (int i = 0; i < cancelReasons.length; i++) {
//                                myMap.put(cancelReasons[i], cancelReasonCodes[i]);
//                            }
//
//                            Integer cancellationReason = myMap.get(cancelReason);
//
//                            //cancellation comment
//                            String cancellationComment = cancelComments.getText().toString();
//
//                            //cancellation request
//                            CancellationRequest request = new CancellationRequest(requestDate, cancellationReason, cancellationComment);
//
//                            progress.setVisibility(View.VISIBLE);
//                            buttonCancel.setEnabled(false);
//
//                            RestClient.getAuthenticated().requestCancellation(request).enqueue(new Callback<CancellationResponse>() {
//                                @Override
//                                public void onResponse(Call<CancellationResponse> call, Response<CancellationResponse> response) {
//                                    CancellationResponse cancellationResponse = response.body();
//                                    progress.setVisibility(View.GONE);
//
//                                    if (cancellationResponse != null && response.isSuccessful()) {
//                                        if (cancellationResponse.getMessage().equals("You have already canceled your account")) {
//
//                                            makeSnackbar(cancellationResponse.getMessage());
//                                        } else {
//                                            AlertDialog.Builder alertConfirmation = new AlertDialog.Builder(getActivity());
//                                            View layout = View.inflate(getActivity(), R.layout.dialog_generic, null);
//
//                                            alertConfirmation.setView(layout);
//                                            alertConfirmation.setTitle("Cancellation Confirmation");
//                                            alertConfirmation.setMessage("Your account has been canceled and the last day you can use MoviePass will be on: " + lastActiveDay + ".");
//                                            alertConfirmation.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    buttonCancel.setEnabled(true);
//                                                }
//                                            });
//                                            AlertDialog dialogConfirmation = alertConfirmation.create();
//                                            dialogConfirmation.show();
//                                        }
//                                    }
//                                }
//
//                                @Override
//                                public void onFailure(Call<CancellationResponse> call, Throwable t) {
//                                    progress.setVisibility(View.GONE);
//                                    buttonCancel.setEnabled(true);
//                                    makeSnackbar(t.getMessage().toString());
//                                }
//                            });
//                        }
//                    });
//                    alert.setNegativeButton("Keep Membership", new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            progress.setVisibility(View.GONE);
//                        }
//                    });
//                    AlertDialog dialog = alert.create();
//                    dialog.show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
//
//            }
//        });
//    }
//
//    public void makeSnackbar(String message) {
//        final Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_INDEFINITE);
//        snackbar.setAction("OK", new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                snackbar.dismiss();
//            }
//        });
//        snackbar.show();
//    }
    }
}

//
//    int userId = UserPreferences.getUserId();
//        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
//        @Override
//        public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
//            UserInfoResponse userInfoUpdateResponse = response.body();
//            if (userInfoUpdateResponse != null) {
//                progress.setVisibility(View.GONE);
//                String nextBillingDate = userInfoUpdateResponse.getNextBillingDate();
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//
//                try {
//                    Date date = format.parse(nextBillingDate);
//
//                    Calendar cal = Calendar.getInstance();
//                    cal.setTime(date);
//                    cal.add(Calendar.DATE, -1);
//                    Date lastDay = cal.getTime();
//                    format = new SimpleDateFormat("MM/dd/yyyy");
//                    nextBillingDate = format.format(lastDay);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//
//                final String lastActiveDay = nextBillingDate.toString();
//
////                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
////
////                    View layout = View.inflate(getActivity(), R.layout.dialog_generic, null);
////
////                    alert.setView(layout);
////                    alert.setTitle(R.string.fragment_profile_cancellation_fragment_header);
////                    alert.setMessage("The last day your account will remain active is on: " + lastActiveDay);
////
////                    alert.setPositiveButton("Cancel Subscription", new DialogInterface.OnClickListener() {
////                        @TargetApi(Build.VERSION_CODES.N)
////                        @Override
////                        public void onClick(DialogInterface dialog, int which) {
////
////                            Calendar c = Calendar.getInstance();
////                            DateFormat df = new android.icu.text.SimpleDateFormat("yyyy-MM-dd");
////                            String requestDate = df.format(c.getTime());
////
////                            String[] cancelReasons = (String[]) spinnerCancelReason.getItems().toArray();
////                            int[] cancelCodes = getResources().getIntArray(R.array.cancel_reason_codes);
////                            mapReasons = new HashMap<>();
////
////                            for (int i = 0; i < cancelReasons.length - 1; i++) {
////                                mapReasons.put(cancelReasons[i], cancelCodes[i]);
////                            }
////
////                            cancelSubscriptionReason = mapReasons.get(spinnerCancelReason);
////                            String angryComments = cancelComments.getText().toString();
////
////
////                            CancellationRequest request = new CancellationRequest(requestDate, cancelSubscriptionReason, angryComments);
////
////
////                        }
////                    });
////                    alert.show();
//
//            }
//        }
//
//        @Override
//        public void onFailure(Call<UserInfoResponse> call, Throwable t) {
//
//        }
//    });
//}