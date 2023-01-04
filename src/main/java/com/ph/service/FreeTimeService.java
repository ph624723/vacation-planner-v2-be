package com.ph.service;

import com.ph.model.TimeSpan;
import com.ph.persistence.model.AbsenceEntity;

import java.util.ArrayList;
import java.util.List;

public class FreeTimeService {
    public static List<TimeSpan> findFreeTimes (TimeSpan in, List<AbsenceEntity> absences){
        List<TimeSpan> results = new ArrayList<>();
        results.add(in);

        for (AbsenceEntity absence : absences) {
            List<TimeSpan> newResults = new ArrayList<>();
            for (TimeSpan freeTime : results) {
                newResults.addAll(freeTime.subtract(absence.asTimeSpan()));
            }
            results = newResults;
        }

        return results;
    }
}
