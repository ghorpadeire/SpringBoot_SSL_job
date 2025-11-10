package com.pranav.ssl_checker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssessmentController {
    private static final Logger log = LoggerFactory.getLogger(AssessmentController.class);
    @GetMapping("/assess")
    public String assess(@RequestParam("hostname") String hostname) {
        log.info("Received hostname: " + hostname);
        return "Ok - recieved hostname  " + hostname;
    }
}
