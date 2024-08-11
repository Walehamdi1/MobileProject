package com.work.truetech.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.work.truetech.entity.ContactRequest;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(ContactRequest contactRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(contactRequest.getEmail());
        message.setSubject("Contact");
        message.setText(contactRequest.getMessage());
        message.setFrom("walahamdi0@gmail.com");

        mailSender.send(message);

        System.out.println("Mail sent successfully!");
    }
}
