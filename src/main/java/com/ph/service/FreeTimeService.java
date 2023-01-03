package com.ph.service;

import com.ph.model.TimeSpan;
import com.ph.rest.webservices.restfulwebservices.model.Absence;

import java.util.ArrayList;
import java.util.List;

public class FreeTimeService {
    public static List<TimeSpan> findFreeTimes (TimeSpan in, List<Absence> absences){
        List<TimeSpan> results = new ArrayList<>();
        results.add(in);

        for (Absence absence : absences) {
            List<TimeSpan> newResults = new ArrayList<>();
            for (TimeSpan freeTime : results) {
                newResults.addAll(freeTime.subtract(absence.asTimeSpan()));
            }
            results = newResults;
        }

        return results;
    }
}
