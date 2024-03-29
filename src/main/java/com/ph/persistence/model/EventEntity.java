package com.ph.persistence.model;

import com.ph.model.TimeSpan;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "event")
public class EventEntity {

	@Id
	@GeneratedValue
	@Getter
	@Setter
	private Long id;

	@Getter
	@Setter
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH, CascadeType.MERGE})
	@JoinColumn(name = "event_id")
	private Set<PersonEntity> persons = new HashSet<>();

	@Getter
	@Setter
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH, CascadeType.MERGE})
	@JoinColumn(name = "event_id_accepted")
	private Set<PersonEntity> acceptedPersons = new HashSet<>();

	@Getter
	@Setter
	@Type(type = "text")
	@Lob
	private String description;

	@Getter
	@Setter
	private java.sql.Date startDate;

	@Getter
	@Setter
	private java.sql.Date endDate;

	@Getter
	@Setter
	@OneToMany(mappedBy = "event")
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<CommentEntity> comments;

	@Getter
	@Setter
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Getter
	@Setter
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Getter
	@Setter
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="planner_config_id")
	private EventPlannerConfigEntity eventPlannerConfig;

	@Getter
	@Setter
	@ManyToOne
	private RoleEntity group;

	public TimeSpan asTimeSpan(){
		return new TimeSpan(startDate,endDate);
	}

	public EventEntity trimDescription(){
		if(description.length() > 20) {
			description = description.substring(0, 17) + "...";
		}
		return this;
	}
}