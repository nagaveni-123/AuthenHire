package com.authenhire.authenhire.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_verifications")
public class CompanyVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String gstNumber;

    @Column(nullable = false, length = 1000)
    private String address;

    @Column(nullable = false)
    private String pincode;

    @Column(nullable = false)
    private String aiStatus = "PENDING";

    @Column(nullable = false)
    private String adminStatus = "PENDING";

    private String gstFile;
    private String panFile;
    private String incorporationFile;
    private String addressProofFile;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getAiStatus() {
        return aiStatus;
    }

    public void setAiStatus(String aiStatus) {
        this.aiStatus = aiStatus;
    }

    public String getAdminStatus() {
        return adminStatus;
    }

    public void setAdminStatus(String adminStatus) {
        this.adminStatus = adminStatus;
    }

    public String getGstFile() {
        return gstFile;
    }

    public void setGstFile(String gstFile) {
        this.gstFile = gstFile;
    }

    public String getPanFile() {
        return panFile;
    }

    public void setPanFile(String panFile) {
        this.panFile = panFile;
    }

    public String getIncorporationFile() {
        return incorporationFile;
    }

    public void setIncorporationFile(String incorporationFile) {
        this.incorporationFile = incorporationFile;
    }

    public String getAddressProofFile() {
        return addressProofFile;
    }

    public void setAddressProofFile(String addressProofFile) {
        this.addressProofFile = addressProofFile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
