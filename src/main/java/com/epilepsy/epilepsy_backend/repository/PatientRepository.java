package com.epilepsy.epilepsy_backend.repository;

import com.epilepsy.epilepsy_backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Search by name (case-insensitive)
    @Query("SELECT p FROM Patient p WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :name, '%'))")
    List<Patient> searchByName(@Param("name") String name);

    // Search by phone number
    @Query("SELECT p FROM Patient p WHERE p.phone LIKE CONCAT('%', :phone, '%')")
    List<Patient> searchByPhone(@Param("phone") String phone);

    // Search by name OR phone
    @Query("SELECT p FROM Patient p WHERE UPPER(p.name) LIKE UPPER(CONCAT('%', :q, '%')) OR p.phone LIKE CONCAT('%', :q, '%')")
    List<Patient> searchByNameOrPhone(@Param("q") String q);

    // Find exact match to avoid duplicates
    @Query("SELECT p FROM Patient p WHERE UPPER(p.name) = UPPER(:name) AND p.age = :age AND p.gender = :gender ORDER BY p.createdAt ASC")
    List<Patient> findExistingPatients(
        @Param("name") String name,
        @Param("age") Integer age,
        @Param("gender") Patient.Gender gender
    );

    // Find by phone exact
    Optional<Patient> findByPhone(String phone);

    // Fetch with visits eagerly
    @Query("SELECT DISTINCT p FROM Patient p LEFT JOIN FETCH p.visits v WHERE p.id = :id ORDER BY v.createdAt DESC")
    Optional<Patient> findByIdWithVisits(@Param("id") Long id);
}
