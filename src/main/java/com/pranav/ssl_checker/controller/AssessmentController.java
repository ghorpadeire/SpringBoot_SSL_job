package com.pranav.ssl_checker.controller;

import com.pranav.ssl_checker.service.TlsProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssessmentController {

    private static final Logger log = LoggerFactory.getLogger(AssessmentController.class);

    // GET /assess?hostname=example.com
    @GetMapping("/assess")
    public String assess(@RequestParam("hostname") String hostname) {
        log.info("Received hostname: " + hostname);

//        return "Ok - recieved hostname  " + hostname;  //For the initial test

        // run the TLS probe
        TlsProbe.Result r = TlsProbe.run(hostname);

        // build a simple text response
        StringBuilder sb = new StringBuilder();

        sb.append("hostname: " ).append(r.getHostname()).append("\n");
        sb.append("cert: ").append(r.isCertValid() ? "valid" : "invalid").append("\n");
        sb.append("Protocol: ").append(r.getProtocol()).append("\n");
        sb.append("Cipher: ").append(r.getCipherSuite()).append("\n");
        sb.append("Error: ").append(r.getError()).append("\n");
        sb.append("Checked: ").append(r.getCheckedAt()).append("\n");

        return sb.toString();
    }
}
