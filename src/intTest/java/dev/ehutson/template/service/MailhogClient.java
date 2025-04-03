package dev.ehutson.template.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MailhogClient {
    private final String apiEndpoint;
    private final ObjectMapper objectMapper;

    public MailhogClient(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
        this.objectMapper = new ObjectMapper();
    }

    public List<Email> getAllEmails() {
        Response response = RestAssured.given()
                .baseUri(apiEndpoint)
                .get("/api/v2/messages")
                .then()
                .statusCode(200)
                .extract()
                .response();

        try {
            JsonNode root = objectMapper.readTree(response.asString());
            List<Email> emails = new ArrayList<>();

            if (root.has("items") && root.get("items").isArray()) {
                root.get("items").forEach(item -> {
                    try {
                        Email email = parseEmail(item);
                        emails.add(email);
                    } catch (Exception e) {
                        //Log and skip problematic items
                    }
                });
            }
            return emails;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse emails", e);
        }
    }

    public void deleteAllEmails() {
        RestAssured.given()
                .baseUri(apiEndpoint)
                .delete("/api/v1/messages")
                .then()
                .statusCode(200);
    }

    public Optional<Email> findEmailBySubject(String subject) {
        return getAllEmails()
                .stream()
                .filter(email ->
                        email.getSubject().contains(subject)
                ).findFirst();
    }

    public Optional<Email> findEmailToRecipient(String emailAddress) {
        return getAllEmails()
                .stream()
                .filter(email ->
                        email.getTo().contains(emailAddress)
                ).findFirst();
    }

    private Email parseEmail(JsonNode node) {
        Email email = new Email();

        if (node.has("Content")) {
            JsonNode content = node.get("Content");

            if (content.has("Headers")) {
                JsonNode headers = content.get("Headers");

                if (headers.has("Subject") && headers.get("Subject").isArray()) {
                    email.setSubject(headers.get("Subject").get(0).asText());
                }

                if (headers.has("From") && headers.get("From").isArray()) {
                    email.setFrom(headers.get("From").get(0).asText());
                }

                if (headers.has("To") && headers.get("To").isArray()) {
                    List<String> to = new ArrayList<>();
                    headers.get("To").forEach(item -> to.add(item.asText()));
                    email.setTo(to);
                }
            }

            if (content.has("Body")) {
                email.setBody(content.get("Body").asText());
            }
        }
        return email;
    }

    public static class Email {
        private String subject;
        private String from;
        private List<String> to = new ArrayList<>();
        private String body;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public List<String> getTo() {
            return to;
        }

        public void setTo(List<String> to) {
            this.to = to;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
