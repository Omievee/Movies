package com.mobile.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.MoviePosterClickListener;
import com.mobile.model.Movie;
import com.moviepass.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

/**
 * Created by ryan on 4/26/17.
 */

public class MoviesComingSoonAdapter extends RecyclerView.Adapter<MoviesComingSoonAdapter.ViewHolder> {
    public static final String TAG = "FOUND IT.. ";
    private final MoviePosterClickListener moviePosterClickListener;
    private RealmList<Movie> moviesArrayList;

    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;

    public MoviesComingSoonAdapter(Context context, RealmList<Movie> moviesArrayList, MoviePosterClickListener moviePosterClickListener) {
        this.moviePosterClickListener = moviePosterClickListener;
        this.moviesArrayList = moviesArrayList;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_movie_poster)
        RelativeLayout listItemMoviePoster;
        @BindView(R.id.poster_movie_title)
        TextView title;
        @BindView(R.id.ticket_top_red_dark)
        SimpleDraweeView mComingSoonMoviePosterDV;
        @BindView(R.id.RELEASEDATE)
        TextView comingSoon;
        @BindView(R.id.frame)
        FrameLayout frame;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            listItemMoviePoster = v.findViewById(R.id.list_item_movie_poster);
            title = v.findViewById(R.id.poster_movie_title);
            mComingSoonMoviePosterDV = v.findViewById(R.id.ticket_top_red_dark);
            comingSoon = v.findViewById(R.id.RELEASEDATE);
            frame = v.findViewById(R.id.frame);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Movie movie = moviesArrayList.get(position);
        holder.title.setText("");
        final Uri imgUrl = Uri.parse(movie.getImageUrl());

        try {
            holder.mComingSoonMoviePosterDV.setImageURI(imgUrl);
            holder.mComingSoonMoviePosterDV.getHierarchy().setFadeDuration(500);
            final String dateComingSoon = movie.getReleaseDate().substring(0, 10);
            final SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                    .setProgressiveRenderingEnabled(true)
                    .build();



            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setTapToRetryEnabled(true)
                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                            super.onFinalImageSet(id, imageInfo, animatable);

                            //Makes foreground of image dark
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                holder.frame.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
//                            }
                            if (imgUrl.toString().contains("default")) {
                                holder.title.setText(movie.getTitle());
                            }

//                            try {
//                                Date date = fm.parse(dateComingSoon);
//
//                                SimpleDateFormat out = new SimpleDateFormat("MM/dd/yyyy");
//                                holder.comingSoon.setText(out.format(date));
//
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }

                        }

                        @Override
                        public void onFailure(String id, Throwable throwable) {
//                        holder.title.setText(movie.getTitle());
                            try {
                                Date date = fm.parse(dateComingSoon);
                                SimpleDateFormat out = new SimpleDateFormat("MM/dd/yyyy");
                                holder.comingSoon.setText(out.format(date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            holder.mComingSoonMoviePosterDV.setImageURI(imgUrl + "/original.jpg");
                            holder.frame.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
                        }
                    })
                    .build();
            holder.mComingSoonMoviePosterDV.setController(controller);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moviePosterClickListener.onMoviePosterClick(holder.getAdapterPosition(), movie, holder.mComingSoonMoviePosterDV);
                }
            });

//            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    moviePosterClickListener.onMoviePosterLongClick(holder.getAdapterPosition(), movie, holder.mComingSoonMoviePosterDV);
//                    return true;
//                }
//            });
//
//            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//
//                    } else if (event.getAction() == MotionEvent.ACTION_UP){
//                        moviePosterClickListener.releaseLongPress();
//                    }
//                    return false;
//                }
//
//
//            });

        }catch (IllegalStateException e) {
            e.printStackTrace();
        }



    }

    @Override
    public int getItemCount() {
        return moviesArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

}