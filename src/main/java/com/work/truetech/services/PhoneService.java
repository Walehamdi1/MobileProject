package com.work.truetech.services;

import com.work.truetech.entity.User;
import com.work.truetech.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Phone;
import com.work.truetech.repository.PhoneRepository;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Optional;


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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userDetails.getId()));

        phone.setUser(user);
        Phone savedPhone = phoneRepository.save(phone);

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String originalFileName = file.getOriginalFilename();
        String newFileName = savedPhone.getId() + "_" + originalFileName;
        fileStorageService.saveFile(file, newFileName, uploadPath);
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

        Optional<Phone> existingPhoneOpt = phoneRepository.findById(phoneId);
        if (existingPhoneOpt.isPresent()) {
            Phone existingPhone = existingPhoneOpt.get();

            existingPhone.setTitle(updatedPhone.getTitle() != null ? updatedPhone.getTitle() : existingPhone.getTitle());
            existingPhone.setModels(updatedPhone.getModels() != null ? updatedPhone.getModels() : existingPhone.getModels());

            if (file != null && !file.isEmpty()) {
                if (existingPhone.getImage() != null) {
                    File oldFile = new File(uploadPath, existingPhone.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                String originalFileName = file.getOriginalFilename();
                String newFileName = existingPhone.getId() + "_" + originalFileName;

                fileStorageService.saveFile(file, newFileName, uploadPath);

                existingPhone.setImage(newFileName);
            }
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

            if (existingPhone.getImage() != null) {
                File imageFile = new File(uploadPath, existingPhone.getImage());
                if (imageFile.exists()) {
                    if (!imageFile.delete()) {
                        System.err.println("Impossible de supprimer le fichier image: " + imageFile.getPath());
                    }
                }
            }
            phoneRepository.delete(existingPhone);
        } else {
            throw new RuntimeException("Model non trouvé avec id: " + id);
        }
    }



}
