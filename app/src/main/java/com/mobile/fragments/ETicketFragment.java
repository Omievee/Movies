package com.mobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.mobile.UserLocationManagerFused;
import com.mobile.activities.ConfirmationActivity;
import com.mobile.activities.TicketType;
import com.mobile.model.Reservation;
import com.mobile.model.Screening;
import com.mobile.model.ScreeningToken;
import com.mobile.model.SelectedSeat;
import com.mobile.network.RestCallback;
import com.mobile.network.RestClient;
import com.mobile.network.RestError;
import com.mobile.requests.CheckInRequest;
import com.mobile.requests.PerformanceInfoRequest;
import com.mobile.requests.SelectedSeatRequest;
import com.mobile.requests.TicketInfoRequest;
import com.mobile.responses.ReservationResponse;
import com.moviepass.R;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by anubis on 5/31/17.
 */

public class ETicketFragment extends DialogFragment {
    public static final String SCREENING = "screening";
    public static final String SHOWTIME = "showtime";
    public static final String SEAT = "seat";
    public static final String PATTERN = "630425";
    View rootView;
    PatternLockView lockView;
    PatternLockViewListener getmPatternLockViewListener;
    String getShowtime;
    SelectedSeat getSeat;
    Screening getTitle;
    SelectedSeat seatObject;

    public static final String TOKEN = "token";
    String providerName;
    TicketInfoRequest ticketRequest;
    CheckInRequest checkinRequest;
    PerformanceInfoRequest mPerformReq;

    View progressWheel;

    public ETicketFragment() {
    }

    public static ETicketFragment newInstance() {
        return new ETicketFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fr_eticketconfirm_noticedialog, container);
        progressWheel = rootView.findViewById(R.id.lock_confirm);
        lockView = rootView.findViewById(R.id.LOCKVIEW);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        lockView.addPatternLockListener(mPatternLockViewListener);

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            getTitle = Parcels.unwrap(bundle.getParcelable(SCREENING));
            getShowtime = bundle.getString(SHOWTIME);
            getSeat = Parcels.unwrap(bundle.getParcelable(SEAT));
        }
    }

    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {

        }


        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {

        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            if (PatternLockUtils.patternToString(lockView, pattern).equals("6304258")) {
                if (getSeat != null) {
                    reserveWithSeat(getTitle, getShowtime, getSeat);

                } else {
                    reserveNoSeat(getTitle, getShowtime);
                }
            } else {
                Toast.makeText(getActivity(), "Incorrect Pattern", Toast.LENGTH_SHORT).show();
                lockView.clearPattern();
            }
            progressWheel.setVisibility(View.VISIBLE);


        }

        @Override
        public void onCleared() {

        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.getActivity();
    }


    @Override
    public void dismiss() {
        super.dismiss();
    }


    public void reserveWithSeat(Screening screening, String showtime, SelectedSeat selectedSeat) {
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(getActivity()).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(getActivity()).updateLocation(mCurrentLocation);

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
        reservationRequest(screening, checkinRequest, showtime, selectedSeat);
    }

    private void reservationRequest(final Screening screening, CheckInRequest checkInRequest, final String showtime, final SelectedSeat selectedSeat) {
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
                    dismiss();

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getActivity(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        dismiss();
                        progressWheel.setVisibility(View.GONE);
                    } catch (Exception e) {
//                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismiss();
                        progressWheel.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void failure(RestError restError) {
                progressWheel.setVisibility(View.GONE);
                String hostname = "Unable to resolve host: No address associated with hostname";

                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains("none.get")) {
                    Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains(hostname.toLowerCase())) {
                    Toast.makeText(getActivity(), R.string.data_connection, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().matches("You have a pending reservation")) {
                    Toast.makeText(getActivity(), R.string.pending_reservation, Toast.LENGTH_LONG).show();
                } else if (restError != null) {
                    Log.d("resResponse:", "else onfail:" + "onRespnse fail");
                    //TODO Check why null sometimes?
//                    Toast.makeText(getActivity(), restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showConfirmation(ScreeningToken token) {
        Intent confirmationIntent = new Intent(getActivity(), ConfirmationActivity.class);
        confirmationIntent.putExtra(TOKEN, Parcels.wrap(token));
        startActivity(confirmationIntent);
        getActivity().finish();
    }


    public void reserveNoSeat(Screening screening, String showtime) {
        Location mCurrentLocation = UserLocationManagerFused.getLocationInstance(getActivity()).mCurrentLocation;
        UserLocationManagerFused.getLocationInstance(getActivity()).updateLocation(mCurrentLocation);


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

            ticketRequest = new TicketInfoRequest(perform);


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
            ticketRequest = new TicketInfoRequest(request);


        }

        providerName = screening.getProvider().providerName;
        checkinRequest = new CheckInRequest(ticketRequest, providerName, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        reservationRequestNoSeat(screening, checkinRequest, showtime);
    }

    private void reservationRequestNoSeat(final Screening screening, CheckInRequest checkInRequest, final String showtime) {
        RestClient.getAuthenticated().checkIn(checkInRequest).enqueue(new RestCallback<ReservationResponse>() {
            @Override
            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
                ReservationResponse reservationResponse = response.body();

                if (reservationResponse != null && reservationResponse.isOk()) {
                    progressWheel.setVisibility(View.GONE);
                    Reservation reservation = reservationResponse.getReservation();

                    String confirmationCode = reservationResponse.getE_ticket_confirmation().getConfirmationCode();
                    String qrUrl = reservationResponse.getE_ticket_confirmation().getBarCodeUrl();

                    ScreeningToken token = new ScreeningToken(screening, showtime, reservation, qrUrl, confirmationCode);

                    showConfirmation(token);
                    dismiss();

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getActivity(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        dismiss();
                        progressWheel.setVisibility(View.GONE);
                    } catch (Exception e) {
                        Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismiss();
                        progressWheel.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void failure(RestError restError) {
                progressWheel.setVisibility(View.GONE);

                String hostname = "Unable to resolve host: No address associated with hostname";

                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains("none.get")) {
                    Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().contains(hostname.toLowerCase())) {
                    Toast.makeText(getActivity(), R.string.data_connection, Toast.LENGTH_LONG).show();
                }
                if (restError != null && restError.getMessage() != null && restError.getMessage().toLowerCase().matches("You have a pending reservation")) {
                    Toast.makeText(getActivity(), R.string.pending_reservation, Toast.LENGTH_LONG).show();
                } else if (restError != null) {
                    Log.d("resResponse:", "else onfail:" + "onRespnse fail");
                    Toast.makeText(getActivity(), restError.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
