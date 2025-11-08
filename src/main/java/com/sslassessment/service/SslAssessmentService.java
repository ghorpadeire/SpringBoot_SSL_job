package com.sslassessment.service;

import com.sslassessment.model.SslAssessment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class SslAssessmentService {

    private static final Pattern HOSTNAME_PATTERN = Pattern.compile(
        "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)(\\.[A-Za-z0-9-]{1,63})*$"
    );

    @Value("${app.ssl.timeout:10000}")
    private int timeout;

    public SslAssessment assessHost(String hostname) {
        SslAssessment assessment = new SslAssessment(hostname);

        if (!isValidHostname(hostname)) {
            assessment.setInProgress(false);
            assessment.setErrorMessage("Invalid hostname format");
            return assessment;
        }

        try {
            performAssessment(hostname, assessment);
        } catch (Exception e) {
            assessment.setErrorMessage("Assessment failed: " + e.getMessage());
        } finally {
            assessment.setInProgress(false);
            assessment.setAssessmentTime(LocalDateTime.now());
        }

        return assessment;
    }

    private void performAssessment(String hostname, SslAssessment assessment) throws Exception {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try (SSLSocket socket = (SSLSocket) factory.createSocket()) {
            socket.setSoTimeout(timeout);
            socket.connect(new java.net.InetSocketAddress(hostname, 443), timeout);
            socket.startHandshake();

            SSLSession session = socket.getSession();

            // Get protocol
            assessment.setProtocol(session.getProtocol());

            // Get cipher suite
            assessment.setCipherSuite(session.getCipherSuite());

            // Verify certificate
            verifyCertificate(hostname, session, assessment);

        } catch (IOException e) {
            throw new Exception("Connection failed: " + e.getMessage(), e);
        }
    }

    private void verifyCertificate(String hostname, SSLSession session, SslAssessment assessment) {
        try {
            Certificate[] certificates = session.getPeerCertificates();

            if (certificates == null || certificates.length == 0) {
                assessment.setCertificateValid(false);
                assessment.setCertificateError("No certificates found");
                return;
            }

            if (certificates[0] instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate) certificates[0];

                // Check validity period
                cert.checkValidity();

                // Check hostname
                if (checkHostnameInCertificate(hostname, cert)) {
                    assessment.setCertificateValid(true);
                } else {
                    assessment.setCertificateValid(false);
                    assessment.setCertificateError("Hostname does not match certificate");
                }
            } else {
                assessment.setCertificateValid(false);
                assessment.setCertificateError("Certificate is not X509");
            }

        } catch (SSLPeerUnverifiedException e) {
            assessment.setCertificateValid(false);
            assessment.setCertificateError("Peer not verified: " + e.getMessage());
        } catch (CertificateException e) {
            assessment.setCertificateValid(false);
            assessment.setCertificateError("Certificate invalid: " + e.getMessage());
        }
    }

    private boolean checkHostnameInCertificate(String hostname, X509Certificate cert) {
        try {
            // Get Subject Alternative Names
            var altNames = cert.getSubjectAlternativeNames();
            if (altNames != null) {
                for (var altName : altNames) {
                    if (altName.get(0).equals(2)) { // DNS name
                        String dnsName = (String) altName.get(1);
                        if (matchesHostname(hostname, dnsName)) {
                            return true;
                        }
                    }
                }
            }

            // Check CN in subject
            String dn = cert.getSubjectX500Principal().getName();
            String cn = extractCN(dn);
            if (cn != null && matchesHostname(hostname, cn)) {
                return true;
            }

        } catch (Exception e) {
            // If we can't parse names, consider it invalid
            return false;
        }

        return false;
    }

    private boolean matchesHostname(String hostname, String certName) {
        if (certName.startsWith("*.")) {
            // Wildcard certificate
            String domain = certName.substring(2);
            return hostname.endsWith("." + domain) || hostname.equals(domain);
        }
        return hostname.equalsIgnoreCase(certName);
    }

    private String extractCN(String dn) {
        String[] parts = dn.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return null;
    }

    public boolean isValidHostname(String hostname) {
        if (hostname == null || hostname.trim().isEmpty()) {
            return false;
        }

        hostname = hostname.trim().toLowerCase();

        // Check if it's an IP address (not allowed)
        if (hostname.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
            return false;
        }

        // Check if it contains port (not allowed)
        if (hostname.contains(":")) {
            return false;
        }

        // Check hostname format
        return HOSTNAME_PATTERN.matcher(hostname).matches();
    }
}
