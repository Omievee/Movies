package com.mobile;

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
import com.mobile.activities.MoviesActivity;
import com.taplytics.sdk.TLGcmBroadcastReceiver;
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

        //A user clicked on the notification! Do whatever you want here!

        /* If you call through to the super,
        Taplytics will launch your app's LAUNCH activity.
        This is optional. */

//        super.pushOpened(context, intent);

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

                        String resultJSON = bundle.get("custom_keys").toString();
                        try {
                            JSONObject root = new JSONObject(resultJSON);
                            String array = root.getString("external_url");

                            Log.d(Constants.TAG, "pushOpened: " + array.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        try {
//                            JSONObject jsonObject = new JSONObject(key);
//                            Iterator<String> myKeys = jsonObject.keys();
//                            if (jsonObject.has("custom_keys")) {
//                                Log.d(Constants.TAG, "true: ");
//                            }
//                            while (myKeys.hasNext()) {
//                                String k = myKeys.next();
//                                JSONObject innterOBJ = jsonObject.getJSONObject(k);
//                                Iterator<String> innerK = innterOBJ.keys();
//                                while (innerK.hasNext()) {
//                                    String innerKEY = myKeys.next();
//                                    String value = innterOBJ.getString(innerKEY);
//
//                                    Log.d(Constants.TAG, "pushOpened: " + value.toString());
//                                }
////                            }
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                    });

                } catch (JSONException e) {
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
