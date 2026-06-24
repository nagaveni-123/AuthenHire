package com.authenhire.authenhire.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.authenhire.authenhire.entity.Company;
import com.authenhire.authenhire.entity.CompanyVerification;
import com.authenhire.authenhire.repo.CompanyRepository;
import com.authenhire.authenhire.repo.CompanyVerificationRepository;

@Service
public class CompanyVerificationService {

    private final CompanyVerificationRepository verificationRepo;
    private final CompanyRepository companyRepo;
    private final OcrVerificationService ocrVerificationService;

    public CompanyVerificationService(CompanyVerificationRepository verificationRepo,
            CompanyRepository companyRepo,
            OcrVerificationService ocrVerificationService) {
        this.verificationRepo = verificationRepo;
        this.companyRepo = companyRepo;
        this.ocrVerificationService = ocrVerificationService;
    }

    @Transactional
    public CompanyVerification submitVerification(CompanyVerification v) {
        if (v.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (v.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID is required");
        }
        if (v.getCompanyName() == null || v.getCompanyName().isBlank()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (v.getGstNumber() == null || v.getGstNumber().isBlank()) {
            throw new IllegalArgumentException("GST number is required");
        }
        if (v.getAddress() == null || v.getAddress().isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
        if (v.getPincode() == null || v.getPincode().isBlank()) {
            throw new IllegalArgumentException("Pincode is required");
        }

        System.out.println("======================================");
        System.out.println("SUBMIT VERIFICATION STARTED");
        System.out.println("Company Name : " + v.getCompanyName());
        System.out.println("GST Number   : " + v.getGstNumber());
        System.out.println("Pincode      : " + v.getPincode());
        System.out.println("GST File     : " + v.getGstFile());
        System.out.println("PAN File     : " + v.getPanFile());
        System.out.println("INC File     : " + v.getIncorporationFile());
        System.out.println("ADDR File    : " + v.getAddressProofFile());

        v.setAdminStatus("PENDING");

        // OCR-based AI verification
        String aiResult = ocrVerificationService.evaluateVerification(v);
        System.out.println("FINAL AI RESULT FROM SERVICE = " + aiResult);
        v.setAiStatus(aiResult);

        CompanyVerification savedVerification = verificationRepo.save(v);

        Company company = companyRepo.findById(v.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        company.setAiStatus(aiResult);
        company.setAdminStatus("PENDING");
        company.setVerified(false);
        companyRepo.save(company);

        System.out.println("VERIFICATION SAVED WITH AI STATUS = " + savedVerification.getAiStatus());
        System.out.println("COMPANY UPDATED WITH AI STATUS = " + company.getAiStatus());
        System.out.println("======================================");

        return savedVerification;
    }

    public List<CompanyVerification> getByStatus(String status) {
        return verificationRepo.findByAdminStatus(status);
    }

    @Transactional
    public void approveVerification(Long id) {
        CompanyVerification v = verificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification not found"));

        v.setAdminStatus("APPROVED");

        if (v.getAiStatus() == null || v.getAiStatus().isBlank()) {
            v.setAiStatus("PENDING");
        }

        verificationRepo.save(v);

        Company company = companyRepo.findById(v.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        company.setAdminStatus("APPROVED");
        company.setAiStatus(v.getAiStatus());
        company.setVerified(true);
        companyRepo.save(company);
    }

    @Transactional
    public void rejectVerification(Long id) {
        CompanyVerification v = verificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Verification not found"));

        v.setAdminStatus("REJECTED");

        if (v.getAiStatus() == null || v.getAiStatus().isBlank()) {
            v.setAiStatus("REJECTED");
        }

        verificationRepo.save(v);

        Company company = companyRepo.findById(v.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        company.setAdminStatus("REJECTED");
        company.setAiStatus(v.getAiStatus());
        company.setVerified(false);
        companyRepo.save(company);
    }
}
