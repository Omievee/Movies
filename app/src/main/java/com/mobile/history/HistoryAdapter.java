package com.mobile.history;

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
import com.mobile.history.model.ReservationHistory;
import com.moviepass.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<ReservationHistory> historyArrayList;

    private final int TYPE_ITEM = 0;
    private final HistoryPosterClickListener historyListener;

    public HistoryAdapter(HistoryPosterClickListener historyListener) {
        this.historyListener = historyListener;
    }

    public void setData(List<ReservationHistory> data) {
        historyArrayList = new ArrayList<>(data);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView posterImageView;

        ViewHolder(View v) {
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
        final ReservationHistory movie = historyArrayList.get(position);
        holder.posterImageView.getHierarchy().setFadeDuration(500);
        holder.posterImageView.setImageURI(movie.getImageUrl());
        Uri imgUrl = Uri.parse(movie.getImageUrl());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(imgUrl)
                .setImageRequest(request)
                .setOldController(holder.posterImageView.getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.posterImageView.setImageURI(movie.getImageUrl() + "/original.jpg");
                    }
                })
                .build();

        if (imgUrl.toString().contains("default")) {
            holder.posterImageView.refreshDrawableState();
        }
        holder.posterImageView.setController(controller);

        ViewCompat.setTransitionName(holder.posterImageView, movie.getTitle());

        holder.itemView.setOnClickListener(v -> {
            historyListener.onPosterClicked(position, movie, false);
        });

    }

    @Override
    public int getItemCount() {
        if(historyArrayList!=null) {
            return historyArrayList.size();
        } return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }


}
