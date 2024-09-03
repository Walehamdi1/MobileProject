package com.work.truetech.controller;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.work.truetech.entity.ContactRequest;
import com.work.truetech.services.MailService;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MailController {
    @Autowired
    MailService emailService;
    @PostMapping("/api/send-email")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody ContactRequest contactRequest) {

        System.out.println("Received email: " + contactRequest.getEmail());
        System.out.println("Received message: " + contactRequest.getMessage());

        Map<String, String> response = new HashMap<>();

        try {
            emailService.sendSimpleEmail(contactRequest);
            response.put("status", "succès");
            response.put("message", "Email envoyé avec succès !");
            return ResponseEntity.ok(response);
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }  catch (MessagingException e) {
            e.printStackTrace();
            response.put("status", "erreur");
            response.put("message", "Échec de l'envoi de l'e-mail.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
