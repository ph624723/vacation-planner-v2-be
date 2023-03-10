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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/absences")
@Api(tags = {"Absences"})
public class AbsenceController implements IController<Absence,Long> {
	
	@Autowired
	private AbsenceJpaRepository repository;
	@Autowired
	private PersonJpaRepository personRepository;

	@ApiOperation(value = "Gets all stored absences",tags = {"Absences"})
	public ResponseEntity<AbsenceListResponse> getAll(
			@ApiParam(value = "Bearer token for authentification", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			if(!AuthService.isTokenValid(authKey)){
				return AuthService.unauthorizedResponse(new AbsenceListResponse());
			}
		}

		List<Absence> results = repository.findAll().stream().map(x -> Absence.fromEntity(x)).collect(Collectors.toList());
		AbsenceListResponse response = new AbsenceListResponse();
		response.setRespondeCode(RepsonseCode.OK);
		response.setList(results);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value = "Gets all stored absences for the specified person",tags = {"Absences"})
	@GetMapping("/person/{personIds}")
	public ResponseEntity<AbsenceByPersonListResponse> getByUser(
			@ApiParam(value = "The persons to get absences for. Comma separated list", required = true)
			@PathVariable
			List<Long> personIds,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new AbsenceByPersonListResponse());
		}

		AbsenceByPersonListResponse response = new AbsenceByPersonListResponse();
		response.setList(new ArrayList<>());
		for (Long personId: personIds) {
			if(personRepository.existsById(personId)){
				List<Absence> results = repository.findByPerson(personRepository.findById(personId).get()).stream().map(x -> Absence.fromEntity(x)).collect(Collectors.toList());
				AbsenceList absenceList = new AbsenceList();
				absenceList.setAbsences(results);
				absenceList.setPersonId(personId);
				response.getList().add(absenceList);
			}else{
				response = new AbsenceByPersonListResponse();
				response.setRespondeCode(RepsonseCode.UNKNOWN_ID);
				response.setMessage(new PersonNotFoundException(personId).getMessage());
				return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
			}
		}

		response.setRespondeCode(RepsonseCode.OK);
		response.setMessage("Considered person ids: "+personIds.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value="Gets time-slots without absences for a comma list of persons",
			notes = "Gets time-slots without absences inside the specified time-frame. Optionally an importance level can be specified up to which absences are to be ignored.",
			tags = {"Free time-slots"})
	@PostMapping("/free/person/{personIds}")
	public ResponseEntity<TimeSpanListResponse> findFreeTimesByUser(
			@ApiParam(value = "The inclusive start-date to end-date of the desired time-frame", required = true)
			@RequestBody
			TimeSpan timeSpan,
			@ApiParam(value = "The importance level up to which (inclusive) absences are to be ignored", required = false)
			@RequestParam(required = false)
			Integer upToLevel,
			@ApiParam(value = "The persons to get absences for. Comma separated list of IDs", required = true)
			@PathVariable
			List<Long> personIds,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new TimeSpanListResponse());
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
				timeSpan.cleanUp(),
				absences);

		response.setMessage("Considered person ids: "+persons.stream().map(x -> x.getId().toString()).collect(Collectors.joining(", ")));
		response.setRespondeCode(RepsonseCode.OK);
		response.setList(freeTimes);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value="Gets time-slots without absences",
					notes = "Gets time-slots without absences inside the specified time-frame. Optionally an importance level can be specified up to which absences are to be ignored.",
					tags = {"Free time-slots"})
	@PostMapping("/free")
	public ResponseEntity<TimeSpanListResponse> findFreeTimes(
			@ApiParam(value = "The inclusive start-date to end-date of the desired time-frame", required = true)
			@RequestBody
			TimeSpan timeSpan,
			@ApiParam(value = "The importance level up to which (inclusive) absences are to be ignored", required = false)
			@RequestParam(required = false)
			Integer upToLevel,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new TimeSpanListResponse());
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
				timeSpan.cleanUp(),
				absences);


		response.setRespondeCode(RepsonseCode.OK);
		response.setList(freeTimes);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@ApiOperation(value = "Get a single absence by ID",tags = {"Absences"})
	public ResponseEntity<AbsenceResponse> get(
			@ApiParam(value = "ID of the desired absence")
			Long id,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new AbsenceResponse());
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

	@ApiOperation(value = "Delete a single absence by ID",tags = {"Absences"})
	public ResponseEntity<Response> delete(
			@ApiParam(value = "ID of the target absence")
			Long id,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey) {
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new Response());
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
					notes="Will use the URI specified ID and ignore message body (if a different ID is present there).",tags = {"Absences"})
	public ResponseEntity<ResourceIdResponse<Long>> update(
			@ApiParam(value = "ID of the target absence")
			Long id,
			@ApiParam(value = "Absence information to be stored")
			Absence absence,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new ResourceIdResponse<Long>());
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

	@ApiOperation(value = "Create a new absence linked to a known person",tags = {"Absences"})
	public ResponseEntity<ResourceIdResponse<Long>> create(
			@ApiParam(value = "Absence information to be stored")
			Absence absence,
			@ApiParam(value = "Bearer token for authentication", required = true)
			@RequestHeader("Authorization")
			String authKey){
		if(!AuthService.isTokenValid(authKey)){
			return AuthService.unauthorizedResponse(new ResourceIdResponse<Long>());
		}

		if(absence != null){
			return saveAbsence(absence, null);
		}else{
			ResourceIdResponse response = new ResourceIdResponse();
			response.setRespondeCode(RepsonseCode.UPDATE_FAILED);
			response.setMessage("Given absence was null");
			return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
		}
	}

	private ResponseEntity<ResourceIdResponse<Long>> saveAbsence(Absence absence, AbsenceEntity oldAbsence){
		try {
			AbsenceEntity absenceEntity = absence.toEntity(oldAbsence, personRepository);

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
