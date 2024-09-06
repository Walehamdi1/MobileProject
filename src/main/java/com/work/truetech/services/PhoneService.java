package com.work.truetech.services;

import com.work.truetech.entity.User;
import com.work.truetech.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public String getPhonesPath() {
        return upload + "/phones";
    }

    @Override
    public Phone createPhone(Phone phone, MultipartFile file) throws IOException {
        String uploadPath = getPhonesPath();
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Find the User by ID from the CustomUserDetails
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userDetails.getId()));

        // Associate the User with the Phone
        phone.setUser(user);
        // Save the Phone entity first to generate an ID
        Phone savedPhone = phoneRepository.save(phone);

        // Ensure the upload directory exists
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Get the original filename and construct the new filename
        String originalFileName = file.getOriginalFilename();
        String newFileName = savedPhone.getId() + "_" + originalFileName;

        // Save the file to the server with the new filename
        fileStorageService.saveFile(file, newFileName, uploadPath);

        // Save only the new filename (not the full path) in the Phone entity
        savedPhone.setImage(newFileName);
        return phoneRepository.save(savedPhone);
    }


    @Override
    public List<Phone> retrievePhones() {
        return phoneRepository.findAll();
    }


    @Override
    public Phone updatePhone(Long phoneId, Phone updatedPhone, MultipartFile file) throws IOException {
        String uploadPath = getPhonesPath();

        // Retrieve the existing phone
        Optional<Phone> existingPhoneOpt = phoneRepository.findById(phoneId);
        if (existingPhoneOpt.isPresent()) {
            Phone existingPhone = existingPhoneOpt.get();

            // Update the title and models if they have changed
            existingPhone.setTitle(updatedPhone.getTitle() != null ? updatedPhone.getTitle() : existingPhone.getTitle());
            existingPhone.setModels(updatedPhone.getModels() != null ? updatedPhone.getModels() : existingPhone.getModels());

            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {
                // Delete the old file if it exists
                if (existingPhone.getImage() != null) {
                    File oldFile = new File(uploadPath, existingPhone.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // Generate the new filename
                String originalFileName = file.getOriginalFilename();
                String newFileName = existingPhone.getId() + "_" + originalFileName;

                // Use the asynchronous method to save the file
                fileStorageService.saveFile(file, newFileName, uploadPath);

                // Update the Phone entity with the new filename (not the full path)
                existingPhone.setImage(newFileName);
            }

            // Save the updated phone entity
            return phoneRepository.save(existingPhone);
        } else {
            throw new EntityNotFoundException("Phone avec id " + phoneId + " non trouvé");
        }
    }



    @Override
    public Phone retrievePhoneById(long id) {
        return phoneRepository.findById(id).get();
    }

    @Override
    public void deletePhone(long id) {
        String uploadPath = getPhonesPath();
        Optional<Phone> optionalPhone = phoneRepository.findById(id);

        if (optionalPhone.isPresent()) {
            Phone existingPhone = optionalPhone.get();

            // Delete the associated image file if it exists
            if (existingPhone.getImage() != null) {
                File imageFile = new File(uploadPath, existingPhone.getImage());
                if (imageFile.exists()) {
                    if (!imageFile.delete()) {
                        // Log or handle if the file deletion fails
                        System.err.println("Failed to delete the image file: " + imageFile.getPath());
                    }
                }
            }

            // Delete the Model entity from the repository
            phoneRepository.delete(existingPhone);
        } else {
            throw new RuntimeException("Model non trouvé avec id: " + id);
        }
    }



}
