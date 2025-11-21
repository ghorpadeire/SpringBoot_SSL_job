package com.pranav.ssl_checker.controller;

import com.pranav.ssl_checker.repo.RecentStore;
import com.pranav.ssl_checker.service.TlsProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*; // GetMapping, PostMapping, RequestParam, ResponseBody

@Controller
public class AssessmentController {

    private static final Logger log = LoggerFactory.getLogger(AssessmentController.class);

    // simple store in memory
    private final RecentStore store = new RecentStore();

    // GET /assess?hostname=example.com
    // returns a small HTML page with the result + "Back to Home" button
    @GetMapping("/assess")
    @ResponseBody
    public String assess(@RequestParam("hostname") String hostname) {
        log.info("Received hostname: " + hostname);

        // run the TLS probe
        TlsProbe.Result r = TlsProbe.run(hostname);
        store.put(r); // save it

        // build the plain text summary first
        StringBuilder sb = new StringBuilder();
        sb.append("hostname: ").append(r.getHostname()).append("\n");
        sb.append("cert: ").append(r.isCertValid() ? "valid" : "invalid").append("\n");
        sb.append("Protocol: ").append(r.getProtocol()).append("\n");
        sb.append("Cipher: ").append(r.getCipherSuite()).append("\n");
        sb.append("Error: ").append(r.getError()).append("\n");
        sb.append("Checked: ").append(r.getCheckedAt()).append("\n");

        String text = sb.toString();

        // escape for <pre>
        String safeText = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"en\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<title>SSL/TLS Assessment Result</title>");
        html.append("</head>");
        html.append("<body>");

        html.append("<h1>Assessment Result</h1>");
        html.append("<p>Below is the raw result for your hostname.</p>");

        html.append("<pre style=\"white-space: pre-wrap;\">");
        html.append(safeText);
        html.append("</pre>");

        html.append("<form action=\"/index.html\" method=\"get\" style=\"margin-top:16px;\">");
        html.append("<button type=\"submit\">Back to Home</button>");
        html.append("</form>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    // GET /recent  -> plain text for JS
    @GetMapping("/recent")
    @ResponseBody
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

    // POST /assessments/clear
    // after clearing, redirect back to index.html instead of showing "cleared"
    @PostMapping("/assessments/clear")
    public String clearAll() {
        store.clear();
        // redirect sends the browser back to the main page
        return "redirect:/index.html";
    }

    // POST /assessments/delete
    // delete one host then go back to index.html
    @PostMapping("/assessments/delete")
    public String deleteOne(@RequestParam("hostname") String hostname) {
        store.delete(hostname);
        return "redirect:/index.html";
    }
}
