package in.coelite.internal.portfolio.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import in.coelite.internal.portfolio.dto.ContactDto;
import in.coelite.internal.portfolio.service.MailService;

@Service
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendMail(ContactDto contactInfo) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("support@coelite.in"); 
        message.setTo("support@coelite.in");
        message.setSubject("New Contact Form Submission");
        message.setText("Name: " + contactInfo.getName() +
                "\nEmail: " + contactInfo.getEmail() +
                "\nMessage:\n" + contactInfo.getMessage()); 
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }        

    }

}
