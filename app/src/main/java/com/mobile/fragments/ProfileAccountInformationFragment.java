package com.mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.mobile.Constants;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.UserPreferences;
import com.mobile.network.RestClient;
import com.mobile.requests.AddressChangeRequest;
import com.mobile.requests.CreditCardChangeRequest;
import com.mobile.responses.UserInfoResponse;
import com.moviepass.R;

import java.util.Arrays;
import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/*
 * Created by anubis on 9/2/17.
 */

public class ProfileAccountInformationFragment extends Fragment {

    private static boolean updateShipping = false, updateBillingAddress = false, updateBillingCard = false;
    private static int YES = 0, NO = 1;
    Context context;
    Activity activity;
    ProfileCancellationFragment cancelSubscription;
    UserInfoResponse userInfoResponse;
    String addressSection, billingSection, creditCardSection;
    View rootView, progress, accountInformation;
    ImageView downArraow, backArrow, downArrow2;
    Switch billingSwitch;
    RelativeLayout userOldBilling, shippingClick, billingClick;
    LinearLayout shippingDetails, bilingDetails, billing2, newBillingData, newBillingData2;
    String userBillingAddress, getUserBillingAddress2, userBillingCity, userBillingState, userBillingZip;
    TextView userName, userEmail, userBillingDate, userPlan, userPlanPrice, userPlanCancel, userBIllingCard, yesNo,
            userBillingChange, userEditShipping, userMPCardNum, userMPExpirNum;

    Button userSave, userCancel;
    EditText userNewAddress2, userNewBillingCC, userNewBillingCVV, userNewBillingExp;
    EditText userNewAddress, userNewCity, userNewState, userNewZip;
    EditText userAddress, userAddress2, userCity, userState, userZip;
    ImageButton userScanCard;
    String MONTH, YEAR;
    //    CustomTextChange customTextChange;
    private static String CAMERA_PERMISSIONS[] = new String[]{
            Manifest.permission.CAMERA
    };
    private ProfileActivityInterface mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_account_details, container, false);



        accountInformation = rootView.findViewById(R.id.UP);
        shippingClick = rootView.findViewById(R.id.MIDDLE);
        billingClick = rootView.findViewById(R.id.END);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

//
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        shippingClick.setOnClickListener(v -> {
            mListener.openProfileAccountShippingInformation();
        });


        accountInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openProfileAccountInformation();
            }
        });


        billingClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openProfileAccountPlanAndInfo();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        Log.d(Constants.TAG, "onAttach cntx: ");

        if (context instanceof ProfileActivityInterface) {
            mListener = (ProfileActivityInterface) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ProfileActivityInterface");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        Log.d(Constants.TAG, "onAttach ACT: ");

        if (activity instanceof ProfileActivityInterface) {
            mListener = (ProfileActivityInterface) activity;
        } else {
            throw new RuntimeException(activity.toString() + " must implement ProfileActivityInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
        mListener = null;
    }
}



