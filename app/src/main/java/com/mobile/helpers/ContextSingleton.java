package com.mobile.helpers;

/**
 * Created by o_vicarra on 2/23/18.
 */

public class ContextSingleton {
    private static ContextSingleton instance;

    private ContextSingleton() {

    }

    public static ContextSingleton getInstance() {

        synchronized (ContextSingleton.class) {
            if (instance == null) {
                instance = new ContextSingleton();
            }
            return instance;
        }
    }


}
