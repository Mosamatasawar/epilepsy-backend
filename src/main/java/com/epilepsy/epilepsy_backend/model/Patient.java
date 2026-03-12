package com.epilepsy.epilepsy_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients")
public class Patient {

    public enum Gender { MALE, FEMALE, OTHER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL,
               fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Visit> visits = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────
    public Long getId()                        { return id; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public Integer getAge()                    { return age; }
    public void setAge(Integer age)            { this.age = age; }
    public Gender getGender()                  { return gender; }
    public void setGender(Gender gender)       { this.gender = gender; }
    public String getPhone()                   { return phone; }
    public void setPhone(String phone)         { this.phone = phone; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getAddress()                 { return address; }
    public void setAddress(String address)     { this.address = address; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public LocalDateTime getUpdatedAt()        { return updatedAt; }
    public List<Visit> getVisits()             { return visits; }
    public void setVisits(List<Visit> visits)  { this.visits = visits; }
}
