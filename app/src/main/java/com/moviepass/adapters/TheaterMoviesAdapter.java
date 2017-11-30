package com.moviepass.adapters;

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
import com.moviepass.R;
import com.moviepass.listeners.ScreeningPosterClickListener;
import com.moviepass.model.Screening;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheaterMoviesAdapter extends RecyclerView.Adapter<TheaterMoviesAdapter.ViewHolder> {
    public static final String TAG = "found";
    private final ScreeningPosterClickListener screeningPosterClickListener;
    private ArrayList<Screening> screeningsArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private int selectedPosition = -1;

    public TheaterMoviesAdapter(ArrayList<Screening> screeningsArrayList, ScreeningPosterClickListener screeningPosterClickListener) {
        this.screeningPosterClickListener = screeningPosterClickListener;
        this.screeningsArrayList = screeningsArrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_cinemaposterCARDVIEW)
        CardView cinemaCardViewListItem;
        @BindView(R.id.cinema_movieTitle)
        TextView cinemaTItle;
        @BindView(R.id.CINEMAPOSTER)
        SimpleDraweeView cinemaPoster;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            cinemaCardViewListItem = v.findViewById(R.id.list_item_cinemaposterCARDVIEW);
            cinemaTItle = v.findViewById(R.id.cinema_movieTitle);
            cinemaPoster = v.findViewById(R.id.CINEMAPOSTER);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cinemaposter, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Screening screening = screeningsArrayList.get(position);
        Log.d(TAG, "first titles: " + screening.getTitle());

        final Uri imgUrl = Uri.parse(screening.getImageUrl());
        Log.d(TAG, "onBindViewHolder: " + imgUrl.toString());


        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build();


        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build();


        holder.cinemaPoster.setImageURI(imgUrl);
        holder.cinemaPoster.getHierarchy().setFadeDuration(500);
        holder.cinemaTItle.setText(screening.getTitle());

        holder.cinemaPoster.setController(controller);

//        if (imgUrl.isEmpty()) {
//            Picasso.Builder builder = new Picasso.Builder(holder.itemView.getContext());
//            builder.listener(new Picasso.Listener() {
//                @Override
//                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
//                    exception.printStackTrace();
//
//                    holder.title.setText(screening.getTitle());
//                }
//            });
//            builder.build()
//                    .load(R.drawable.ticket_top_red_dark)
//                    .placeholder(R.drawable.ticket_top_red_dark)
//                    .error(R.drawable.ticket_top_red_dark)
//                    .into(holder.posterImageView);
//            holder.title.setText(screening.getTitle());
//        } else {
//            Picasso.Builder builder = new Picasso.Builder(holder.itemView.getContext());
//            builder.listener(new Picasso.Listener() {{
//                @Override
//                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
//                    exception.printStackTrace();
//
//                    holder.title.setText(screening.getTitle());
//                }
//            });
//            builder.build()
//                    .load(imgUrl)
//                    .placeholder(R.drawable.ticket_top_red_dark)
//                    .error(R.drawable.ticket_top_red_dark)
//                    .into(holder.posterImageView);
//        }

//        holder.listItemMoviePoster.setTag(position);
//
//        ViewCompat.setTransitionName(holder.posterImageView, screening.getImageUrl());
//
//        final List<String> startTimes = screening.getStartTimes();
//
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final int currentPosition = holder.getLayoutPosition();
//                if (selectedPosition != currentPosition) {
//
//                    // Show Ripple and then change color
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            // Temporarily save the last selected position
//                            int lastSelectedPosition = selectedPosition;
//                            // Save the new selected position
//                            selectedPosition = currentPosition;
//                            // update the previous selected row
//                            notifyItemChanged(lastSelectedPosition);
//                            // select the clicked row
//                            holder.itemView.setSelected(true);
//                        }
//                    }, 150);
//                }
//
//                screeningPosterClickListener.onScreeningPosterClick(holder.getAdapterPosition(), screening, startTimes, holder.posterImageView);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return screeningsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}