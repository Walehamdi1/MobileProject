package com.work.truetech.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Phone;
import com.work.truetech.repository.ModelRepository;
import com.work.truetech.repository.PhoneRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class ModelService implements IModelService {

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    PhoneRepository phoneRepository;
    @Value("${upload.path}")
    private String upload;
    //private String uploadPath = upload + "/models";

    // Method to get the full path for models
    public String getModelsPath() {
        return upload + "/models";
    }

    @Override
    public Model createModel(Model model, Long phoneId, MultipartFile file) throws IOException {
        String uploadPath = getModelsPath();
        System.out.println("Upload Path: " + uploadPath);

        Optional<Phone> optPhone = phoneRepository.findById(phoneId);

        // Save the Model entity first to generate an ID
        Model savedModel = modelRepository.save(model);

        // Ensure the upload directory exists
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        if (optPhone.isPresent()) {
            Phone phone = optPhone.get();

            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {
                // Generate the new filename
                String originalFileName = file.getOriginalFilename();
                String newFileName = savedModel.getId() + "_" + originalFileName;

                // Save the new file to the server
                Path filePath = Paths.get(uploadPath, newFileName);
                Files.write(filePath, file.getBytes());

                // Update the Model entity with the new filename (not the full path)
                savedModel.setImage(newFileName);
            }

            // Associate the Model with the Phone
            savedModel.setPhone(phone);
            phone.getModels().add(savedModel);

            // Save the updated Model entity
            return modelRepository.save(savedModel);
        } else {
            throw new RuntimeException("Phone non trouvé avec id: " + phoneId);
        }
    }


    @Override
    public List<Model> retrieveModels() {
        return modelRepository.findAll();
    }

    @Override
    public List<Model> retrieveModelByPhone(Long phoneId) {
        return modelRepository.findByPhoneId(phoneId);
    }

    @Override
    public Model getModelById(Long modelId) {
        return modelRepository.findById(modelId).get();
    }

    @Override
    public Model updateModel(Long modelId, Model updatedModel, MultipartFile file) throws IOException {
        String uploadPath = getModelsPath();

        // Retrieve the existing model by ID
        Optional<Model> existingModelOpt = modelRepository.findById(modelId);
        if (existingModelOpt.isPresent()) {
            Model existingModel = existingModelOpt.get();

            // Update the title if it's provided
            if (updatedModel.getTitle() != null && !updatedModel.getTitle().isEmpty()) {
                existingModel.setTitle(updatedModel.getTitle());
            }

            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {
                // Delete the old file if it exists
                if (existingModel.getImage() != null) {
                    File oldFile = new File(uploadPath, existingModel.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // Ensure the upload directory exists
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Generate the new filename
                String originalFileName = file.getOriginalFilename();
                String newFileName = modelId + "_" + originalFileName;

                // Save the new file to the server
                fileStorageService.saveFile(file, newFileName, uploadPath);

                // Update the Model entity with the new filename (not the full path)
                existingModel.setImage(newFileName);
            }

            // Save and return the updated model
            return modelRepository.save(existingModel);
        } else {
            throw new EntityNotFoundException("Model avec id " + modelId + " non trouvé");
        }
    }


    @Override
    public void deleteModel(Long id) {
        // Find the model by ID
        Optional<Model> optModel = modelRepository.findById(id);

        if (optModel.isPresent()) {
            Model model = optModel.get();

            // Check if the model has an associated image
            if (model.getImage() != null) {
                // Construct the file path
                String uploadPath = getModelsPath();
                Path filePath = Paths.get(uploadPath, model.getImage());

                try {
                    // Delete the file if it exists
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    // Log an error if the file deletion fails
                    System.err.println("Impossible de supprimer le fichier: " + filePath);
                    e.printStackTrace();
                }
            }

            // Delete the model from the database
            modelRepository.delete(model);
        } else {
            throw new RuntimeException("Modèle non trouvé avec l'ID:" + id);
        }
    }


}
