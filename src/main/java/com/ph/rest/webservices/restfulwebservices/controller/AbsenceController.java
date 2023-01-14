package com.ph.rest.webservices.restfulwebservices.controller;

import com.ph.model.PersonNotFoundException;
import com.ph.model.TimeSpan;
import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.repository.AbsenceJpaRepository;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.rest.webservices.restfulwebservices.model.*;
import com.ph.service.AuthService;
import com.ph.service.FreeTimeService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/absences")
public class AbsenceController implements IController<Absence,Long> {
	
	@Autowired
	private AbsenceJpaRepository repository;
	@Autowired
	private PersonJpaRepository personRepository;

	@ApiOperation(value = "Gets all stored absences")
	public ResponseEntity<AbsenceListResponse> getAll(
			@ApiParam(value = "Bearer token for authentification", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			AbsenceListResponse response = new AbsenceListResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		List<Absence> results = repository.findAll().stream().map(x -> Absence.fromEntity(x)).collect(Collectors.toList());
		AbsenceListResponse response = new AbsenceListResponse();
		response.setRespondeCode(RepsonseCode.OK);
		response.setList(results);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value = "Gets all stored absences for the specified person")
	@GetMapping("/person/{personId}")
	public ResponseEntity<AbsenceListResponse> getByUser(
			@ApiParam(value = "The user to get absences for", required = true)
			@PathVariable
			Long personId,
			@ApiParam(value = "Bearer token for authentification", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			AbsenceListResponse response = new AbsenceListResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		if(personRepository.existsById(personId)){
			List<Absence> results = repository.findByPerson(personRepository.findById(personId).get()).stream().map(x -> Absence.fromEntity(x)).collect(Collectors.toList());
			AbsenceListResponse response = new AbsenceListResponse();
			response.setRespondeCode(RepsonseCode.OK);
			response.setList(results);
			return new ResponseEntity<>(response,HttpStatus.OK);
		}else{
			AbsenceListResponse response = new AbsenceListResponse();
			response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
			response.setList(null);
			response.setMessage(new PersonNotFoundException(personId).getMessage());
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value="Gets time-slots without absences for a single person",
			notes = "Gets time-slots without absences inside the specified time-frame. Optionally an importance level can be specified up to which absences are to be ignored.")
	@GetMapping("/free/person/{personIds}")
	public ResponseEntity<TimeSpanListResponse> findFreeTimesByUser(
			@ApiParam(value = "The inclusive start-date of the desired time-frame in ISO format", required = true)
			@RequestHeader("start")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate startDate,
			@ApiParam(value = "The inclusive end-date of the desired time-frame in ISO format", required = true)
			@RequestHeader("end")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate endDate,
			@ApiParam(value = "The importance level up to which (inclusive) absences are to be ignored", required = false)
			@RequestParam(required = false)
			Integer upToLevel,
			@ApiParam(value = "The users to get absences for. Comma separated list of IDs", required = true)
			@PathVariable
			List<Long> personIds,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			TimeSpanListResponse response = new TimeSpanListResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		List<PersonEntity> persons = personRepository.findAllById(personIds);
		if(persons.isEmpty()) {
			TimeSpanListResponse response = new TimeSpanListResponse();
			response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
			response.setList(null);
			response.setMessage(new PersonNotFoundException(personIds).getMessage());
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}

		TimeSpanListResponse response = new TimeSpanListResponse();
		List<AbsenceEntity> absences = repository.findByPersonIn(persons);
		if(upToLevel != null){
			absences = absences.stream()
					.filter(absence -> absence.getLevel() > upToLevel)
					.collect(Collectors.toList());
			response.setMessage(absences.size()+" relevant absences with importance level > "+upToLevel);
		}

		List<TimeSpan> freeTimes = FreeTimeService.findFreeTimes(
				new TimeSpan(startDate,endDate),
				absences);

		response.setMessage("Considered person ids: "+persons.stream().map(x -> x.getId().toString()).collect(Collectors.joining(", ")));
		response.setRespondeCode(RepsonseCode.OK);
		response.setList(freeTimes);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value="Gets time-slots without absences",
					notes = "Gets time-slots without absences inside the specified time-frame. Optionally an importance level can be specified up to which absences are to be ignored.")
	@GetMapping("/free")
	public ResponseEntity<TimeSpanListResponse> findFreeTimes(
			@ApiParam(value = "The inclusive start-date of the desired time-frame in ISO format", required = true)
			@RequestHeader("start")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate startDate,
			@ApiParam(value = "The inclusive end-date of the desired time-frame in ISO format", required = true)
			@RequestHeader("end")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate endDate,
			@ApiParam(value = "The importance level up to which (inclusive) absences are to be ignored", required = false)
			@RequestParam(required = false)
			Integer upToLevel,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			TimeSpanListResponse response = new TimeSpanListResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		TimeSpanListResponse response = new TimeSpanListResponse();
		List<AbsenceEntity> absences = repository.findAll();
		if(upToLevel != null){
			absences = absences.stream()
					.filter(absence -> absence.getLevel() > upToLevel)
					.collect(Collectors.toList());
			response.setMessage(absences.size()+" relevant absences with importance level > "+upToLevel);
		}

		List<TimeSpan> freeTimes = FreeTimeService.findFreeTimes(
				new TimeSpan(startDate,endDate),
				absences);


		response.setRespondeCode(RepsonseCode.OK);
		response.setList(freeTimes);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value = "Get a single absence by ID")
	public ResponseEntity<AbsenceResponse> get(
			@ApiParam(value = "ID of the desired absence")
			Long id,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			AbsenceResponse response = new AbsenceResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		if(repository.existsById(id)){
			Absence absence = Absence.fromEntity(repository.findById(id).orElse(null));

			AbsenceResponse response = new AbsenceResponse();
			response.setRespondeCode(RepsonseCode.OK);
			response.setAbsence(absence);
			return new ResponseEntity(response,HttpStatus.OK);
		}else{
			AbsenceResponse response = new AbsenceResponse();
			response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
			response.setAbsence(null);
			return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Delete a single absence by ID")
	public ResponseEntity<Response> delete(
			@ApiParam(value = "ID of the target absence")
			Long id,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey) {
		if(!AuthService.isTokenValid(authKey)){
			Response response = new Response();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		if(repository.existsById(id)){
			repository.deleteById(id);

			Response response = new Response();
			response.setRespondeCode(RepsonseCode.DELETE_SUCCESSFULL);
			response.setMessage("Deleted absence with ID: "+id);
			return new ResponseEntity<>(response,HttpStatus.OK);
		}else{
			Response response = new Response();
			response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
			return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Update a single absence by ID",
					notes="Will use the URI specified ID and ignore message body (if a different ID is present there).")
	public ResponseEntity<ResourceIdResponse<Long>> update(
			@ApiParam(value = "ID of the target absence")
			Long id,
			@ApiParam(value = "Absence information to be stored")
			Absence absence,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		if(absence != null){
			Optional<AbsenceEntity> oldAbsence = repository.findById(id);
			if(oldAbsence.isPresent()){
				return saveAbsence(absence,oldAbsence.get());
			}else{
				Response response = new Response();
				response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
				return new ResponseEntity(response,HttpStatus.BAD_REQUEST);
			}
		}else{
			ResourceIdResponse response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
			response.setMessage("Given absence was null");
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}
	}

	@ApiOperation(value = "Create a new absence linked to a known person")
	public ResponseEntity<ResourceIdResponse<Long>> create(
			@ApiParam(value = "Absence information to be stored")
			Absence absence,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			ResourceIdResponse response = new ResourceIdResponse();
			response.setMessage("Authorization key is invalid");
			response.setRespondeCode(RepsonseCode.TOKEN_DENIED);
			return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
		}

		return saveAbsence(absence, null);
	}

	private ResponseEntity<ResourceIdResponse<Long>> saveAbsence(Absence absence, AbsenceEntity oldPerson){
		try {
			AbsenceEntity absenceEntity = absence.toEntity(oldPerson, personRepository);
			System.out.println("Post-------------------"+absenceEntity.getStartDate()+" "+absenceEntity.getEndDate());
			AbsenceEntity absenceUpdated = repository.save(absenceEntity);

			ResourceIdResponse<Long> response = new ResourceIdResponse();
			response.setResourceId(absenceUpdated.getId());
			response.setRespondeCode(RepsonseCode.SAVE_SUCCESSFULL);
			response.setMessage("Saved absence with id: "+absenceUpdated.getId());
			return new ResponseEntity<>(response, HttpStatus.OK);
		}catch (PersonNotFoundException ex){
			ResourceIdResponse<Long> response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.SAVE_FAILED);
			response.setMessage(ex.getMessage());
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}
	}
		
}
