package com.authenhire.authenhire.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.authenhire.authenhire.entity.CompanyVerification;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OcrVerificationService {

    @Value("${app.ocr.tessdata-path}")
    private String tessdataPath;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final Pattern GST_PATTERN
            = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][A-Z0-9][Zz][A-Z0-9]$");

    private static final Pattern PINCODE_PATTERN
            = Pattern.compile("^[0-9]{6}$");

    public String evaluateVerification(CompanyVerification verification) {

        String extractedText = extractAllPossibleText(verification).toLowerCase(Locale.ROOT);

        System.out.println("======================================");
        System.out.println("OCR FULL TEXT:");
        System.out.println(extractedText);
        System.out.println("======================================");

        if (extractedText.isBlank()) {
            System.out.println("No OCR text found -> PENDING");
            return "PENDING";
        }

        String gst = safeUpper(verification.getGstNumber());
        String normalizedExtracted = extractedText.replaceAll("[^a-zA-Z0-9]", "");
        String normalizedGst = gst.toLowerCase(Locale.ROOT).replaceAll("[^a-zA-Z0-9]", "");

        boolean gstFormatValid = verification.getGstNumber() != null
                && GST_PATTERN.matcher(verification.getGstNumber().trim().toUpperCase()).matches();

        boolean gstMatched = gstFormatValid
                && !normalizedGst.isBlank()
                && normalizedExtracted.contains(normalizedGst);

        boolean pinMatched = false;
        String pin = safe(verification.getPincode());
        if (!pin.isBlank() && PINCODE_PATTERN.matcher(pin).matches()) {
            pinMatched = extractedText.contains(pin);
        }

        boolean companyMatched = containsImportantWords(extractedText, verification.getCompanyName());
        boolean addressMatched = containsImportantWords(extractedText, verification.getAddress());

        System.out.println("GST MATCHED      = " + gstMatched);
        System.out.println("PINCODE MATCHED  = " + pinMatched);
        System.out.println("COMPANY MATCHED  = " + companyMatched);
        System.out.println("ADDRESS MATCHED  = " + addressMatched);

        // FINAL RULES
        // 1. GST is the main proof
        // 2. If GST + (name or pincode or address) matches => VERIFIED
        if (gstMatched && (companyMatched || pinMatched || addressMatched)) {
            System.out.println("FINAL AI RESULT = VERIFIED");
            return "VERIFIED";
        }

        // If OCR text exists but GST failed, reject
        if (!gstMatched) {
            System.out.println("FINAL AI RESULT = REJECTED");
            return "REJECTED";
        }

        // Fallback
        System.out.println("FINAL AI RESULT = PENDING");
        return "PENDING";
    }

    private String extractAllPossibleText(CompanyVerification verification) {
        List<String> allText = new ArrayList<>();

        addFileText(allText, verification.getGstFile());
        addFileText(allText, verification.getPanFile());
        addFileText(allText, verification.getIncorporationFile());
        addFileText(allText, verification.getAddressProofFile());

        return String.join(" ", allText).trim();
    }

    private void addFileText(List<String> allText, String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        File file = resolveFile(fileName);

        System.out.println("Trying OCR file path: " + file.getAbsolutePath());

        if (!file.exists() || !file.isFile()) {
            System.out.println("OCR file not found: " + file.getAbsolutePath());
            return;
        }

        String extracted = runOcr(file);
        if (!extracted.isBlank()) {
            allText.add(extracted);
        }
    }

    private File resolveFile(String fileName) {
        String normalized = fileName.replace("\\", "/").trim();

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (normalized.startsWith("uploads/")) {
            return new File(normalized);
        }

        return new File(uploadDir, normalized);
    }

    private String runOcr(File file) {
        try {
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage("eng");

            String text = tesseract.doOCR(file);

            System.out.println("OCR extracted from " + file.getName() + ":");
            System.out.println(text);

            return text == null ? "" : text;
        } catch (TesseractException e) {
            System.out.println("OCR error for file: " + file.getAbsolutePath());
            e.printStackTrace();
            return "";
        }
    }

    private boolean containsImportantWords(String extractedText, String originalText) {
        if (originalText == null || originalText.isBlank()) {
            return false;
        }

        String[] words = originalText
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .toLowerCase(Locale.ROOT)
                .trim()
                .split("\\s+");

        for (String word : words) {
            if (word.length() < 4) {
                continue;
            }
            if (extractedText.contains(word)) {
                return true; // one good word is enough
            }
        }

        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
