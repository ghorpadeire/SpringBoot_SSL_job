package com.sslassessment.model;

import java.time.LocalDateTime;

public class SslAssessment {
    private String hostname;
    private boolean certificateValid;
    private String certificateError;
    private String protocol;
    private String cipherSuite;
    private LocalDateTime assessmentTime;
    private boolean inProgress;
    private String errorMessage;

    public SslAssessment() {
    }

    public SslAssessment(String hostname) {
        this.hostname = hostname;
        this.inProgress = true;
        this.assessmentTime = LocalDateTime.now();
    }

    // Getters and Setters
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isCertificateValid() {
        return certificateValid;
    }

    public void setCertificateValid(boolean certificateValid) {
        this.certificateValid = certificateValid;
    }

    public String getCertificateError() {
        return certificateError;
    }

    public void setCertificateError(String certificateError) {
        this.certificateError = certificateError;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    public LocalDateTime getAssessmentTime() {
        return assessmentTime;
    }

    public void setAssessmentTime(LocalDateTime assessmentTime) {
        this.assessmentTime = assessmentTime;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCertificateStatus() {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            return "Error: " + errorMessage;
        }
        if (certificateValid) {
            return "Valid";
        }
        return "Invalid: " + (certificateError != null ? certificateError : "Unknown error");
    }
}
