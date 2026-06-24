package com.authenhire.authenhire.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authenhire.authenhire.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByAdminStatus(String adminStatus);

    long countByAdminStatus(String adminStatus);

    List<Company> findByVerifiedTrue();

    // company search
    List<Company> findByNameContainingIgnoreCase(String name);

    // IMPORTANT → used for job posting
    Optional<Company> findByUserId(Long userId);
}
