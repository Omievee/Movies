package com.mobile.helpers;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.mobile.Constants;
import com.mobile.fragments.TheatersFragment;
import com.mobile.model.Theater;
import com.mobile.network.RestClient;
import com.mobile.responses.LocalStorageTheaters;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by o_vicarra on 3/28/18.
 */

public class RealmTaskService extends GcmTaskService {

    public static final String GCM_REPEAT_TAG = "repeat|[7200,1800]";

    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        scheduleRepeatTask(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {

        Bundle extras = taskParams.getExtras();
        Handler h = new Handler(getMainLooper());

        if (taskParams.getTag().equals(GCM_REPEAT_TAG)) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    RestClient.getLocalStorageAPI().getAllMoviePassTheaters().enqueue(new Callback<LocalStorageTheaters>() {
                        @Override
                        public void onResponse(Call<LocalStorageTheaters> call, Response<LocalStorageTheaters> response) {
                            LocalStorageTheaters locallyStoredTheaters = response.body();
                            if (locallyStoredTheaters != null && response.isSuccessful()) {
                                TheatersFragment.tRealm.executeTransactionAsync(R -> {

                                    for (int j = 0; j < locallyStoredTheaters.getTheaters().size(); j++) {
                                        Theater RLMTH = R.createObject(Theater.class, locallyStoredTheaters.getTheaters().get(j).getId());
                                        RLMTH.setMoviepassId(locallyStoredTheaters.getTheaters().get(j).getMoviepassId());
                                        RLMTH.setTribuneTheaterId(locallyStoredTheaters.getTheaters().get(j).getTribuneTheaterId());
                                        RLMTH.setName(locallyStoredTheaters.getTheaters().get(j).getName());
                                        RLMTH.setAddress(locallyStoredTheaters.getTheaters().get(j).getAddress());
                                        RLMTH.setCity(locallyStoredTheaters.getTheaters().get(j).getCity());
                                        RLMTH.setState(locallyStoredTheaters.getTheaters().get(j).getState());
                                        RLMTH.setZip(locallyStoredTheaters.getTheaters().get(j).getZip());
                                        RLMTH.setDistance(locallyStoredTheaters.getTheaters().get(j).getDistance());
                                        RLMTH.setLat(locallyStoredTheaters.getTheaters().get(j).getLat());
                                        RLMTH.setLon(locallyStoredTheaters.getTheaters().get(j).getLon());
                                        RLMTH.setTicketType(locallyStoredTheaters.getTheaters().get(j).getTicketType());

                                    }
                                }, () -> {
                                    Log.d(Constants.TAG, "onSuccess: ");
                                }, error -> {
                                    // Transaction failed and was automatically canceled.
                                    Log.d(Constants.TAG, "Realm onError: " + error.getMessage());
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<LocalStorageTheaters> call, Throwable t) {
                            Toast.makeText(RealmTaskService.this, "Error while downloading Theaters.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Toast.makeText(RealmTaskService.this, "EXECUTED!", Toast.LENGTH_SHORT).show();

                }
            });
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }


    public static void scheduleRepeatTask(Context context) {
        try {
            PeriodicTask periodic = new PeriodicTask.Builder()
                    //specify target service - must extend GcmTaskService
                    .setService(RealmTaskService.class)
                    //repeat every 60 seconds
                    .setPeriod(43200)
                    //specify how much earlier the task can be executed (in seconds)
                    .setFlex(7200)
                    //tag that is unique to this task (can be used to cancel task)
                    .setTag(GCM_REPEAT_TAG)
                    //whether the task persists after device reboot
                    .setPersisted(true)
                    //set required network state, this line is optional
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    .build();


            GcmNetworkManager.getInstance(context).schedule(periodic);
            Log.d(Constants.TAG, "repeating task scheduled");
        } catch (Exception e) {
            Log.e(Constants.TAG, "scheduling failed");
            e.printStackTrace();
        }
    }
}
