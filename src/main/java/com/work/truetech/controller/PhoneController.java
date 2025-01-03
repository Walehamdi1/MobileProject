package com.work.truetech.controller;

import com.work.truetech.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Phone;
import com.work.truetech.repository.PhoneRepository;
import com.work.truetech.services.IPhoneService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class PhoneController {

    @Autowired
    IPhoneService iPhoneService;

    @Autowired
    PhoneRepository phoneRepository;

    @PostMapping("/admin/phone/add-phone")
    @ResponseBody
    public ResponseEntity<?> createPhone(@RequestParam("title") String title,
                            @RequestParam("file") MultipartFile file) {

        try {
            if (phoneRepository.findByTitle(title)!= null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Le nom de l'option existe déjà, veuillez en choisir un autre.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (file.getSize() > (50 * 1024 * 1024)) { // Example size limit: 50MB
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("message", "La taille de l'image dépasse la limite autorisée (5MB).")
                );
            }
            Phone phone = new Phone();
            phone.setTitle(title);
            Phone createdPhone = iPhoneService.createPhone(phone, file);
            return new ResponseEntity<>(createdPhone, HttpStatus.CREATED);
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }  catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/phone/find-all-phones")
    @ResponseBody
    public List<?> getPhone() {
        try {
            List<Phone> listPhone = iPhoneService.retrievePhones();
            return listPhone;
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré lors de la récupération des téléphones.");
        } catch (Exception e) {
            throw new RuntimeException("Impossible de récupérer les téléphones: " + e.getMessage(), e);
        }
    }


    @GetMapping("/api/phone/find-phone/{phoneId}")
    @ResponseBody
    public Phone getPhoneById(@PathVariable("phoneId") long phoneId) {
        try {
            return iPhoneService.retrievePhoneById(phoneId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }

    @PutMapping("/admin/phone/update-phone/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePhone(@PathVariable("id") Long phoneId,
                                         @RequestParam(value = "title", required = false) String title,
                                         @RequestParam(value = "file", required = false) MultipartFile file) {

        Optional<Phone> existingPhoneOpt = phoneRepository.findById(phoneId);
        if (!existingPhoneOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Phone avec id " + phoneId + " non trouvé");
        }

        Phone existingPhone = existingPhoneOpt.get();
        if (title != null && !title.equals(existingPhone.getTitle()) && phoneRepository.findByTitle(title) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le nom de l'option existe déjà, veuillez en choisir un autre.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            Phone phoneToUpdate = new Phone();
            phoneToUpdate.setTitle(title != null ? title : existingPhone.getTitle());

            Phone updatedPhone = iPhoneService.updatePhone(phoneId, phoneToUpdate, file);
            return new ResponseEntity<>(updatedPhone, HttpStatus.OK);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/admin/phone/delete-phone/{id}")
    @ResponseBody
    public void deletePhone(@PathVariable("id") Long phoneId) {
        try {
            iPhoneService.deletePhone(phoneId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }

}
