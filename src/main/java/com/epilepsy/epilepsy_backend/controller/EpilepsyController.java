package com.epilepsy.epilepsy_backend.controller;

import com.epilepsy.epilepsy_backend.dto.Dtos;
import com.epilepsy.epilepsy_backend.model.Patient;
import com.epilepsy.epilepsy_backend.model.Visit;
import com.epilepsy.epilepsy_backend.repository.PatientRepository;
import com.epilepsy.epilepsy_backend.repository.VisitRepository;
import com.epilepsy.epilepsy_backend.service.VisitService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class EpilepsyController {

    @Autowired PatientRepository patientRepo;
    @Autowired VisitRepository   visitRepo;
    @Autowired VisitService      visitService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ── Patients ──────────────────────────────────────────────────────────

    @GetMapping("/patients")
    @Transactional(readOnly = true)
    public ResponseEntity<?> listPatients() {
        List<Dtos.PatientResponse> list = patientRepo.findAll().stream()
            .map(Dtos.PatientResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/patients/search")
    @Transactional(readOnly = true)
    public ResponseEntity<?> searchPatients(@RequestParam String q) {
        List<Dtos.PatientResponse> list = patientRepo.searchByNameOrPhone(q).stream()
            .map(Dtos.PatientResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/patients/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPatient(@PathVariable Long id) {
        Patient p = patientRepo.findByIdWithVisits(id)
            .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + id));
        return ResponseEntity.ok(Dtos.PatientWithVisitsResponse.from(p, baseUrl));
    }

    @PostMapping("/patients")
    @Transactional
    public ResponseEntity<?> createPatient(@RequestBody Dtos.CreatePatientRequest req) {
         System.out.println("DEBUG createPatient gender: '" + req.gender + "'");
        Patient p = new Patient();
        p.setName(req.name.trim());
        p.setAge(req.age);
        p.setGender(Patient.Gender.valueOf(req.gender.trim().toUpperCase()));
        p.setPhone(req.phone);
        p.setEmail(req.email);
        p.setAddress(req.address);
        return ResponseEntity.ok(Dtos.PatientResponse.from(patientRepo.save(p)));
    }

    @PutMapping("/patients/{id}")
    @Transactional
    public ResponseEntity<?> updatePatient(@PathVariable Long id,
                                           @RequestBody Dtos.CreatePatientRequest req) {
        Patient p = patientRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + id));
        p.setName(req.name.trim());
        p.setAge(req.age);
        p.setGender(Patient.Gender.valueOf(req.gender.trim().toUpperCase()));
        p.setPhone(req.phone);
        p.setEmail(req.email);
        p.setAddress(req.address);
        return ResponseEntity.ok(Dtos.PatientResponse.from(patientRepo.save(p)));
    }

    @DeleteMapping("/patients/{id}")
    @Transactional
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        if (!patientRepo.existsById(id))
            throw new EntityNotFoundException("Patient not found: " + id);
        patientRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Visits / Diagnosis ────────────────────────────────────────────────

    @PostMapping("/visits")
    public ResponseEntity<?> createVisit(
        @RequestParam(required = false) Long    patientId,
        @RequestParam(required = false) String  name,
        @RequestParam(required = false) Integer age,
        @RequestParam(required = false) String  gender,
        @RequestParam(required = false) String  phone,
        @RequestParam(required = false) String  email,
        @RequestParam(required = false) String  address,
        @RequestParam(required = false) String  prescription,
        @RequestParam("t1File")         MultipartFile t1File,
        @RequestParam("flairFile")      MultipartFile flairFile
    ) throws Exception {
         System.out.println("DEBUG gender received: '" + gender + "'");
        Dtos.CreateVisitRequest req = new Dtos.CreateVisitRequest();
        req.patientId    = patientId;
        req.name         = name;
        req.age          = age;
        req.gender       = gender != null ? gender.trim().toUpperCase() : null;
        req.phone        = phone;
        req.email        = email;
        req.address      = address;
        req.prescription = prescription;

        // VisitService.createVisit takes exactly 3 args (req, t1File, flairFile)
        // baseUrl is injected inside VisitService via @Value — not passed here
        Dtos.VisitResponse resp = visitService.createVisit(req, t1File, flairFile);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/visits/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getVisit(@PathVariable Long id) {
        Visit found = visitRepo.findByIdWithPatient(id)
            .orElseThrow(() -> new EntityNotFoundException("Visit not found: " + id));
        return ResponseEntity.ok(Dtos.VisitResponse.from(found, baseUrl, null));
    }

    @GetMapping("/patients/{id}/visits")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPatientVisits(@PathVariable Long id) {
        Patient p = patientRepo.findByIdWithVisits(id)
            .orElseThrow(() -> new EntityNotFoundException("Patient not found: " + id));
        List<Dtos.VisitResponse> visits = p.getVisits().stream()
            .map(v -> Dtos.VisitResponse.from(v, baseUrl, null))
            .collect(Collectors.toList());
        return ResponseEntity.ok(visits);
    }

    // ── Error handlers ────────────────────────────────────────────────────

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleError(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
    }
}
