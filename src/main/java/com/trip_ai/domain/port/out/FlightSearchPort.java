package com.trip_ai.domain.port.out;

import com.trip_ai.domain.model.FlightOffer;

import java.time.LocalDate;
import java.util.List;

public interface FlightSearchPort {
    List<FlightOffer> search(String origin, String destination,
                             LocalDate dateFrom, LocalDate dateTo, int adults);
}
