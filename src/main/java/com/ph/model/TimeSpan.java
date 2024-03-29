package com.ph.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateUtils;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.sql.Date;
import java.util.List;

public class TimeSpan {

    @Getter
    @Setter
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date start;
    @Getter
    @Setter
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Date end;

    @Getter
    @Setter
    @JsonIgnore
    private String name;

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
                result.add(new TimeSpan(start, Date.valueOf(compare.getStart().toLocalDate().minusDays(1))));
            }
            if(compare.getEnd().before(end)){
                result.add(new TimeSpan(Date.valueOf(compare.getEnd().toLocalDate().plusDays(1)), end));
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

    public TimeSpan (Date start, Date end, String name){
        this(start, end);
        this.name = name;
    }

    public TimeSpan(){

    }

    public TimeSpan cleanUp(){
        if(start.after(end)){
            Date tmp = end;
            this.end = start;
            this.start = tmp;
        }
        return this;
    }

    public static List<TimeSpan> fuse(List<TimeSpan> list){
        list.sort(Comparator.comparing(TimeSpan::getStart));
        List<TimeSpan> results = new ArrayList<>();
        for (int i=0;i<list.size();i++){
            TimeSpan timeSpan = list.get(i);
            while (i<(list.size()-1) && !timeSpan.getEnd().before(list.get(i+1).getStart())){
                i++;
                timeSpan.setEnd(
                        timeSpan.getEnd().after(list.get(i).getEnd()) ?
                                timeSpan.getEnd() :
                                list.get(i).getEnd());
                timeSpan.setName(timeSpan.getName() + " / " +list.get(i).getName());
            }
            results.add(timeSpan);
        }
        return results;
    }
}
