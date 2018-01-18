package com.mobile.responses;

import android.support.annotation.NonNull;

import com.mobile.model.Screening;
import com.mobile.model.Theater;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by anubis on 6/10/17.
 */

@Parcel
public class ScreeningsResponse implements Comparable {

    public String availability;
    public List<Screening> screenings;
    public List<Theater> theaters;

    public List<Theater> getTheaters() {
        return theaters;
    }

    public List<Screening> getScreenings() {
        return screenings;
    }

    @Override
    public int compareTo(@NonNull Object o) {

        return 0;
    }
}
