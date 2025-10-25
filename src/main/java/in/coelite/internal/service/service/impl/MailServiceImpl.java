// package in.coelite.internal.portfolio.service.impl;

// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// import in.coelite.internal.portfolio.dto.ContactDto;
// import in.coelite.internal.portfolio.service.MailService;

// @Service
// public class MailServiceImpl implements MailService {
//     private final JavaMailSender mailSender;

//     public MailServiceImpl(JavaMailSender mailSender) {
//         this.mailSender = mailSender;
//     }

//     @Override
//     public void sendMail(ContactDto contactInfo) {

//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setFrom("support@coelite.in"); 
//         message.setTo("support@coelite.in");
//         message.setSubject("New Contact Form Submission");
//         message.setText("Name: " + contactInfo.getName() +
//                 "\nEmail: " + contactInfo.getEmail() +
//                 "\nMessage:\n" + contactInfo.getMessage()); 
//         try {
//             mailSender.send(message);
//         } catch (Exception e) {
//             e.printStackTrace();
//         }        

//     }

// }// src/main/java/in/coelite/internal/portfolio/service/impl/MailServiceImpl.java

package in.coelite.internal.portfolio.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import in.coelite.internal.portfolio.dto.ContactDto;
import in.coelite.internal.portfolio.service.MailService;

@Service
public class MailServiceImpl implements MailService {
    
    private final WebClient webClient;
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    
    // Record for the Resend JSON body (requires Java 16+, otherwise use a class/Lombok)
    private record ResendEmailRequest(String from, String to, String subject, String html) {}

    // Constructor Injection: Guarantees @Value is loaded before WebClient is created.
    public MailServiceImpl(WebClient.Builder webClientBuilder, 
                           @Value("${resend.api.key}") String resendApiKey) {
        
        // Initialize WebClient with base URL and Authorization header (Bearer token)
        this.webClient = webClientBuilder
            .baseUrl(RESEND_API_URL)
            .defaultHeader("Authorization", "Bearer " + resendApiKey) 
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public void sendMail(ContactDto contactInfo) {

        // 1. Prepare dynamic content and HTML message
        String name = contactInfo.getName();
        String email = contactInfo.getEmail();
        String messageHtml = contactInfo.getMessage().replace("\n", "<br>");
        
        // URL-encode the message body for the mailto reply link
        String mailtoBody = "Name:%20" + name + 
                            "%0AEmail:%20" + email + 
                            "%0A%0A" + 
                            contactInfo.getMessage().replace("\n", "%0A");

        String htmlMessage = buildHtmlContent(name, email, messageHtml, mailtoBody);

        // 2. Build the Resend API Request Body
        ResendEmailRequest requestBody = new ResendEmailRequest(
            // ⚠️ Use a verified domain/identity from your Resend account for 'from'
            "onboarding@resend.dev", 
            "support@coelite.in",
            "New Contact Form Submission from " + name,
            htmlMessage // The full styled HTML content
        );

        // 3. Send the POST Request
        try {
            webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                // Handle non-2xx errors (e.g., 401 Unauthorized, 400 Bad Request)
                .onStatus(status -> status.isError(), response -> {
                    return response.bodyToMono(String.class)
                                   .flatMap(body -> {
                                       throw new RuntimeException("Resend API failed with status " + response.statusCode() + ". Response body: " + body);
                                   });
                })
                .bodyToMono(String.class)
                .block(); // Blocks until the call is complete

            System.out.println("Email sent successfully via Resend API for: " + name);
        } catch (Exception e) {
            System.err.println("Failed to send email via Resend API: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper method to build the HTML string using StringBuilder
    private String buildHtmlContent(String name, String email, String messageHtml, String mailtoBody) {
        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<!DOCTYPE html>")
                   .append("<html lang=\"en\">")
                   .append("<head>")
                   .append("    <meta charset=\"UTF-8\">")
                   .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                   .append("    <title>Contact Details</title>")
                   .append("    <style>")
                   // Minified CSS
                   .append("        body { font-family: Arial, sans-serif; background-color: #f4f4f9; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }")
                   .append("        .contact-card { background: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15); width: 100%; max-width: 450px; text-align: left; }")
                   .append("        h2 { text-align: center; color: #1e88e5; margin-bottom: 25px; border-bottom: 2px solid #e0e0e0; padding-bottom: 10px; }")
                   .append("        .detail-item { margin-bottom: 15px; line-height: 1.6; }")
                   .append("        .detail-item strong { display: inline-block; width: 80px; color: #555; font-weight: 600; }")
                   .append("        .message-box { background-color: #f9f9f9; border: 1px dashed #ccc; padding: 15px; border-radius: 6px; margin-top: 20px; }")
                   .append("        .message-box p { margin: 0; white-space: pre-wrap; color: #333; font-style: italic; }")
                   .append("        .mailto-link { display: block; margin-top: 30px; text-align: center; }")
                   .append("        .mailto-link a { background-color: #4CAF50; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; transition: background-color 0.3s ease; }")
                   .append("        .mailto-link a:hover { background-color: #45a049; }")
                   .append("    </style>")
                   .append("</head>")
                   .append("<body>")
                   .append("    <div class=\"contact-card\">")
                   .append("        <h2>New Contact Form Submission</h2>")
                   
                   // DYNAMIC DATA FIELDS
                   .append("        <div class=\"detail-item\">")
                   .append("            <strong>Name:</strong> ").append(name)
                   .append("        </div>")
                   .append("        <div class=\"detail-item\">")
                   .append("            <strong>Email:</strong> <a href=\"mailto:").append(email).append("\">").append(email).append("</a>")
                   .append("        </div>")
                   .append("        <div class=\"message-box\">")
                   .append("            <strong>Message:</strong>")
                   .append("            <p>").append(messageHtml).append("</p>")
                   .append("        </div>")
                   
                   // DYNAMIC MAILTO LINK
                   .append("        <div class=\"mailto-link\">")
                   .append("            <a href=\"mailto:support@coelite.in?subject=Re:%20Contact%20from%20").append(name)
                   .append("&body=").append(mailtoBody).append("\">")
                   .append("                Reply to this Message")
                   .append("            </a>")
                   .append("        </div>")
                   
                   .append("        <small style=\"display: block; text-align: center; margin-top: 15px; color: #999;\">")
                   .append("            *This is an automated notification. Click 'Reply' to respond to the sender.")
                   .append("        </small>")
                   .append("    </div>")
                   .append("</body>")
                   .append("</html>");

        return htmlBuilder.toString();
    }
}
