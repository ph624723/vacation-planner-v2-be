package com.ph.model;

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
    private final Date start;
    @Getter
    private final Date end;

    public boolean includes(Date date){
        return date != null && start.before(date) && end.after(date);
    }

    public boolean includes(TimeSpan compare){
        return compare != null &&
                compare.getEnd().before(end) &&
                compare.getStart().after(start);
    }
    public boolean intersects(TimeSpan compare){
        return compare != null &&
                compare.getEnd().after(start) &&
                compare.getStart().before(end);
    }
    public List<TimeSpan> subtract (TimeSpan compare){
        List<TimeSpan> result = new ArrayList<>();
        if(compare != null && this.intersects(compare)){
            if(compare.getStart().after(start)){
                result.add(new TimeSpan(start, DateUtils.addDays(compare.getStart(),-1)));
            }
            if(compare.getEnd().before(end)){
                result.add(new TimeSpan(DateUtils.addDays(compare.getEnd(),1), end));
            }
        }else {
            result.add(this);
        }
        return result;
    }
    public TimeSpan (Date start, Date end){
        if(end.after(start)){
            this.start = start;
            this.end = end;
        }else{
            this.end = start;
            this.start = end;
        }
    }
}
