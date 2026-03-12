package com.epilepsy.epilepsy_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "mri_path", nullable = false, length = 500)
    private String mriPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Result result;

    @Column(columnDefinition = "TEXT")
    private String prescription;

    // ── Confidence fields (added for AI result storage) ───────────────────
    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "epilepsy_prob")
    private Double epilepsyProb;

    @Column(name = "no_epilepsy_prob")
    private Double noEpilepsyProb;
    // ─────────────────────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Result {
        EPILEPSY_DETECTED("Epilepsy Detected"),
        NO_EPILEPSY_DETECTED("No Epilepsy Detected");

        private final String label;
        Result(String label)      { this.label = label; }
        public String getLabel()  { return label; }
    }

    // ── Getters & Setters ─────────────────────────────────────────────────
    public Long getId()                              { return id; }
    public Patient getPatient()                      { return patient; }
    public void setPatient(Patient patient)          { this.patient = patient; }
    public String getMriPath()                       { return mriPath; }
    public void setMriPath(String mriPath)           { this.mriPath = mriPath; }
    public Result getResult()                        { return result; }
    public void setResult(Result result)             { this.result = result; }
    public String getPrescription()                  { return prescription; }
    public void setPrescription(String p)            { this.prescription = p; }
    public Double getConfidence()                    { return confidence; }
    public void setConfidence(Double confidence)     { this.confidence = confidence; }
    public Double getEpilepsyProb()                  { return epilepsyProb; }
    public void setEpilepsyProb(Double v)            { this.epilepsyProb = v; }
    public Double getNoEpilepsyProb()                { return noEpilepsyProb; }
    public void setNoEpilepsyProb(Double v)          { this.noEpilepsyProb = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
}
