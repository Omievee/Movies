package com.mobile.helpers;

import android.content.Context;

/**
 * Created by o_vicarra on 2/23/18.
 */

public class ContextSingleton {
    public static ContextSingleton instance;
    public final Context currentContext;

    private ContextSingleton(Context context) {
        currentContext = context;

    }

    public static ContextSingleton getInstance(Context context) {

        synchronized (ContextSingleton.class) {
            if (instance == null) {
                instance = new ContextSingleton(context);
            }
            return instance;
        }
    }


    public String getGlobalContext() {
        return currentContext.getPackageName();
    }


}
