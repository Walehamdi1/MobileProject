package com.work.truetech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.work.truetech.entity.ContactRequest;
import com.work.truetech.services.MailService;

@RestController
public class MailController {
    @Autowired
    MailService emailService;
    @PostMapping("/api/send-email")
    public String sendEmail(@RequestBody ContactRequest contactRequest) {

        System.out.println("Received email: " + contactRequest.getEmail());
        System.out.println("Received message: " + contactRequest.getMessage());
        emailService.sendSimpleEmail(contactRequest);
        return "Email sent successfully!";
    }
}
