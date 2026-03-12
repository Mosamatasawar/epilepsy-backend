package com.epilepsy.epilepsy_backend.service;

import com.epilepsy.epilepsy_backend.dto.Dtos;
import com.epilepsy.epilepsy_backend.model.Patient;
import com.epilepsy.epilepsy_backend.model.Visit;
import com.epilepsy.epilepsy_backend.repository.PatientRepository;
import com.epilepsy.epilepsy_backend.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class VisitService {

    @Value("${app.upload-dir:D:/EpilepsySystem/uploads/mri}")
    private String uploadDir;

    @Value("${app.ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final PatientRepository patientRepo;
    private final VisitRepository   visitRepo;
    private final RestTemplate      restTemplate;

    public VisitService(PatientRepository patientRepo,
                        VisitRepository   visitRepo,
                        RestTemplate      restTemplate) {
        this.patientRepo  = patientRepo;
        this.visitRepo    = visitRepo;
        this.restTemplate = restTemplate;
    }

    public Dtos.VisitResponse createVisit(Dtos.CreateVisitRequest req,
                                          MultipartFile t1File,
                                          MultipartFile flairFile)
                                          throws IOException {

        // 1. Resolve patient
        Patient patient;
        if (req.patientId != null) {
            patient = patientRepo.findById(req.patientId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Patient not found: " + req.patientId));
        } else {
            Patient.Gender gender = Patient.Gender.valueOf(req.gender.trim().toUpperCase());
            java.util.List<Patient> existing = patientRepo.findExistingPatients(
                req.name.trim(), req.age, gender);

            if (!existing.isEmpty()) {
                patient = existing.get(0);
                if (req.phone   != null && !req.phone.isBlank())   patient.setPhone(req.phone);
                if (req.email   != null && !req.email.isBlank())   patient.setEmail(req.email);
                if (req.address != null && !req.address.isBlank()) patient.setAddress(req.address);
                patientRepo.save(patient);
            } else {
                Patient newPatient = new Patient();
                newPatient.setName(req.name.trim());
                newPatient.setAge(req.age);
                newPatient.setGender(gender);
                newPatient.setPhone(req.phone);
                newPatient.setEmail(req.email);
                newPatient.setAddress(req.address);
                patient = patientRepo.save(newPatient);
            }
        }

        // 2. Save both NIfTI files
        String t1Path    = saveNiftiFile(t1File,    "t1");
        String flairPath = saveNiftiFile(flairFile, "flair");

        // 3. Call AI service
        Dtos.AiPredictionResponse aiResp = callAiService(t1Path, flairPath);

        // 4. Build visit and save directly via visitRepo to get generated ID back
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setMriPath(t1Path);
        visit.setPrescription(req.prescription);
        visit.setResult(mapResult(aiResp.result));
        visit.setConfidence(aiResp.confidence);
        visit.setEpilepsyProb(aiResp.epilepsyProb);
        visit.setNoEpilepsyProb(aiResp.noEpilepsyProb);

        // Save directly — returns the managed entity with generated id and createdAt
        Visit savedVisit = visitRepo.save(visit);

        // 5. Return response using savedVisit which has id and createdAt populated
        return Dtos.VisitResponse.from(savedVisit, baseUrl, aiResp);
    }

    private String saveNiftiFile(MultipartFile file, String prefix)
            throws IOException {
        validateNifti(file);
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        String ext      = getExtension(file.getOriginalFilename());
        String filename = prefix + "_" + UUID.randomUUID() + "." + ext;
        Path   dest     = dir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return dest.toString();
    }

    private Dtos.AiPredictionResponse callAiService(String t1Path, String flairPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("t1_file",    new FileSystemResource(t1Path));
        body.add("flair_file", new FileSystemResource(flairPath));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Dtos.AiPredictionResponse> response = restTemplate.postForEntity(
            aiServiceUrl + "/predict", request, Dtos.AiPredictionResponse.class);

        if (response.getBody() == null || response.getBody().result == null)
            throw new RuntimeException("AI service returned empty response");

        return response.getBody();
    }

    private Visit.Result mapResult(String label) {
        return "Epilepsy Detected".equalsIgnoreCase(label)
            ? Visit.Result.EPILEPSY_DETECTED
            : Visit.Result.NO_EPILEPSY_DETECTED;
    }

    private void validateNifti(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null)
            throw new IllegalArgumentException("Missing filename");
        if (!name.endsWith(".nii") && !name.endsWith(".nii.gz"))
            throw new IllegalArgumentException("Only .nii and .nii.gz files are allowed");
        if (file.getSize() > 200L * 1024 * 1024)
            throw new IllegalArgumentException("File exceeds 200 MB limit");
    }

    private String getExtension(String filename) {
        if (filename.endsWith(".nii.gz")) return "nii.gz";
        int dot = filename.lastIndexOf('.');
        return (dot < 0) ? "nii" : filename.substring(dot + 1);
    }
}
