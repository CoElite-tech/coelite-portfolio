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
        String htmlMessage = "<!DOCTYPE html>"
    + "<html lang=\"en\">"
    + "<head>"
    + "    <meta charset=\"UTF-8\">"
    + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
    + "    <title>Contact Details</title>"
    + "    <style>"
    + "        body { font-family: Arial, sans-serif; background-color: #f4f4f9; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }"
    + "        .contact-card { background: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15); width: 100%; max-width: 450px; text-align: left; }"
    + "        h2 { text-align: center; color: #1e88e5; margin-bottom: 25px; border-bottom: 2px solid #e0e0e0; padding-bottom: 10px; }"
    + "        .detail-item { margin-bottom: 15px; line-height: 1.6; }"
    + "        .detail-item strong { display: inline-block; width: 80px; color: #555; font-weight: 600; }"
    + "        .message-box { background-color: #f9f9f9; border: 1px dashed #ccc; padding: 15px; border-radius: 6px; margin-top: 20px; }"
    + "        .message-box p { margin: 0; white-space: pre-wrap; color: #333; font-style: italic; }"
    + "        .mailto-link { display: block; margin-top: 30px; text-align: center; }"
    + "        .mailto-link a { background-color: #4CAF50; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; transition: background-color 0.3s ease; }"
    + "        .mailto-link a:hover { background-color: #45a049; }"
    + "    </style>"
    + "</head>"
    + "<body>"
    + "    <div class=\"contact-card\">"
    + "        <h2>New Contact Form Submission</h2>" // Modified title for clarity
    
    // --- DYNAMIC DATA FIELDS ---
    + "        <div class=\"detail-item\">"
    + "            <strong>Name:</strong> " + contactInfo.getName() + 
    + "        </div>"
    
    + "        <div class=\"detail-item\">"
    + "            <strong>Email:</strong> <a href=\"mailto:" + contactInfo.getEmail() + "\">" + contactInfo.getEmail() + "</a>" +
    + "        </div>"
    
    + "        <div class=\"message-box\">"
    + "            <strong>Message:</strong>"
    + "            <p>" + contactInfo.getMessage().replace("\n", "<br>") + "</p>" // Replace newlines with <br> for HTML
    + "        </div>"
    
    // --- DYNAMIC MAILTO LINK ---
    + "        <div class=\"mailto-link\">"
    + "            <a href=\"mailto:support@coelite.in?subject=Re:%20Contact%20from%20" + contactInfo.getName() + 
                 "&body=Name:%20" + contactInfo.getName() + "%0AEmail:%20" + contactInfo.getEmail() + "%0A%0A" + 
                 contactInfo.getMessage().replace("\n", "%0A") + "\">" // URL-encoded body
    + "                Reply to this Message"
    + "            </a>"
    + "        </div>"
    
    + "        <small style=\"display: block; text-align: center; margin-top: 15px; color: #999;\">"
    + "            *This is an automated notification. Click 'Reply' to respond to the sender."
    + "        </small>"
    + "    </div>"
    + "</body>"
    + "</html>";

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
