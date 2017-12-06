package com.moviepass.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.moviepass.R;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.model.Screening;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheaterMoviesAdapter extends RecyclerView.Adapter<TheaterMoviesAdapter.ViewHolder> {

    View root;
    GridLayout showtimeGrid;
    public static final String TAG = "found";
    //    private final ScreeningPosterClickListener screeningPosterClickListener;
    ShowtimeClickListener showtimeClickListener;
    private ArrayList<Screening> screeningsArrayList;
    ArrayList<String> showtimesArrayList;
    List<String> startTimes;
    private boolean qualifiersApproved;
    private final int TYPE_ITEM = 0;
    private LayoutInflater inflater;
    private Context context;
    private int selectedPosition = -1;
    boolean selected = false;
    public TextView showtime = null;
    String currentSelection;

    View previousView;


    TheaterShowtimesAdapter showtimesAdapter;

    public TheaterMoviesAdapter(Context context, ArrayList<String> showtimesArrayList, ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener, boolean qualifiersApproved) {
        this.showtimeClickListener = showtimeClickListener;
        this.screeningsArrayList = screeningsArrayList;
        this.qualifiersApproved = qualifiersApproved;
        this.showtimesArrayList = showtimesArrayList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_cinemaposterCARDVIEW)
        CardView cinemaCardViewListItem;
        @BindView(R.id.cinema_movieTitle)
        TextView cinemaTItle;
        @BindView(R.id.CINEMAPOSTER)
        SimpleDraweeView cinemaPoster;
        @BindView(R.id.SHOWTIMEGRID)
        GridLayout showtimeGrid;
//
//        TextView cardView1, cardView2, cardView3, cardView4,
//                cardView5, cardView6, cardView7, cardView8,
//                cardView9, cardView10, cardView11, cardView12,
//                cardView0, cardView13, cardView14, cardView15;

        LinearLayout showTimesLayout;
        ListView listView;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            cinemaCardViewListItem = v.findViewById(R.id.list_item_cinemaposterCARDVIEW);
            cinemaTItle = v.findViewById(R.id.cinema_movieTitle);
            cinemaPoster = v.findViewById(R.id.CINEMAPOSTER);
            showtimeGrid = v.findViewById(R.id.SHOWTIMEGRID);


        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        root = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_cinemaposter, parent, false);
        return new ViewHolder(root);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Screening screening = screeningsArrayList.get(position);
        startTimes = screening.getStartTimes();

        //FRESCO code..
        final Uri imgUrl = Uri.parse(screening.getImageUrl());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build();
        holder.cinemaPoster.setImageURI(imgUrl);
        holder.cinemaPoster.getHierarchy().setFadeDuration(500);
        holder.cinemaTItle.setText(screening.getTitle());
        holder.cinemaPoster.setController(controller);

        //onBind set up Gridlayout & begin a loop to create a new TextView for each showtime in the respective Array.
        holder.showtimeGrid.setRowCount(1);
        holder.showtimeGrid.setColumnCount(screening.getStartTimes().size());
        holder.showtimeGrid.removeAllViews();
        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                showtime = new TextView(root.getContext());
                showtime.setText(screening.getStartTimes().get(i));
                holder.showtimeGrid.addView(showtime);
                showtime.setTextSize(20);
                showtime.setTextColor(root.getResources().getColor(R.color.white));
                showtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showtime.setPadding(50, 50, 50, 50);
                final TextView finalShowtime = showtime;
                finalShowtime.setSelected(false);
                //onclick on each showtime will execute the showtimelistener & create reservtion if possible.
                showtime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!finalShowtime.isSelected()) {
                            finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background_selected));
                            finalShowtime.setPadding(50, 50, 50, 50);
                            String selectedShowTime = finalShowtime.getText().toString();
                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, selectedShowTime);
                            finalShowtime.setSelected(true);
                        } else {
                            finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                            finalShowtime.setPadding(50, 50, 50, 50);
                            String selectedShowTime = finalShowtime.getText().toString();
                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, selectedShowTime);
                            finalShowtime.setSelected(false);
                        }

//                        finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background_selected));
//                        finalShowtime.setPadding(50, 50, 50, 50);
//                        finalShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background_selected));
//                        finalShowtime.setPadding(50, 50, 50, 50);


                    }
                });
            }
        }
    }


    @Override
    public int getItemCount() {
        return screeningsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    public void fadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);

    }

    public void fadeOut(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator()); //add this
        fadeOut.setDuration(1000);

        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeOut);
        view.setAnimation(animation);
    }


}