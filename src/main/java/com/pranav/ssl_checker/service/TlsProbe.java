package com.pranav.ssl_checker.service;

import javax.net.ssl.*;
import java.net.IDN;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Instant;

//Very small helper that connects to a hostname on port 443 using TLS
//  and reports certificate validity, negotiated protocol, and cipher suite.
//  Written with simple code and lots of comments for beginners.

public class TlsProbe {

//    imple data holder for the result.
//    I use a normal class instead of a Java record so it is easy to read.

    public static class Result {
        private final String hostname;
        private final boolean certValid;
        private final String protocol;
        private final String cipherSuite;
        private final String error;
        private final Instant checkedAt;

        public Result(String hostname,
                      boolean certValid,
                      String protocol,
                      String cipherSuite,
                      String error,
                      Instant checkedAt) {
            this.hostname = hostname;
            this.certValid = certValid;
            this.protocol = protocol;
            this.cipherSuite = cipherSuite;
            this.error = error;
            this.checkedAt = checkedAt;
        }

        public String getHostname()   { return hostname; }
        public boolean isCertValid()  { return certValid; }
        public String getProtocol()   { return protocol; }
        public String getCipherSuite(){ return cipherSuite; }
        public String getError()      { return error; }
        public Instant getCheckedAt() { return checkedAt; }
    }


//      Very basic hostname check.
//      Rules:
//       - must not be empty
//       - only letters, numbers, dots, and hyphens
//       - must contain at least one dot
//       - must not include a port like example.com:8443
//       - must not look like an IP address

    public static boolean isValidHostname(String host) {
        if (host == null) return false;
        host = host.trim();
        if (host.isEmpty()) return false;

        // reject ports
        if (host.contains(":")) return false;

        // reject IPv4
        if (host.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) return false;

        // reject obvious IPv6 form
        if (host.contains("::")) return false;

        // only a to z, 0 to 9, dot, hyphen
        boolean charsOk = host.matches("(?i)^[a-z0-9.-]+$");
        if (!charsOk) return false;

        // must have at least one dot so single labels like localhost fail
        return host.contains(".");
    }


//     * Connects to the host on port 443 and performs a TLS handshake.
//     * If the handshake and hostname verification succeed, we mark the cert as valid.
//     * If anything fails, we return an error message.


    public static Result run(String host) {
        // capture one timestamp to keep the result consistent
        Instant now = Instant.now();

        // stop early if the hostname is not acceptable
        if (!isValidHostname(host)) {
            return new Result(host, false, null, null, "invalid hostname", now);
        }

        try {
            // get a TLS context with default JVM settings
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, null); // nulls mean: use default key and trust stores

            // we use this factory to create TLS sockets
            SSLSocketFactory factory = context.getSocketFactory();

            // try-with-resources closes the socket automatically
            try (SSLSocket socket = (SSLSocket) factory.createSocket(host, 443)) {

                // turn on hostname verification for HTTPS
                // without this, a wrong certificate name could still pass
                SSLParameters params = socket.getSSLParameters();
                params.setEndpointIdentificationAlgorithm("HTTPS");
                socket.setSSLParameters(params);

                // start the TLS handshake
                // if the certificate is expired, self signed without trust, or the name does not match,
                // this call will throw an exception
                socket.startHandshake();

                // if we reach here, the handshake worked
                SSLSession session = socket.getSession();

                // read the negotiated protocol, for example TLSv1.3
                String protocol = session.getProtocol();

                // read the negotiated cipher suite, for example TLS_AES_128_GCM_SHA256
                String cipher = session.getCipherSuite();

                // build a successful result
                return new Result(host, true, protocol, cipher, null, now);
            }

        } catch (SSLPeerUnverifiedException e) {
            // handshake happened but the peer could not be verified
            // common reasons: hostname mismatch or untrusted issuer
            return new Result(host, false, null, null, "hostname mismatch or untrusted cert", now);

        } catch (SSLHandshakeException e) {
            // handshake failed for a TLS reason
            // examples: protocol version issues, bad certificate, weak ciphers not allowed
            return new Result(host, false, null, null, "tls handshake failed", now);

        } catch (Exception e) {
            // anything else such as DNS error, connection timeout, or socket problem
            return new Result(host, false, null, null, "error: " + e.getClass().getSimpleName(), now);
        }
    }

    // utility class, no instances needed
    private TlsProbe() {}
}