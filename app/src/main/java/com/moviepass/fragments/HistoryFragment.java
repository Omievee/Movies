package com.moviepass.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moviepass.R;
import com.moviepass.adapters.HistoryAdapter;
import com.moviepass.adapters.MoviesComingSoonAdapter;
import com.moviepass.adapters.MoviesNewReleasesAdapter;
import com.moviepass.model.Movie;
import com.moviepass.network.RestClient;
import com.moviepass.responses.HistoryResponse;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by anubis on 7/31/17.
 */

public class HistoryFragment extends Fragment {

    private HistoryAdapter historyAdapter;
    ArrayList<Movie> historyArrayList;
    @BindView(R.id.history_recycler_view)
    RecyclerView historyRecyclerView;

    View progress;
    TextView noHistory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, rootView);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Reservation History");

        progress = rootView.findViewById(R.id.progress);
        noHistory = rootView.findViewById(R.id.no_history);

        historyArrayList = new ArrayList<>();

        LinearLayoutManager newReleasesLayoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        historyRecyclerView = rootView.findViewById(R.id.history_recycler_view);
        historyRecyclerView.setLayoutManager(newReleasesLayoutManager);
        historyRecyclerView.setItemAnimator(null);

        historyAdapter = new HistoryAdapter(getActivity(), historyArrayList);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadHistory();
    }

    private void loadHistory() {

        progress.setVisibility(View.VISIBLE);

        RestClient.getAuthenticated().getReservations().enqueue(new Callback<HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryResponse> call, Response<HistoryResponse> response) {

                if (response.body() != null && response.isSuccessful()) {
                    HistoryResponse historyResponse = response.body();

                    progress.setVisibility(View.GONE);
                    historyArrayList.clear();

                    if (historyAdapter != null) {
                        historyRecyclerView.getRecycledViewPool().clear();
                        historyAdapter.notifyDataSetChanged();
                    }

                    if (historyResponse != null) {
                        Log.d("getHistory", String.valueOf(historyResponse.getHistory()));

                        if (!historyResponse.getHistory().isEmpty()) {
                            historyArrayList.addAll(historyResponse.getHistory());
                            historyRecyclerView.setAdapter(historyAdapter);
                        } else {
                            noHistory.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }

            @Override
            public void onFailure(Call<HistoryResponse> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Log.d("historyResponse", "Unable to download history: " + t.getMessage().toString());

            }
        });
    }
}
