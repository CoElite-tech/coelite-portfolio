package in.coelite.internal.portfolio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.coelite.internal.portfolio.dto.ContactDto;
import in.coelite.internal.portfolio.service.MailService;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private MailService mailService;
    public ContactController(MailService mailService){
        this.mailService = mailService;
    }

    @PostMapping({ "/mail/", "/mail" })
    public ResponseEntity<String> sendMail(@RequestBody ContactDto contactDto) {
        try{
            this.mailService.sendMail(contactDto);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
