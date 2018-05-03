package com.mobile.adapters;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.helpers.LogUtils;
import com.mobile.listeners.ShowtimeClickListener;
import com.mobile.model.Movie;
import com.mobile.model.Screening;
import com.mobile.model.Theater;
import com.moviepass.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by ryan on 4/26/17.
 */

public class MovieTheatersAdapter extends RecyclerView.Adapter<MovieTheatersAdapter.ViewHolder> {
    public static final String TAG = "Showtimes/";
    public Screening screening;
    ViewHolder HOLDER;
    public String check;
    View root;
    private LinkedList<Screening> screeningsArrayList;
    private LinkedList<Theater> theaterArrayList;
    private ArrayList<String> ShowtimesList;
    private ShowtimeClickListener showtimeClickListener;

    //    public Button showTime;
    public RadioButton currentTime = null;
    public RadioButton showTime;


    public MovieTheatersAdapter(LinkedList<Theater> theaterArrayList, LinkedList<Screening> screeningsArrayList, ShowtimeClickListener showtimeClickListener) {

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
        RelativeLayout cardview;
        @BindView(R.id.THEATER_DISTANCE_LISTITEM)
        TextView distance;
        @BindView(R.id.THEATER_ADDRESS_LISTITEM)
        TextView address1;
        @BindView(R.id.progress)
        View progress;
        @BindView(R.id.ONE)
        RelativeLayout ONE;

        ImageView iconSeat, iconTicket;


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
            distance = v.findViewById(R.id.THEATER_DISTANCE_LISTITEM);
            address1 = v.findViewById(R.id.THEATER_ADDRESS_LISTITEM);
            ONE = v.findViewById(R.id.ONE);
            iconSeat = v.findViewById(R.id.icon_seat);
            iconTicket = v.findViewById(R.id.icon_ticket);
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

        Theater theater = new Theater();
        ShowtimesList = new ArrayList<>();

        screening = screeningsArrayList.get(position);
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
                theater = theaterArrayList.get(i);
                if (theater.ticketTypeIsETicket()) {
                    HOLDER.iconSeat.setVisibility(View.GONE);
                } else if (theater.ticketTypeIsStandard()) {
                    HOLDER.iconSeat.setVisibility(View.GONE);
                    HOLDER.iconTicket.setVisibility(View.GONE);
                }
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
                HOLDER.showTimesGrid.addView(showTime);


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
                            holder.showTimesGrid.removeView(showTime);
//                            if(holder.showTimesGrid.getChildCount() == 0) {
//                                holder.theaterCardViewListItem.setVisibility(View.GONE);
//                            }
                        }

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                showTime.setBackground(root.getResources().getDrawable(R.drawable.showtime_background));
                showTime.setTypeface(Typeface.DEFAULT_BOLD);
                showTime.setPadding(30, 20, 30, 20);
                showTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                showTime.setButtonDrawable(null);
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 50, 30);
                showTime.setLayoutParams(params);
                final Screening select = screening;
                currentTime = showTime;
                if (!screening.isApproved()) {
                    currentTime.setClickable(false);
                    holder.notSupported.setVisibility(View.VISIBLE);
                    holder.notSupported.setText(screening.getDisabledExplanation());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        holder.ONE.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
                    }
                } else {
                    Theater finalTheater = theater;
                    HOLDER.showTimesGrid.setOnCheckedChangeListener((group, checkedId) -> {
                        RadioButton checked = group.findViewById(checkedId);
                        if (currentTime != null) {
                            currentTime.setChecked(false);
                        }
                        if (checked.isChecked()) {
                            currentTime = checked;
                            String selectedShowTime = currentTime.getText().toString();
                            showtimeClickListener.onShowtimeClick(finalTheater, holder.getAdapterPosition(), selectedScreening, selectedShowTime);
                        }
                    });
                }

            }
            if (queryRealm()) {
                currentTime.setClickable(false);
                holder.notSupported.setVisibility(View.VISIBLE);
                holder.notSupported.setText("You've already seen this movie");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.ONE.setForeground(Resources.getSystem().getDrawable(android.R.drawable.screen_background_dark_transparent));
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

    boolean queryRealm() {
        RealmConfiguration historyConfig = new RealmConfiguration.Builder()
                .name("History.Realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm historyRealm = Realm.getInstance(historyConfig);
        RealmResults<Movie> checkMovieID = historyRealm.where(Movie.class)
                .equalTo("id", screening.getMoviepassId())
                .findAll();

        LogUtils.newLog(TAG, "queryRealm: " + checkMovieID.size());
        for (int i = 0; i < checkMovieID.size(); i++) {
            LogUtils.newLog(TAG, "queryRealm:  " + checkMovieID.get(i).getId());
            if (checkMovieID.get(i).getId() == screening.getMoviepassId() & screening.isApproved()) {
                return true;
            }
        }
        return false;
    }


}
