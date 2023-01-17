package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.PersonNotFoundException;
import com.ph.model.TimeSpan;
import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.AbsenceJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import com.ph.service.EmailServiceImpl;
import com.ph.service.FreeTimeService;
import com.ph.service.HashService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/view")
@ApiIgnore
public class ViewController {

    @Autowired
    PersonJpaRepository personJpaRepository;

    @Autowired
    AbsenceJpaRepository absenceJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @GetMapping(value = "/persons")
    public ModelAndView  allPersonsView(){
        ModelAndView model = new ModelAndView("Person/list");

        model.addObject("persons", personJpaRepository.findAll());

        return model;
    }
    @GetMapping(value = "/persons/edit")
    public ModelAndView  editPersonView(
            @RequestParam(required = true)
            Long personId
    ){
        ModelAndView model = new ModelAndView("Person/edit");

        Person person = Person.fromEntity(personJpaRepository.findById(personId).get());

        model.addObject("person", person);

        return model;
    }

    @PostMapping(value = "/persons")
    public String updateAbsenceView(@ModelAttribute Person person, Model model){
        PersonEntity personEntity = person.toEntity(personJpaRepository.findById(person.getId()).get());
        personJpaRepository.save(personEntity);
        model.addAttribute("person", person);
        return "Person/confirm";
    }

    @GetMapping(value = "/absences")
    public ModelAndView  allAbsencesView(
            @RequestParam(required = false)
            Long personId
    ){
        ModelAndView model = new ModelAndView("Absence/list");
        String titleText;
        List<AbsenceEntity> absences;
        PersonEntity person = null;
        if(personId == null){
            absences = absenceJpaRepository.findAll();
            titleText = "all persons";
        }else{
            person = personJpaRepository.findById(personId).get();
            absences = absenceJpaRepository.findByPerson(person);
            titleText = person.getName();
        }

        absences = absences.stream().map(x -> x.trimDescription()).sorted(Comparator.comparing(AbsenceEntity::getStartDate)).collect(Collectors.toList());

        model.addObject("absences", absences);
        model.addObject("titleText", titleText);
        model.addObject("person", person);
        model.addObject("personId", personId == null ? -1 : personId);

        return model;
    }
    @GetMapping(value = "/absences/edit")
    public ModelAndView  editAbsenceView(
            @RequestParam(required = true)
            Long absenceId
    ){
        ModelAndView model = new ModelAndView("Absence/edit");

        Absence absence = Absence.fromEntity(absenceJpaRepository.findById(absenceId).get());
        List<PersonEntity> availablePersons = personJpaRepository.findAll();
        List<AbsenceEntity.Importance> importanceLevels = Arrays.asList(AbsenceEntity.Importance.values());

        model.addObject("absence", absence);
        model.addObject("availablePersons", availablePersons);
        model.addObject("importanceLevels", importanceLevels);

        return model;
    }

    @GetMapping(value = "/absences/delete")
    public ModelAndView  deleteAbsenceView(
            @RequestParam(required = true)
            Long absenceId
    ){

        absenceJpaRepository.deleteById(absenceId);
        ModelAndView model = new ModelAndView("redirect:/view/absences");

        return model;
    }

    @PostMapping(value = "/absences")
    public ModelAndView updateAbsenceView(@ModelAttribute Absence absence){
        ModelAndView model = new ModelAndView("Absence/confirm");
        if(absence.getStartDate().compareTo(absence.getEndDate()) > 0){
            java.sql.Date start = absence.getEndDate();
            absence.setEndDate(absence.getStartDate());
            absence.setStartDate(start);
        }
        try {
            AbsenceEntity absenceEntity;
            if(absence.getId() != null) {
                absenceEntity = absence.toEntity(absenceJpaRepository.findById(absence.getId()).get(), personJpaRepository);
            }else{
                absenceEntity = absence.toEntity(null, personJpaRepository);
            }
            absenceJpaRepository.save(absenceEntity);
            model.addObject("person", absenceEntity.getPerson());
        }catch(PersonNotFoundException e){
            return new ModelAndView("redirect:/error");
        }
        return model;
    }

    @GetMapping(value = "/absences/add")
    public ModelAndView  addAbsenceView(
            @RequestParam(required = false)
            Long personId
    ){
        ModelAndView model = new ModelAndView("Absence/edit");

        Absence absence = new Absence();
        if(personId != null && personId != -1) absence.setPersonId(personId);
        List<PersonEntity> availablePersons = personJpaRepository.findAll();
        List<AbsenceEntity.Importance> importanceLevels = Arrays.asList(AbsenceEntity.Importance.values());

        model.addObject("absence", absence);
        model.addObject("availablePersons", availablePersons);
        model.addObject("importanceLevels", importanceLevels);

        return model;
    }

    @GetMapping(value = "/home")
    public ModelAndView  homeView(){

        ModelAndView model = new ModelAndView("main");

        return model;
    }

    @GetMapping(value = "/index")
    public ModelAndView  indexView(){
        return new ModelAndView("index");
    }

    @GetMapping(value = "/index-o")
    public ModelAndView  indexOView(){
        return new ModelAndView("index-original");
    }

    @GetMapping(value = "/index-o0")
    public ModelAndView  indexO0View(){
        return new ModelAndView("index-original-0");
    }

    @GetMapping(value = "/events/test")
    public ModelAndView  eventTestView(
            @RequestParam("personIds")
            List<Long> personIds,
            @RequestParam("start")
            java.sql.Date start,
            @RequestParam("end")
            java.sql.Date end
    ) {
        ModelAndView model = new ModelAndView("Event/test");

        //java.sql.Date start = java.sql.Date.valueOf("2018-12-10");
        //java.sql.Date end = java.sql.Date.valueOf("2019-01-30");

        List<Month> months = new ArrayList<>();
        LocalDate tmp = start.toLocalDate();
        int firstDay = tmp.getDayOfMonth();
        while (tmp.isBefore(end.toLocalDate())){
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

        List<PersonEntity> persons = personJpaRepository.findAllById(personIds);
        model.addObject("months", months);

        List<CalendarPerson> calendarPeople = new ArrayList<>();
        for (PersonEntity person : persons) {
            CalendarPerson calendarPerson = new CalendarPerson();
            calendarPerson.setPerson(Person.fromEntity(person));
            calendarPerson.setCalendarEvents(aggregateCalendarEvents(start, end, person.getAbsences().stream().map(x -> x.asTimeSpan()).collect(Collectors.toList())));
            calendarPeople.add(calendarPerson);
        }
        model.addObject("people", calendarPeople);
        model.addObject("duration", diffInDays(start,end)+1);

        List<TimeSpan> freeTimes = FreeTimeService.findFreeTimes(new TimeSpan(start,end),
                persons.stream().map(x -> x.getAbsences()).flatMap(List::stream).collect(Collectors.toList()));
        freeTimes.forEach(x -> x.setName("free"));
        model.addObject("freeTimes", aggregateCalendarEvents(start, end, freeTimes));

        return model;
    }

    public List<CalendarEvent> aggregateCalendarEvents(java.sql.Date start,java.sql.Date end, List<TimeSpan> list){
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
                results.add(new CalendarEvent(
                        diffInDays(absence.getStart(), absence.getEnd())+1,
                        absence.getName()
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
            }
        }else{
            results.add(new CalendarEvent(diffInDays(start,end)+1));
        }
        return results;
    }

    public long diffInDays(Date a, Date b){
        long diffInMillies = b.getTime() - a.getTime();
        long diff = TimeUnit.DAYS.convert(Math.abs(diffInMillies), TimeUnit.MILLISECONDS);
        return diff * (diffInMillies > 0 ? 1 : -1);
    }


    @GetMapping(value = "/changePassword")
    public ModelAndView  changePwView(){
        ModelAndView model = new ModelAndView("Generic/changePw");

        LoginCredentials credentials = new LoginCredentials();

        model.addObject("credentials", credentials);

        return model;
    }
    @PostMapping(value = "/changePassword")
    public String processNewPw(
            @Valid
            @ModelAttribute
            LoginCredentials credentials,
            BindingResult bindingResult,
            Model model){

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails)principal).getUsername();

        UserEntity user = userJpaRepository.findById(username).get();
        if(user.getPassword().equals(HashService.MD5(credentials.getPassword()))){
            if(bindingResult.hasErrors()){
                credentials.setErrorText("Password should be at least 6 characters");
                model.addAttribute("credentials", credentials);
                return "Generic/changePw";
            }

            if(credentials.getNewPassword().equals(credentials.getNewPassword2())){
                user.setPassword(HashService.MD5(credentials.getNewPassword()));
                userJpaRepository.save(user);

                //ModelAndView model = new ModelAndView("redirect:/view/home");
                return "redirect:/view/home";
            }else{
                credentials.setErrorText("New password fields do not match");
                //ModelAndView model = new ModelAndView("Generic/changePw");
                model.addAttribute("credentials", credentials);
                return "Generic/changePw";
            }
        }else{
            credentials.setErrorText("Wrong password");
            //ModelAndView model = new ModelAndView("Generic/changePw");
            model.addAttribute("credentials", credentials);
            return "Generic/changePw";
        }
    }

}
