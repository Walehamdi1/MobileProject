package com.work.truetech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Phone;
import com.work.truetech.repository.PhoneRepository;
import com.work.truetech.services.IPhoneService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//@RequestMapping("/api/phone")
//@CrossOrigin(origins="http://localhost:4200")
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
                response.put("message", "Option name already exists, please choose a different one.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            Phone phone = new Phone();
            phone.setTitle(title);
            Phone createdPhone = iPhoneService.createPhone(phone, file);
            return new ResponseEntity<>(createdPhone, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/phone/find-all-phones")
    @ResponseBody
    public List<?> getPhone() {
        List<Phone> listPhone = iPhoneService.retrievePhones();
        return listPhone;
    }

    @GetMapping("/api/phone/find-phone/{phoneId}")
    @ResponseBody
    public Phone getPhoneById(@PathVariable("phoneId") long phoneId) {
        return  iPhoneService.retrievePhoneById(phoneId);
    }

    @PutMapping("/admin/phone/update-phone/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePhone(@PathVariable("id") Long phoneId, @RequestParam("title") String title,
                             @RequestParam("file") MultipartFile file) {
        try {
            Phone phone = new Phone();
            phone.setTitle(title);
            Phone updatedPhone = iPhoneService.updatePhone(phoneId,phone, file);
            return new ResponseEntity<>(updatedPhone, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/admin/phone/delete-phone/{id}")
    @ResponseBody
    public void deletePhone(@PathVariable("id") Long phoneId) {
        iPhoneService.deletePhone(phoneId);
    }

}
