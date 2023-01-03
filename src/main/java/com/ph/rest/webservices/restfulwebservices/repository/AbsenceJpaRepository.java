package com.ph.rest.webservices.restfulwebservices.repository;

import com.ph.rest.webservices.restfulwebservices.model.Absence;
import com.ph.rest.webservices.restfulwebservices.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbsenceJpaRepository extends JpaRepository<Absence, Long>{
    List<Absence> findByUser(User user);
}