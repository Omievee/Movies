package com.moviepass.adapters;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.moviepass.R;
import com.moviepass.listeners.ShowtimeClickListener;
import com.moviepass.model.Screening;
import com.moviepass.model.Theater;

import java.lang.reflect.Array;
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


    public MovieTheatersAdapter(ArrayList<Screening> screeningsArrayList, ArrayList<Theater> theaterArrayList, ShowtimeClickListener showtimeClickListener) {
        this.screeningsArrayList = screeningsArrayList;
        this.theaterArrayList = theaterArrayList;
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
        RadioGroup showTimesGrid;
        @BindView(R.id.Not_Supported)
        TextView notSupported;
        @BindView(R.id.ONE)
        LinearLayout cardview;

        @BindView(R.id.THEATER_DISTANCE_LISTITEM)
        TextView distance;

        @BindView(R.id.progress)
        View progress;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            theaterCardViewListItem = v.findViewById(R.id.THEATERS_LISTITEM);
            TheaterName = v.findViewById(R.id.THEATER_NAME_LISTITEM);
            TheaterAddressListItem = v.findViewById(R.id.THEATER_ADDRESS2_LISTITEM);
            TheaterPin = v.findViewById(R.id.THEATER_PIN_LISTITEM);
            progress = v.findViewById(R.id.progress);
            showTimesGrid = v.findViewById(R.id.THEATER_SHOWTIMEGRID);
            notSupported = v.findViewById(R.id.Not_Supported);
            cardview = v.findViewById(R.id.ONE);
            distance = v.findViewById(R.id.THEATER_DISTANCE_LISTITEM);
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
        Theater theater = theaterArrayList.get(position);

        int theaterID = screeningsArrayList.get(position).getTribuneTheaterId();

        for (int i = 0; i < theaterArrayList.size(); i++) {
            if (theaterArrayList.get(i).getTribuneTheaterId() == theaterID) {
                double distance = theaterArrayList.get(i).getDistance();
                HOLDER.distance.setText(String.valueOf(distance));
                final Uri uri = Uri.parse("geo:" + theaterArrayList.get(i).getLat() + "," + theaterArrayList.get(i).getLon() + "?q=" + Uri.encode(theaterArrayList.get(i).getName()));

                HOLDER.distance.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
                            mapIntent.setPackage("com.google.android.apps.maps");
                            HOLDER.itemView.getContext().startActivity(mapIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(HOLDER.itemView.getContext(), "Google Maps isn't installed", Toast.LENGTH_SHORT).show();

                        } catch (Exception x) {
                            x.getMessage();
                        }
                    }
                });
            }
        }
        HOLDER.TheaterName.setText(screening.getTheaterName());
        HOLDER.TheaterAddressListItem.setText(screening.getTheaterAddress());
        HOLDER.showTimesGrid.removeAllViews();
        HOLDER.showTimesGrid.setPadding(40, 10, 40, 10);
        final Screening selectedScreening = screening;

        if (screening.getStartTimes() != null) {
            for (int i = 0; i < screening.getStartTimes().size(); i++) {
                showTime = new RadioButton(root.getContext());
                showTime.setText(screening.getStartTimes().get(i));
                showTime.setTextSize(16);
                HOLDER.showTimesGrid.addView(showTime);
                showTime.setTextColor(root.getResources().getColor(R.color.white_ish));
                showTime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showTime.setPadding(30, 20, 30, 20);
                showTime.setButtonDrawable(null);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 50, 30);
                showTime.setLayoutParams(params);
                final Screening select = screening;
                currentTime = showTime;
                HOLDER.showTimesGrid.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton checked = group.findViewById(checkedId);
                        if (screening.getFormat().equals("2D")) {
                            if (currentTime != null) {
                                currentTime.setChecked(false);
                            }
                            currentTime = checked;
                            String selectedShowTime = currentTime.getText().toString();
                            showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), selectedScreening, selectedShowTime);
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "This screening is not supported", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                if (screening.getFormat().equals("3D") || screening.getFormat().equals("IMAX 3D") || screening.getFormat().equals("IMAX")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        currentTime.setClickable(false);
                        holder.notSupported.setVisibility(View.VISIBLE);
                        holder.cardview.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
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
