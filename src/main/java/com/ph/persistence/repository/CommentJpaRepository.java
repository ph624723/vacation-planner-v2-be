package com.ph.persistence.repository;

import com.ph.persistence.model.AbsenceEntity;
import com.ph.persistence.model.CommentEntity;
import com.ph.persistence.model.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long>{
}