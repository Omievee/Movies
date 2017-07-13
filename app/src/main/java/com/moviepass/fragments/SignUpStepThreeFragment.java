package com.moviepass.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PayPal;
import com.braintreepayments.api.exceptions.ErrorWithResponse;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.braintreepayments.api.interfaces.BraintreeCancelListener;
import com.braintreepayments.api.interfaces.BraintreeErrorListener;
import com.braintreepayments.api.interfaces.PaymentMethodNonceCreatedListener;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.moviepass.Constants;
import com.moviepass.R;
import com.moviepass.network.RestClient;

import org.json.JSONObject;

import butterknife.OnClick;
import io.card.payment.CardIOActivity;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepThreeFragment extends Fragment {

    ArrayAdapter<CharSequence> mStatesAdapter;

    Spinner mState;
    EditText mZip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_three, container, false);

        mState = rootView.findViewById(R.id.state);
        mZip = rootView.findViewById(R.id.et_zip);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mStatesAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.states_abbrev, R.layout.item_white_spinner);
        mStatesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mState.setAdapter(mStatesAdapter);

        String zipHeight = String.valueOf(mZip.getHeight());

        Log.d("zipHeight: ", zipHeight);

        return rootView;
    }

    public static SignUpStepThreeFragment newInstance(String text) {

        SignUpStepThreeFragment f = new SignUpStepThreeFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
}
