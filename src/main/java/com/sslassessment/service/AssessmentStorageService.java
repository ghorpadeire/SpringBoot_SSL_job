package com.sslassessment.service;

import com.sslassessment.model.SslAssessment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AssessmentStorageService {

    @Value("${app.max.assessments:10}")
    private int maxAssessments;

    private final Map<String, SslAssessment> assessments = new ConcurrentHashMap<>();
    private final LinkedList<String> assessmentOrder = new LinkedList<>();

    public synchronized void saveAssessment(SslAssessment assessment) {
        String hostname = assessment.getHostname().toLowerCase();

        // If hostname already exists, remove it from order
        if (assessments.containsKey(hostname)) {
            assessmentOrder.remove(hostname);
        }

        // Add to the front (most recent)
        assessments.put(hostname, assessment);
        assessmentOrder.addFirst(hostname);

        // Keep only the most recent maxAssessments
        while (assessmentOrder.size() > maxAssessments) {
            String oldestHostname = assessmentOrder.removeLast();
            assessments.remove(oldestHostname);
        }
    }

    public SslAssessment getAssessment(String hostname) {
        return assessments.get(hostname.toLowerCase());
    }

    public List<SslAssessment> getRecentAssessments() {
        return assessmentOrder.stream()
                .map(assessments::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public synchronized boolean deleteAssessment(String hostname) {
        String lowerHostname = hostname.toLowerCase();
        if (assessments.containsKey(lowerHostname)) {
            assessments.remove(lowerHostname);
            assessmentOrder.remove(lowerHostname);
            return true;
        }
        return false;
    }

    public boolean hasAssessment(String hostname) {
        return assessments.containsKey(hostname.toLowerCase());
    }
}
