package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.mobile.Constants;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.network.RestClient;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;


public class ProfileAccountShippingInformation extends Fragment {
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private String mParam1;
//    private String mParam2;


    Button save,cancel;
    EditText address1,address2,city,state,zip;
    View rootView, progress;
    UserInfoResponse userInfoResponse;
    boolean firstClick = true;
    private Context context;
    private ProfileActivityInterface mListener;
    private TextInputLayout address1TextInputLayout, cityTextInputLayout, stateTextInputLayout, zipTextInputLayout;
//    private OnFragmentInteractionListener mListener;

    public ProfileAccountShippingInformation() {
        // Required empty public constructor
    }

//    public static ProfileAccountShippingInformation newInstance(String param1, String param2) {
//        ProfileAccountShippingInformation fragment = new ProfileAccountShippingInformation();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_profile_account_shipping_information, container, false);
        address1 = rootView.findViewById(R.id.Address1);
        address2 = rootView.findViewById(R.id.Address2);
        city = rootView.findViewById(R.id.city);
        state = rootView.findViewById(R.id.state);
        zip = rootView.findViewById(R.id.zip);
        progress = rootView.findViewById(R.id.progress);
        save = rootView.findViewById(R.id.saveChanges);
        cancel = rootView.findViewById(R.id.cancelChanges);

        address1.addTextChangedListener(new CustomTextWatcher());
        address2.addTextChangedListener(new CustomTextWatcher());
        zip.addTextChangedListener(new CustomTextWatcher());
        state.addTextChangedListener(new CustomTextWatcher());
        city.addTextChangedListener(new CustomTextWatcher());

        address1TextInputLayout = rootView.findViewById(R.id.address1TextInputLayout);
        cityTextInputLayout = rootView.findViewById(R.id.cityTextInputLayout);
        stateTextInputLayout = rootView.findViewById(R.id.stateTextInputLayout);
        zipTextInputLayout = rootView.findViewById(R.id.zipTextInputLayout);


        progress.setVisibility(View.VISIBLE);
        loadUserInfo();

        address1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(firstClick){
                    firstClick = false;
                    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                            .build();

                    try {
                        Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(typeFilter).build(getActivity());
                        startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE2);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                    }
                    return true;
                }
                else{
                    return false;
                }
            }
        });


        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE2) {
            if (resultCode == RESULT_OK) {
                address1.clearFocus();
                address2.clearFocus();
                state.clearFocus();
                zip.clearFocus();
                city.clearFocus();

                Place place = PlaceAutocomplete.getPlace(context, data);
                String address = place.getAddress().toString();
                List<String> localList = Arrays.asList(address.split(",", -1));
                for (int i = 0; i < localList.size(); i++) {
                    if (localList.get(2).trim().length() < 8) {
                        Toast.makeText(context, "Invalid", Toast.LENGTH_SHORT).show();
                    } else {
                        address1.setText(localList.get(0));
                        city.setText(localList.get(1).trim());
                        String State = localList.get(2).substring(0, 3).trim();
                        String zipString = localList.get(2).substring(4, 9);
                        state.setText(State);
                        zip.setText(zipString);
                    }
                }
                address1.clearFocus();
                address2.clearFocus();
                state.clearFocus();
                zip.clearFocus();
                city.clearFocus();
                saveChanges();

            }
        }
    }

    private void saveChanges() {
        save.setTextColor(getResources().getColor(R.color.new_red));
        cancel.setTextColor(getResources().getColor(R.color.white));
        save.setClickable(true);
        save.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            updateShippingAddress();
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.closeFragment();
            }
        });
    }

    private void updateShippingAddress() {
        int userId = UserPreferences.getUserId();
        if (address1.getText().toString() != userInfoResponse.getShippingAddressLine1()) {

            if(!address1.getText().toString().trim().isEmpty() && !city.getText().toString().trim().isEmpty() && !zip.getText().toString().trim().isEmpty() && !state.getText().toString().trim().isEmpty()){
                String newAddress = address1.getText().toString();
                String newAddress2 = address2.getText().toString();
                String newCity = city.getText().toString();
                String newZip = zip.getText().toString();
                String newState = state.getText().toString();

                String type = "shippingAddress";

                AddressChangeRequest request = new AddressChangeRequest(newAddress, newAddress2, newCity, newState, newZip, type);
                RestClient.getAuthenticated().updateAddress(userId, request).enqueue(new Callback<Object>() {
                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {
                        if(response!=null & response.isSuccessful()){
                            Toast.makeText(context, "Address updated", Toast.LENGTH_SHORT).show();
                            mListener.closeFragment();
                        }
                        else{
                            Toast.makeText(context, "Invalid address. Please try another address.", Toast.LENGTH_SHORT).show();
                        }
                        progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(context, "Server Response Error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                if(address1.getText().toString().trim().isEmpty()) {
                    address1TextInputLayout.setError("Required");
                    address1.clearFocus();
                }
                if(state.getText().toString().trim().isEmpty()) {
                    stateTextInputLayout.setError("Required");
                    state.clearFocus();
                }
                if(zip.getText().toString().trim().isEmpty()) {
                    zipTextInputLayout.setError("Required");
                    zip.clearFocus();
                }
                if(city.getText().toString().trim().isEmpty()) {
                    cityTextInputLayout.setError("Required");
                    city.clearFocus();
                }
                progress.setVisibility(View.GONE);
            }

        }

    }

    private void loadUserInfo() {
        int userId = UserPreferences.getUserId();
        RestClient.getAuthenticated().getUserData(userId).enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                userInfoResponse = response.body();
                if (userInfoResponse != null) {

                    String address = userInfoResponse.getShippingAddressLine2();
                    List<String> addressList = Arrays.asList(address.split(",", -1));
                    String shippingCity = "", shippingState = "", shippingZip ="";

                    address1.setText(userInfoResponse.getShippingAddressLine1());

                    for (int i = 0; i < addressList.size(); i++) {
                        city.setText(addressList.get(0));
                        state.setText(addressList.get(1));
                        zip.setText(addressList.get(2));
                    }

                    progress.setVisibility(View.GONE);

                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                Toast.makeText(context, "Server Error; Please try again.", Toast.LENGTH_SHORT).show();
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
//        mListener = null;
    }
//
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

    public class CustomTextWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (address1.isFocused() || address2.isFocused() || city.isFocused() || state.isFocused() || zip.isFocused()) {
                Log.d("TEEEXT", "afterTextChanged: "+s.toString());
                saveChanges();
            }
        }
    }
}
