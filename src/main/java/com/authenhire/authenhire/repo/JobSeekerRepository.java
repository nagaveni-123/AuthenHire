package com.authenhire.authenhire.repo;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authenhire.authenhire.entity.JobSeeker;
import com.authenhire.authenhire.entity.User;

public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long> {
        Optional<JobSeeker> findByUser(User user);

}
