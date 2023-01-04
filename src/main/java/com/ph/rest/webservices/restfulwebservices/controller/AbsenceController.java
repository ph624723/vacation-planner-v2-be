package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.TimeSpan;
import com.ph.rest.webservices.restfulwebservices.model.Absence;
import com.ph.rest.webservices.restfulwebservices.repository.AbsenceJpaRepository;
import com.ph.rest.webservices.restfulwebservices.repository.UserJpaRepository;
import com.ph.service.FreeTimeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
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

	@ApiOperation(value = "Gets all stored absences")
	public List<Absence> getAll(){
		return repository.findAll();
	}

	@ApiOperation(value = "Gets all stored absences for the specified user")
	@GetMapping("/user/{username}")
	public List<Absence> getByUser(
			@ApiParam(value = "The user to get absences for", required = true)
			@PathVariable
			String username){
		if(userRepository.existsById(username)){
			return repository.findByUser(userRepository.findById(username).get());
		}else{
			return null;
		}
	}

	@ApiOperation(value="Gets time-slots without absences",
					notes = "Gets time-slots without absences inside the specified time-frame. Optionally an importance level can be specified up to which absences are to be ignored.")
	@GetMapping("/free")
	public List<TimeSpan> findFreeTimes(
			@ApiParam(value = "The inclusive start-date of the desired time-frame in ISO format", required = true)
			@RequestHeader("start")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			Date startDate,
			@ApiParam(value = "The inclusive end-date of the desired time-frame in ISO format", required = true)
			@RequestHeader("end")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			Date endDate,
			@ApiParam(value = "The importance level up to which (inclusive) absences are to be ignored", required = false)
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

	@ApiOperation(value = "Get a single absence by ID",
					notes = "Will return null if ID does not exist.")
	public Absence get(
			@ApiParam(value = "ID of the desired absence")
			Long id){
		return repository.findById(id).orElse(null);
	}

	@ApiOperation(value = "Delete a single absence by ID")
	public ResponseEntity<Void> delete(
			@ApiParam(value = "ID of the target absence")
			Long id) {

		repository.deleteById(id);

		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "Update a single absence by ID",
					notes="Will use the URI specified ID and ignore message body (if a different ID is present there).")
	public ResponseEntity<Absence> update(
			@ApiParam(value = "ID of the target absence")
			Long id,
			@ApiParam(value = "Absence information to be stored")
			Absence absence){

		absence.setId(id);

		Absence elementUpdated = repository.save(absence);
		
		return new ResponseEntity<Absence>(elementUpdated, HttpStatus.OK);
	}

	@ApiOperation(value = "Create a new absence linked to a known user")
	public ResponseEntity<Void> create(
			@ApiParam(value = "Absence information to be stored")
			Absence absence){

		Absence createdelement = repository.save(absence);
		
		//Location
		//Get current resource url
		///{id}
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(createdelement.getId()).toUri();
		
		return ResponseEntity.created(uri).build();
	}
		
}
