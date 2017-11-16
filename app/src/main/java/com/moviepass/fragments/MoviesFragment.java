package com.moviepass.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.view.ViewCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.moviepass.MoviePosterClickListener;
import com.moviepass.R;
import com.moviepass.UserPreferences;
import com.moviepass.activities.MovieActivity;
import com.moviepass.adapters.MoviesComingSoonAdapter;
import com.moviepass.adapters.MoviesNewReleasesAdapter;
import com.moviepass.adapters.MoviesTopBoxOfficeAdapter;
import com.moviepass.model.Movie;
import com.moviepass.model.MoviesResponse;
import com.moviepass.network.Api;
import com.moviepass.network.RestClient;
import com.moviepass.requests.CardActivationRequest;
import com.moviepass.responses.CardActivationResponse;

import org.parceler.Parcels;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ryan on 4/25/17.
 */

public class MoviesFragment extends Fragment implements MoviePosterClickListener {

    public static final String MOVIES = "movies";
    public static final String EXTRA_MOVIE_IMAGE_TRANSITION_NAME = "movie_image_transition_name";
    public static final String EXTRA_MOVIE_ITEM = "movie_image_url";

    Api api;

    private MoviesNewReleasesAdapter mMoviesNewReleasesAdapter;
    private MoviesTopBoxOfficeAdapter mMoviesTopBoxOfficeAdapter;
    private MoviesComingSoonAdapter mMoviesComingSoonAdapter;

    MoviesFragment mMoviesFragment;
    MoviesResponse mMoviesResponse;
    ArrayList<Movie> mMovieArrayList;

    ArrayList<Movie> mMoviesTopBoxOffice;
    ArrayList<Movie> mMoviesComingSoon;
    ArrayList<Movie> mMoviesNewReleases;

    @BindView(R.id.new_releases)
    RecyclerView mNewReleasesRecyclerView;
    @BindView(R.id.top_box_office)
    RecyclerView mTopBoxOfficeRecyclerView;
    @BindView(R.id.coming_soon)
    RecyclerView mComingSoonRecyclerView;

    View progress;

    private OnFragmentInteractionListener listener;

    public static MoviesFragment newInstance() {
        return new MoviesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, rootView);

        progress = rootView.findViewById(R.id.progress);

        Api api;

        mMoviesNewReleases = new ArrayList<>();
        mMoviesTopBoxOffice = new ArrayList<>();
        mMoviesComingSoon = new ArrayList<>();

        /* New Releases RecyclerView */
        LinearLayoutManager newReleasesLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mNewReleasesRecyclerView = rootView.findViewById(R.id.new_releases);
        mNewReleasesRecyclerView.setLayoutManager(newReleasesLayoutManager);
        mNewReleasesRecyclerView.setItemAnimator(null);

        mMoviesNewReleasesAdapter = new MoviesNewReleasesAdapter(getActivity(), mMoviesNewReleases, this);

        /* Top Box Office RecyclerView */
        LinearLayoutManager topBoxOfficeLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mTopBoxOfficeRecyclerView = rootView.findViewById(R.id.top_box_office);
        mTopBoxOfficeRecyclerView.setLayoutManager(topBoxOfficeLayoutManager);
        mTopBoxOfficeRecyclerView.setItemAnimator(null);

        mMoviesTopBoxOfficeAdapter = new MoviesTopBoxOfficeAdapter(getActivity(), mMoviesTopBoxOffice, this);

        /* Coming Soon RecyclerView */
        LinearLayoutManager comingSoonLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);

        mComingSoonRecyclerView = rootView.findViewById(R.id.coming_soon);
        mComingSoonRecyclerView.setLayoutManager(comingSoonLayoutManager);
        mComingSoonRecyclerView.setItemAnimator(null);

        mMoviesComingSoonAdapter = new MoviesComingSoonAdapter(getActivity(), mMoviesComingSoon, this);

        loadMovies();

        if (isPendingSubscription()) {
            showActivateCardDialog();
        }




        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mMoviesNewReleasesAdapter != null) {
            mNewReleasesRecyclerView.setAdapter(mMoviesNewReleasesAdapter);
        }

        if (mTopBoxOfficeRecyclerView != null) {
            mTopBoxOfficeRecyclerView.setAdapter(mMoviesTopBoxOfficeAdapter);
        }

        if (mComingSoonRecyclerView != null) {
            mComingSoonRecyclerView.setAdapter(mMoviesComingSoonAdapter);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        /*if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    //TODO:
    public void onMoviePosterClick(int pos, Movie movie, ImageView sharedImageView) {
        Intent movieIntent = new Intent(getActivity(), MovieActivity.class);
        movieIntent.putExtra(MovieActivity.MOVIE, Parcels.wrap(movie));
        movieIntent.putExtra(EXTRA_MOVIE_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                sharedImageView,
                ViewCompat.getTransitionName(sharedImageView));

        startActivity(movieIntent, options.toBundle());
    }

    private void loadMovies() {
        RestClient.getAuthenticated().getMovies(UserPreferences.getLatitude(), UserPreferences.getLongitude()).enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {

                if (response.body() != null && response.isSuccessful()) {
                    mMoviesResponse = response.body();

                    mMoviesNewReleases.clear();
                    mMoviesTopBoxOffice.clear();
                    mMoviesComingSoon.clear();

                    if (mMoviesNewReleasesAdapter != null) {
                        mNewReleasesRecyclerView.getRecycledViewPool().clear();
                        mMoviesNewReleasesAdapter.notifyDataSetChanged();
                    }

                    if (mMoviesTopBoxOfficeAdapter != null) {
                        mTopBoxOfficeRecyclerView.getRecycledViewPool().clear();
                        mMoviesTopBoxOfficeAdapter.notifyDataSetChanged();
                    }

                    if (mMoviesComingSoonAdapter != null) {
                        mComingSoonRecyclerView.getRecycledViewPool().clear();
                        mMoviesComingSoonAdapter.notifyDataSetChanged();
                    }

                    if (mMoviesResponse != null) {
                        mMoviesNewReleases.addAll(mMoviesResponse.getNewReleases());
                        mNewReleasesRecyclerView.setAdapter(mMoviesNewReleasesAdapter);

                        mMoviesTopBoxOffice.addAll(mMoviesResponse.getTopBoxOffice());
                        mTopBoxOfficeRecyclerView.setAdapter(mMoviesTopBoxOfficeAdapter);

                        mMoviesComingSoon.addAll(mMoviesResponse.getComingSoon());
                        mComingSoonRecyclerView.setAdapter(mMoviesComingSoonAdapter);

                    }


                } else {
                    /* TODO : FIX IF RESPONSE IS NULL */

                }
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {

            }
        });
    }

    public boolean isPendingSubscription() {
        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
            return true;
        } else {
            return false;
        }
    }

    private void showActivateCardDialog() {
        View dialoglayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_activate_card, null);
        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getActivity());
        alert.setView(dialoglayout);

        final EditText editText = dialoglayout.findViewById(R.id.activate_card);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(4);
        editText.setFilters(filters);

        alert.setTitle(getString(R.string.dialog_activate_card_header));
        alert.setMessage(R.string.dialog_activate_card_enter_card_digits);
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                String digits = editText.getText().toString();
                dialog.dismiss();

                if (digits.length() == 4) {
                    CardActivationRequest request = new CardActivationRequest(digits);
                    progress.setVisibility(View.VISIBLE);

                    RestClient.getAuthenticated().activateCard(request).enqueue(new retrofit2.Callback<CardActivationResponse>() {
                        @Override
                        public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
                            CardActivationResponse cardActivationResponse = response.body();
                            progress.setVisibility(View.GONE);

                            if (cardActivationResponse != null && response.isSuccessful()) {
                                String cardActivationResponseMessage = cardActivationResponse.getMessage();
                                Toast.makeText(getActivity(), R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
                            }

                        }
                        @Override
                        public void onFailure(Call<CardActivationResponse> call, Throwable t) {
                            progress.setVisibility(View.GONE);
                            showActivateCardDialog();
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), R.string.dialog_activate_card_must_enter_four_digits, Toast.LENGTH_LONG).show();
                }
            }
        });
        alert.setNegativeButton("Activate Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                Toast.makeText(getActivity(), R.string.dialog_activate_card_must_activate_future, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public interface OnFragmentInteractionListener {
    }


}
