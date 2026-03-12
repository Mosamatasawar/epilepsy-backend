package com.epilepsy.epilepsy_backend.dto;

import com.epilepsy.epilepsy_backend.model.Patient;
import com.epilepsy.epilepsy_backend.model.Visit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class Dtos {

    // ── Request DTOs ──────────────────────────────────────────────────────

    public static class CreatePatientRequest {
        public String  name;
        public Integer age;
        public String  gender;
        public String  phone;
        public String  email;
        public String  address;
    }

    public static class CreateVisitRequest {
        public Long    patientId;   // optional — reuse existing patient
        public String  name;
        public Integer age;
        public String  gender;
        public String  phone;
        public String  email;
        public String  address;
        public String  prescription;
    }

    // AI service raw response
    public static class AiPredictionResponse {
        public String result;
        public Double confidence;
        public Double epilepsyProb;
        public Double noEpilepsyProb;
    }

    // ── Response DTOs (NEW — used by new endpoints) ───────────────────────

    /** Full patient card with visit count — used by /patients list */
    public static class PatientResponse {
        public Long    id;
        public String  name;
        public Integer age;
        public String  gender;
        public String  phone;
        public String  email;
        public String  address;
        public String  registeredAt;
        public int     visitCount;

        public static PatientResponse from(Patient p) {
            PatientResponse r = new PatientResponse();
            r.id           = p.getId();
            r.name         = p.getName();
            r.age          = p.getAge();
            r.gender       = p.getGender().name();
            r.phone        = p.getPhone();
            r.email        = p.getEmail();
            r.address      = p.getAddress();
            r.registeredAt = p.getCreatedAt() != null
                ? p.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
            r.visitCount   = p.getVisits() != null ? p.getVisits().size() : 0;
            return r;
        }
    }

    /** Patient + full visits list — used by /patients/{id} */
    public static class PatientWithVisitsResponse {
        public Long              id;
        public String            name;
        public Integer           age;
        public String            gender;
        public String            phone;
        public String            email;
        public String            address;
        public String            registeredAt;
        public List<VisitResponse> visits;

        public static PatientWithVisitsResponse from(Patient p, String baseUrl) {
            PatientWithVisitsResponse r = new PatientWithVisitsResponse();
            r.id           = p.getId();
            r.name         = p.getName();
            r.age          = p.getAge();
            r.gender       = p.getGender().name();
            r.phone        = p.getPhone();
            r.email        = p.getEmail();
            r.address      = p.getAddress();
            r.registeredAt = p.getCreatedAt() != null
                ? p.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
            r.visits       = p.getVisits().stream()
                .map(v -> VisitResponse.from(v, baseUrl, null))
                .collect(Collectors.toList());
            return r;
        }
    }

    // ── Response DTOs (OLD — kept for PatientService compatibility) ───────

    /** Used by PatientService.search() */
    public static class PatientSummaryResponse {
        public Long    id;
        public String  name;
        public Integer age;
        public String  gender;
        public String  phone;
        public int     visitCount;
        public String  registeredAt;

        public static PatientSummaryResponse from(Patient p) {
            PatientSummaryResponse r = new PatientSummaryResponse();
            r.id           = p.getId();
            r.name         = p.getName();
            r.age          = p.getAge();
            r.gender       = p.getGender().name();
            r.phone        = p.getPhone();
            r.visitCount   = p.getVisits() != null ? p.getVisits().size() : 0;
            r.registeredAt = p.getCreatedAt() != null
                ? p.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
            return r;
        }
    }

    /** Used by PatientService.getDetail() */
    public static class PatientDetailResponse {
        public Long              id;
        public String            name;
        public Integer           age;
        public String            gender;
        public String            phone;
        public String            email;
        public String            address;
        public LocalDateTime     createdAt;
        public List<VisitResponse> visits;
    }

    // ── VisitResponse — unified, supports both 2-arg and 3-arg callers ────

    public static class VisitResponse {
        public Long    id;
        public Long    patientId;
        public String  patientName;
        public Integer patientAge;
        public String  patientGender;
        public String  patientPhone;
        public String  patientEmail;
        public String  patientAddress;
        public String  prescription;
        public String  result;
        public Double  confidence;
        public Double  epilepsyProb;
        public Double  noEpilepsyProb;
        public String  confidenceLevel;   // HIGH / MEDIUM / LOW
        public String  visitDate;
        public String  mriUrl;

        /**
         * 3-arg version — used by new endpoints and VisitService.
         * Pass aiResp=null when loading saved visits.
         */
        public static VisitResponse from(Visit v, String baseUrl,
                                         AiPredictionResponse aiResp) {
            VisitResponse r = new VisitResponse();
            r.id             = v.getId();
            r.patientId      = v.getPatient().getId();
            r.patientName    = v.getPatient().getName();
            r.patientAge     = v.getPatient().getAge();
            r.patientGender  = v.getPatient().getGender().name();
            r.patientPhone   = v.getPatient().getPhone();
            r.patientEmail   = v.getPatient().getEmail();
            r.patientAddress = v.getPatient().getAddress();
            r.prescription   = v.getPrescription();
            r.result         = v.getResult() != null ? v.getResult().name() : null;
            r.visitDate      = v.getCreatedAt() != null
                ? v.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")) : "";

            // Use fresh aiResp values if provided, else fall back to stored values
            if (aiResp != null) {
                r.confidence     = aiResp.confidence;
                r.epilepsyProb   = aiResp.epilepsyProb;
                r.noEpilepsyProb = aiResp.noEpilepsyProb;
            } else {
                r.confidence     = v.getConfidence();
                r.epilepsyProb   = v.getEpilepsyProb();
                r.noEpilepsyProb = v.getNoEpilepsyProb();
            }

            r.confidenceLevel = mapConfidenceLevel(r.confidence);
            r.mriUrl          = null;
            return r;
        }

        /**
         * 2-arg version — kept for PatientService.getDetail() compatibility.
         * Delegates to the 3-arg version with aiResp=null.
         */
        public static VisitResponse from(Visit v, String baseUrl) {
            return from(v, baseUrl, null);
        }

        private static String mapConfidenceLevel(Double confidence) {
            if (confidence == null) return "LOW";
            if (confidence >= 80.0) return "HIGH";
            if (confidence >= 60.0) return "MEDIUM";
            return "LOW";
        }
    }
}
