package com.epilepsy.epilepsy_backend.controller;
import org.springframework.transaction.annotation.Transactional;
import com.epilepsy.epilepsy_backend.dto.Dtos;
import com.epilepsy.epilepsy_backend.service.PatientService;
import com.epilepsy.epilepsy_backend.service.VisitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EpilepsyController {

    @Value("${app.upload-dir:D:/EpilepsySystem/uploads/mri}")
    private String uploadDir;

    private final VisitService   visitService;
    private final PatientService patientService;

    public EpilepsyController(VisitService visitService,
                               PatientService patientService) {
        this.visitService   = visitService;
        this.patientService = patientService;
    }

    // POST /api/visits  — accepts T1 + FLAIR NIfTI files
    @PostMapping(value = "/visits",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Dtos.VisitResponse> createVisit(
            @RequestParam("name")                          String name,
            @RequestParam("age")                           Integer age,
            @RequestParam("gender")                        String gender,
            @RequestParam(value = "prescription",
                          required = false)                String prescription,
            @RequestParam("t1_file")                       MultipartFile t1File,
            @RequestParam("flair_file")                    MultipartFile flairFile)
            throws IOException {

        Dtos.CreateVisitRequest req = new Dtos.CreateVisitRequest();
        req.name         = name;
        req.age          = age;
        req.gender       = gender;
        req.prescription = prescription;

        return ResponseEntity.ok(
            visitService.createVisit(req, t1File, flairFile));
    }

    // GET /api/patients
    @GetMapping("/patients")
    @Transactional
    public ResponseEntity<List<Dtos.PatientSummaryResponse>>
            searchPatients(
                @RequestParam(required = false) String  name,
                @RequestParam(required = false) Integer age,
                @RequestParam(required = false) String  gender) {

        return ResponseEntity.ok(
            patientService.search(name, age, gender));
    }

    // GET /api/patients/{id}
    @GetMapping("/patients/{id}")
    public ResponseEntity<Dtos.PatientDetailResponse>
            getPatient(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getDetail(id));
    }

    // GET /api/images/{filename}
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String filename) {

        java.nio.file.Path path =
            Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new FileSystemResource(path);

        if (!resource.exists())
            return ResponseEntity.notFound().build();

        String contentType = filename.endsWith(".png")
            ? "image/png" : "image/jpeg";

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }
    // DELETE /api/patients/{id}
    @DeleteMapping("/patients/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
    patientService.deletePatient(id);
    return ResponseEntity.noContent().build();
}

}
