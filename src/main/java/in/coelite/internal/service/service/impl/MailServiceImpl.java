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
    String name = contactInfo.getName();
String email = contactInfo.getEmail();
// Convert newlines in the message to <br> for HTML display
String messageHtml = contactInfo.getMessage().replace("\n", "<br>"); 

// URL-encode the message body for the mailto reply link
String mailtoBody = "Name:%20" + name + 
                    "%0AEmail:%20" + email + 
                    "%0A%0A" + 
                    contactInfo.getMessage().replace("\n", "%0A");

String htmlMessage = "<!DOCTYPE html>"
    + "<html lang='en'>"
    + "<head>"
    + "    <meta charset='UTF-8'>"
    + "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>"
    + "    <title>Contact Details</title>"
    + "</head>"
    // Inline style for body (centering container)
    + "<body style='font-family: Arial, sans-serif; background-color: #f4f4f9; margin: 0; padding: 20px;'>"
    
    // Outer container table for centering and max-width (Email Best Practice)
    + "    <table border='0' cellpadding='0' cellspacing='0' width='100%' style='max-width: 450px; margin: 0 auto;'>"
    + "        <tr>"
    + "            <td align='center'>"
    
    // Contact Card Table (Styled)
    + "                <table border='0' cellpadding='0' cellspacing='0' width='100%' "
    + "                       style='background: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15); text-align: left;'>"
    + "                    <tr>"
    // H2/Title Row
    + "                        <td style='text-align: center; color: #1e88e5; border-bottom: 2px solid #e0e0e0; padding-bottom: 10px;'>"
    + "                            <h2 style='margin: 0; font-size: 20px;'>New Contact Submission</h2>"
    + "                        </td>"
    + "                    </tr>"

    // Dynamic Name Detail
    + "                    <tr>"
    + "                        <td style='margin-bottom: 15px; line-height: 1.6; padding-top: 15px;'>"
    + "                            <strong style='display: inline-block; width: 80px; color: #555; font-weight: 600;'>Name:</strong> " + name
    + "                        </td>"
    + "                    </tr>"

    // Dynamic Email Detail
    + "                    <tr>"
    + "                        <td style='margin-bottom: 15px; line-height: 1.6; padding-bottom: 15px;'>"
    + "                            <strong style='display: inline-block; width: 80px; color: #555; font-weight: 600;'>Email:</strong> "
    + "                            <a href='mailto:" + email + "' style='color: #1e88e5;'>" + email + "</a>"
    + "                        </td>"
    + "                    </tr>"
    
    // Dynamic Message Box
    + "                    <tr>"
    + "                        <td style='padding-top: 10px;'>"
    + "                            <div style='background-color: #f9f9f9; border: 1px dashed #ccc; padding: 15px; border-radius: 6px; margin-top: 20px;'>"
    + "                                <strong style='display: block; margin-bottom: 5px; color: #333;'>Message:</strong>"
    + "                                <p style='margin: 0; white-space: pre-wrap; color: #333; font-style: italic;'>" + messageHtml + "</p>"
    + "                            </div>"
    + "                        </td>"
    + "                    </tr>"

    // Reply Button/Link
    + "                    <tr>"
    + "                        <td style='padding: 30px 0 10px; text-align: center;'>"
    + "                            <a href='mailto:support@coelite.in?subject=Re:%20Contact%20from%20" + name
    + "&body=" + mailtoBody + "'" // Final dynamic mailto link
    + "                               style='background-color: #4CAF50; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; transition: background-color 0.3s ease;'>"
    + "                                Reply to this Message"
    + "                            </a>"
    + "                        </td>"
    + "                    </tr>"

    // Footer Note
    + "                    <tr>"
    + "                        <td style='text-align: center; padding-top: 15px;'>"
    + "                            <small style='display: block; color: #999; font-size: 12px;'>"
    + "                                *This message was sent from your website. Click 'Reply' to respond directly to " + name + "."
    + "                            </small>"
    + "                        </td>"
    + "                    </tr>"
    + "                </table>"
    + "            </td>"
    + "        </tr>"
    + "    </table>"
    + "</body>"
    + "</html>"; // <--- This final line must NOT have a '+'
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
