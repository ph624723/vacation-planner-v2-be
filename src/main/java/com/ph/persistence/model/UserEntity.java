package com.ph.persistence.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

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

	@Override
	public String toString(){
		return "[\n"+
				"name:"+name+"\n"+
				"password:"+password+"\n"+
				"person:"+personData.toString()+
				"\n]";
	}
}