package com.ph.rest.webservices.restfulwebservices.Service;

import com.ph.model.TimeSpan;
import com.ph.rest.webservices.restfulwebservices.model.CalendarEvent;
import com.ph.rest.webservices.restfulwebservices.model.DayOfMonth;
import com.ph.rest.webservices.restfulwebservices.model.Month;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TimeLineService {


    public List<Month> generateMonths(Date start, Date end){

        List<Month> months = new ArrayList<>();
        LocalDate tmp = start.toLocalDate();
        int firstDay = tmp.getDayOfMonth();
        while (tmp.isBefore(end.toLocalDate()) || tmp.isEqual(end.toLocalDate())){
            Month month = new Month();
            month.setName(tmp.getMonth().name());

            List<DayOfMonth> dates = new ArrayList<>();
            int lastDay = tmp.getYear() == end.toLocalDate().getYear() && tmp.getMonth().equals(end.toLocalDate().getMonth()) ?
                    end.toLocalDate().getDayOfMonth() :
                    tmp.getMonth().length(tmp.isLeapYear());
            for (int i =firstDay; i<=lastDay;i++) dates.add(new DayOfMonth(i));
            month.setDays(dates);
            tmp = tmp.plusMonths(1).withDayOfMonth(1);
            firstDay = 1;
            months.add(month);
        }
        return months;
    }

    public List<CalendarEvent> aggregateCalendarEvents(java.sql.Date start, java.sql.Date end, List<TimeSpan> list){
        List<CalendarEvent> results = new ArrayList<>();
        list = TimeSpan.fuse(list.stream()
                .filter(x -> !(x.getEnd().before(start) || x.getStart().after(end)))
                .sorted(Comparator.comparing(TimeSpan::getStart))
                .collect(Collectors.toList()));
        if(!list.isEmpty()){

            long startOffset = diffInDays(start, list.get(0).getStart());
            if(startOffset > 0){
                results.add(
                        new CalendarEvent(startOffset)
                );
            } else if (startOffset < 0) {
                list.get(0).setStart(start);
            }
            for (int i=0;i<list.size();i++) {
                TimeSpan absence = list.get(i);
                System.out.println("-------------add: "+absence.getName()+" "+ absence.getStart()+" "+absence.getEnd()+" "+(diffInDays(absence.getStart(), absence.getEnd())+1));
                results.add(new CalendarEvent(
                        diffInDays(absence.getStart(), absence.getEnd())+1,
                        absence.getName(),
                        absence.getStart(),
                        absence.getEnd()
                ));
                if(i < list.size()-1 && diffInDays(absence.getEnd(),list.get(i+1).getStart()) > 1){
                    TimeSpan nextAbsence = list.get(i+1);
                    results.add(new CalendarEvent(
                            diffInDays(absence.getEnd(), nextAbsence.getStart())-1));
                }
            }
            long endOffset = diffInDays(list.get(list.size()-1).getEnd(), end);
            if(endOffset > 0){
                results.add(
                        new CalendarEvent(
                                endOffset+1
                        )
                );
            } else if (endOffset < 0) {
                CalendarEvent lastEvent = results.get(results.size()-1);
                lastEvent.setDuration(lastEvent.getDuration()+endOffset);
            } else{
                System.out.println("------------- "+results.get(results.size()-1).getDuration());
            }
        }else{
            results.add(new CalendarEvent(diffInDays(start,end)+1));
        }
        return results;
    }

    public long diffInDays(java.util.Date a, java.util.Date b){
        long diffInMillies = b.getTime() - a.getTime();
        long diff = TimeUnit.DAYS.convert(Math.abs(diffInMillies), TimeUnit.MILLISECONDS);
        return diff * (diffInMillies > 0 ? 1 : -1);
    }
}
