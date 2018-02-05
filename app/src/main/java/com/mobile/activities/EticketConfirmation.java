package com.mobile.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mobile.fragments.ETicketFragment;
import com.mobile.helpers.BottomNavigationViewHelper;
import com.mobile.model.Screening;
import com.mobile.model.SelectedSeat;
import com.moviepass.R;

import org.parceler.Parcels;

public class EticketConfirmation extends BaseActivity {

    TextView etixTitle, etixTheater, etixShowtime, etixSeat, seatTExt;
    SimpleDraweeView etixPoster;
    ImageView etixOnBack;
    Screening screeningObject;
    SelectedSeat seatObject;
    Button etixConfirm;
    String selectedShowTime;
    View progressWheel;
    RelativeLayout relSeat;

    public static final String SEAT = "seat";
    public static final String TAG = "FOUND IT";

    public static final String MOVIE = "movie";
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String THEATER = "theater";
    public static final String TOKEN = "token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_eticket_confirmation);

        etixConfirm = findViewById(R.id.ETIX_GET);
        etixTitle = findViewById(R.id.ETIX_MOVIE_TITLE);
        etixShowtime = findViewById(R.id.ETIX_SHOWTIME);
        etixPoster = findViewById(R.id.ETIX_MOVIEPOSTER);
        etixTheater = findViewById(R.id.ETIX_THEATER);
        etixSeat = findViewById(R.id.ETIX_SEAT);
        etixOnBack = findViewById(R.id.Etix_ONBACK);
        relSeat = findViewById(R.id.relSeat);

        bottomNavigationView = findViewById(R.id.ETIX_NAV);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        etixOnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        //set details for confirmation page..

        Intent intent = getIntent();
        screeningObject = Parcels.unwrap(intent.getParcelableExtra(SCREENING));
        selectedShowTime = getIntent().getStringExtra(SHOWTIME);
        seatObject = Parcels.unwrap(getIntent().getParcelableExtra(SEAT));

        etixTitle.setText(screeningObject.getTitle());
        etixShowtime.setText(selectedShowTime);
        etixTheater.setText(screeningObject.getTheaterName());

        if (seatObject != null) {
            etixSeat.setText(seatObject.getSeatName());
            relSeat.setVisibility(View.VISIBLE);
        }

        progressWheel = findViewById(R.id.etixprogress);

        Uri uri = Uri.parse(screeningObject.getImageUrl());
        etixPoster.setImageURI(uri);

        etixConfirm.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            //new variables for data objects

            Screening screening = screeningObject;
            String selectedTiime = selectedShowTime;

            bundle.putParcelable(SCREENING, Parcels.wrap(screening));
            bundle.putString(SHOWTIME, selectedTiime);

            if (seatObject != null) {
                SelectedSeat seat = new SelectedSeat(seatObject.getSelectedSeatRow(), seatObject.getSelectedSeatColumn(), seatObject.getSeatName());
                bundle.putParcelable(SEAT, Parcels.wrap(seat));
            }


            ETicketFragment fragobj = new ETicketFragment();
            fragobj.setArguments(bundle);
            FragmentManager fm = getSupportFragmentManager();
            fragobj.show(fm, "fr_eticketconfirm_noticedialog");
        });

    }


    int getContentViewId() {
        return R.layout.activity_browse;
    }

    int getNavigationMenuItemId() {
        return R.id.action_movies;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                int itemId = item.getItemId();
                if (itemId == R.id.action_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
//                } else if (itemId == R.id.action_reservations) {
//                    Toast.makeText(SelectSeatActivity.this, "E-Ticket Activity", Toast.LENGTH_LONG).show();
//                    startActivity(new Intent(getApplicationContext(), ReservationsActivity.class));
                } else if (itemId == R.id.action_movies) {
                } else if (itemId == R.id.action_theaters) {
                } else if (itemId == R.id.action_settings) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                }
                finish();
            }
        }, 300);
        return true;
    }

    private void updateNavigationBarState() {
        int actionId = getNavigationMenuItemId();
        selectBottomNavigationBarItem(actionId);
    }

    void selectBottomNavigationBarItem(int itemId) {
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0, size = menu.size(); i < size; i++) {
            MenuItem item = menu.getItem(i);
            boolean shouldBeChecked = item.getItemId() == itemId;
            if (shouldBeChecked) {
                item.setChecked(true);
                break;
            }
        }
    }

}
