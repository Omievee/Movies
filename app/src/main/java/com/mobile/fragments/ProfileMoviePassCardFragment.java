//package com.mobile.fragments;
//
//import android.app.Fragment;
//import android.content.DialogInterface;
//import android.os.Bundle;
//import android.support.v7.widget.DefaultItemAnimator;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.InputFilter;
//import android.text.InputType;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.mobile.UserPreferences;
//import com.mobile.model.MoviePassCard;
//import com.mobile.network.RestClient;
//import com.mobile.requests.CardActivationRequest;
//import com.mobile.responses.CardActivationResponse;
//import com.moviepass.R;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
///**
// * Created by anubis on 8/1/17.
// */
//
//public class ProfileMoviePassCardFragment extends Fragment {
//
//    ArrayList<MoviePassCard> moviePassCardArrayList;
//
//    @BindView(R.id.recycler_view)
//    RecyclerView moviepassCardRecyclerView;
//    @BindView(R.id.text_no_card)
//    TextView textNoCard;
//    @BindView(R.id.button_activate)
//    Button buttonActivate;
//    @BindView(R.id.progress)
//    View progress;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_profile_moviepass_card, container, false);
//        ButterKnife.bind(this, rootView);
//
////        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
////        toolbar.setTitle("MoviePass Card");
//
//        moviePassCardArrayList = new ArrayList<>();
//
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
//
//        moviepassCardRecyclerView = rootView.findViewById(R.id.recycler_view);
//        moviepassCardRecyclerView.setLayoutManager(mLayoutManager);
//
//        progress = rootView.findViewById(R.id.progress);
//        textNoCard = rootView.findViewById(R.id.text_no_card);
//        buttonActivate = rootView.findViewById(R.id.button_activate);
//
//        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
//        itemAnimator.setAddDuration(250);
//        itemAnimator.setRemoveDuration(250);
//        moviepassCardRecyclerView.setItemAnimator(itemAnimator);
//
//
//        if (isPendingSubscription()) {
//            buttonActivate.setVisibility(View.VISIBLE);
//            buttonActivate.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    showActivateCardDialog();
//                }
//            });
//        } else {
//            loadMoviePassCards();
//        }
//
//        return rootView;
//    }
//
//    private void loadMoviePassCards() {
//        progress.setVisibility(View.VISIBLE);
//
//        RestClient.getAuthenticated().getMoviePassCards().enqueue(new Callback<List<MoviePassCard>>() {
//            @Override
//            public void onResponse(Call<List<MoviePassCard>> call, Response<List<MoviePassCard>> response) {
//                progress.setVisibility(View.GONE);
//
//                if (response.isSuccessful() && response.body() != null) {
//                    List<MoviePassCard> moviePassCardsResponse = response.body();
//
//                    moviePassCardArrayList.clear();
//
//                    if (moviePassCardAdapter != null) {
//                        moviepassCardRecyclerView.getRecycledViewPool().clear();
//                        moviePassCardAdapter.notifyDataSetChanged();
//                    }
//
//                    moviePassCardArrayList.addAll(moviePassCardsResponse);
//
//                    Log.d("resultList", "resultList: " + moviePassCardsResponse);
//
//                    if (moviePassCardArrayList != null && moviePassCardArrayList.size() == 0) {
//                        moviepassCardRecyclerView.setVisibility(View.GONE);
//                        textNoCard.setVisibility(View.VISIBLE);
//                    } else {
//                        moviepassCardRecyclerView.setVisibility(View.VISIBLE);
//                        textNoCard.setVisibility(View.GONE);
//
//                        moviepassCardRecyclerView.setAdapter(moviePassCardAdapter);
//                        moviepassCardRecyclerView.setTranslationY(0);
//                        moviepassCardRecyclerView.setAlpha(1.0f);
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<List<MoviePassCard>> call, Throwable t) {
//                progress.setVisibility(View.GONE);
//            }
//        });
//    }
//
//    public boolean isPendingSubscription() {
//        if (UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_ACTIVATION") ||
//                UserPreferences.getRestrictionSubscriptionStatus().matches("PENDING_FREE_TRIAL")) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private void showActivateCardDialog() {
//        View dialoglayout = getActivity().getLayoutInflater().inflate(R.layout.dialog_activate_card, null);
//        android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getActivity());
//        alert.setView(dialoglayout);
//
//        final EditText editText = dialoglayout.findViewById(R.id.activate_card);
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
//        InputFilter[] filters = new InputFilter[1];
//        filters[0] = new InputFilter.LengthFilter(4);
//        editText.setFilters(filters);
//
//        alert.setTitle(getString(R.string.dialog_activate_card_header));
//        alert.setMessage(R.string.dialog_activate_card_enter_card_digits);
//        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(final DialogInterface dialog, int which) {
//                String digits = editText.getText().toString();
//                dialog.dismiss();
//
//                if (digits.length() == 4) {
//                    CardActivationRequest request = new CardActivationRequest(digits);
//                    progress.setVisibility(View.VISIBLE);
//
//                    RestClient.getAuthenticated().activateCard(request).enqueue(new retrofit2.Callback<CardActivationResponse>() {
//                        @Override
//                        public void onResponse(Call<CardActivationResponse> call, Response<CardActivationResponse> response) {
//                            CardActivationResponse cardActivationResponse = response.body();
//                            progress.setVisibility(View.GONE);
//
//                            if (cardActivationResponse != null && response.isSuccessful()) {
//                                String cardActivationResponseMessage = cardActivationResponse.getMessage();
//                                Toast.makeText(getActivity(), R.string.dialog_activate_card_successful, Toast.LENGTH_LONG).show();
//                                buttonActivate.setVisibility(View.GONE);
//                                loadMoviePassCards();
//                            } else {
//                                Toast.makeText(getActivity(), R.string.dialog_activate_card_bad_four_digits, Toast.LENGTH_LONG).show();
//                            }
//
//                        }
//
//                        @Override
//                        public void onFailure(Call<CardActivationResponse> call, Throwable t) {
//                            progress.setVisibility(View.GONE);
//
//                            showActivateCardDialog();
//                        }
//                    });
//                } else {
//                    Toast.makeText(getActivity(), R.string.dialog_activate_card_must_enter_four_digits, Toast.LENGTH_LONG).show();
//                }
//            }
//        });
//        alert.setNegativeButton("Activate Later", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(final DialogInterface dialog, int which) {
//                Toast.makeText(getActivity(), R.string.dialog_activate_card_must_activate_future, Toast.LENGTH_LONG).show();
//                dialog.dismiss();
//            }
//        });
//        alert.show();
//    }
//
//    void manageVisiblity() {
//        if (moviePassCardArrayList != null && moviePassCardArrayList.size() == 0) {
//            moviepassCardRecyclerView.setVisibility(View.GONE);
//            textNoCard.setVisibility(View.VISIBLE);
//        } else {
//            moviepassCardRecyclerView.setVisibility(View.VISIBLE);
//            textNoCard.setVisibility(View.GONE);
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//    }
//}
//
