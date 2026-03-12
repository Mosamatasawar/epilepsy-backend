package com.epilepsy.epilepsy_backend.repository;

import com.epilepsy.epilepsy_backend.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    // Fetch visit with patient eagerly to avoid lazy loading issues
    @Query("SELECT v FROM Visit v JOIN FETCH v.patient WHERE v.id = :id")
    Optional<Visit> findByIdWithPatient(@Param("id") Long id);
}