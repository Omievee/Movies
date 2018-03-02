package com.mobile.adapters;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
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

import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Screening;
import com.mobile.model.Theater;
import com.moviepass.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieTheatersAdapter extends RecyclerView.Adapter<MovieTheatersAdapter.ViewHolder> {
    public static final String TAG = "Showtimes/";
    public Screening screening;

    public static final String inputFormat = "HH:mm";

    private Date date;
    private Date dateCompareOne;
    private Date dateCompareTwo;
    SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.US);

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


    public MovieTheatersAdapter(ArrayList<Theater> theaterArrayList, ArrayList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener) {

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
        @BindView(R.id.THEATER_ADDRESS_LISTITEM)
        TextView address1;
        @BindView(R.id.progress)
        View progress;
        @BindView(R.id.ONE)
        LinearLayout ONE;


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
            address1 = v.findViewById(R.id.THEATER_ADDRESS_LISTITEM);
            ONE = v.findViewById(R.id.ONE);
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


        Log.d(TAG, "======================================================: ");
        Log.d(TAG, "title: " + screening.getTitle());
        Log.d(TAG, "theater name : " + screening.getTheaterName());
        Log.d(TAG, "approved: " + screening.isApproved());
        Log.d(TAG, "2d: " + screening.is2D());
        Log.d(TAG, "theater event: " + screening.isTheatreEvent());
        Log.d(TAG, "RPX: " + screening.isRpx());
        Log.d(TAG, "3D: " + screening.is3D());
        Log.d(TAG, "Etx: " + screening.isEtx());
        Log.d(TAG, "largeFormat: " + screening.isLargeFormat());




        if (screeningsArrayList.size() == 0) {
            holder.ONE.setVisibility(View.GONE);
            holder.notSupported.setVisibility(View.VISIBLE);
            holder.notSupported.setText("No Theaters Found");
        }

        int theaterID = screeningsArrayList.get(position).getTribuneTheaterId();
        String theaterName = screeningsArrayList.get(position).getTheaterName();


        for (int i = 0; i < theaterArrayList.size(); i++) {
            if (theaterArrayList.get(i).getTribuneTheaterId() == theaterID) {
                double distance = theaterArrayList.get(i).getDistance();
                String name = theaterArrayList.get(i).getName();
                String address = theaterArrayList.get(i).getCity() + " " + theaterArrayList.get(i).getState() + " " + theaterArrayList.get(i).getZip();
                HOLDER.distance.setText(String.valueOf(distance) + " miles");
                HOLDER.TheaterName.setText(name);
                HOLDER.address1.setText(address);
                final Uri uri = Uri.parse("geo:" + theaterArrayList.get(i).getLat() + "," + theaterArrayList.get(i).getLon() + "?q=" + Uri.encode(theaterArrayList.get(i).getName()));

                HOLDER.distance.setOnClickListener(v -> {
                    try {
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
                        mapIntent.setPackage("com.google.android.apps.maps");
                        HOLDER.itemView.getContext().startActivity(mapIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(HOLDER.itemView.getContext(), "Google Maps isn't installed", Toast.LENGTH_SHORT).show();
                    } catch (Exception x) {
                        x.getMessage();
                    }
                });
                HOLDER.TheaterPin.setOnClickListener(view -> {
                    try {
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.valueOf(uri)));
                        mapIntent.setPackage("com.google.android.apps.maps");
                        HOLDER.itemView.getContext().startActivity(mapIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(HOLDER.itemView.getContext(), "Google Maps isn't installed", Toast.LENGTH_SHORT).show();
                    } catch (Exception x) {
                        x.getMessage();
                    }
                });
            }
        }
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
//TODO: REMOVE SHOWTIMES ONCE THE TIME HAS PASSED
//                Calendar now = Calendar.getInstance();
//
//                int hour = now.get(Calendar.HOUR);
//                int minute = now.get(Calendar.MINUTE);
//                int amPM = now.get(Calendar.AM_PM);
//
//                String AM_PM;
//                if (amPM == 0) {
//                    AM_PM = "AM";
//                } else {
//                    AM_PM = "PM";
//                }
//
//                date = parseDate(hour + ":" + minute + " " + AM_PM);
//                dateCompareOne = parseDate(screening.getStartTimes().get(i));
                showTime.setTextColor(root.getResources().getColor(R.color.white_ish));
                showTime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showTime.setPadding(30, 20, 30, 20);
                showTime.setButtonDrawable(null);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 50, 30);
                showTime.setLayoutParams(params);
                final Screening select = screening;
                currentTime = showTime;
                if (screening.getFormat().matches("2D")) {
                    HOLDER.showTimesGrid.setOnCheckedChangeListener((group, checkedId) -> {
                        RadioButton checked = group.findViewById(checkedId);
                        if (currentTime != null) {
                            currentTime.setChecked(false);
                        }
                        currentTime = checked;
                        String selectedShowTime = currentTime.getText().toString();
                        showtimeClickListener.onShowtimeClick(holder.getAdapterPosition(), selectedScreening, selectedShowTime);
                    });
                } else {
                    currentTime.setClickable(false);
                    holder.notSupported.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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


    private Date parseDate(String date) {
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }


}
