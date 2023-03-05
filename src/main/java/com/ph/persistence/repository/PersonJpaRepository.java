package com.ph.persistence.repository;

import com.ph.persistence.model.PersonEntity;
import com.ph.persistence.model.RoleEntity;
import com.ph.rest.webservices.restfulwebservices.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PersonJpaRepository extends JpaRepository<PersonEntity, Long>{
    Set<PersonEntity> findByRolesIsIn(List<RoleEntity> roles);

    List<PersonEntity> findByContact(String contact);
}