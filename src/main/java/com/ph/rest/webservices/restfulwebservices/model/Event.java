package com.ph.rest.webservices.restfulwebservices.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.*;
import com.ph.persistence.repository.PersonJpaRepository;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.util.DateUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ApiModel(description = "Meant to store potential event times for groups of users.")
public class Event {
	@ApiModelProperty(position = 0, value = "", required = false)
	@Getter
	@Setter
	private Long id;
	@ApiModelProperty(position = 1, required = true)
	@Getter
	@Setter
	private Set<Long> personIds;
	@Getter
	@Setter
	private Set<Long> personIdsAccepted;
	@ApiModelProperty(position = 5, required = false, value = "Vacation with you.")
	@Getter
	@Setter
	private String description;
	@ApiModelProperty(position = 2, required = true, value = "1993-05-12")
	@Getter
	@Setter
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Date startDate;
	@ApiModelProperty(position = 3, required = true, value = "1993-05-24")
	@Getter
	@Setter
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Date endDate;
	@Getter
	@Setter
	private EventPlannerConfigEntity eventPlannerConfig;

	@Getter
	@Setter
	private String groupName;

	public static Event fromEntity(EventEntity entity){
		Event event = new Event();
		event.setId(entity.getId());
		event.setPersonIds(entity.getPersons().stream().map(x -> x.getId()).collect(Collectors.toSet()));
		event.setPersonIdsAccepted(entity.getAcceptedPersons().stream().map(x -> x.getId()).collect(Collectors.toSet()));
		event.setDescription(entity.getDescription());
		event.setStartDate(entity.getStartDate());
		event.setEndDate(entity.getEndDate());
		event.setEventPlannerConfig(entity.getEventPlannerConfig());
		event.setGroupName(entity.getGroup() != null ? entity.getGroup().getName() : "");
		return event;
	}

	public EventEntity toEntity(EventEntity oldEvent, PersonJpaRepository personJpaRepository) throws PersonNotFoundException {
		EventEntity entity = oldEvent == null ?
				new EventEntity() :
				oldEvent;
		entity.setDescription(description);
		entity.setStartDate(startDate);
		entity.setEndDate(endDate);
		entity.setEventPlannerConfig(eventPlannerConfig);
		entity.setGroup(new RoleEntity(groupName));
		if(personIds != null){
			entity.setPersons(new HashSet<>());
			for (Long personId :
					personIds) {
				Optional<PersonEntity> personEntity = personJpaRepository.findById(personId);
				if(personEntity.isPresent()) {
					entity.getPersons().add(personEntity.get());
				}else{
					throw new PersonNotFoundException(personId);
				}
			}
		}
		if(personIdsAccepted != null){
			entity.setAcceptedPersons(new HashSet<>());
			for (Long personId :
					personIdsAccepted) {
				Optional<PersonEntity> personEntity = personJpaRepository.findById(personId);
				if(personEntity.isPresent()) {
					entity.getAcceptedPersons().add(personEntity.get());
				}else{
					throw new PersonNotFoundException(personId);
				}
			}
		}
		return entity;
	}
}