package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.TimeSpan;
import com.ph.rest.webservices.restfulwebservices.model.Absence;
import com.ph.rest.webservices.restfulwebservices.model.User;
import com.ph.rest.webservices.restfulwebservices.repository.AbsenceJpaRepository;
import com.ph.rest.webservices.restfulwebservices.repository.UserJpaRepository;
import com.ph.service.FreeTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/absences")
public class AbsenceController implements IController<Absence,Long> {

	private final String dateFormat = "yyyy-mm-dd";
	
	@Autowired
	private AbsenceJpaRepository repository;
	@Autowired
	private UserJpaRepository userRepository;

	public List<Absence> getAll(){
		return repository.findAll();
	}

	@GetMapping("/user/{username}")
	public List<Absence> getByUser(@PathVariable String username){
		if(userRepository.existsById(username)){
			return repository.findByUser(userRepository.findById(username).get());
		}else{
			return null;
		}
	}

	@GetMapping("/free")
	public List<TimeSpan> findFreeTimes(
			@RequestHeader("start")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			Date startDate,
			@RequestHeader("end")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			Date endDate,
			@RequestParam(required = false)
			Integer upToLevel){

		List<Absence> absences = repository.findAll();
		if(upToLevel != null){
			absences = absences.stream()
					.filter(absence -> absence.getLevel() > upToLevel)
					.collect(Collectors.toList());
		}

		return FreeTimeService.findFreeTimes(
				new TimeSpan(startDate,endDate),
				absences);
	}

	public Absence get(Long id){
		return repository.findById(id).orElse(null);
	}

	public ResponseEntity<Void> delete(Long id) {

		repository.deleteById(id);

		return ResponseEntity.noContent().build();
	}

	public ResponseEntity<Absence> update(
			Long id,  Absence absence){

		absence.setId(id);

		Absence elementUpdated = repository.save(absence);
		
		return new ResponseEntity<Absence>(elementUpdated, HttpStatus.OK);
	}

	public ResponseEntity<Void> create(Absence element){

		Absence createdelement = repository.save(element);
		
		//Location
		//Get current resource url
		///{id}
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(createdelement.getId()).toUri();
		
		return ResponseEntity.created(uri).build();
	}
		
}
