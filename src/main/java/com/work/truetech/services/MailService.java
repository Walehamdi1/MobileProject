package com.work.truetech.services;

import com.work.truetech.entity.User;
import com.work.truetech.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    @Autowired
    private UserRepository userRepository;
    public void sendSimpleEmail(ContactRequest contactRequest)  throws MessagingException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername());

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        context.setVariable("name", user.getUsername());
        context.setVariable("questions", contactRequest.getQuestions());
        context.setVariable("phone", user.getPhone());
        context.setVariable("email", user.getEmail());

        String htmlContent = templateEngine.process("emailTemplate", context);
        helper.setTo("helmi.br1999@gmail.com");
        helper.setSubject("TrueTech Mail");
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);

        System.out.println("Mail envoyé avec succès !");
    }
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendWelcomeEmail(String to, String username, String password) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Bienvenue à notre service!");
        String htmlContent = generateEmailContent(username, password);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String generateEmailContent(String username, String password) {
        return """
                <html>
                <body>
                <div style="font-family: Arial, sans-serif; margin: 20px;">
                    <h1>Bienvenue à notre service!</h1>
                    <p>Pseudo: <strong>%s</strong></p>
                    <p>Mot de passe: <strong>%s</strong></p>
                    <p>Cordialement,<br>TrueTech</p>
                </div>
                </body>
                </html>
                """.formatted(username, password);
    }
    
}
