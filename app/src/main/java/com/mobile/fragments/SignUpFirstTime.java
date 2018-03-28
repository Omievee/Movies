package com.mobile.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.Constants;
import com.mobile.activities.SignUpActivity;
import com.mobile.extensions.CustomAutoCompleteDropDown;
import com.mobile.model.ProspectUser;
import com.mobile.network.RestClient;
import com.mobile.requests.CredentialsRequest;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SignUpFirstTime extends Fragment {

//    MaterialSpinner spinnerGender;
    CustomAutoCompleteDropDown spinnerGender;
    RelativeLayout relativeLayout;
    View progress;
    static final int DATE_DIALOG_ID = 0;
    Button signupNowButton;
    TextView seeMap;
    EditText DOB;
    int month, year, day;
    Calendar myCalendar;
    EditText signupEmailInput, signupEmailConfirm, signupPasswordInput;
    TextInputLayout emailTextInputLayout, email2TextInputLayout, passwordTextInputLayout, genderTextInputLayout, birthTextInputLayout;
    Context context;


    public SignUpFirstTime() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        relativeLayout = view.findViewById(R.id.relative_layout);
        signupEmailInput = view.findViewById(R.id.SIGNUP_EMAIL);
        signupPasswordInput = view.findViewById(R.id.SIGNUP_PASSSWORD);
        signupNowButton = view.findViewById(R.id.SIGNUP_BUTTON);
        progress = view.findViewById(R.id.progress);
        DOB = view.findViewById(R.id.DOB);
        signupEmailConfirm = view.findViewById(R.id.SIGNUP_EMAIL_confirm);
        spinnerGender = view.findViewById(R.id.SPINNER);

        signupEmailConfirm.clearFocus();
        signupEmailInput.clearFocus();
        signupPasswordInput.clearFocus();

        emailTextInputLayout = view.findViewById(R.id.emailTextInputLayout);
        email2TextInputLayout = view.findViewById(R.id.email2TextInputLayout);
        passwordTextInputLayout = view.findViewById(R.id.passwordTextInputLayout);
        genderTextInputLayout = view.findViewById(R.id.genderTextInputLayout);
        birthTextInputLayout = view.findViewById(R.id.birthdayTextInputLayout);

        signupEmailInput.addTextChangedListener(new CustomTextWatcher());
        signupEmailConfirm.addTextChangedListener(new CustomTextWatcher());
        signupPasswordInput.addTextChangedListener(new CustomTextWatcher());
        spinnerGender.addTextChangedListener(new CustomTextWatcher());
        DOB.addTextChangedListener(new CustomTextWatcher());

//        spinnerGender.setItems("Gender", "Male", "Female", "Other");
        String items[] = {"Male","Female","Other"};
        spinnerGender.setAdapter(new ArrayAdapter<String>(context,R.layout.spinner_layout, items));

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }
        };

        DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(context,R.style.MyDatePickerDialogTheme, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

//        spinnerGender.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
//                signupEmailConfirm.clearFocus();
//                signupEmailInput.clearFocus();
//                signupPasswordInput.clearFocus();
//            }
//        });

        spinnerGender.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });


        signupNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupPasswordInput.clearFocus();
                signupEmailInput.clearFocus();
                signupEmailConfirm.clearFocus();
                emailTextInputLayout.setError(null);
                email2TextInputLayout.setError(null);
                passwordTextInputLayout.setError(null);
                Log.d(Constants.TAG, "onClick: " + DOB.getText().toString());
                        progress.setVisibility(View.VISIBLE);
                        final String email1 = signupEmailInput.getText().toString().trim();
                        final String email2 = signupEmailConfirm.getText().toString().trim();
                        final String password = signupPasswordInput.getText().toString().trim();
                        final String gender = spinnerGender.getText().toString().trim();
                        final String birthday = DOB.getText().toString().trim();
                        if(isValidEmail(email1, email2) && isValidPassword(password) && isValidBirthday() && isValidGender()) {
                            final CredentialsRequest request = new CredentialsRequest(email1);
                            RestClient.getsAuthenticatedRegistrationAPI().registerCredentials(request).enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> call, Response<Object> response) {
                                    progress.setVisibility(View.GONE);
                                    if (response != null && response.isSuccessful()) {
                                        if (response.body().toString().contains("user exists")) {
                                            Toast.makeText(context, "User already exists", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Log.d("------>", "onResponse: "+response.body());
                                            ProspectUser.email = email1;
                                            ProspectUser.password = password;
                                            ProspectUser.gender = gender;
                                            ProspectUser.dateOfBirth = birthday;

                                            ((SignUpActivity) getActivity()).setEmail(email1);
                                            ((SignUpActivity) getActivity()).setPassword(password);
                                            ((SignUpActivity) getActivity()).setGender(gender);
                                            ((SignUpActivity) getActivity()).setDOB(birthday);
                                            ((SignUpActivity) getActivity()).setPage();
                                            ((SignUpActivity) getActivity()).confirmFirstStep();

                                        }

                                    }
                                    else {
                                        progress.setVisibility(View.GONE);
                                        Toast.makeText(context, "Server Error, Try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Object> call, Throwable t) {
                            /* TODO : Handle failure situation */
                                }
                            });
                        }else{
                            progress.setVisibility(View.GONE);
                        }
                        isValidEmail(email1, email2);
                        isValidPassword(password);
                        isValidBirthday();
                        isValidGender();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        DOB.setText(sdf.format(myCalendar.getTime()));
        birthTextInputLayout.setError(null);
    }

    public boolean isValidEmail(CharSequence target, CharSequence target2) {
        signupEmailInput.clearFocus();
        signupEmailConfirm.clearFocus();
        boolean valid = true;
        if(target.toString().trim().isEmpty()) {
            if(target.toString().trim().isEmpty())
                emailTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_email_empty));
            valid = false;
        }
        if (target == null) {
            emailTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_email_invalid));
            valid = false;
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches() && !target.toString().trim().isEmpty()){
            emailTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_email_invalid));
            valid = false;
        }
        if(!target.toString().equalsIgnoreCase(target2.toString()) || target2.toString().trim().isEmpty()){
            email2TextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_email_match));
            valid = false;
        }
        return valid;
    }

    public boolean isValidGender(){
        if(spinnerGender.getText().toString().trim().isEmpty()){
            genderTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_empty_gender));
            return false;
        }
        return true;
    }

    public boolean isValidBirthday() {
        if (DOB.getText().toString().trim().isEmpty()) {
            birthTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_empty_birthday));
            return false;
        } else {
            String myFormat = "MM/dd/yy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
            if (!(myCalendar.get(Calendar.YEAR) <= 2000)) {
                birthTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_under_age));
                return false;
            }
            return true;
        }
    }

    public boolean isValidPassword(CharSequence target) {
        signupPasswordInput.clearFocus();
        if(target.toString().trim().isEmpty()){
            passwordTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_empty));
            return false;
        } if(target.toString().trim().length()<6){
            passwordTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_more_than_6_characters));
            return false;
        } if(target.toString().trim().length()>20){
            passwordTextInputLayout.setError(getResources().getString(R.string.fragment_profile_account_information_password_invalid));
            return false;
        }
        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_first_time, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context=null;
    }

    public class CustomTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if(signupEmailInput.hasFocus())
                emailTextInputLayout.setError(null);
            if(signupEmailConfirm.hasFocus())
                email2TextInputLayout.setError(null);
            if(signupPasswordInput.hasFocus())
                passwordTextInputLayout.setError(null);
            if(spinnerGender.hasFocus())
                genderTextInputLayout.setError(null);
            if(DOB.hasFocus())
                birthTextInputLayout.setError(null);


        }
    }

}
