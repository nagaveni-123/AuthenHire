package com.authenhire.authenhire.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.authenhire.authenhire.entity.Company;
import com.authenhire.authenhire.entity.Job;
import com.authenhire.authenhire.repo.CompanyRepository;
import com.authenhire.authenhire.repo.JobRepository;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobService(JobRepository jobRepository,
            CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    // post job
    public String postJob(Long userId,
            String title,
            String location,
            String jobType,
            String experience,
            String salaryRange,
            String description) {

        Optional<Company> companyOpt = companyRepository.findByUserId(userId);

        if (companyOpt.isEmpty()) {
            return "Company not found.";
        }

        Company company = companyOpt.get();

        if (!company.isVerified()) {
            return "Only verified companies can post jobs.";
        }

        Job job = new Job();
        job.setTitle(title);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setExperience(experience);
        job.setSalaryRange(salaryRange);
        job.setDescription(description);
        job.setCompany(company);
        job.setPostedAt(LocalDateTime.now());

        jobRepository.save(job);

        return null;
    }

    // GET JOB BY ID (add this)
    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }
}
