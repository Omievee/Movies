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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.extensions.Selectable;
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
    public Screening screening;


    ViewHolder HOLDER;
    private int EnabledButton;

    public static int lastCheckedPos = -1;

    public String selectedTheater;
    public String check;
    int counter;
    View root;
    private ArrayList<Screening> screeningsArrayList;
    private ArrayList<Theater> theaterArrayList;
    private ArrayList<String> ShowtimesList;
    private ShowtimeClickListener showtimeClickListener;

    //    public Button showTime;
    public RadioButton currentTime = null;
    public RadioButton showTime;


    public MovieTheatersAdapter(ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener) {
        this.screeningsArrayList = screeningsArrayList;
        this.showtimeClickListener = showtimeClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public static final int SINGLE_SELECTION = 1;
        Selectable showtimeSelected;
        AdapterView.OnItemSelectedListener onItemSelectedListener;

        @BindView(R.id.THEATERS_LISTITEM)
        CardView theaterCardViewListItem;
        @BindView(R.id.THEATER_NAME_LISTITEM)
        TextView TheaterName;
        @BindView(R.id.THEATER_ADDRESS2_LISTITEM)
        TextView TheaterAddressListItem;
        @BindView(R.id.THEATER_PIN_LISTITEM)
        ImageView TheaterPin;
        @BindView(R.id.THEATER_SHOWTIMEGRID)
        RadioGroup showTimesGrid;

//        @BindView(R.id.SHOWTIME_MOVIE)
//        RadioButton showTime;

        @BindView(R.id.progress)
        View progress;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            theaterCardViewListItem = v.findViewById(R.id.THEATERS_LISTITEM);
            TheaterName = v.findViewById(R.id.THEATER_NAME_LISTITEM);
            TheaterAddressListItem = v.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
            TheaterPin = v.findViewById(R.id.THEATER_PIN_LISTITEM);
//            showTime = v.findViewById(R.id.SHOWTIME_MOVIE);
            progress = v.findViewById(R.id.progress);
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
        HOLDER = holder;

        ShowtimesList = new ArrayList<>();
        screening = screeningsArrayList.get(position);


        HOLDER.TheaterName.setText(screening.getTheaterName());
        HOLDER.TheaterAddressListItem.setText(screening.getTheaterAddress());
//        HOLDER.showTime.setTextSize(20);
        HOLDER.showTimesGrid.removeAllViews();
        HOLDER.showTimesGrid.setPadding(40, 10, 40, 10);


        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                showTime = new RadioButton(root.getContext());
                showTime.setText(screening.getStartTimes().get(i));
                showTime.setTextSize(20);
                HOLDER.showTimesGrid.addView(showTime);
                showTime.setTextColor(root.getResources().getColor(R.color.white));
                showTime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showTime.setPadding(30, 20, 30, 20);
                showTime.setButtonDrawable(null);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 50, 0);
                showTime.setLayoutParams(params);
                final Screening select = screening;
                currentTime = showTime;
                HOLDER.showTimesGrid.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton checked = group.findViewById(checkedId);
                        if (currentTime != null) {
                            currentTime.setChecked(false);
                        }
                        currentTime = checked;
                    }
                });

//                HOLDER.showTime.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//
////                        selected.setSelected(true);
////
////                        EnabledButton = selected.getId();
////                        Log.d(TAG, "Enabled button: " + EnabledButton);
//////
////                        for (int i = 0; i < screening.getStartTimes().size(); i++) {
////                            HOLDER.showTime.setId(i);
////                            if (EnabledButton != HOLDER.showTime.getId()) {
////                                HOLDER.itemView.findViewById(i).setSelected(false);
//////                                    // Temporarily save the last selected position
//////                                    int lastSelectedPosition = lastCheckedPos;
//////                                    // Save the new selected position
//////                                    lastCheckedPos = HOLDER.getLayoutPosition();
//////                                    // update the previous selected row
//////                                    notifyItemChanged(lastSelectedPosition);
//////                                }
////
////                                Log.d(TAG, "holder ID: " + HOLDER.itemView.findViewById(i).toString());
////                            }
////                        }
//                    }
//                });

//                showTime.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        currentTime = selectedShowtime;
//                        currentTime.setSelected(true);
//                        Log.d(TAG, "onClick: " + currentTime.isSelected());
//                        if (screening.getFormat().equals("2D")) {
//                            if (lastCheckedPos != holder.getLayoutPosition()) {
//                                // Temporarily save the last selected position
//                                int lastSelectedPosition = lastCheckedPos;
//                                // Save the new selected position
//                                lastCheckedPos = holder.getLayoutPosition();
//                                // update the previous selected row
//                                notifyItemChanged(lastSelectedPosition);
//                            }
//                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), select, selectedShowtime.getText().toString());
//                        } else {
//                            Toast.makeText(holder.itemView.getContext(), R.string.Not_Supportd, Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//
//                });


                if (screening.getFormat().equals("3D") || screening.getFormat().equals("IMAX 3D") || screening.getFormat().equals("IMAX")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.theaterCardViewListItem.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
                    }
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


//
//    public class SelectableViewHolder extends RecyclerView.ViewHolder {
//        ShowtimeButton showtimeButton;
//        AdapterView.OnItemSelectedListener itemSelectedListener;
//        Selectable item;
//
//
//        public static final int SINGLE_SELECT = 1;
//
//        public SelectableViewHolder(View itemView) {
//            super(itemView);
//        }
//
//
//
//
//    }


}
