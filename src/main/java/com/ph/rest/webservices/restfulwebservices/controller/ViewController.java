package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.*;
import com.ph.persistence.model.*;
import com.ph.persistence.repository.*;
import com.ph.rest.webservices.restfulwebservices.Service.EventService;
import com.ph.rest.webservices.restfulwebservices.Service.PersonService;
import com.ph.rest.webservices.restfulwebservices.Service.TimeLineService;
import com.ph.rest.webservices.restfulwebservices.Service.UserService;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.rest.webservices.restfulwebservices.viewmodel.CommentViewModel;
import com.ph.service.EmailService;
import com.ph.service.FreeTimeService;
import com.ph.service.HashService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    CommentJpaRepository commentJpaRepository;

    @Autowired
    TimeLineService timeLineService;

    @Autowired
    EventService eventService;

    @Autowired
    UserService userService;

    @Autowired
    PersonService personService;
    @Autowired
    EmailService emailService;

    private final String APP_BASE_URL = "http://vacationplannerv2-be-dev3.eba-pisehxks.us-east-1.elasticbeanstalk.com/view/events/show?eventId=";

    @GetMapping(value = "/persons")
    public ModelAndView  allPersonsView(){
        ModelAndView model = new ModelAndView("Person/list");

        model.addObject("persons",personService.getAvailablePersons());

        return model;
    }
    @GetMapping(value = "/persons/edit")
    public ModelAndView  editPersonView(
            @RequestParam(required = true)
            Long personId
    ){
        if(!personService.isPersonAvailable(personId)){
            return get403ResourceNoAccessErrorResponse();
        }

        ModelAndView model = new ModelAndView("Person/edit");

        Person person = Person.fromEntity(personJpaRepository.findById(personId).get());

        model.addObject("person", person);

        return model;
    }

    @PostMapping(value = "/persons")
    public String updateAbsenceView(@ModelAttribute Person person, Model model){
        PersonEntity oldEntiy = personJpaRepository.findById(person.getId()).get();
        String oldMail = ""+oldEntiy.getContact();
        PersonEntity personEntity = person.toEntity(oldEntiy);
        personEntity = personJpaRepository.save(personEntity);
        model.addAttribute("person", person);
        if(!oldMail.equals(personEntity.getContact())){
            model.addAttribute("mailchanged", true);
            personService.sendEmailNotification(personEntity);
        }else {
            model.addAttribute("mailchanged", false);
        }

        return "Person/confirm";
    }

    @GetMapping(value = "/persons/show")
    public ModelAndView  showPersonView(
            @RequestParam
            Long personId
    ){
        if(!personService.isPersonAvailable(personId)){
            return get403ResourceNoAccessErrorResponse();
        }

        ModelAndView model = new ModelAndView("Person/show");

        UserEntity authUser = userService.getCurrentlyAuthenticatedUser();

        PersonEntity person = personJpaRepository.findById(personId).get();
        List<AbsenceEntity> absenceEntities = absenceJpaRepository.findByPerson(person);
        List<Absence> absences = absenceEntities.stream()
                .sorted(Comparator.comparing(AbsenceEntity::getStartDate))
                .map(x -> Absence.fromEntity(x.trimDescription())).collect(Collectors.toList());
        List<String> personNames = absenceEntities.stream().map(x -> x.getPerson().getName()).collect(Collectors.toList());
        List<Long> durations = absences.stream().map(x -> timeLineService.diffInDays(x.getStartDate(),x.getEndDate())+1).collect(Collectors.toList());
        List<Event> events = eventJpaRepository.findByGroupIn(authUser.getPersonData().getRoles()).stream()
                .filter(x -> x.getPersons().stream().anyMatch(y -> y.getId().equals(personId)))
                .sorted(Comparator.comparing(EventEntity::getStartDate))
                .map(x -> Event.fromEntity(x))
                .collect(Collectors.toList());
        String  titleText = person.getName();

        model.addObject("absences", absences);
        model.addObject("personNames", personNames);
        model.addObject("durations", durations);
        model.addObject("events", events);
        model.addObject("titleText", titleText);
        model.addObject("person", person);
        model.addObject("personId", personId);

        return model;
    }

    @GetMapping(value = "/absences")
    public ModelAndView  allAbsencesView(){
        ModelAndView model = new ModelAndView("Absence/list");

        String titleText = "all persons";
        List<AbsenceEntity> absenceEntities = absenceJpaRepository.findByPersonIn(personService.getAvailablePersons());
        List<Absence> absences = absenceEntities.stream()
                .sorted(Comparator.comparing(AbsenceEntity::getStartDate))
                .map(x -> Absence.fromEntity(x))
                .collect(Collectors.toList());
        List<String> personNames = absenceEntities.stream().map(x -> x.getPerson().getName()).collect(Collectors.toList());
        List<Long> durations = absences.stream().map(x -> timeLineService.diffInDays(x.getStartDate(),x.getEndDate())+1).collect(Collectors.toList());


        absenceEntities = absenceEntities.stream().map(x -> x.trimDescription()).sorted(Comparator.comparing(AbsenceEntity::getStartDate)).collect(Collectors.toList());

        model.addObject("absences", absences);
        model.addObject("personNames", personNames);
        model.addObject("durations", durations);
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
        if(!personService.isPersonAvailable(absence.getPersonId())){
            return get403ResourceNoAccessErrorResponse();
        }

        List<PersonEntity> availablePersons = personService.getAvailablePersons();

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
        AbsenceEntity absence = absenceJpaRepository.findById(absenceId).get();
        if(!personService.isPersonAvailable(absence.getPerson().getId())){
            return get403ResourceNoAccessErrorResponse();
        }

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
        if(personId != null && personId != -1) {
            if(!personService.isPersonAvailable(personId)){
                return get403ResourceNoAccessErrorResponse();
            }
            absence.setPersonId(personId);
        }

        List<PersonEntity> availablePersons = personService.getAvailablePersons();

        List<AbsenceEntity.Importance> importanceLevels = Arrays.asList(AbsenceEntity.Importance.values());

        model.addObject("absence", absence);
        model.addObject("availablePersons", availablePersons);
        model.addObject("importanceLevels", importanceLevels);

        return model;
    }

    @GetMapping(value = "/home")
    public ModelAndView  homeView(){

        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();
        if(currentUser == null)
            return new ModelAndView("main");
        else
            return showPersonView(currentUser.getPersonData().getId());
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
        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();

        if(eventEntity.getPersons().contains(userService.getCurrentlyAuthenticatedUser().getPersonData())){
            eventJpaRepository.delete(eventEntity);
        }else{
            return get403ResourceNoAccessErrorResponse();
        }

        return new ModelAndView("redirect:/view/home");
    }

    @PostMapping(value ="/events/comments/delete/{commentId}")
    public ModelAndView deleteComment(
            @PathVariable
            long commentId
    ){
        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();

        CommentEntity comment = commentJpaRepository.findById(commentId).get();
        if(!currentUser.getPersonData().getComments().stream().anyMatch(x -> x.getId().equals(commentId))){
            return get403ResourceNoAccessErrorResponse();
        }
        long eventId = comment.getEvent().getId();
        for (CommentEntity replier : comment.getRepliedBy()) {
            replier.setReplyTo(null);
            commentJpaRepository.save(replier);
        }
        PersonEntity person = comment.getPerson();
        person.getComments().remove(comment);
        personJpaRepository.save(person);
        EventEntity event = comment.getEvent();
        event.getComments().remove(comment);
        commentJpaRepository.delete(comment);
        return new ModelAndView("redirect:/view/events/show?eventId="+eventId);
    }

    @GetMapping(value = "/events/comments/editComment")
    public ModelAndView  editCommentView(
            @RequestParam(required = true)
            long eventId,
            @RequestParam(required = false)
            Long replyToId,
            @RequestParam(required = false)
            Long commentId
    ){
        ModelAndView model = new ModelAndView("Event/editComment");

        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();
        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();
        if(!eventEntity.getPersons().contains(currentUser.getPersonData())){
            return get403ResourceNoAccessErrorResponse();
        }

        Comment comment;
        if(commentId != null) {
            comment = Comment.fromEntity(eventEntity.getComments().stream().filter(x -> x.getId().equals(commentId)).findAny().get());
            if(!currentUser.getPersonData().getId().equals(comment.getPersonId())){
                return get403ResourceNoAccessErrorResponse();
            }
        }else{
            comment = new Comment();
            comment.setPersonId(currentUser.getPersonData().getId());
            comment.setEventId(eventId);
        }
        if(replyToId != null){
            comment.setReplyToId(replyToId);
        }

        List<CommentViewModel> availableComments = eventEntity.getComments().stream()
                .filter(x -> !x.getId().equals(commentId))
                .map(x -> CommentViewModel.fromEntity(x)).collect(Collectors.toList());


        model.addObject("comment", comment);
        model.addObject("availableComments", availableComments);

        return model;
    }

    @PostMapping(value = "/events/comments/editComment")
    public ModelAndView saveComment(@ModelAttribute Comment comment) throws PersonNotFoundException, EventNotFoundException, CommentNotFoundException {
        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();
        EventEntity eventEntity = eventJpaRepository.findById(comment.getEventId()).get();
        if(!currentUser.getPersonData().getId().equals(comment.getPersonId()) || !eventEntity.getPersons().contains(currentUser.getPersonData())){
            return get403ResourceNoAccessErrorResponse();
        }
        if(comment.getReplyToId() != null && comment.getReplyToId().equals((long)-1))
            comment.setReplyToId(null);
        CommentEntity oldComment = null;
        if(comment.getId() != null)
            oldComment = commentJpaRepository.findById(comment.getId()).orElse(null);
        CommentEntity savedComment = commentJpaRepository.save(comment.toEntity(oldComment,personJpaRepository,eventJpaRepository,commentJpaRepository));

        String url = APP_BASE_URL+eventEntity.getId();
        eventService.sendNewCommentMail(eventEntity,url, savedComment);
        return new ModelAndView("redirect:/view/events/show?eventId="+comment.getEventId());
    }

    @GetMapping(value = "/events/show")
    public ModelAndView  showEventView(
            @RequestParam()
            Long eventId
    ){
        ModelAndView model = new ModelAndView("Event/show");

        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();
        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();
        if(!eventEntity.getPersons().contains(currentUser.getPersonData())){
            return get403ResourceNoAccessErrorResponse();
        }

        Event event = Event.fromEntity(eventEntity);
        List<Person> personsAccepted = eventEntity.getAcceptedPersons().stream().map(x -> Person.fromEntity(x)).collect(Collectors.toList());
        List<Person> personsNotAccepted = eventEntity.getPersons().stream()
                .filter(x -> !eventEntity.getAcceptedPersons().contains(x))
                .map(x -> Person.fromEntity(x)).collect(Collectors.toList());
        List<CommentViewModel> comments = eventEntity.getComments().stream()
                .sorted(Comparator.comparing(CommentEntity::getCreated))
                .map(x -> CommentViewModel.fromEntity(x)).collect(Collectors.toList());
        comments.forEach(x -> x.setEditable(x.getPerson().getId().equals(currentUser.getPersonData().getId())));

        model.addObject("event", event);
        model.addObject("personsAccepted", personsAccepted);
        model.addObject("personsMissing", personsNotAccepted);
        model.addObject("comments", comments);

        boolean isAccepted = personsAccepted.stream().anyMatch(x -> x.getId() == currentUser.getPersonData().getId());
        model.addObject("isAccepted", isAccepted);
        model.addObject("allAccepted", event.getPersonIdsAccepted().equals(event.getPersonIds()));

        return model;
    }

    @GetMapping(value = "/events/accept")
    public ModelAndView  acceptEvent(
            @RequestParam()
            Long eventId
    ){
        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();
        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();
        if(!eventEntity.getPersons().contains(currentUser.getPersonData())){
            return get403ResourceNoAccessErrorResponse();
        }

        eventEntity.getAcceptedPersons().add(currentUser.getPersonData());
        eventJpaRepository.save(eventEntity);

        if(eventEntity.getAcceptedPersons().containsAll(eventEntity.getPersons())){
            //event fully accepted
            String url = APP_BASE_URL+eventEntity.getId();
            eventService.sendAllAcceptedMail(eventEntity,url);
        }

        return new ModelAndView("redirect:/view/events/show?eventId="+eventId);
    }

    @GetMapping(value = "/events/decline")
    public ModelAndView  declineEvent(
            @RequestParam()
            Long eventId
    ){
        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();
        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();
        if(!eventEntity.getPersons().contains(currentUser.getPersonData())){
            return get403ResourceNoAccessErrorResponse();
        }

        eventEntity.getAcceptedPersons().remove(currentUser.getPersonData());
        eventJpaRepository.save(eventEntity);

        return new ModelAndView("redirect:/view/events/show?eventId="+eventId);
    }

    @GetMapping(value = "/events/finalize")
    public ModelAndView  finalizeEvent(
            @RequestParam()
            Long eventId
    ){
        EventEntity eventEntity = eventJpaRepository.findById(eventId).get();
        UserEntity currentUser = userService.getCurrentlyAuthenticatedUser();
        if(!eventEntity.getPersons().contains(currentUser.getPersonData())){
            return get403ResourceNoAccessErrorResponse();
        }

        if(eventEntity.getAcceptedPersons().equals(eventEntity.getPersons())){
            for (PersonEntity person : eventEntity.getPersons()) {
                if(person.equals(currentUser.getPersonData())) continue;

                AbsenceEntity absence = new AbsenceEntity();
                absence.setStartDate(eventEntity.getStartDate());
                absence.setEndDate(eventEntity.getEndDate());
                absence.setDescription(eventEntity.getDescription());
                absence.setLevel(AbsenceEntity.Importance.Medium.getLevel());
                absence.setPerson(person);
                absenceJpaRepository.save(absence);
            }

            Absence myAbsence = new Absence();
            myAbsence.setStartDate(eventEntity.getStartDate());
            myAbsence.setEndDate(eventEntity.getEndDate());
            myAbsence.setDescription(eventEntity.getDescription());
            myAbsence.setLevel(AbsenceEntity.Importance.Medium.getLevel());
            myAbsence.setPersonId(currentUser.getPersonData().getId());
            return updateAbsenceView(myAbsence);
        }

        return new ModelAndView("redirect:/view/events/show?eventId="+eventId);
    }

    @PostMapping(value = "/events/plan", params = "save")
    public ModelAndView saveEvent(
            @ModelAttribute
            Event event,
            HttpServletRequest request
    ){
        if(event.getGroupName() == null){
            ModelAndView model = setupEventPlannerView(event);
            model.addObject("errorText", "Please add a group for the event.");
            return model;
        }
        List<PersonEntity> eventPersons = personJpaRepository.findAllById(event.getPersonIds());
        if(!eventPersons.stream()
                .allMatch(x -> x.getRoles().stream()
                        .anyMatch(y -> y.getName().equals(event.getGroupName())))){
            ModelAndView model = setupEventPlannerView(event);
            model.addObject("errorText", "Not all selected participants are part of group: "+event.getGroupName()+". Please make sure all participants share the selected group.");
            return model;
        }
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

        UserEntity authUser = userService.getCurrentlyAuthenticatedUser();
        PersonEntity personEntity = authUser.getPersonData();

        EventEntity oldEventEntity= null;
        Collection<Long> mailPeopleIds = event.getPersonIds();
        if(event.getId()!= null){
            oldEventEntity = eventJpaRepository.findById(event.getId()).orElse(null);
            Event oldEvent = Event.fromEntity(oldEventEntity);
            if(!event.equalsTimeframe(oldEvent)){
                event.setPersonIdsAccepted(new HashSet<>());
            }
            if(event.equalsExceptForPersons(oldEvent)){
                mailPeopleIds = CollectionUtils.removeAll(event.getPersonIds(),oldEvent.getPersonIds());
            }
        }
        event.getPersonIdsAccepted().add(authUser.getPersonData().getId());
        event.getPersonIdsAccepted().retainAll(event.getPersonIds());
        try {
            EventEntity eventEntity = event.toEntity(oldEventEntity, personJpaRepository);

            eventEntity = eventJpaRepository.save(eventEntity);

            String url = APP_BASE_URL+eventEntity.getId();

            Collection<Long> finalMailPeopleIds = mailPeopleIds;
            Set<PersonEntity> mailPersons = eventEntity.getPersons().stream()
                    .filter(x -> finalMailPeopleIds.contains(x.getId()))
                    .collect(Collectors.toSet());
            eventService.sendEmailNotifications(eventEntity,mailPersons,url,personEntity.getName());
            if(eventEntity.getAcceptedPersons().equals(eventEntity.getPersons())){
                //event fully accepted
                eventService.sendAllAcceptedMail(eventEntity,url);
            }
        }catch (PersonNotFoundException e){
            return new ModelAndView("redirect:/view/home");
        }

        return new ModelAndView( "Event/confirm");
    }

    @PostMapping(value = "/events/plan", params = "update")
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

            UserEntity authUser = userService.getCurrentlyAuthenticatedUser();
            event.setPersonIds(Stream.of(authUser.getPersonData().getId()).collect(Collectors.toSet()));
            if(!authUser.getPersonData().getRoles().isEmpty())
                event.setGroupName(authUser.getPersonData().getRoles().get(0).getName());
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
        model.addObject("selectablePersons",personService.getAvailablePersons().stream()
                .map(x -> Person.fromEntity(x))
                .collect(Collectors.toList()));
        model.addObject("selectableImportances", AbsenceEntity.Importance.values());
        model.addObject("selectableGroups", Person.fromEntity(userService.getCurrentlyAuthenticatedUser().getPersonData()).getRoleNames());


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

        UserEntity user = userService.getCurrentlyAuthenticatedUser();
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

    private ModelAndView get403ResourceNoAccessErrorResponse(){
        ModelAndView model = new ModelAndView("Generic/Error/403ResourceNoAccessError");
        model.setStatus(HttpStatus.FORBIDDEN);
        return model;
    }

    @ExceptionHandler(Exception.class)
    private ModelAndView get500InternalErrorResponse(Exception e){
        ModelAndView model = new ModelAndView("Generic/Error/500InternalError");
        model.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return model;
    }
}
