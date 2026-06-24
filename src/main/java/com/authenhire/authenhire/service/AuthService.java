package com.authenhire.authenhire.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.authenhire.authenhire.entity.Company;
import com.authenhire.authenhire.entity.CompanyVerification;
import com.authenhire.authenhire.entity.JobSeeker;
import com.authenhire.authenhire.entity.User;
import com.authenhire.authenhire.repo.CompanyRepository;
import com.authenhire.authenhire.repo.JobSeekerRepository;
import com.authenhire.authenhire.repo.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final CompanyRepository companyRepository;
    private final FileStorageService fileStorage;
    private final CompanyVerificationService companyVerificationService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository,
            JobSeekerRepository jobSeekerRepository,
            CompanyRepository companyRepository,
            FileStorageService fileStorage,
            CompanyVerificationService companyVerificationService) {
        this.userRepository = userRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.companyRepository = companyRepository;
        this.fileStorage = fileStorage;
        this.companyVerificationService = companyVerificationService;
    }

    // Old signup method kept for compatibility if used anywhere else
    public String signup(String fullName, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            return "Email already registered!";
        }

        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(password));
        u.setRole("JOB_SEEKER");
        userRepository.save(u);

        return null;
    }

    // Job seeker signup
    public String signupJobSeeker(String fullName,
            String email,
            String password,
            String phone,
            String qualification,
            String planType,
            String candidateType,
            Integer experienceYears,
            String preferredLocation,
            String skills,
            String paymentMethod,
            MultipartFile resumeFile) {

        try {
            if (userRepository.existsByEmail(email)) {
                return "Email already registered!";
            }

            User u = new User();
            u.setFullName(fullName);
            u.setEmail(email);
            u.setPasswordHash(encoder.encode(password));
            u.setRole("JOB_SEEKER");
            u = userRepository.save(u);

            JobSeeker js = new JobSeeker();
            js.setUser(u);
            js.setPhone(phone);
            js.setQualification(qualification);
            js.setCreatedAt(LocalDateTime.now());

            if (resumeFile != null && !resumeFile.isEmpty()) {
                String resumePath = fileStorage.save(resumeFile, "jobseekers");
                js.setResumeFile(resumePath);
            }

            if ("PREMIUM".equalsIgnoreCase(planType)) {
                js.setPlanType("PREMIUM");
                js.setCandidateType(candidateType);
                js.setExperienceYears(experienceYears);
                js.setPreferredLocation(preferredLocation);
                js.setSkills(skills);
                js.setPaymentMethod(paymentMethod);
                js.setPaymentStatus("PAID");

                if ("FRESHER".equalsIgnoreCase(candidateType)) {
                    js.setPremiumFee(99.0);
                } else {
                    js.setPremiumFee(199.0);
                }
            } else {
                js.setPlanType("NORMAL");
                js.setPaymentStatus("FREE");
                js.setPremiumFee(0.0);
                js.setCandidateType(null);
                js.setExperienceYears(null);
                js.setPreferredLocation(null);
                js.setSkills(null);
                js.setPaymentMethod(null);
            }

            jobSeekerRepository.save(js);

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error while saving job seeker: " + e.getMessage();
        }
    }

    // Company signup
    public String signupCompany(String fullName,
            String email,
            String password,
            String companyName,
            String website,
            String location,
            String about,
            String gstNumber,
            String address,
            String pincode,
            String paymentMethod,
            MultipartFile logoFile,
            MultipartFile gstFile,
            MultipartFile panFile,
            MultipartFile incorporationFile,
            MultipartFile addressProofFile) {

        try {
            if (userRepository.existsByEmail(email)) {
                return "Email already registered!";
            }

            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPasswordHash(encoder.encode(password));
            user.setRole("COMPANY");
            user = userRepository.save(user);

            String folder = "company_" + user.getId();

            Company company = new Company();
            company.setUserId(user.getId());
            company.setName(companyName);
            company.setWebsite(website);
            company.setLocation(location);
            company.setAbout(about);

            if (logoFile != null && !logoFile.isEmpty()) {
                company.setLogoUrl(fileStorage.save(logoFile, folder));
            }

            company.setVerified(false);
            company.setAiStatus("PENDING");
            company.setAdminStatus("PENDING");

            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                company.setRegistrationPaymentMethod(paymentMethod);
                company.setRegistrationPaymentStatus("SUCCESS");
                company.setRegistrationFee(499.0);
            }

            company = companyRepository.save(company);

            user.setCompanyId(company.getId());
            userRepository.save(user);

            CompanyVerification verification = new CompanyVerification();
            verification.setUserId(user.getId());
            verification.setCompanyId(company.getId());
            verification.setCompanyName(company.getName());
            verification.setGstNumber(gstNumber);
            verification.setAddress(address);
            verification.setPincode(pincode);
            verification.setCreatedAt(LocalDateTime.now());

            if (gstFile != null && !gstFile.isEmpty()) {
                verification.setGstFile(fileStorage.save(gstFile, folder));
            }
            if (panFile != null && !panFile.isEmpty()) {
                verification.setPanFile(fileStorage.save(panFile, folder));
            }
            if (incorporationFile != null && !incorporationFile.isEmpty()) {
                verification.setIncorporationFile(fileStorage.save(incorporationFile, folder));
            }
            if (addressProofFile != null && !addressProofFile.isEmpty()) {
                verification.setAddressProofFile(fileStorage.save(addressProofFile, folder));
            }

            System.out.println("AUTH SERVICE -> CALLING COMPANY VERIFICATION SERVICE");
            companyVerificationService.submitVerification(verification);

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error while saving company: " + e.getMessage();
        }
    }

    public Optional<User> loginAndGetUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> encoder.matches(password, u.getPasswordHash()));
    }

    public boolean login(String email, String password) {
        return loginAndGetUser(email, password).isPresent();
    }
}
