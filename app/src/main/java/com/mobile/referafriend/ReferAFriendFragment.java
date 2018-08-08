package com.mobile.referafriend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.network.RestClient;
import com.mobile.responses.ReferAFriendResponse;
import com.moviepass.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ReferAFriendFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    Activity myActivity;
    Context myContext;
    Button submitReferralButton;
    ImageView twitter, facebok;
    EditText firstName, lastName, email;
    TextView referMessage, referTitle;
    TextInputLayout fistNameTextInputLayout, lastNameTextInputLayout, emailTextInputLayout;
    String friendEmail;
    View root, progress;
    ReferAFriendResponse referalResponse;

    public ReferAFriendFragment() {
        // Required empty public constructor
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_refer_a_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        twitter = view.findViewById(R.id.TwitterRefer);
        facebok = view.findViewById(R.id.FacebookRefer);
        firstName = view.findViewById(R.id.RF);
        lastName = view.findViewById(R.id.RL);
        referMessage = view.findViewById(R.id.Refermsg);
        referTitle = view.findViewById(R.id.ReferTitle);
        email = view.findViewById(R.id.RE);
        progress = view.findViewById(R.id.progress);
        submitReferralButton = view.findViewById(R.id.ReferSubmit);
        fistNameTextInputLayout = view.findViewById(R.id.ReferName);
        lastNameTextInputLayout = view.findViewById(R.id.ReferLast);
        emailTextInputLayout = view.findViewById(R.id.ReferEmail);
        root = view.findViewById(R.id.root);

        getReferalInfo();


        submitReferralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (allFieldsAreValid()) {
                    friendEmail = email.getText().toString();
                    submitReferral();
                }

            }
        });
    }

    public boolean allFieldsAreValid() {
        boolean valid = true;
        fistNameTextInputLayout.setError("");
        lastNameTextInputLayout.setError("");
        emailTextInputLayout.setError("");
        if (firstName.getText().toString().trim().isEmpty()) {
            fistNameTextInputLayout.setError("Required");
            valid = false;
        }
        if (lastName.getText().toString().trim().isEmpty()) {
            lastNameTextInputLayout.setError("Required");
            valid = false;
        }

        if (!isEmailValid(email.getText().toString())) {
            emailTextInputLayout.setError("A valid email address is required");
        }

        return valid;

    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        myActivity = activity;
    }

    public void getReferalInfo() {
        progress.setVisibility(View.VISIBLE);
        RestClient.getAuthenticated().referAFriend().enqueue(new Callback<ReferAFriendResponse>() {
            @Override
            public void onResponse(Call<ReferAFriendResponse> call, Response<ReferAFriendResponse> response) {
                referalResponse = response.body();
                if (response.isSuccessful()) {
                    progress.setVisibility(View.GONE);
                    if (referalResponse != null) {
                        referMessage.setText(referalResponse.getReferralMessage());
                        referTitle.setText(referalResponse.getReferralTitle());
                    } else {
                        Toast.makeText(myContext, "Unable to retrieve invite messages.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<ReferAFriendResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(myContext, "Unable to retrieve invite messages.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void submitReferral() {
        if (referalResponse != null) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("application/octet-stream");
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("message/rfc822");
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, referalResponse.getEmailSubject());
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{friendEmail});
            Spanned emailMessege = Html.fromHtml(referalResponse.getEmailMessage());
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hey " + firstName.getText().toString() + " " + lastName.getText().toString() + "," + emailMessege);
            startActivity(emailIntent);
        }
    }

}
