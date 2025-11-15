package com.pranav.ssl_checker.controller;

import com.pranav.ssl_checker.repo.RecentStore;
import com.pranav.ssl_checker.service.TlsProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*; // @GetMapping, @PostMapping, @RequestParam, @ResponseBody


// * AssessmentController
// * -----------------------------------------
// * Simple Spring MVC controller that exposes a few HTTP endpoints.
// * /assess runs a live TLS check for a given hostname and returns plain text
// * /recent shows last 10 checks from in-memory store in plain text
// *  /assessments/clear clears the in-memory store
// *  /assessments/delete removes one hostname from the store
// *
// * Notes for me
// * 1) @Controller tells Spring this class handles web requests.
// * 2) We are not using templates right now, so methods that return text are marked with @ResponseBody.
// * 3) We keep results only in memory, so after app restart data will be empty again. That is expected.

@Controller
public class AssessmentController {

    private static final Logger log = LoggerFactory.getLogger(AssessmentController.class);

    // simple store in memory
    // I am creating one RecentStore object and reusing it for all requests
    // This is ok for a tiny app because access is synchronized inside RecentStore
    private final RecentStore store = new RecentStore();


//     * GET /assess?hostname=example.com
//     * ---------------------------------------------------
//     * Purpose:
//     * - Take a hostname from the query param
//     * - Run TLS probe using TlsProbe.run(...)
//     * - Save the result into our in-memory store
//     * - Return a human readable plain text summary
//     *
//     * Why @ResponseBody:
//     * - We are not returning a view name, we are returning the text directly as HTTP response

    @GetMapping(value = "/assess", produces="text/plain; charset=UTF-8")
    @ResponseBody
    public String assess(@RequestParam("hostname") String hostname) {
        // logging is always helpful for debugging on console
        log.info("Received hostname: " + hostname);

        // quick manual test line used earlier
        // return "Ok - recieved hostname  " + hostname;  // For the initial test

        // run the TLS probe
        // If hostname is invalid or handshake fails, TlsProbe will handle and set error text
        TlsProbe.Result r = TlsProbe.run(hostname);

        // save the result so we can show it later on /recent
        store.put(r);

        // build a simple multi-line text so it is easy to read in browser
        StringBuilder sb = new StringBuilder();
        sb.append("hostname: ").append(r.getHostname()).append("\n");
        sb.append("cert: ").append(r.isCertValid() ? "valid" : "invalid").append("\n");
        sb.append("Protocol: ").append(r.getProtocol()).append("\n");
        sb.append("Cipher: ").append(r.getCipherSuite()).append("\n");
        sb.append("Error: ").append(r.getError()).append("\n");
        sb.append("Checked: ").append(r.getCheckedAt()).append("\n");
        return sb.toString();
    }


//     * GET /recent
//     * ---------------------------------------------------
//     * Purpose:
//     * - Show last 10 assessment results as plain text
//     * - Newest should come first
//     *
//     * Why plain text:
//     * - We decided to keep UI static for now
//     * - Later we can replace this with an HTML table or a template if needed

    @GetMapping("/recent")
    @ResponseBody
    public String recent() {
        // get newest first snapshot from store
        TlsProbe.Result[] list  = store.listNewestFirst();

        // build a simple one line per item
        StringBuilder sb = new StringBuilder();
        sb.append("Recent (newest first):\n");
        for (TlsProbe.Result r : list) {
            sb.append(r.getHostname()).append(" | ");
            sb.append(r.isCertValid() ? "valid" : "invalid").append(" | ");
            sb.append(r.getProtocol()).append(" | ");
            sb.append(r.getCipherSuite()).append(" | ");
            sb.append(r.getCheckedAt()).append("\n");
        }
        if (list.length == 0) sb.append("(empty)\n"); // when app just started or after clear
        return sb.toString();
    }


//     * POST /assessments/clear
//     * ---------------------------------------------------
//     * Purpose:
//     * - Remove everything from in-memory store
//     * How to call:
//     * - From HTML form with method="post"
//     * - Or via curl: curl -X POST http://localhost:8080/assessments/clear

    @PostMapping("/assessments/clear")
    @ResponseBody
    public String clearAll() {
        store.clear();
        return "cleared";
    }


//     * POST /assessments/delete
//     * ---------------------------------------------------
//     * Purpose:
//     * - Remove only the given hostname from the store
//     * How to call:
//     * - HTML form with method="post" and an input named hostname
//     * - Or via curl:
//     *   curl -X POST -d "hostname=example.com" http://localhost:8080/assessments/delete
//     *
//     * Why plain text return:
//     * - We are keeping things simple and returning a small confirmation message

    @PostMapping("/assessments/delete")
    @ResponseBody
    public String deleteOne(@RequestParam("hostname") String hostname) {
        store.delete(hostname);
        return "deleted: " + hostname;
    }
    // Open one saved result by hostname (plain text)
    // GET /results?hostname=example.com
    @GetMapping("/results")
    @ResponseBody
    public String results(@RequestParam("hostname") String hostname) {
        // try to fetch from in-memory store first
        TlsProbe.Result r = store.get(hostname);

        // if app restarted or not present, run fresh probe
        if (r == null) {
            r = TlsProbe.run(hostname);
            store.put(r); // keep it in recent
        }
        log.info("Received hostname: {}", hostname);

        // print in easy-to-read format (same style as /assess)
        StringBuilder sb = new StringBuilder();
        sb.append("hostname: ").append(r.getHostname()).append("\n");
        sb.append("cert: ").append(r.isCertValid() ? "valid" : "invalid").append("\n");
        sb.append("Protocol: ").append(r.getProtocol()).append("\n");
        sb.append("Cipher: ").append(r.getCipherSuite()).append("\n");
        sb.append("Error: ").append(r.getError()).append("\n");
        sb.append("Checked: ").append(r.getCheckedAt()).append("\n");
        return sb.toString();
    }

}
