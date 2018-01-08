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
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.extensions.Selectable;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieTheatersAdapter extends RecyclerView.Adapter<MovieTheatersAdapter.ViewHolder> {
    public static final String TAG = "Showtimes/";
    public Screening screening;

    public static int lastCheckedPos = -1;

    public String selectedTheater;
    public String check;
    int counter;
    View root;
    private ArrayList<Screening> screeningsArrayList;
    private ArrayList<Theater> theaterArrayList;
    private ArrayList<String> ShowtimesList;
    private ShowtimeClickListener showtimeClickListener;

    public SingleSelectToggleGroup group;
    //    public Button showTime;
    public Button currentTime;


    public MovieTheatersAdapter(ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener) {
        this.screeningsArrayList = screeningsArrayList;
        this.showtimeClickListener = showtimeClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {
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
        GridLayout showTimesGrid;

        @BindView(R.id.SHOWTIME_MOVIE)
        CheckedTextView showTime;

        @BindView(R.id.progress)
        View progress;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            theaterCardViewListItem = v.findViewById(R.id.THEATERS_LISTITEM);
            TheaterName = v.findViewById(R.id.THEATER_NAME_LISTITEM);
            TheaterAddressListItem = v.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
            TheaterPin = v.findViewById(R.id.THEATER_PIN_LISTITEM);
            showTime = v.findViewById(R.id.SHOWTIME_MOVIE);
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

        ShowtimesList = new ArrayList<>();
        screening = screeningsArrayList.get(position);


        holder.TheaterName.setText(screening.getTheaterName());
        holder.TheaterAddressListItem.setText(screening.getTheaterAddress());
        holder.showTimesGrid.setRowCount(1);
        holder.showTimesGrid.setColumnCount(screening.getStartTimes().size());
        holder.showTime.setTextSize(20);
        holder.showTimesGrid.removeAllViews();
        holder.showTimesGrid.setPadding(40, 10, 40, 10);
        holder.showTimesGrid.setUseDefaultMargins(false);
        holder.showTimesGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);


        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                holder.showTime.setText(screening.getStartTimes().get(i));

                holder.showTime = new CheckedTextView(root.getContext());
                holder.showTime.setId(i);
                holder.showTimesGrid.addView(holder.showTime);
                holder.showTime.setTextColor(root.getResources().getColor(R.color.white));
                holder.showTime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                holder.showTime.setPadding(20, 10, 20, 10);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(0, 0, 70, 0);
                holder.showTime.setLayoutParams(params);

//                final Button selectedShowtime = showTime;

                final Screening select = screening;
                lastCheckedPos = holder.getLayoutPosition();


                final CheckedTextView selected = holder.showTime;

                holder.showTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "onClick: " +        selected.getText().toString());
                    }
                });

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
