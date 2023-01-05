package com.ph.rest.webservices.restfulwebservices.model;

import com.ph.model.PersonNotFoundException;
import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.repository.PersonJpaRepository;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Null;
import java.util.Date;
import java.util.Optional;

@ApiModel(description = "Meant to store absent times for individual users where no further plans are desired. \n" +
						"Includes an importance level from 0 (irrelevant) to 3 (high) to indicate the strictness of unavailability.")
public class Absence {
	@ApiModelProperty(position = 0, value = "", required = false)
	@Getter
	@Setter
	private Long id;
	@ApiModelProperty(position = 1, required = true)
	@Getter
	@Setter
	private Long personId;
	@ApiModelProperty(position = 5, required = false, value = "Vacation without you.")
	@Getter
	@Setter
	private String description;
	@ApiModelProperty(position = 2, required = true, value = "1993-05-12")
	@Getter
	@Setter
	private java.sql.Date startDate;
	@ApiModelProperty(position = 3, required = true, value = "1993-05-24")
	@Getter
	@Setter
	private java.sql.Date endDate;
	@ApiModelProperty(position = 4, required = true, value = "2")
	@Getter
	@Setter
	private int level;

	public static Absence fromEntity(AbsenceEntity entity){
		Absence absence = new Absence();
		absence.setId(entity.getId());
		absence.setPersonId(entity.getPerson().getId());
		absence.setDescription(entity.getDescription());
		absence.setStartDate(entity.getStartDate());
		absence.setEndDate(entity.getEndDate());
		absence.setLevel(entity.getLevel());
		return absence;
	}

	public AbsenceEntity toEntity(AbsenceEntity oldAbsence, PersonJpaRepository personJpaRepository) throws PersonNotFoundException {
		AbsenceEntity entity = oldAbsence == null ?
				new AbsenceEntity() :
				oldAbsence;
		entity.setDescription(description);
		entity.setLevel(level);
		entity.setStartDate(startDate);
		entity.setEndDate(endDate);
		Optional<PersonEntity> personEntity = personJpaRepository.findById(personId);
		if(personEntity.isPresent()) {
			entity.setPerson(personEntity.get());
		}else{
			throw new PersonNotFoundException(personId);
		}
		return entity;
	}
}