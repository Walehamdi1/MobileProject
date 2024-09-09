package com.work.truetech.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.work.truetech.entity.ContactRequest;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private SpringTemplateEngine templateEngine;
    public void sendSimpleEmail(ContactRequest contactRequest)  throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        context.setVariable("name", contactRequest.getName());
        context.setVariable("message", contactRequest.getMessage());
        context.setVariable("phone", contactRequest.getPhone());
        context.setVariable("email", contactRequest.getEmail());

        String htmlContent = templateEngine.process("emailTemplate", context);
        helper.setTo("helmi.br1999@gmail.com");
        helper.setSubject("TrueTech Mail");
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);

        System.out.println("Mail envoyé avec succès !");
    }
}
