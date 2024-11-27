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

    public String getModelsPath() {
        return upload + "/models";
    }

    @Override
    public Model createModel(Model model, Long phoneId, MultipartFile file) throws IOException {
        String uploadPath = getModelsPath();
        System.out.println("Upload Path: " + uploadPath);

        Optional<Phone> optPhone = phoneRepository.findById(phoneId);
        Model savedModel = modelRepository.save(model);

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        if (optPhone.isPresent()) {
            Phone phone = optPhone.get();

            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String newFileName = savedModel.getId() + "_" + originalFileName;

                Path filePath = Paths.get(uploadPath, newFileName);
                Files.write(filePath, file.getBytes());

                savedModel.setImage(newFileName);
            }

            savedModel.setPhone(phone);
            phone.getModels().add(savedModel);

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

        Optional<Model> existingModelOpt = modelRepository.findById(modelId);
        if (existingModelOpt.isPresent()) {
            Model existingModel = existingModelOpt.get();

            if (updatedModel.getTitle() != null && !updatedModel.getTitle().isEmpty()) {
                existingModel.setTitle(updatedModel.getTitle());
            }

            if (file != null && !file.isEmpty()) {
                if (existingModel.getImage() != null) {
                    File oldFile = new File(uploadPath, existingModel.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                String originalFileName = file.getOriginalFilename();
                String newFileName = modelId + "_" + originalFileName;

                fileStorageService.saveFile(file, newFileName, uploadPath);
                existingModel.setImage(newFileName);
            }

            return modelRepository.save(existingModel);
        } else {
            throw new EntityNotFoundException("Model avec id " + modelId + " non trouvé");
        }
    }


    @Override
    public void deleteModel(Long id) {
        Optional<Model> optModel = modelRepository.findById(id);

        if (optModel.isPresent()) {
            Model model = optModel.get();
            if (model.getImage() != null) {
                String uploadPath = getModelsPath();
                Path filePath = Paths.get(uploadPath, model.getImage());

                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Impossible de supprimer le fichier: " + filePath);
                    e.printStackTrace();
                }
            }
            modelRepository.delete(model);
        } else {
            throw new RuntimeException("Modèle non trouvé avec l'ID:" + id);
        }
    }


}
