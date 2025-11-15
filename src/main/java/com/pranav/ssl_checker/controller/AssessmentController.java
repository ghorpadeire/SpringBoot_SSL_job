package com.pranav.ssl_checker.controller;

import com.pranav.ssl_checker.repo.RecentStore;
import com.pranav.ssl_checker.service.TlsProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssessmentController {

    private static final Logger log = LoggerFactory.getLogger(AssessmentController.class);

    //simple store in memory
    private final RecentStore store = new RecentStore();

    // GET /assess?hostname=example.com
    @GetMapping("/assess")
    public String assess(@RequestParam("hostname") String hostname) {
        log.info("Received hostname: " + hostname);

    // return "Ok - recieved hostname  " + hostname;  //For the initial test

        // run the TLS probe
        TlsProbe.Result r = TlsProbe.run(hostname);
        store.put(r); // save it
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

    //Temporary helper to see the last 10 in plain text
    //later we will switch to Thymeleaf page

    @GetMapping("/recent")
    public String recent() {
        TlsProbe.Result[] list  = store.listNewestFirst();
        StringBuilder sb = new StringBuilder();
        sb.append("Recent (newest first):\n");
        for (TlsProbe.Result r : list) {
            sb.append(r.getHostname()).append(" | ");
            sb.append(r.isCertValid() ? "valid" : "invalid").append(" | ");
            sb.append(r.getProtocol()).append(" | ");
            sb.append(r.getCipherSuite()).append(" | ");
            sb.append(r.getCheckedAt()).append("\n");
        }
        if (list.length == 0) sb.append("(empty)\n");
        return sb.toString();
    }

    //clear all
    @PostMapping ("/assessments/clear")
    public String clearAll() {
        store.clear();
        return "cleared";
    }

    //Delete one
    @PostMapping("/asswssments/clear")
    public String deleteOne(@RequestParam("hostname") String hostname) {
        store.delete(hostname);
        return "deleted: " + hostname;
    }
}
