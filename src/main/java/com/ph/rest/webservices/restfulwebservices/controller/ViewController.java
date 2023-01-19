package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.EmailFailedException;
import com.ph.model.PersonNotFoundException;
import com.ph.model.TimeSpan;
import com.ph.persistence.model.*;
import com.ph.persistence.repository.AbsenceJpaRepository;
import com.ph.persistence.repository.EventJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.Service.EventService;
import com.ph.rest.webservices.restfulwebservices.Service.TimeLineService;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.FreeTimeService;
import com.ph.service.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Date;
import java.util.*;
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
    EventJpaRepository eventJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    TimeLineService timeLineService;

    @Autowired
    EventService eventService;

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

    @GetMapping(value = "/persons/show")
    public ModelAndView  showPersonView(
            @RequestParam
            Long personId
    ){
        ModelAndView model = new ModelAndView("Person/show");
        String titleText;
        List<AbsenceEntity> absences;
        PersonEntity person = null;

        person = personJpaRepository.findById(personId).get();
        absences = absenceJpaRepository.findByPerson(person);
        List<Event> events = eventJpaRepository.findAll().stream()
                .filter(x -> x.getPersons().stream().anyMatch(y -> y.getId().equals(personId)))
                .sorted(Comparator.comparing(EventEntity::getStartDate))
                .map(x -> Event.fromEntity(x))
                .collect(Collectors.toList());
        titleText = person.getName();

        absences = absences.stream().map(x -> x.trimDescription()).sorted(Comparator.comparing(AbsenceEntity::getStartDate)).collect(Collectors.toList());

        model.addObject("absences", absences);
        model.addObject("events", events);
        model.addObject("titleText", titleText);
        model.addObject("person", person);
        model.addObject("personId", personId);

        return model;
    }

    @GetMapping(value = "/absences")
    public ModelAndView  allAbsencesView(){
        ModelAndView model = new ModelAndView("Absence/list");
        String titleText;
        List<AbsenceEntity> absences;
        absences = absenceJpaRepository.findAll();
        titleText = "all persons";


        absences = absences.stream().map(x -> x.trimDescription()).sorted(Comparator.comparing(AbsenceEntity::getStartDate)).collect(Collectors.toList());

        model.addObject("absences", absences);
        model.addObject("titleText", titleText);
        model.addObject("person", null);
        model.addObject("personId", -1);

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

    //@GetMapping(value = "/index")
    //public ModelAndView  indexView(){return new ModelAndView("index");}

    //@GetMapping(value = "/index-o")
    //public ModelAndView  indexOView(){ return new ModelAndView("index-original");}

    //@GetMapping(value = "/index-o0")
    //public ModelAndView  indexO0View(){return new ModelAndView("index-original-0"); }

    @GetMapping(value = "/events/delete")
    public ModelAndView  deleteEventView(
            @RequestParam()
            Long eventId
    ){
        ModelAndView model = new ModelAndView("redirect:/view/home");

        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();
        eventJpaRepository.delete(eventEntity);

        return model;
    }

    @GetMapping(value = "/events/show")
    public ModelAndView  showEventView(
            @RequestParam()
            Long eventId
    ){
        ModelAndView model = new ModelAndView("Event/show");

        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();
        Event event = Event.fromEntity(eventEntity);
        List<Person> persons = eventEntity.getPersons().stream().map(x -> Person.fromEntity(x)).collect(Collectors.toList());

        model.addObject("event", event);
        model.addObject("persons", persons);

        return model;
    }

    @PostMapping(value = "/events/save")
    public ModelAndView saveEvent(
            @ModelAttribute
            Event event,
            HttpServletRequest request
    ){
        if(event.getDescription().isEmpty()){
            ModelAndView model = setupEventPlannerView(event);
            model.addObject("errorText", "Please add a description for the event.");
            return model;
        }
        if(event.getPersonIds().isEmpty()){
            ModelAndView model = setupEventPlannerView(event);
            model.addObject("errorText", "Please add at least one person for the event.");
            return model;
        }

        EventEntity oldEvent= null;
        if(event.getId()!= null){
            oldEvent = eventJpaRepository.findById(event.getId()).orElse(null);
        }
        try {
            EventEntity eventEntity = event.toEntity(oldEvent, personJpaRepository);

            System.out.println("eventEntity------------------ "+eventEntity.getDescription());
            System.out.println("eventEntity------------------ "+eventEntity.getId());
            System.out.println("eventEntity------------------ "+eventEntity.getStartDate());
            System.out.println("eventEntity------------------ "+eventEntity.getEndDate());
            System.out.println("eventEntity------------------ "+eventEntity.getEventPlannerConfig().getId());
            System.out.println("eventEntity------------------ "+eventEntity.getEventPlannerConfig().getStart());
            System.out.println("eventEntity------------------ "+eventEntity.getEventPlannerConfig().getEnd());
            System.out.println("eventEntity------------------ "+eventEntity.getEventPlannerConfig().getIgnoreAbsenceToLevel());

            eventEntity = eventJpaRepository.save(eventEntity);

            String url = "http://vacationplannerv2-be-dev3.eba-pisehxks.us-east-1.elasticbeanstalk.com/view/events/show?eventId="+eventEntity.getId();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = ((UserDetails)auth.getPrincipal()).getUsername();
            PersonEntity personEntity = userJpaRepository.findById(username).get().getPersonData();

            eventService.sendEmailNotifications(eventEntity,url,personEntity.getName());
        }catch (PersonNotFoundException e){
            return new ModelAndView("redirect:/view/home");
        }

        return new ModelAndView( "Event/confirm");
    }

    @PostMapping(value = "/events/plan")
    public ModelAndView updateEventPlannerConfig(
            @ModelAttribute
            Event event
    ){
        return setupEventPlannerView(event);
    }

    @GetMapping(value = "/events/plan")
    public ModelAndView  getEventPlannerView(
            @RequestParam(value = "eventId", required = false)
            Long eventId
    ) {
        Event event;
        if(eventId != null){
            event = Event.fromEntity(eventJpaRepository.findById(eventId).get());
        }else{
            Date start = new java.sql.Date(new java.util.Date().getTime());
            Date end = java.sql.Date.valueOf(start.toLocalDate().plusDays(20));
            int ignoreToLevel = -1;
            event = new Event();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = ((UserDetails)auth.getPrincipal()).getUsername();
            PersonEntity personEntity = userJpaRepository.findById(username).get().getPersonData();
            event.setPersonIds(Arrays.asList(personEntity.getId()));
            EventPlannerConfigEntity eventPlannerConfig = new EventPlannerConfigEntity();
            eventPlannerConfig.setStart(start);
            eventPlannerConfig.setEnd(end);
            eventPlannerConfig.setIgnoreAbsenceToLevel(ignoreToLevel);
            event.setEventPlannerConfig(eventPlannerConfig);

            event.setStartDate(start);
            event.setEndDate(end);
        }

        return setupEventPlannerView(event);
    }

    private ModelAndView setupEventPlannerView(Event event){
        ModelAndView model = new ModelAndView("Event/test");

        // setup dialog
        model.addObject("event_config",event);
        model.addObject("selectablePersons",personJpaRepository.findAll().stream()
                .map(x -> Person.fromEntity(x))
                .collect(Collectors.toList()));
        model.addObject("selectableImportances", AbsenceEntity.Importance.values());


        // free times table
        java.sql.Date start =event.getEventPlannerConfig().getStart();
        java.sql.Date end = event.getEventPlannerConfig().getEnd();
        List<PersonEntity> persons = personJpaRepository.findAllById(event.getPersonIds());
        int ignoreToLevel = event.getEventPlannerConfig().getIgnoreAbsenceToLevel();

        model.addObject("months", timeLineService.generateMonths(start,end));

        List<CalendarPerson> calendarPeople = new ArrayList<>();
        for (PersonEntity person : persons) {
            CalendarPerson calendarPerson = new CalendarPerson();
            calendarPerson.setPerson(Person.fromEntity(person));
            calendarPerson.setCalendarEvents(timeLineService.aggregateCalendarEvents(start, end, person.getAbsences().stream()
                    .filter(x -> x.getLevel() > ignoreToLevel)
                    .map(x -> x.asTimeSpan()).collect(Collectors.toList())));
            calendarPeople.add(calendarPerson);
        }
        model.addObject("people", calendarPeople);
        model.addObject("duration", timeLineService.diffInDays(start,end)+1);

        List<TimeSpan> freeTimes = FreeTimeService.findFreeTimes(new TimeSpan(start,end),
                        persons.stream().map(x -> x.getAbsences()).flatMap(List::stream)
                        .filter(x -> x.getLevel() > ignoreToLevel)
                        .collect(Collectors.toList()));
        freeTimes.forEach(x -> x.setName("free"));
        freeTimes.forEach(x -> System.out.println("-------------- "+x.getEnd()));
        System.out.println("-------------- "+end);
        model.addObject("freeTimes", timeLineService.aggregateCalendarEvents(start, end, freeTimes));

        return model;
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
