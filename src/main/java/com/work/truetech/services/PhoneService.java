package com.work.truetech.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Phone;
import com.work.truetech.repository.PhoneRepository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service

public class PhoneService implements IPhoneService {
    @Autowired
    PhoneRepository phoneRepository;
    @Value("${upload.path}")
    private String upload;

    public String getPhonesPath() {
        return upload + "/phones";
    }

    @Override
    public Phone createPhone(Phone phone, MultipartFile file) throws IOException {
        String uploadPath = getPhonesPath();

        Phone savedPhone = phoneRepository.save(phone);

        // Ensure the upload directory exists
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Save the file to the server
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, savedPhone.getId() + "_" + fileName);
        Files.write(filePath, file.getBytes());

        // Update the Phone entity with the file path
        savedPhone.setImage(filePath.toString());
        return phoneRepository.save(savedPhone);
    }

    @Override
    public List<Phone> retrievePhones() {
        return phoneRepository.findAll();
    }


    @Override
    public Phone updatePhone(Long phoneId, Phone updatedPhone, MultipartFile file) throws IOException {
        String uploadPath = getPhonesPath();

        Optional<Phone> existingPhoneOpt = phoneRepository.findById(phoneId);

        if (existingPhoneOpt.isPresent()) {

            Phone existingPhone = existingPhoneOpt.get();
            existingPhone.setTitle(updatedPhone.getTitle());
            existingPhone.setModels(updatedPhone.getModels());
            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {

                // Delete the old file if it exists
                if (existingPhone.getImage() != null) {
                    File oldFile = new File(existingPhone.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // Ensure the upload directory exists
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Save the new file to the server
                String fileName = file.getOriginalFilename();
                Path filePath = Paths.get(uploadPath, existingPhone.getId() + "_" + fileName);
                Files.write(filePath, file.getBytes());

                // Update the Phone entity with the new file path
                existingPhone.setImage(filePath.toString());
            } else {
                // If no new file is provided, retain the existing image path
                existingPhone.setImage(updatedPhone.getImage());
            }

            return phoneRepository.save(existingPhone);
        } else {
            throw new EntityNotFoundException("Phone with id " + phoneId + " not found");
        }
    }

    @Override
    public Phone retrievePhoneById(long id) {
        return phoneRepository.findById(id).get();
    }

    @Override
    public void deletePhone(long id) {
        phoneRepository.deleteById(id);
    }



}
