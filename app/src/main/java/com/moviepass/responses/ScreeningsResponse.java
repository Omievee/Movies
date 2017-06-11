package com.moviepass.responses;

import com.moviepass.model.Screening;

import org.parceler.Parcel;

import java.util.List;

/**
 * Created by anubis on 6/10/17.
 */

public class ScreeningsResponse {

    public String availability;
    public List<Screening> screenings;

    public List<Screening> getScreenings() {
        return screenings;
    }
}
