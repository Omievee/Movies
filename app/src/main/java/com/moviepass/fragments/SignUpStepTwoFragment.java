package com.moviepass.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.moviepass.R;
import com.moviepass.activities.SignUpActivity;
import com.moviepass.adapters.PlansAdapter;
import com.moviepass.listeners.PlanClickListener;
import com.moviepass.model.Plan;
import com.moviepass.network.RestClient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepTwoFragment extends Fragment implements PlanClickListener {

    PlansAdapter mPlansAdapter;

    RelativeLayout mRelativeLayout;
    @BindView(R.id.recycler_view_plans)
    RecyclerView mRecyclerView;

    ArrayList<Plan> mPlans;
    Button mNext;
    String zip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_two, container, false);

        LinearLayoutManager mLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mRelativeLayout = rootView.findViewById(R.id.relative_layout);
        mRecyclerView = rootView.findViewById(R.id.recycler_view_plans);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mNext = getActivity().findViewById(R.id.button_next);

        mPlans = new ArrayList<>();
        mPlansAdapter = new PlansAdapter(mPlans, this);

        return rootView;
    }

    // This is for actions only available when SignUpTwoFrag is visible
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            zip = ((SignUpActivity) getActivity()).getZip();
            if (zip != null) {
                getPlans(zip);
            }

            mNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Snackbar snackbar = Snackbar.make(mRelativeLayout, R.string.fragment_sign_up_step_two_select_plan, Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }
            });
        }
    }

    public void getPlans(String zip) {
        RestClient.getAuthenticated().getPlans(zip).enqueue(new Callback<Plan>() {
            @Override
            public void onResponse(Call<Plan> call, Response<Plan> response) {
                Plan plan = response.body();

                //Doesn't need to check ifSuccessful() because already determined zip was valid in SignUpStepOneFrag
                mPlans.clear();
                mPlans.add(plan);

                Log.d("mPlans: ", mPlans.toString());

                if (mPlansAdapter != null) {
                    mRecyclerView.getRecycledViewPool().clear();
                    mPlansAdapter.notifyDataSetChanged();
                }

                mRecyclerView.setAdapter(mPlansAdapter);
                mRecyclerView.setTranslationY(0);
                mRecyclerView.setAlpha(1.0f);
            }

            @Override
            public void onFailure(Call<Plan> call, Throwable t) {

            }
        });
    }

    public static SignUpStepTwoFragment newInstance(String zip) {
        Bundle b = new Bundle();
        SignUpStepTwoFragment signUpStepTwoFragment = new SignUpStepTwoFragment();
        b.putString("zip", zip);

        signUpStepTwoFragment.setArguments(b);

        return signUpStepTwoFragment;
    }

    @Override
    public void onPlanClick(int pos, @NotNull Plan plan) {
        final Plan finalPlan = plan;

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SignUpActivity) getActivity()).setPlan(finalPlan);
            }
        });
    }

}
