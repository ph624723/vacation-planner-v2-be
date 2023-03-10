package com.ph.rest.webservices.restfulwebservices.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.EventEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.repository.PersonJpaRepository;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
	private List<Long> personIds;
	@ApiModelProperty(position = 5, required = false, value = "Vacation with you.")
	@Getter
	@Setter
	private String description;
	@ApiModelProperty(position = 2, required = true, value = "1993-05-12")
	@Getter
	@Setter
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private java.sql.Date startDate;
	@ApiModelProperty(position = 3, required = true, value = "1993-05-24")
	@Getter
	@Setter
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private java.sql.Date endDate;

	public static Event fromEntity(EventEntity entity){
		Event event = new Event();
		event.setId(entity.getId());
		event.setPersonIds(entity.getPersons().stream().map(x -> x.getId()).collect(Collectors.toList()));
		event.setDescription(entity.getDescription());
		event.setStartDate(entity.getStartDate());
		event.setEndDate(entity.getEndDate());
		return event;
	}

	public EventEntity toEntity(EventEntity oldEvent, PersonJpaRepository personJpaRepository) throws PersonNotFoundException {
		EventEntity entity = oldEvent == null ?
				new EventEntity() :
				oldEvent;
		entity.setDescription(description);
		entity.setStartDate(startDate);
		entity.setEndDate(endDate);
		if(personIds != null){
			entity.setPersons(new ArrayList<>());
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
		return entity;
	}
}