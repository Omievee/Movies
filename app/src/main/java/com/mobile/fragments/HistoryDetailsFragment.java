package com.mobile.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.SwipeDismissBehavior;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Profile;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.Constants;
import com.mobile.Interfaces.ProfileActivityInterface;
import com.mobile.activities.ProfileActivity;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import jp.wasabeef.blurry.Blurry;

/**
 * Created by o_vicarra on 3/27/18.
 */

public class HistoryDetailsFragment extends DialogFragment implements GestureDetector.OnGestureListener {

    private static final String HISTORY_POSTER = "poster";
    private static final String EXTRA_TRANSITION_NAME = "transition_name";
    Activity myActivity;
    Context myContext;
    SimpleDraweeView enlargedImage;
    TextView historyDate, historyTitle, historyLocal;
    ImageView close;
    //    public ViewGroup CONTAINER;
    ProfileActivity prof;

    ProfileActivityInterface profInterface;

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
        setSharedElementEnterTransition(TransitionInflater.from(myActivity).inflateTransition(android.R.transition.move).setDuration(20000));
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fr_historydetails, container, false);


        ((ProfileActivity) this.getActivity()).CONTAINER = container;





        Blurry.with(myActivity).radius(25).sampling(3).onto(((ProfileActivity) this.getActivity()).CONTAINER);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        enlargedImage = view.findViewById(R.id.enlargedImage);
        historyDate = view.findViewById(R.id.historyDate);
        historyLocal = view.findViewById(R.id.historyLocal);
        historyTitle = view.findViewById(R.id.HistoryTitle);
        close = view.findViewById(R.id.close);


        close.setOnClickListener(v -> {
            myActivity.getFragmentManager().popBackStack();
            Blurry.delete(((ProfileActivity) this.getActivity()).CONTAINER);
        });


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

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        dismiss();
        Blurry.delete(((ProfileActivity) myActivity).CONTAINER);
        return true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        myActivity.getFragmentManager().popBackStack();
        Blurry.delete(((ProfileActivity) this.getActivity()).CONTAINER);
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

}
