package com.mobile

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.helpshift.Core
import com.taplytics.sdk.TLGcmBroadcastReceiver
import com.taplytics.sdk.Taplytics
import org.json.JSONException
import org.json.JSONObject

class TaplyticsReceiver : TLGcmBroadcastReceiver() {


    override fun pushOpened(p0: Context?, p1: Intent?) {
        super.pushOpened(p0, p1)
        if (p1?.extras != null) {

            val bundle = p1.extras;
            val json = JSONObject();
            val keys = bundle.keySet()

            for (key: String in keys) {
                try {
                    // json.put(key, bundle.get(key)); see edit below
                    json.put(key, JSONObject.wrap(bundle.get(key)));
                    val newBundle = bundle.get(Constants.CUSTOM_DATA);

                    Taplytics.setTaplyticsPushTokenListener { s ->
                        if (bundle.get("custom_keys") != null) {
                            val resultJSON = bundle.get("custom_keys").toString();
                            try {
                                val root = JSONObject(resultJSON);
                                val array = root.getString("external_url");

                                val notifIntent = Intent(Intent.ACTION_VIEW, Uri.parse(array));
                                p0?.startActivity(notifIntent);


                            } catch (e: JSONException) {
                                e.printStackTrace();
                            }
                        } else {
                            super.pushOpened(p0, p1)
                        }

                    }


                } catch (e: JSONException) {
                    //Handle exception here
                }
            }
        }
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        super.onReceive(p0, p1)
        if (p1 != null) {
            val origin = p1.extras.getString("origin");
            if (origin != null && origin == "helpshift") {
                Core.handlePush(p0, p1)
            }
        }
    }
}