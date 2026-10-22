package com.hitech.lms.domain.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubjectRepository extends JpaRepository<Subject, String>,
        JpaSpecificationExecutor<Subject> {
}