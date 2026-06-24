package com.authenhire.authenhire.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.authenhire.authenhire.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
        SELECT j FROM Job j
        WHERE (:q IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (:loc IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :loc, '%')))
          AND (:type IS NULL OR j.jobType = :type)
        ORDER BY j.postedAt DESC
    """)
    List<Job> search(String q, String loc, String type);

    List<Job> findTop6ByOrderByPostedAtDesc();

    List<Job> findByCompany_IdOrderByPostedAtDesc(Long companyId);
}
