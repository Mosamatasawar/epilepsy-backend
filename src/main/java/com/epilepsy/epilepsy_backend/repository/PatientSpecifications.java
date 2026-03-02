package com.epilepsy.epilepsy_backend.repository;

import com.epilepsy.epilepsy_backend.model.Patient;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecifications {

    public static Specification<Patient> hasName(String name) {
        return (root, query, cb) ->
            name == null || name.isBlank()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")),
                          "%" + name.toLowerCase() + "%");
    }

    public static Specification<Patient> hasAge(Integer age) {
        return (root, query, cb) ->
            age == null
                ? cb.conjunction()
                : cb.equal(root.get("age"), age);
    }

    public static Specification<Patient> hasGender(String gender) {
        return (root, query, cb) -> {
            if (gender == null || gender.isBlank()) return cb.conjunction();
            try {
                return cb.equal(root.get("gender"),
                                Patient.Gender.valueOf(gender));
            } catch (IllegalArgumentException e) {
                return cb.disjunction();
            }
        };
    }
}