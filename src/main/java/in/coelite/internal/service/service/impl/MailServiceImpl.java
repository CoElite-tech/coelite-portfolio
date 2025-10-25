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

// }
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
    
    // We will use the WebClient to make the HTTP call
    private final WebClient webClient;
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    
    // Using a record for the JSON body (requires Java 16+, otherwise use a class)
    private record ResendEmailRequest(String from, String to, String subject, String html) {}

    // --- CONSTRUCTOR: Injecting @Value and initializing WebClient ---
    // The key injection happens here first, guaranteeing it's available for WebClient setup.
    public MailServiceImpl(WebClient.Builder webClientBuilder, 
                           @Value("${key}") String resendApiKey) {
        
        // This is where we configure the WebClient with the base headers (API Key)
        this.webClient = webClientBuilder
            .baseUrl(RESEND_API_URL)
            // Set the Authorization header correctly here: "Bearer [key]"
            .defaultHeader("Authorization", "Bearer " + resendApiKey) 
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public void sendMail(ContactDto contactInfo) {

        // 1. Create a simple HTML version of the message
        String htmlMessage = "<h1>New Contact Form Submission</h1>" +
                             "<p><strong>Name:</strong> " + contactInfo.getName() + "</p>" +
                             "<p><strong>Email:</strong> " + contactInfo.getEmail() + "</p>" +
                             "<p><strong>Message:</strong></p>" +
                             "<div style=\"border: 1px solid #ccc; padding: 10px;\">" + 
                                contactInfo.getMessage().replace("\n", "<br>") + 
                             "</div>";

        // 2. Build the Resend API Request Body
        ResendEmailRequest requestBody = new ResendEmailRequest(
            "onboarding@resend.dev", // Must be a verified sender in your Resend account
            "support@coelite.in",
            "New Contact Form Submission from " + contactInfo.getName(),
            htmlMessage
        );

        try {
            // 3. Send the POST Request
            webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                // Use onStatus to handle non-2xx status codes (like 401, 400, 500)
                .onStatus(status -> status.isError(), response -> {
                    // Throw a specific exception on error
                    return response.bodyToMono(String.class)
                                   .flatMap(body -> {
                                       throw new RuntimeException("Resend API failed with status " + response.statusCode() + ". Response body: " + body);
                                   });
                })
                .bodyToMono(String.class) // Expecting a JSON string response (e.g., {"id": "..."})
                .block(); 
                
            System.out.println("Email sent successfully via Resend API.");
        } catch (Exception e) {
            // The improved error handling catches the exception thrown by onStatus
            System.err.println("Failed to send email via Resend API: " + e.getMessage());
            e.printStackTrace();
            
            // You can optionally re-throw the exception here if the application should stop
            // throw new RuntimeException("Email service failure.", e);
        }
    }
}
