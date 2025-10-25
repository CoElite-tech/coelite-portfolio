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
    
    @Value("${resend.api.key}")
    private String resendApiKey;

    private final WebClient webClient;
    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    
    // Using a record/class for the Resend JSON body
    private record ResendEmailRequest(String from, String to, String subject, String html) {}

    public MailServiceImpl(WebClient.Builder webClientBuilder) {
        // Build and configure the WebClient with the base URL and Authorization header
        System.out.println("DEBUG: Resend API Key loaded: " + resendApiKey); // Check your console
        this.webClient = webClientBuilder
            .baseUrl(RESEND_API_URL)
            .defaultHeader("Authorization", "Bearer " + resendApiKey)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Override
    public void sendMail(ContactDto contactInfo) {

        // 1. Create an HTML version of the message
        String htmlMessage = "<h1>New Contact Form Submission</h1>" +
                             "<p><strong>Name:</strong> " + contactInfo.getName() + "</p>" +
                             "<p><strong>Email:</strong> " + contactInfo.getEmail() + "</p>" +
                             "<p><strong>Message:</strong></p>" +
                             "<div style=\"border: 1px solid #ccc; padding: 10px;\">" + 
                                contactInfo.getMessage().replace("\n", "<br>") + 
                             "</div>";

        // 2. Build the Resend API Request Body
        ResendEmailRequest requestBody = new ResendEmailRequest(
            "onboarding@resend.dev", // Verified sender
            "support@coelite.in",
            "New Contact Form Submission from " + contactInfo.getName(),
            htmlMessage
        );

        try {
            // 3. Send the POST Request
            webClient.post()
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .toBodilessEntity() // We don't care about the response body, just status
                .block(); // Blocks until the call is complete (use .subscribe() for non-blocking)

            System.out.println("Email sent successfully via Resend API.");
        } catch (Exception e) {
            System.err.println("Failed to send email via Resend API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
