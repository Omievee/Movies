package com.mobile.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mobile.Constants;
import com.mobile.activities.SignUpActivity;
import com.mobile.activities.SignUpFirstOpenActivity;
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

    MaterialSpinner spinnerGender;
    RelativeLayout relativeLayout;
    View progress;
    static final int DATE_DIALOG_ID = 0;
    Button signupNowButton;
    TextView seeMap;
    EditText DOB;
    int month, year, day;
    Calendar myCalendar;
    EditText signupEmailInput, signupEmailConfirm, signupPasswordInput;
    TextInputLayout emailTextInputLayout, email2TextInputLayout, passwordTextInputLayout;
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

        spinnerGender.setItems("Gender", "Male", "Female", "Other");

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

        spinnerGender.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                signupEmailConfirm.clearFocus();
                signupEmailInput.clearFocus();
                signupPasswordInput.clearFocus();
            }
        });

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
                if (!signupEmailConfirm.getText().toString().trim().isEmpty() && !signupEmailInput.getText().toString().trim().isEmpty() && !signupPasswordInput.getText().toString().trim().isEmpty()) {
                    if (!signupEmailInput.getText().toString().trim().equals(signupEmailConfirm.getText().toString().trim())) {
                        Toast.makeText(view.getContext(), "Emails do not match", Toast.LENGTH_SHORT).show();
                    } else if (DOB.getText().toString().equals("") || spinnerGender.getText().toString().equals("Gender")) {
                        Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show();
                    } else {
                        progress.setVisibility(View.VISIBLE);
                        final String email1 = signupEmailInput.getText().toString().trim();
                        final String password = signupPasswordInput.getText().toString().trim();
                        final String gender = spinnerGender.getText().toString().trim();
                        final String birthday = DOB.getText().toString().trim();
                        if (isValidEmail(email1) && isValidPassword(password)) {
                            final CredentialsRequest request = new CredentialsRequest(email1);
                            RestClient.getsAuthenticatedRegistrationAPI().registerCredentials(request).enqueue(new Callback<Object>() {
                                @Override
                                public void onResponse(Call<Object> call, Response<Object> response) {
                                    progress.setVisibility(View.GONE);
                                    if (response != null && response.isSuccessful()) {
                                        if (response.body().toString().contains(" userExists=1.0")) {
                                            Toast.makeText(context, "User already exists", Toast.LENGTH_SHORT).show();
                                        } else {
                                            ProspectUser.email = email1;
                                            ProspectUser.password = password;
                                            ProspectUser.gender = gender;
                                            ProspectUser.dateOfBirth = birthday;

//                                            Intent intent = new Intent(context, SignUpActivity.class);
//                                            intent.putExtra("email1", email1);
//                                            intent.putExtra("password", password);
//                                            intent.putExtra("gender", gender);
//                                            intent.putExtra("dateOfBirth", birthday);
//                                            startActivity(intent);

                                            ((SignUpActivity) getActivity()).setEmail(email1);
                                            ((SignUpActivity) getActivity()).setPassword(password);
                                            ((SignUpActivity) getActivity()).setGender(gender);
                                            ((SignUpActivity) getActivity()).setDOB(birthday);
                                            ((SignUpActivity) getActivity()).setPage();
                                            ((SignUpActivity) getActivity()).confirmFirstStep();

                                            Log.d("BLABLA", "onResponse: "+ProspectUser.email);
                                        }

                                    }
                                    else {
                                        Toast.makeText(context, "Server Error, Try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Object> call, Throwable t) {
                            /* TODO : Handle failure situation */
                                }
                            });
                        } else if (!isValidEmail(email1)) {
                            emailTextInputLayout.setError("Invalid Email Address");
                            signupEmailInput.clearFocus();
//                            Snackbar snackbar = Snackbar.make(relativeLayout, "Please enter a valid email1 address", Snackbar.LENGTH_INDEFINITE);
//                            snackbar.setAction("OK", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    progress.setVisibility(View.GONE);
//                                }
//                            });
//
//                            // Changing message text color
//                            snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                            snackbar.show();
                        } else if (!isValidPassword(password)) {
                            passwordTextInputLayout.setError("Invalid password");
                            signupPasswordInput.clearFocus();
//                            if (password.length() < 4) {
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please create a password longer than four characters", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                        } else if (password.length() > 20) {
                            passwordTextInputLayout.setError("Invalid password");
                            signupPasswordInput.clearFocus();
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please create password shorter than twenty characters", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                        } else if (password.contains(" ")) {
                            passwordTextInputLayout.setError("Invalid password");
                            signupPasswordInput.clearFocus();
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please create password without spaces", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                        } else {
                            passwordTextInputLayout.setError("Invalid password");
                            signupPasswordInput.clearFocus();
//                                Snackbar snackbar = Snackbar.make(relativeLayout, "Please enter a valid password", Snackbar.LENGTH_INDEFINITE);
//                                snackbar.setAction("OK", new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        progress.setVisibility(View.GONE);
//                                    }
//                                });
//                                // Changing message text color
//                                snackbar.setActionTextColor(ContextCompat.getColor(SignUpFirstOpenActivity.this, R.color.red));
//                                snackbar.show();
                        }
                    }
                } else {
                    if(signupPasswordInput.getText().toString().trim().isEmpty()){
                        passwordTextInputLayout.setError("Required");
                        signupPasswordInput.clearFocus();
                    }
                    if(signupEmailInput.getText().toString().trim().isEmpty()){
                        emailTextInputLayout.setError("Required");
                        signupEmailInput.clearFocus();
                    }
                    if(signupEmailConfirm.getText().toString().trim().isEmpty()){
                        email2TextInputLayout.setError("Required");
                        signupEmailConfirm.clearFocus();
                    }
                }
            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        if (myCalendar.get(Calendar.YEAR) <= 2000) {
            DOB.setText(sdf.format(myCalendar.getTime()));
        } else {
            DOB.setText("");
            Toast.makeText(context, "Must be 18 years of age or older", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isValidPassword(CharSequence target) {
        if (target == null || target.length() < 4 || target.length() > 20) {
            return false;
        } else {
            return true;
        }
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

}
