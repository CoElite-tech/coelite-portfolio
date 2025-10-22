package in.coelite.internal.portfolio.service;

import in.coelite.internal.portfolio.dto.ContactDto;

public interface MailService {
    public void sendMail(ContactDto contactInfo);
}
