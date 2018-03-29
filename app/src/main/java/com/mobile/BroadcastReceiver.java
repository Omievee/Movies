package com.mobile;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.braintreepayments.api.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.helpshift.Core;
import com.mobile.activities.MoviesActivity;
import com.taplytics.sdk.TLGcmBroadcastReceiver;
import com.taplytics.sdk.TLGcmIntentService;
import com.taplytics.sdk.Taplytics;
import com.taplytics.sdk.TaplyticsPushTokenListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by anubis on 2/10/18.
 */

public class BroadcastReceiver extends TLGcmBroadcastReceiver {

    String webURL;

    @Override
    public void pushOpened(Context context, Intent intent) {
        if (intent.getExtras() != null) {

            Bundle bundle = intent.getExtras();
            JSONObject json = new JSONObject();
            Set<String> keys = bundle.keySet();

            for (String key : keys) {
                try {
                    // json.put(key, bundle.get(key)); see edit below
                    json.put(key, JSONObject.wrap(bundle.get(key)));
                    Log.d("jsonthings", json.put(key, JSONObject.wrap(bundle.get(key))).toString());
                    Log.d("custom_keys", bundle.get("custom_keys").toString());
                    Object newBundle = bundle.get(Constants.CUSTOM_DATA);


                    Taplytics.setTaplyticsPushTokenListener(s -> {

                        if (bundle.get("custom_keys") != null) {
                            String resultJSON = bundle.get("custom_keys").toString();
                            try {
                                JSONObject root = new JSONObject(resultJSON);
                                String array = root.getString("external_url");

                                Intent notifIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(array));
                                context.startActivity(notifIntent);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            super.pushOpened(context, intent);
                        }


                    });

                } catch (JSONException e) {
                    //Handle exception here
                }
            }
        }


//        if (intent.getDataString() != null) {
//            Log.d("intent", intent.getDataString());
//        }
//
//        if (intent.getStringExtra("external_url") != null) {
//            Log.d("intentURL", intent.getStringExtra("external_url"));
//        }
//
//        JSONObject customKeys = new JSONObject();
//        Taplytics.trackPushOpen("tl_id", customKeys);
//        try {
//            String externalURL = customKeys.getString("external_url");
//            Log.d("externalUrl", externalURL);
//        } catch (Exception e) {
//
//        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent!=null){
            String origin = intent.getExtras().getString("origin");
            if(origin!=null && origin.equalsIgnoreCase("helpshift")){
                Core.handlePush(context,intent);
            }
            ComponentName comp = new ComponentName(context.getPackageName(), TLGcmIntentService.class.getName());
            startWakefulService(context,intent.setComponent(comp));
            setResultCode(Activity.RESULT_OK);
        }
    }

    @Override
    public void pushDismissed(Context context, Intent intent) {
        //The push has been dismissed :(

    }

    @Override
    public void pushReceived(Context context, Intent intent) {
        //The push was received, but not opened yet!

        /*
        If you add the custom data of tl_silent = true to the push notification,
        there will be no push notification presented to the user. However, this will
        still be triggered, meaning you can use this to remotely trigger something
        within the application!
         */
    }

}
