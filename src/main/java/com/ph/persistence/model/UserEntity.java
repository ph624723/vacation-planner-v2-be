package com.ph.persistence.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user")
public class UserEntity {
	@Id
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String password;
	@Getter
	@Setter
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "person_id")
	private PersonEntity personData;

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

	@Override
	public String toString(){
		return "[\n"+
				"name:"+name+"\n"+
				"password:"+password+"\n"+
				"person:"+personData.toString()+
				"\n]";
	}
}