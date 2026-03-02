package com.epilepsy.epilepsy_backend.dto;

import com.epilepsy.epilepsy_backend.model.Patient;
import com.epilepsy.epilepsy_backend.model.Visit;
import java.time.LocalDateTime;
import java.util.List;

public class Dtos {

    public static class CreateVisitRequest {
        public String name;
        public Integer age;
        public String gender;
        public String prescription;
    }

    public static class PatientSummaryResponse {
        public Long id;
        public String name;
        public Integer age;
        public String gender;
        public LocalDateTime createdAt;
        public int totalVisits;

        public static PatientSummaryResponse from(Patient p) {
            PatientSummaryResponse r = new PatientSummaryResponse();
            r.id          = p.getId();
            r.name        = p.getName();
            r.age         = p.getAge();
            r.gender      = p.getGender().name();
            r.createdAt   = p.getCreatedAt();
            r.totalVisits = p.getVisits().size();
            return r;
        }
    }

    public static class VisitResponse {
        public Long id;
        public String mriPath;
        public String mriUrl;
        public String result;
        public String prescription;
        public LocalDateTime createdAt;
        public Double confidence;
        public Double epilepsyProb;
        public Double noEpilepsyProb;

        // Used when loading from DB (history view) — no confidence stored
        public static VisitResponse from(Visit v, String baseUrl) {
            VisitResponse r = new VisitResponse();
            r.id           = v.getId();
            r.mriPath      = v.getMriPath();
            r.mriUrl       = null; // NIfTI files are not served as images
            r.result       = v.getResult().getLabel();
            r.prescription = v.getPrescription();
            r.createdAt    = v.getCreatedAt();
            // confidence not stored in DB, so left as null for history view
            return r;
        }

        // Used immediately after prediction — includes live confidence values
        public static VisitResponse from(Visit v, String baseUrl,
                                         AiPredictionResponse ai) {
            VisitResponse r = from(v, baseUrl);
            if (ai != null) {
                r.confidence     = ai.confidence;
                r.epilepsyProb   = ai.epilepsy_prob;
                r.noEpilepsyProb = ai.no_epilepsy_prob;
            }
            return r;
        }
    }

    public static class PatientDetailResponse {
        public Long id;
        public String name;
        public Integer age;
        public String gender;
        public LocalDateTime createdAt;
        public List<VisitResponse> visits;
    }

    public static class AiPredictionResponse {
        public String result;
        public Double confidence;
        public Double epilepsy_prob;
        public Double no_epilepsy_prob;
    }
}