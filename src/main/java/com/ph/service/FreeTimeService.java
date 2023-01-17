package com.ph.service;

import com.ph.model.TimeSpan;
import com.ph.persistence.model.AbsenceEntity;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FreeTimeService {
    public static List<TimeSpan> findFreeTimes (TimeSpan in, List<AbsenceEntity> absences){
        List<TimeSpan> timespans = absences.stream().map(x -> x.asTimeSpan()).collect(Collectors.toList());
        return findFreeTimesBySpan(in,timespans);
    }

    public static List<TimeSpan> findFreeTimesBySpan (TimeSpan in, List<TimeSpan> absences){
        List<TimeSpan> results = new ArrayList<>();
        results.add(in);

        for (TimeSpan absence : absences) {
            List<TimeSpan> newResults = new ArrayList<>();
            for (TimeSpan freeTime : results) {
                newResults.addAll(freeTime.subtract(absence));
            }
            results = newResults;
        }

        return results;
    }
}
