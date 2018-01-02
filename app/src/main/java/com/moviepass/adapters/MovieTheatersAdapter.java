package com.moviepass.adapters;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieTheatersAdapter extends RecyclerView.Adapter<MovieTheatersAdapter.ViewHolder> {
    public static final String TAG = "Showtimes/";
    Screening screening;
    private int selectedEnabledHorz;
    private int selectedEnabledVert = RecyclerView.NO_POSITION;
    View root;
    private ArrayList<Screening> screeningsArrayList;
    private ArrayList<Theater> theaterArrayList;
    private ArrayList<String> ShowtimesList;
    private ShowtimeClickListener showtimeClickListener;

    public Button showTime;


    public MovieTheatersAdapter(ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener) {
        this.screeningsArrayList = screeningsArrayList;
        this.showtimeClickListener = showtimeClickListener;

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.THEATERS_LISTITEM)
        CardView theaterCardViewListItem;
        @BindView(R.id.THEATER_NAME_LISTITEM)
        TextView TheaterName;
        @BindView(R.id.THEATER_ADDRESS2_LISTITEM)
        TextView TheaterAddressListItem;
        @BindView(R.id.THEATER_PIN_LISTITEM)
        ImageView TheaterPin;
        @BindView(R.id.THEATER_SHOWTIMEGRID)
        GridLayout showTimesGrid;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            theaterCardViewListItem = v.findViewById(R.id.THEATERS_LISTITEM);
            TheaterName = v.findViewById(R.id.THEATER_NAME_LISTITEM);
            TheaterAddressListItem = v.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
            TheaterPin = v.findViewById(R.id.THEATER_PIN_LISTITEM);
            showTimesGrid = v.findViewById(R.id.THEATER_SHOWTIMEGRID);

        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        root = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_theaters_and_showtimes, parent, false);
        return new ViewHolder(root);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        ShowtimesList = new ArrayList<>();
        screening = screeningsArrayList.get(position);


        holder.TheaterName.setText(screening.getTheaterName());
        holder.TheaterAddressListItem.setText(screening.getTheaterAddress());
        holder.showTimesGrid.setRowCount(1);
        holder.showTimesGrid.setColumnCount(screening.getStartTimes().size());
        holder.showTimesGrid.removeAllViews();
        holder.showTimesGrid.setPadding(40, 10, 40, 10);
        holder.showTimesGrid.setUseDefaultMargins(false);
        holder.showTimesGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);

        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {

                showTime = new Button(root.getContext());
                showTime.setId(i);

                showTime.setText(screening.getStartTimes().get(i));
                holder.showTimesGrid.addView(showTime);
                showTime.setTextSize(20);

                showTime.setTextColor(root.getResources().getColor(R.color.white));
                showTime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showTime.setPadding(20, 10, 20, 10);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(0, 0, 70, 0);
                showTime.setLayoutParams(params);

                final Button selectedShowtime = showTime;

                selectedShowtime.setSelected(selectedEnabledVert == position);

                showTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (screening.getFormat().equals("2D")) {
                            selectedShowtime.setSelected(true);
                            selectedEnabledHorz = selectedShowtime.getId();

                            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                                if (selectedEnabledHorz != i)
                                     holder.itemView.findViewById(i).setSelected(false);
//                                notifyItemChanged(selectedEnabledHorz);
                                selectedShowtime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background_selected));

                            }
//                            selectedEnabledVert = holder.getLayoutPosition();
                            String showtimeSelection = selectedShowtime.getText().toString();
                            Log.d(TAG, "onClick: " + showtimeSelection);
                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), screening, showtimeSelection);

                        } else {
                            holder.theaterCardViewListItem.setForeground(root.getResources().getDrawable(R.drawable.poster_gradient));
                            Toast.makeText(holder.itemView.getContext(), R.string.Not_Supportd, Toast.LENGTH_SHORT).show();
                        }

                    }

                });


            }

            if (!screening.getFormat().equals("2D")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.theaterCardViewListItem.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
                }
            }
        }
    }


    @Override
    public int getItemCount() {
        return screeningsArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
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
