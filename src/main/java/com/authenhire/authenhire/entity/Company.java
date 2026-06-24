package com.authenhire.authenhire.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    private String location;
    private String website;
    private String logoUrl;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(length = 2000)
    private String about;

    @Column(nullable = false)
    private String aiStatus = "PENDING";

    @Column(nullable = false)
    private String adminStatus = "PENDING";

    // ✅ NEW PAYMENT FIELDS
    @Column(name = "registration_payment_status")
    private String registrationPaymentStatus;

    @Column(name = "registration_payment_method")
    private String registrationPaymentMethod;

    @Column(name = "registration_fee")
    private Double registrationFee;

    // -------- GETTERS & SETTERS --------

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
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

    // ✅ NEW GETTERS & SETTERS

    public String getRegistrationPaymentStatus() {
        return registrationPaymentStatus;
    }

    public void setRegistrationPaymentStatus(String registrationPaymentStatus) {
        this.registrationPaymentStatus = registrationPaymentStatus;
    }

    public String getRegistrationPaymentMethod() {
        return registrationPaymentMethod;
    }

    public void setRegistrationPaymentMethod(String registrationPaymentMethod) {
        this.registrationPaymentMethod = registrationPaymentMethod;
    }

    public Double getRegistrationFee() {
        return registrationFee;
    }

    public void setRegistrationFee(Double registrationFee) {
        this.registrationFee = registrationFee;
    }
}