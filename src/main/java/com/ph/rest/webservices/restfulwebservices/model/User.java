package com.ph.rest.webservices.restfulwebservices.model;

import com.ph.model.PersonNotFoundException;
import com.ph.model.UserNotFoundException;
import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.UserEntity;
import com.ph.persistence.repository.PersonJpaRepository;
import com.ph.persistence.repository.UserJpaRepository;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Optional;

public class User {
	@Id
	@Getter
	@Setter
	private String name;
	@Getter
	@Setter
	private String password;
	@Getter
	@Setter
	private Person person;

	public static User fromEntity(UserEntity entity){
		User user = new User();
		user.setName(entity.getName());
		user.setPassword(entity.getPassword());
		user.setPerson(Person.fromEntity(entity.getPersonData()));
		return user;
	}

	public UserEntity toEntity() {
		UserEntity entity = new UserEntity();
		entity.setName(name);
		entity.setPassword(password);
		entity.setPersonData(person.toEntity(entity));
		return entity;
	}

	@Override
	public String toString(){
		return "[\n"+
				"name:"+name+"\n"+
				"password:"+password+"\n"+
				"person:"+person.toString()+
				"\n]";
	}
}