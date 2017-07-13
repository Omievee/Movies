package com.moviepass.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moviepass.R;
import com.moviepass.adapters.PlansAdapter;
import com.moviepass.listeners.PlanClickListener;
import com.moviepass.model.Plan;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by anubis on 7/11/17.
 */

public class SignUpStepTwoFragment extends Fragment implements PlanClickListener{

    PlansAdapter mPlansAdapter;

    @BindView(R.id.recycler_view_plans)
    RecyclerView mRecyclerView;

    ArrayList<Plan> mPlans;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up_step_two, container, false);

        mPlans = new ArrayList<>();

        LinearLayoutManager mLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_view_plans);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mPlansAdapter = new PlansAdapter(mPlans, this);
        mPlans.clear();

        if (mPlansAdapter != null) {
            mRecyclerView.getRecycledViewPool().clear();
            mPlansAdapter.notifyDataSetChanged();
        }

//        if (mTheatersResponse != null) {
//            mPlans.addAll(mTheatersResponse.getTheaters());
            mRecyclerView.setAdapter(mPlansAdapter);
            mRecyclerView.setTranslationY(0);
            mRecyclerView.setAlpha(1.0f);
//        }

        return rootView;
    }

    public static SignUpStepOneFragment newInstance(String text) {

        SignUpStepOneFragment f = new SignUpStepOneFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onPlanClick(int pos, @NotNull Plan plan, @NotNull String showtime) {

    }
}
