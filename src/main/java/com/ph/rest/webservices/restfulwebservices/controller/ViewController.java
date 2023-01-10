package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.repository.AbsenceJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.Absence;
import com.ph.rest.webservices.restfulwebservices.model.Person;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/view")
public class ViewController {

    @Autowired
    PersonJpaRepository personJpaRepository;

    @Autowired
    AbsenceJpaRepository absenceJpaRepository;

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
        if(personId == null){
            absences = absenceJpaRepository.findAll();
            titleText = "all persons";
        }else{
            PersonEntity person = personJpaRepository.findById(personId).get();
            absences = absenceJpaRepository.findByPerson(person);
            titleText = person.getName();
        }

        model.addObject("absences", absences);
        model.addObject("titleText", titleText);
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

    @PostMapping(value = "/absences")
    public String updateAbsenceView(@ModelAttribute Absence absence, Model model){
        try {
            AbsenceEntity absenceEntity;
            if(absence.getId() != null) {
                absenceEntity = absence.toEntity(absenceJpaRepository.findById(absence.getId()).get(), personJpaRepository);
            }else{
                absenceEntity = absence.toEntity(null, personJpaRepository);
            }
            absenceJpaRepository.save(absenceEntity);
            model.addAttribute("person", absenceEntity.getPerson());
        }catch(PersonNotFoundException e){
            return "redirect:/error";
        }
        return "Absence/confirm";
    }

    @GetMapping(value = "/absences/add")
    public ModelAndView  addAbsenceView(
            @RequestParam(required = false)
            Long personId
    ){
        ModelAndView model = new ModelAndView("Absence/edit");

        Absence absence = new Absence();
        if(personId != -1) absence.setPersonId(personId);
        List<PersonEntity> availablePersons = personJpaRepository.findAll();
        List<AbsenceEntity.Importance> importanceLevels = Arrays.asList(AbsenceEntity.Importance.values());

        model.addObject("absence", absence);
        model.addObject("availablePersons", availablePersons);
        model.addObject("importanceLevels", importanceLevels);

        return model;
    }
}
