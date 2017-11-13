package com.moviepass.requests;

import com.moviepass.activities.TicketType;

/**
 * Created by anubis on 6/20/17.
 */

public class PerformanceInfoRequest {

    int normalizedMovieId;
    String externalMovieId;
    String format;
    int tribuneTheaterId;
    int screeningId;
    String dateTime;
    int performanceNumber;
    String sku;
    Double price;
    String auditorium;
    String performanceId;
    String sessionId;
    String cinemaChainId;
    String showtimeId;
    TicketType ticketType;


    public PerformanceInfoRequest(int normalizedMovieId,
                                  String externalMovieId,
                                  String format,
                                  int tribuneTheaterId,
                                  int screeningId,
                                  String dateTime,
                                  int performanceNumber,
                                  String sku,
                                  Double price,
                                  String auditorium,
                                  String performanceId,
                                  String sessionId,
                                  String cinemaChainId,
                                  TicketType ticketType,
                                  String showtimeId
    ) {

        this.normalizedMovieId = normalizedMovieId;
        this.externalMovieId = externalMovieId;
        this.format = format;
        this.tribuneTheaterId = tribuneTheaterId;
        this.screeningId = screeningId;
        this.dateTime = dateTime;
        this.performanceNumber = performanceNumber;
        this.sku = sku;
        this.price = price;
        this.auditorium = auditorium;
        this.performanceId = performanceId;
        this.sessionId = sessionId;
        this.cinemaChainId = cinemaChainId;
        this.showtimeId = showtimeId;
        this.ticketType = ticketType;
    }

    public PerformanceInfoRequest(int normalizedMovieId,
                                  String externalMovieId,
                                  String format,
                                  int tribuneTheaterId,
                                  int screeningId,
                                  String dateTime) {

        this.normalizedMovieId = normalizedMovieId;
        this.externalMovieId = externalMovieId;
        this.format = format;
        this.tribuneTheaterId = tribuneTheaterId;
        this.screeningId = screeningId;
        this.dateTime = dateTime;
    }

    public PerformanceInfoRequest(String dateTime,
                                  String externalMovieId,
                                  int performanceNumber,
                                  int tribuneTheaterId,
                                  String format,
                                  int normalizedMovieId,
                                  String sku,
                                  Double price,
                                  String auditorium,
                                  String performanceId,
                                  String sessionId) {

        this.normalizedMovieId = normalizedMovieId;
        this.externalMovieId = externalMovieId;
        this.format = format;
        this.tribuneTheaterId = tribuneTheaterId;
        this.dateTime = dateTime;
        this.performanceNumber = performanceNumber;
        this.sku = sku;
        this.price = price;
        this.auditorium = auditorium;
        this.performanceId = performanceId;
        this.sessionId = sessionId;
    }

}
