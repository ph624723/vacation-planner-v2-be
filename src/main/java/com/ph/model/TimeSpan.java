package com.ph.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeSpan {

    @Getter
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate start;
    @Getter
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDate end;

    public boolean includes(LocalDate date){
        return date != null && start.isBefore(date) && end.isAfter(date);
    }

    public boolean includes(TimeSpan compare){
        return compare != null &&
                compare.getEnd().isBefore(end) &&
                compare.getStart().isAfter(start);
    }
    public boolean intersects(TimeSpan compare){
        return compare != null &&
                compare.getEnd().isAfter(start) &&
                compare.getStart().isBefore(end);
    }
    public List<TimeSpan> subtract (TimeSpan compare){
        List<TimeSpan> result = new ArrayList<>();
        if(compare != null && this.intersects(compare)){
            if(compare.getStart().isAfter(start)){
                result.add(new TimeSpan(start, compare.getStart().minusDays(1)));//DateUtils.addDays(compare.getStart(),-1)));
            }
            if(compare.getEnd().isBefore(end)){
                result.add(new TimeSpan(compare.getEnd().plusDays(1),end)); //DateUtils.addDays(compare.getEnd(),1), end));
            }
        }else {
            result.add(this);
        }
        return result;
    }
    public TimeSpan (LocalDate start, LocalDate end){
        if(end.isAfter(start)){
            this.start = start;
            this.end = end;
        }else{
            this.end = start;
            this.start = end;
        }
    }
}
