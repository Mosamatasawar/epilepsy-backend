package com.epilepsy.epilepsy_backend.service;

import com.epilepsy.epilepsy_backend.dto.Dtos;
import com.epilepsy.epilepsy_backend.model.Patient;
import com.epilepsy.epilepsy_backend.repository.PatientRepository;
import com.epilepsy.epilepsy_backend.repository.PatientSpecifications;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final PatientRepository patientRepo;

    public PatientService(PatientRepository patientRepo) {
        this.patientRepo = patientRepo;
    }

    public List<Dtos.PatientSummaryResponse> search(String name,
                                                     Integer age,
                                                     String gender) {
        Specification<Patient> spec = Specification
            .where(PatientSpecifications.hasName(name))
            .and(PatientSpecifications.hasAge(age))
            .and(PatientSpecifications.hasGender(gender));

        // AFTER — use the searchByName query method that already exists on PatientRepository
return patientRepo.searchByName(name == null ? "" : name)
    .stream()
    .filter(p -> age    == null || p.getAge().equals(age))
    .filter(p -> gender == null || gender.isBlank()
                               || p.getGender().name().equalsIgnoreCase(gender))
    .map(Dtos.PatientSummaryResponse::from)
    .collect(Collectors.toList());
    }

    public Dtos.PatientDetailResponse getDetail(Long id) {
    Patient p = patientRepo.findById(id)
        .orElseThrow(() ->
            new EntityNotFoundException("Patient not found: " + id));

    Dtos.PatientDetailResponse resp = new Dtos.PatientDetailResponse();
    resp.id        = p.getId();
    resp.name      = p.getName();
    resp.age       = p.getAge();
    resp.gender    = p.getGender().name();
    resp.createdAt = p.getCreatedAt();
    resp.visits    = p.getVisits()
        .stream()
        .sorted(java.util.Comparator.comparing(
            com.epilepsy.epilepsy_backend.model.Visit::getCreatedAt).reversed())
        .map(v -> Dtos.VisitResponse.from(v, baseUrl))
        .collect(java.util.stream.Collectors.toList());
    return resp;
}
public void deletePatient(Long id) {
    if (!patientRepo.existsById(id))
        throw new EntityNotFoundException("Patient not found: " + id);
    patientRepo.deleteById(id);
}
}