package com.ph.persistence.repository;

import com.ph.persistence.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventJpaRepository extends JpaRepository<EventEntity,Long> {

}
