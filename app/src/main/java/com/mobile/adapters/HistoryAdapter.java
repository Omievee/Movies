package com.mobile.adapters;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.Interfaces.historyPosterClickListener;
import com.mobile.model.Movie;
import com.moviepass.R;

import io.realm.RealmList;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private RealmList<Movie> historyArrayList;

    private final int TYPE_ITEM = 0;
    private Context context;
    ViewHolder HOLDER;
    private final historyPosterClickListener historyListener;

    public HistoryAdapter(Context context, RealmList<Movie> historyArrayList, historyPosterClickListener historyListener) {
        this.historyArrayList = historyArrayList;
        this.context = context;
        this.historyListener = historyListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView posterImageView;


        public ViewHolder(View v) {
            super(v);
            posterImageView = v.findViewById(R.id.historyPoster);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Movie movie = historyArrayList.get(position);
        Uri imgUrl = null;
        if (movie != null) {
            imgUrl = Uri.parse(movie.getImageUrl());
        }
        holder.posterImageView.setImageURI(imgUrl);
        holder.posterImageView.getHierarchy().setFadeDuration(500);
        HOLDER = holder;
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build();

        Uri finalImgUrl = imgUrl;
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(imgUrl)
                .setImageRequest(request)
                .setOldController(holder.posterImageView.getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (finalImgUrl.toString().contains("default")) {
                            // holder.title.setText(movie.getTitle());
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.posterImageView.setImageURI(finalImgUrl + "/original.jpg");
                    }
                })
                .build();

        if (imgUrl.toString().contains("default")) {
            holder.posterImageView.refreshDrawableState();
        }
        holder.posterImageView.setController(controller);

        ViewCompat.setTransitionName(holder.posterImageView, movie.getTitle());

        holder.itemView.setOnClickListener(v -> {
            historyListener.onPosterClicked(position, movie, holder.posterImageView);
        });


    }

    @Override
    public int getItemCount() {
        return historyArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }


}
