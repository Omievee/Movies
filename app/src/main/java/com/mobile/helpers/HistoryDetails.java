package com.mobile.helpers;

import android.content.Context;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.AttributeSet;

/**
 * Created by o_vicarra on 3/27/18.
 */

public class HistoryDetails extends TransitionSet {

    public HistoryDetails() {
        init();
    }

    /**
     * This constructor allows us to use this transition in XML
     */
    public HistoryDetails(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).
                addTransition(new ChangeTransform()).
                addTransition(new ChangeImageTransform());
    }
}
