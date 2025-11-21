
# SSl_checker

Project Description

A lightweight Spring Boot application that performs real-time SSL/TLS assessments for any HTTPS hostname. The tool connects to the target server over port 443, validates the certificate, and extracts the negotiated TLS protocol and cipher suite. Results are stored in an in-memory circular buffer (last 10 assessments), allowing quick review, deletion, and refresh.

The frontend is a simple static HTML + JavaScript page that interacts with the backend through minimal REST endpoints. No database, no complex UI — just a clean, fast, interview-ready implementation focused entirely on correctness and clarity.

Designed to demonstrate:
• SSL/TLS handshake using Java’s JSSE API
• Thread-safe in-memory data storage
• Clean REST controller design in Spring Boot
• Practical debugging endpoints
• A simple and functional UI without frameworks

Built specifically for technical interviews and portfolio showcasing.

SSL/TLS Assessment Tool
A minimal Spring Boot web application that performs real-time SSL/TLS checks on HTTPS hostnames. Designed to be extremely simple, interview-ready, and focused only on core functionality.

Features
• Check SSL/TLS configuration of any hostname (always uses port 443)
• Validates certificate (valid / invalid / error message)
• Shows negotiated TLS protocol (e.g., TLSv1.2, TLSv1.3)
• Shows negotiated cipher suite
• Stores last 10 assessment results in memory
• Delete one assessment or clear all assessments
• Clean, static HTML + JavaScript UI
• No database, no complex frameworks

Tech Stack
• Java 17
• Spring Boot
• JSSE (Java SSL/TLS API)
• HTML + JavaScript (static page)

How It Works

User enters a hostname on the home page

Backend connects to hostname:443 using TLS

Extracts certificate validity, protocol, cipher suite, and timestamp

Saves result into an in-memory store (maximum 10 entries, newest first)

Frontend table fetches results from /recent and displays them

REST Endpoints
GET /assess?hostname=x
Runs a live TLS assessment and returns an HTML result page

GET /recent
Returns the last 10 assessments in plain text (used by the UI)

POST /assessments/clear
Clears all stored assessment results

POST /assessments/delete
Deletes one hostname’s result

GET /results?hostname=x
Shows an already saved result or performs a new assessment if missing

Why This Project Exists
• Demonstrates a full SSL/TLS handshake implementation using Java
• Shows clean REST API design in Spring Boot
• Implements a thread-safe in-memory repository
• Very small, intentional scope — ideal for interviews and GitHub portfolios
• No fancy CSS/JS, only core logic and clarity

How to Run
mvn spring-boot:run
Open in browser: http://localhost:8080/
