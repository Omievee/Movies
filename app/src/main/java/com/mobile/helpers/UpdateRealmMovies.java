package com.mobile.helpers;

import android.content.Context;
import android.content.Intent;

import com.mobile.BroadcastReceiver;
import com.mobile.activities.MoviesActivity;
import com.mobile.fragments.MoviesFragment;

/**
 * Created by o_vicarra on 3/16/18.
 */

public class UpdateRealmMovies extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Intent updateMovies = new Intent(context, MoviesFragment.class);
        context.startService(updateMovies);
    }
}
