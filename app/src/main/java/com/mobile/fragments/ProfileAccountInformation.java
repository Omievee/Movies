package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import com.helpshift.support.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.DeviceID;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.activities.LogInActivity;
import com.mobile.model.User;
import com.mobile.network.RestClient;
import com.mobile.requests.ChangePasswordRequest;
import com.mobile.requests.LogInRequest;
import com.mobile.responses.ChangePasswordResponse;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.fragments.PendingReservationFragment.TAG;

public class ProfileAccountInformation extends Fragment {

    private ProfileActivityInterface mListener;
    private Context context;
    private View rootView, progress;
    private TextView userName,userEmail,moviePassCard;
    private UserInfoResponse userInfoResponse;

    public ProfileAccountInformation() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView =  inflater.inflate(R.layout.fragment_profile_account_information, container, false);
        userName = rootView.findViewById(R.id.USER_NAME);
        userEmail = rootView.findViewById(R.id.USER_EMAIL);
        moviePassCard = rootView.findViewById(R.id.MPCardNum);
        progress = rootView.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        loadUserInfo();

        return rootView;
    }


    private void loadUserInfo() {
        int userId = UserPreferences.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {

                    String firstName = userInfoResponse.getUser().getFirstName();
                    String lastName = userInfoResponse.getUser().getLastName();
                    String email = userInfoResponse.getEmail();

                    userName.setText(firstName + " " + lastName);
                    userEmail.setText(email);

                    moviePassCard.setText(userInfoResponse.getMoviePassCardNumber());

                    progress.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(getActivity(), "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
                mListener.closeFragment();
                Log.d(Constants.TAG, "onFailure: " + t.getMessage());
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if (context instanceof ProfileActivityInterface) {
            mListener = (ProfileActivityInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileActivityInterface");
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;

        if (context instanceof ProfileActivityInterface) {
            mListener = (ProfileActivityInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileActivityInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        this.context = null;
    }

}
