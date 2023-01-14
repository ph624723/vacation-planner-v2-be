package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.AbsenceJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.persistence.repository.UserJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.Absence;
import com.ph.rest.webservices.restfulwebservices.model.LoginCredentials;
import com.ph.rest.webservices.restfulwebservices.model.Person;
import com.ph.service.AuthService;
import com.ph.service.EmailServiceImpl;
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

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/view")
public class ViewController {

    @Autowired
    PersonJpaRepository personJpaRepository;

    @Autowired
    AbsenceJpaRepository absenceJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    EmailServiceImpl emailService;

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
        System.out.println("Get-------------------"+absence.getStartDate()+" "+absence.getEndDate());
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
        if(absence.getStartDate().compareTo(absence.getEndDate()) > 0){
            LocalDate start = absence.getEndDate();
            absence.setEndDate(absence.getStartDate());
            absence.setStartDate(start);
        }
        System.out.println("Post-------------------"+absence.getStartDate()+" "+absence.getEndDate());
        ModelAndView model = new ModelAndView("Absence/confirm");
        try {
            AbsenceEntity absenceEntity;
            if(absence.getId() != null) {
                absenceEntity = absence.toEntity(absenceJpaRepository.findById(absence.getId()).get(), personJpaRepository);
            }else{
                absenceEntity = absence.toEntity(null, personJpaRepository);
            }
            System.out.println("Post-------------------"+absenceEntity.getStartDate()+" "+absenceEntity.getEndDate());
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
        //System.out.println(emailService.sendSimpleMail("phegerp@gmail.com","test: "+ LocalTime.now().toString(),"Testmessage"));
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
