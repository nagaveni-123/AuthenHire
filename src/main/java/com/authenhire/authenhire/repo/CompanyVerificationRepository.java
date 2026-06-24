package com.authenhire.authenhire.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authenhire.authenhire.entity.CompanyVerification;

public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, Long> {

    long countByAdminStatus(String adminStatus);

    List<CompanyVerification> findByAdminStatus(String adminStatus);

    List<CompanyVerification> findByCompanyId(Long companyId);

    Optional<CompanyVerification> findTopByCompanyIdOrderByCreatedAtDesc(Long companyId);
}
