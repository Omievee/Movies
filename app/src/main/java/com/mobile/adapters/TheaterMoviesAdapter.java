package com.mobile.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import com.helpshift.support.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mobile.activities.TheaterActivity;
import com.mobile.fragments.SynopsisFragment;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Screening;
import com.moviepass.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class TheaterMoviesAdapter extends RecyclerView.Adapter<TheaterMoviesAdapter.ViewHolder> {

    public static final String MOVIE = "movie";
    public static final String TITLE = "title";
    View root;
    public static final String TAG = "found";
    ShowtimeClickListener showtimeClickListener;
    private LinkedList<Screening> screeningsArrayList;
    ArrayList<String> showtimesArrayList;
    List<String> startTimes;
    private final int TYPE_ITEM = 0;
    public RadioButton showtime;
    public RadioButton currentTime;
    Context context;
    String selectedShowTime;
    ViewHolder HOLDER;

    public TheaterMoviesAdapter(Context context,  LinkedList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener) {
        this.showtimeClickListener = showtimeClickListener;
        this.screeningsArrayList = screeningsArrayList;

        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.list_item_cinemaposterCARDVIEW)
        RelativeLayout cinemaCardViewListItem;
        @BindView(R.id.cinema_movieTitle)
        TextView cinemaTItle;
        @BindView(R.id.CINEMAPOSTER)
        SimpleDraweeView cinemaPoster;
        @BindView(R.id.SHOWTIMEGRID)
        RadioGroup showtimeGrid;
        @BindView(R.id.cinema_Synopsis)
        ImageButton synopsis;
        @BindView(R.id.cinema_movieRating)
        TextView movieRating;

        @BindView(R.id.cinema_movieTime)
        TextView movieTime;

        @BindView(R.id.Not_Supported)
        TextView notSupported;

        CardView CardShowtime;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            cinemaCardViewListItem = v.findViewById(R.id.list_item_cinemaposterCARDVIEW);
            cinemaTItle = v.findViewById(R.id.cinema_movieTitle);
            cinemaPoster = v.findViewById(R.id.CINEMAPOSTER);
            showtimeGrid = v.findViewById(R.id.SHOWTIMEGRID);
            notSupported = v.findViewById(R.id.Not_Supported);
            movieRating = v.findViewById(R.id.cinema_movieRating);
            movieTime = v.findViewById(R.id.cinema_movieTime);
            synopsis = v.findViewById(R.id.cinema_Synopsis);
            CardShowtime = v.findViewById(R.id.CardShowtime);
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
        HOLDER = holder;
        startTimes = screening.getStartTimes();

        //FRESCO code..
        final Uri imgUrl = Uri.parse(screening.getLandscapeImageUrl());
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(imgUrl)
                .setProgressiveRenderingEnabled(true)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request).build();
        HOLDER.cinemaPoster.setImageURI(imgUrl);
        HOLDER.cinemaPoster.getHierarchy().setFadeDuration(500);
        HOLDER.cinemaTItle.setText(screening.getTitle());
        HOLDER.cinemaPoster.setController(controller);

        int t = screening.getRunningTime();
        int hours = t / 60; //since both are ints, you get an int
        int minutes = t % 60;

        if (t == 0) {
            HOLDER.movieTime.setVisibility(View.INVISIBLE);
        } else if (hours > 1) {
            String translatedRunTime = hours + " hours " + minutes + " minutes";
            HOLDER.movieTime.setText(translatedRunTime);
        } else {
            String translatedRunTime = hours + " hour " + minutes + " minutes";
            HOLDER.movieTime.setText(translatedRunTime);
        }


        if (screening.getSynopsis().equals("")) {
            HOLDER.synopsis.setVisibility(View.INVISIBLE);
        }

        HOLDER.movieRating.setText("Rated: " + screening.getRating());
        HOLDER.showtimeGrid.removeAllViews();

        final Screening selectedScreening = screening;
        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                showtime = new RadioButton(root.getContext());
                showtime.setText(screening.getStartTimes().get(i));
                showtime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                HOLDER.showtimeGrid.addView(showtime);


                try {
                    Date systemClock = new Date();

                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
                    String curTime = sdf.format(systemClock);

                    Date theaterTime = sdf.parse(screening.getStartTimes().get(i));
                    Date myTime = sdf.parse(curTime);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(theaterTime);
                    cal.add(Calendar.MINUTE, 30);


                    if (myTime.after(cal.getTime())) {
                        if (cal.getTime().getHours() > 3) {
                            holder.showtimeGrid.removeView(showtime);
                        }
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }


                showtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showtime.setPadding(30, 20, 30, 20);
                showtime.setButtonDrawable(null);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 50, 30);
                showtime.setLayoutParams(params);
                final Screening select = screening;
                currentTime = showtime;

                if (!screening.isApproved()) {

                    currentTime.setClickable(false);
                    holder.notSupported.setVisibility(View.VISIBLE);
                    holder.notSupported.setText(screening.getDisabledExplanation());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.cinemaCardViewListItem.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
                    }
                } else {
                    HOLDER.showtimeGrid.setOnCheckedChangeListener((group, checkedId) -> {
                        RadioButton checked = group.findViewById(checkedId);
                        if (screening.isApproved()) {
                            if (currentTime != null) {
                                currentTime.setChecked(false);
                            }
                            if(checked.isChecked()){
                                HOLDER.cinemaCardViewListItem.setBackgroundColor(holder.itemView.getResources().getColor(R.color.charcoalGrey));
                                currentTime = checked;
                                selectedShowTime = currentTime.getText().toString();
                                showtimeClickListener.onShowtimeClick(null, holder.getAdapterPosition(), selectedScreening, selectedShowTime);
                            }
//                            HOLDER.cinemaCardViewListItem.setBackgroundColor(holder.itemView.getResources().getColor(R.color.charcoalGrey));
//                            currentTime = checked;
//                            selectedShowTime = currentTime.getText().toString();
//                            showtimeClickListener.onShowtimeClick(null, holder.getAdapterPosition(), selectedScreening, selectedShowTime);
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "This screening is not supported", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }


            if (screening.getTitle().equals("Check In if Movie Missing")) {
                HOLDER.movieRating.setVisibility(View.GONE);
                HOLDER.cinemaTItle.setText("Unlisted Showtime");
                HOLDER.showtimeGrid.removeView(showtime);
                HOLDER.synopsis.setVisibility(View.GONE);
                HOLDER.showtimeGrid.setVisibility(View.GONE);
                HOLDER.movieTime.setText("Click here to check in to a movie that is playing at this theater, but isn't appearing on the app.");
                HOLDER.cinemaCardViewListItem.setOnClickListener(v -> {
                    currentTime.setChecked(false);
                    HOLDER.cinemaCardViewListItem.setBackgroundColor(holder.itemView.getResources().getColor(R.color.new_red));
                    showtimeClickListener.onShowtimeClick(null, holder.getAdapterPosition(), selectedScreening, showtime.getText().toString());
                });
            }
        }
        holder.synopsis.setOnClickListener(view -> {
            String synopsis = screening.getSynopsis();
            String title = screening.getTitle();
            Bundle bundle = new Bundle();
            bundle.putString(MOVIE, synopsis);
            bundle.putString(TITLE, title);
            SynopsisFragment fragobj = new SynopsisFragment();
            fragobj.setArguments(bundle);
            FragmentManager fm = ((TheaterActivity) context).getSupportFragmentManager();
            fragobj.show(fm, "fr_dialogfragment_synopsis");

        });
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