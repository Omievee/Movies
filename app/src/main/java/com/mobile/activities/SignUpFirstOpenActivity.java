package com.mobile.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mobile.model.Plans;
import com.mobile.model.ProspectUser;
import com.mobile.network.RestClient;
import com.mobile.onboard.OnboardingActivityV2;
import com.mobile.responses.PlanResponse;
import com.moviepass.R;

import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 6/15/17.
 */

public class SignUpFirstOpenActivity extends AppCompatActivity {

    public static final String SELECTED_PLAN = "selectedPlan";

    TextView planOnePrice, planTwoPrice;
    TextView planOneDescription, planTwoDescription;
    TextView planOneDisclaimer;
    View planOneView, planTwoView;
    TextView signIn, next;
    TextView titlePlanOne, titlePlanTwo;
    View progress;
    Plans selectedPlan;
    PlanResponse planResponse;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_signup_first_open);

        planOnePrice = findViewById(R.id.planOnePrice);
        planTwoPrice = findViewById(R.id.planTwoPrice);
        planOneDescription = findViewById(R.id.planOneDescription);
        planTwoDescription = findViewById(R.id.planTwoDescription);
        planOneDisclaimer = findViewById(R.id.planOneDisclaimer);
        planOneView = findViewById(R.id.planOne);
        planTwoView = findViewById(R.id.planTwo);
        signIn = findViewById(R.id.signIn);
        next = findViewById(R.id.button_next);
        titlePlanOne = findViewById(R.id.titlePlanOne);
        titlePlanTwo = findViewById(R.id.titlePlanTwo);
        progress = findViewById(R.id.progress);


        planOneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable image=getResources().getDrawable(R.drawable.selected_border_color);
                planOneView.setBackground(image);
                planTwoView.setBackground(null);
                selectedPlan = planResponse.getPlans().get(0);
            }
        });

        planTwoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable image=getResources().getDrawable(R.drawable.selected_border_color);
                planTwoView.setBackground(image);
                planOneView.setBackground(null);
                selectedPlan = planResponse.getPlans().get(1);
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpFirstOpenActivity.this,LogInActivity.class);
                startActivity(intent);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpFirstOpenActivity.this,SignUpActivity.class);
                intent.putExtra(SELECTED_PLAN, Parcels.wrap(selectedPlan));
                ProspectUser.plan = selectedPlan;
                startActivity(intent);
            }
        });

        progress.setVisibility(View.VISIBLE);
        getPlans();

    }

    public void getPlans(){
        RestClient.getsAuthenticatedRegistrationAPI().getPlans().enqueue(new Callback<PlanResponse>() {
            @Override
            public void onResponse(Call<PlanResponse> call, Response<PlanResponse> response) {
                if(response!=null && response.isSuccessful()){
                    progress.setVisibility(View.GONE);
                    planResponse = response.body();
                    Plans planOne = planResponse.getPlans().get(0);
                    Plans planTwo = planResponse.getPlans().get(1);
                    planOnePrice.setText(planOne.getPrice());
                    titlePlanOne.setText(planOne.getName());
                    planOneDescription.setText(planOne.getPlanDescription());
                    planOneDisclaimer.setText(planOne.getDisclaimer());

                    planTwoPrice.setText(planTwo.getPrice());
                    titlePlanTwo.setText(planTwo.getName());
                    planTwoDescription.setText(planTwo.getPlanDescription());

                    if(planOne.getIsFeatured().equalsIgnoreCase("true")){
                        Drawable image=getResources().getDrawable(R.drawable.selected_border_color);
                        planOneView.setBackground(image);
                        selectedPlan = planOne;
                    } else{
                        Drawable image=getResources().getDrawable(R.drawable.selected_border_color);
                        planTwoView.setBackground(image);
                        selectedPlan = planTwo;
                    }
                }
                else{
                    progress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<PlanResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
            }

        });
    }



    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, OnboardingActivityV2.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
    }


}
