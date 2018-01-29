package com.mobile.adapters;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.mobile.Constants;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private ArrayList<Movie> historyArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public HistoryAdapter(Context context, ArrayList<Movie> historyArrayList) {
        this.historyArrayList = historyArrayList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.historyCard)
        CardView listItemHistory;

        @BindView(R.id.historyPoster)
        SimpleDraweeView posterImageView;
        @BindView(R.id.MovieTitle)
        TextView title;
        @BindView(R.id.Theater)
        TextView theater;
        @BindView(R.id.Date)
        TextView date;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemHistory = v.findViewById(R.id.historyCard);
            posterImageView = v.findViewById(R.id.historyPoster);
            title = v.findViewById(R.id.MovieTitle);
            theater = v.findViewById(R.id.Theater);
            date = v.findViewById(R.id.Date);
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

        String movieTitle = movie.getTitle();
        final Uri imgUrl = Uri.parse(movie.getLandscapeImageUrl());
        holder.posterImageView.setImageURI(imgUrl);
        holder.posterImageView.getHierarchy().setFadeDuration(500);

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .setSource(imgUrl)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(holder.posterImageView.getController())
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);
                        if (imgUrl.toString().contains("default")) {
                            holder.title.setText(movie.getTitle());
                        }
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        holder.posterImageView.setImageURI(imgUrl + "/original.jpg");
                    }
                })
                .build();

        if (imgUrl.toString().contains("default")) {
            holder.posterImageView.refreshDrawableState();
        }
        holder.posterImageView.setController(controller);
        long createdAt = movie.getCreatedAt();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");


        holder.title.setText(movieTitle);
        holder.date.setText(sdf.format(new Date(createdAt)));
        holder.listItemHistory.setTag(position);
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