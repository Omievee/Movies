package com.moviepass.activities;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.moviepass.R;
import com.moviepass.UserLocationManagerFused;
import com.moviepass.fragments.ETicketFragment;
import com.moviepass.helpers.BottomNavigationViewHelper;
import com.moviepass.model.PerformanceInfo;
import com.moviepass.model.Reservation;
import com.moviepass.model.Screening;
import com.moviepass.model.ScreeningToken;
import com.moviepass.model.SelectedSeat;
import com.moviepass.network.RestCallback;
import com.moviepass.network.RestClient;
import com.moviepass.network.RestError;
import com.moviepass.requests.CheckInRequest;
import com.moviepass.requests.PerformanceInfoRequest;
import com.moviepass.requests.SelectedSeatRequest;
import com.moviepass.requests.TicketInfoRequest;
import com.moviepass.responses.ReservationResponse;

import org.json.JSONObject;
import org.parceler.Parcels;

import retrofit2.Call;
import retrofit2.Response;

public class EticketConfirmation extends BaseActivity {

    TextView etixConfirm, etixTitle, etixTheater, etixShowtime, etixSeat;
    SimpleDraweeView etixPoster;
    ImageView etixOnBack;
    Screening screeningObject;
    SelectedSeat seatObject;
    String selectedShowTime;
    String providerName;
    TicketInfoRequest ticketRequest;
    CheckInRequest checkinRequest;
    PerformanceInfoRequest mPerformReq;
    View progressWheel;
    View root;

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
        screeningObject = Parcels.unwrap(getIntent().getParcelableExtra(SCREENING));

        etixTitle.setText(screeningObject.getTitle());
        etixShowtime.setText(selectedShowTime);
        etixTheater.setText(screeningObject.getTheaterName());
        etixSeat.setText("Seat " + seatObject.getSeatName());
        progressWheel = findViewById(R.id.etixprogress);

        Uri uri = Uri.parse(screeningObject.getImageUrl());

        etixPoster.setImageURI(uri);


        etixConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Bundle bundle = new Bundle();
                //new variables for data objects
                Screening screening = screeningObject;
                SelectedSeat seat = new SelectedSeat(seatObject.getSelectedSeatRow(), seatObject.getSelectedSeatColumn(), seatObject.getSeatName());

                bundle.putParcelable(SCREENING, Parcels.wrap(screening));
                bundle.putString(SHOWTIME, selectedShowTime);
                bundle.putParcelable(SEAT, Parcels.wrap(seat));


                ETicketFragment fragobj = new ETicketFragment();
                fragobj.setArguments(bundle);
                FragmentManager fm = getSupportFragmentManager();
                fragobj.show(fm, "fr_eticketconfirm_noticedialog");
            }
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


    public void reserve(Screening screening, String showtime, SelectedSeat selectedSeat) {
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(this).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(this).updateLocation(mCurrentLocation);
        SelectedSeatRequest selectedSeatRequest = new SelectedSeatRequest(selectedSeat.getSelectedSeatRow(), selectedSeat.getSelectedSeatColumn());


        if (screening.getProvider().getProviderName().equalsIgnoreCase("MOVIEXCHANGE")) {

            int normalizedMovieId = screening.getMoviepassId();
            String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
            String format = screening.getFormat();
            int tribuneTheaterId = screening.getTribuneTheaterId();
            int screeningId = screening.getProvider().getPerformanceInfo(showtime).getScreeningId();
            int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
            String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
            Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
            String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
            String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
            String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
            String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
            int theater = screening.getProvider().getTheater();
            String cinemaChainId = screening.getProvider().getPerformanceInfo(showtime).getCinemaChainId();
            String showtimeId = screening.getProvider().getPerformanceInfo(showtime).getShowtimeId();
            TicketType ticketType = screening.getProvider().getPerformanceInfo(showtime).getTicketType();


            PerformanceInfoRequest perform = new PerformanceInfoRequest(
                    normalizedMovieId,
                    externalMovieId,
                    format,
                    tribuneTheaterId,
                    screeningId,
                    dateTime,
                    performanceNumber,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId,
                    cinemaChainId,
                    ticketType,
                    showtimeId);

            ticketRequest = new TicketInfoRequest(perform, selectedSeatRequest);


        } else {
            //IF not movieXchange then it will simply request these parameters:
            int normalizedMovieId = screening.getMoviepassId();
            String externalMovieId = screening.getProvider().getPerformanceInfo(showtime).getExternalMovieId();
            String format = screening.getFormat();
            int tribuneTheaterId = screening.getTribuneTheaterId();
            int performanceNumber = screening.getProvider().getPerformanceInfo(showtime).getPerformanceNumber();
            String sku = screening.getProvider().getPerformanceInfo(showtime).getSku();
            Double price = screening.getProvider().getPerformanceInfo(showtime).getPrice();
            String dateTime = screening.getProvider().getPerformanceInfo(showtime).getDateTime();
            String auditorium = screening.getProvider().getPerformanceInfo(showtime).getAuditorium();
            String performanceId = screening.getProvider().getPerformanceInfo(showtime).getPerformanceId();
            String sessionId = screening.getProvider().getPerformanceInfo(showtime).getSessionId();
//            int theater = screeningObject.getProvider().getTheater();

            PerformanceInfoRequest request = new PerformanceInfoRequest(
                    dateTime,
                    externalMovieId,
                    performanceNumber,
                    tribuneTheaterId,
                    format,
                    normalizedMovieId,
                    sku,
                    price,
                    auditorium,
                    performanceId,
                    sessionId);
            ticketRequest = new TicketInfoRequest(request, selectedSeatRequest);


        }

        providerName = screening.getProvider().providerName;
        checkinRequest = new CheckInRequest(ticketRequest, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        reservationRequest(screening, checkinRequest, showtime, seatObject);
    }

    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest,
                                    final String showtime, final SelectedSeat selectedSeat) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null && reservationResponse.isOk()) {
                    progressWheel.setVisibility(View.GONE);
                    Reservation reservation = reservationResponse.getReservation();

                    String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();
                    String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();

                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode, selectedSeat);

                    showConfirmation(token);

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());

                        Toast.makeText(EticketConfirmation.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        progressWheel.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Log.d(TAG, "onResponse: " + e.getMessage());
                        Toast.makeText(EticketConfirmation.this, "YOYO", Toast.LENGTH_SHORT).show();
                        progressWheel.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void failure(RestError restError) {
                progressWheel.setVisibility(View.GONE);

                String hostname = "Unable to resolve host: No address associated with hostname";

                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains("none.get")) {
                    Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains(hostname.toLowerCase())) {
                    Toast.makeText(getApplicationContext(), R.string.data_connection, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().matches("You have a pending reservation")) {
                    Toast.makeText(getApplicationContext(), R.string.pending_reservation, Toast.LENGTH_LONG).show();
                } else if (restError != null) {
                    Log.d("resResponse:", "else onfail:" + "onRespnse fail");
                    Toast.makeText(getApplicationContext(), restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showConfirmation(ScreeningToken token) {
        Intent confirmationIntent = new Intent(EticketConfirmation.this, ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        finish();
    }


//    protected PerformanceInfoRequest checkProviderDoPerformanceInfoRequest() {
//
//        if (screeningObject.getProvider().getProviderName().equalsIgnoreCase("MOVIEXCHANGE")) {
//
//            int normalizedMovieId = screeningObject.getMoviepassId();
//            String externalMovieId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getExternalMovieId();
//            String format = screeningObject.getFormat();
//            int tribuneTheaterId = screeningObject.getTribuneTheaterId();
//            int screeningId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getScreeningId();
//            int performanceNumber = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceNumber();
//            String sku = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSku();
//            Double price = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPrice();
//            String dateTime = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getDateTime();
//            String auditorium = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getAuditorium();
//            String performanceId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceId();
//            String sessionId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSessionId();
//            int theater = screeningObject.getProvider().getTheater();
//            String cinemaChainId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getCinemaChainId();
//            String showtimeId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getShowtimeId();
//            TicketType ticketType = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getTicketType();
//
//
//            mPerformReq = new PerformanceInfoRequest(
//                    normalizedMovieId,
//                    externalMovieId,
//                    format,
//                    tribuneTheaterId,
//                    screeningId,
//                    dateTime,
//                    performanceNumber,
//                    sku,
//                    price,
//                    auditorium,
//                    performanceId,
//                    sessionId,
//                    cinemaChainId,
//                    ticketType,
//                    showtimeId);
////            getSeats(tribuneTheaterId, theater, mPerformReq);
//            return mPerformReq;
//
//
//        } else {
//            //IF not movieXchange then it will simply request these parameters:
//            int normalizedMovieId = screeningObject.getMoviepassId();
//            String externalMovieId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getExternalMovieId();
//            String format = screeningObject.getFormat();
//            int tribuneTheaterId = screeningObject.getTribuneTheaterId();
//            int performanceNumber = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceNumber();
//            String sku = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSku();
//            Double price = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPrice();
//            String dateTime = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getDateTime();
//            String auditorium = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getAuditorium();
//            String performanceId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getPerformanceId();
//            String sessionId = screeningObject.getProvider().getPerformanceInfo(selectedShowTime).getSessionId();
//            int theater = screeningObject.getProvider().getTheater();
//
//            mPerformReq = new PerformanceInfoRequest(
//                    dateTime,
//                    externalMovieId,
//                    performanceNumber,
//                    tribuneTheaterId,
//                    format,
//                    normalizedMovieId,
//                    sku,
//                    price,
//                    auditorium,
//                    performanceId,
//                    sessionId);
//
////            getSeats(tribuneTheaterId, theater, mPerformReq);
//
//            return mPerformReq;
//        }
//
//    }


}
