package com.ph.persistence.repository;

import com.ph.persistence.model.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonJpaRepository extends JpaRepository<PersonEntity, Long>{

}