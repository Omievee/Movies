package com.mobile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.braintreepayments.api.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.taplytics.sdk.TLGcmBroadcastReceiver;
import com.taplytics.sdk.Taplytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

/**
 * Created by anubis on 2/10/18.
 */

public class BroadcastReceiver extends TLGcmBroadcastReceiver {

    @Override
    public void pushOpened(Context context, Intent intent) {

        //A user clicked on the notification! Do whatever you want here!

        /* If you call through to the super,
        Taplytics will launch your app's LAUNCH activity.
        This is optional. */
        super.pushOpened(context, intent);

        if (intent.getExtras() != null) {

            Bundle bundle = intent.getExtras();

            JSONObject json = new JSONObject();
            Set<String> keys = bundle.keySet();
            for (String key : keys) {
                try {
                    // json.put(key, bundle.get(key)); see edit below
                    json.put(key, JSONObject.wrap(bundle.get(key)));
                    Log.d("jsonthings", json.put(key, JSONObject.wrap(bundle.get(key))).toString());

                    Log.d("custon_keys", bundle.get("custom_keys").toString());

                    Object newBundle = bundle.get("custom_keys");

                    bundle.get("custom_keys");


                } catch(JSONException e) {
                    //Handle exception here
                }
            }
        }

        if (intent.getDataString() != null) {
            Log.d("intent", intent.getDataString());
        }

        if (intent.getStringExtra("external_url") != null) {
            Log.d("intentURL", intent.getStringExtra("external_url"));
        }

        JSONObject customKeys = new JSONObject();
        Taplytics.trackPushOpen("tl_id", customKeys);
        try {
            String externalURL = customKeys.getString("external_url");
            Log.d("externalUrl", externalURL);
        } catch (Exception e) {

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
