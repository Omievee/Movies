package com.mobile.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by o_vicarra on 3/27/18.
 */

public class HistoryDetailsFragment extends Fragment {

    private static final String HISTORY_POSTER = "poster";
    private static final String EXTRA_TRANSITION_NAME = "transition_name";
    Activity myActivity;
    Context myContext;
    SimpleDraweeView enlargedImage;
    TextView historyDate, historyTitle, historyLocal;
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
    public void onAttach(Context context) {
        super.onAttach(context);
        myContext = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        myActivity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity.startPostponedEnterTransition();
        setSharedElementEnterTransition(TransitionInflater.from(myActivity).inflateTransition(android.R.transition.explode));
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fr_historydetails, container, false);


        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enlargedImage = view.findViewById(R.id.enlargedImage);
        historyDate = view.findViewById(R.id.historyDate);
        historyLocal = view.findViewById(R.id.historyLocal);
        historyTitle = view.findViewById(R.id.HistoryTitle);


        Movie historyItem = getArguments().getParcelable(HISTORY_POSTER);
        String transition = getArguments().getString(EXTRA_TRANSITION_NAME);

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

                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                    }
                })
                .build();

        long createdAt = historyItem.getCreatedAt();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        historyDate.setText(sdf.format(new Date(createdAt)));

        historyLocal.setText(historyItem.getTheaterName());
        historyTitle.setText(historyItem.getTitle());
        enlargedImage.setTransitionName(transition);
        enlargedImage.setController(controller);
    }
}
