package com.mobile.extensions;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

public class CustomAutoCompleteDropDown extends AppCompatAutoCompleteTextView {

        //    implements AdapterView.OnItemClickListener
        private static final int MAX_CLICK_DURATION = 200;
        private long startClickTime;
        private boolean isPopup;
        private int mPosition = ListView.INVALID_POSITION;

        public CustomAutoCompleteDropDown(Context context) {
            super(context);
//        setOnItemClickListener(this);
        }

        public CustomAutoCompleteDropDown(Context arg0, AttributeSet arg1) {
            super(arg0, arg1);
//        setOnItemClickListener(this);
        }

        public CustomAutoCompleteDropDown(Context arg0, AttributeSet arg1, int arg2) {
            super(arg0, arg1, arg2);
//        setOnItemClickListener(this);
        }

        @Override
        public boolean enoughToFilter() {
            return true;
        }

        @Override
        protected void onFocusChanged(boolean focused, int direction,
                                      Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            if (focused) {
                performFiltering("", 0);
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
                setKeyListener(null);
                dismissDropDown();
            } else {
                isPopup = false;
            }
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP: {
                    if (isPopup) {
                        dismissDropDown();
                    } else {
                        requestFocus();
                        showDropDown();
                    }
                    break;
                }
            }

            return super.onTouchEvent(event);
        }

        @Override
        public void showDropDown() {
            super.showDropDown();
            isPopup = true;
        }

        @Override
        public void dismissDropDown() {
            super.dismissDropDown();
            isPopup = false;
        }

        public int getPosition() {
            return mPosition;
        }
}
