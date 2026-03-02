package com.epilepsy.epilepsy_backend.repository;

import com.epilepsy.epilepsy_backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long>,
                                           JpaSpecificationExecutor<Patient> {

    // DISTINCT prevents duplicate rows from JOIN FETCH when patient has multiple visits
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.visits v WHERE p.id = :id ORDER BY v.createdAt DESC")
    Optional<Patient> findByIdWithVisits(@Param("id") Long id);

    // Find existing patient by name+age+gender to avoid duplicates on re-submission
    @Query("SELECT p FROM Patient p WHERE UPPER(p.name) = UPPER(:name) AND p.age = :age AND p.gender = :gender ORDER BY p.createdAt ASC")
    java.util.List<Patient> findExistingPatients(@Param("name") String name,
                                                  @Param("age") Integer age,
                                                  @Param("gender") Patient.Gender gender);
}