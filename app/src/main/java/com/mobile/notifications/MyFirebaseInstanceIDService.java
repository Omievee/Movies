package com.mobile.notifications;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.helpshift.support.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.helpshift.Core;
import com.mobile.Constants;
import com.mobile.UserPreferences;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
         Log.d(TAG, "Refreshed token: " + refreshedToken);

        UserPreferences.saveFirebaseHelpshiftToken(refreshedToken);

        Core.registerDeviceToken(this,refreshedToken);
    }

}