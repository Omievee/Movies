package com.mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.Constants;
import com.mobile.helpers.LogUtils;
import com.mobile.model.Movie;
import com.mobile.network.RestClient;
import com.mobile.responses.HistoryResponse;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.wasabeef.blurry.Blurry;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by o_vicarra on 3/27/18.
 */

public class HistoryDetailsFragment extends MPFragment {

    private static final String HISTORY_POSTER = "poster";
    private static final String EXTRA_TRANSITION_NAME = "transition_name";
    Activity myActivity;
    Context myContext;
    SimpleDraweeView enlargedImage;
    TextView historyDate, historyTitle, historyLocal, likeittext;
    ImageView close, like, dislike;

    ViewGroup blurry;

    public HistoryDetailsFragment() {
    }


    public static HistoryDetailsFragment newInstance(Movie moviePoster, String transitionName) {
        HistoryDetailsFragment fragment = new HistoryDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable(HISTORY_POSTER, moviePoster);
        bundle.putString(EXTRA_TRANSITION_NAME, transitionName);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity.startPostponedEnterTransition();
        setSharedElementEnterTransition(TransitionInflater.from(myActivity).inflateTransition(android.R.transition.explode).setDuration(2000));
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fr_historydetails, container, false);
        blurry = container;

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        return root;
    }

    @Override
    public boolean onBack() {
        if (blurry != null) {
            Blurry.delete(blurry);
            blurry = null;
            return true;
        }
        return super.onBack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enlargedImage = view.findViewById(R.id.enlargedImage);
        historyDate = view.findViewById(R.id.historyDate);
        historyLocal = view.findViewById(R.id.historyLocal);
        historyTitle = view.findViewById(R.id.HistoryTitle);
        close = view.findViewById(R.id.close);
        like = view.findViewById(R.id.like);
        dislike = view.findViewById(R.id.dislike);
        likeittext = view.findViewById(R.id.liketext);

        close.setOnClickListener(v -> {
            myActivity.onBackPressed();
        });


        Movie historyItem = getArguments().getParcelable(HISTORY_POSTER);
        String transition = getArguments().getString(EXTRA_TRANSITION_NAME);

        Log.d(Constants.TAG, "onViewCreated: " + historyItem.getUserRating());

        if (historyItem.getUserRating() != null) {
            likeittext.setVisibility(View.GONE);
            if (historyItem.getUserRating().equals("GOOD")) {
                like.setImageDrawable(getResources().getDrawable(R.drawable.thumbsupselect));
                dislike.setVisibility(View.GONE);
            } else if (historyItem.getUserRating().equals("BAD")) {
                dislike.setImageDrawable(getResources().getDrawable(R.drawable.thumbsdownselect));
                like.setVisibility(View.GONE);
            }

        } else {


            like.setOnClickListener(v -> rateMovie(historyItem.getId(), "GOOD"));

            dislike.setOnClickListener(v -> rateMovie(historyItem.getId(), "BAD"));
        }

        Uri imgUrl = Uri.parse(historyItem.getImageUrl());


        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(imgUrl)
                .setImageRequest(request)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        enlargedImage.setImageURI(imgUrl);
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        if (historyItem.getImageUrl().contains("https://s3.amazonaws.com/")) {
                            enlargedImage.setImageURI(imgUrl + "/original.jpg");
                        }
                    }
                })
                .build();


        long createdAt = historyItem.getCreatedAt();
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy");
        historyDate.setText(sdf.format(new Date(createdAt)));

        historyLocal.setText(historyItem.getTheaterName());
        historyTitle.setText(historyItem.getTitle());
        enlargedImage.setTransitionName(transition);
        enlargedImage.setController(controller);


    }


    private void rateMovie(int historyId, String userRating) {
        HistoryResponse rating = new HistoryResponse(userRating);
        RestClient.getAuthenticated().submitRating(historyId, rating).enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {
                Handler h = new Handler();
                if (response.isSuccessful()) {
                    if (userRating.equals("GOOD")) {
                        dislike.setVisibility(View.GONE);
                        fadeOut(dislike);
                        animate(like);
                    } else if (userRating.equals("BAD")) {
                        like.setVisibility(View.GONE);
                        fadeOut(like);
                        animate(dislike);
                    }
                    PastReservationsFragment.newInstance().queryRealmForObjects();
                    h.postDelayed(() -> myActivity.onBackPressed(), 2000);
                }
            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                Toast.makeText(myActivity, t.getMessage(), Toast.LENGTH_SHORT).show();
                LogUtils.newLog(Constants.TAG, "onFailure: " + t.getMessage());
            }
        });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity = getActivity();
    }

    public void animate(View view) {
        AnimationSet expandAndShrink = new AnimationSet(true);
        ScaleAnimation expand = new ScaleAnimation(
                1f, 1.5f,
                1f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        expand.setDuration(500);

        ScaleAnimation shrink = new ScaleAnimation(
                1.5f, 1f,
                1.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f);
        shrink.setStartOffset(500);
        shrink.setDuration(500);

        expandAndShrink.addAnimation(expand);
        expandAndShrink.addAnimation(shrink);
        expandAndShrink.setFillAfter(true);
        expandAndShrink.setInterpolator(new AccelerateInterpolator(1.0f));

        view.startAnimation(expandAndShrink);
    }

}
