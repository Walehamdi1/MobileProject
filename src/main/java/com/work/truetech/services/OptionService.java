package com.work.truetech.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Option;
import com.work.truetech.repository.ModelRepository;
import com.work.truetech.repository.OptionRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class OptionService implements IOptionService {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Value("${upload.path}")
    private String upload;

    public String getOptionsPath() {
        return upload + "/options";
    }
    @Override
    public Option createOption(Option option, Long modelId, MultipartFile file) throws IOException {
        String uploadPath = getOptionsPath();
        Optional<Model> optModel = modelRepository.findById(modelId);

        // Save the Option entity first to generate an ID
        Option savedOption = optionRepository.save(option);

        // Ensure the upload directory exists
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        if (optModel.isPresent()) {
            Model model = optModel.get();

            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {
                // Generate the new filename
                String originalFileName = file.getOriginalFilename();
                String newFileName = savedOption.getId() + "_" + originalFileName;

                // Save the new file to the server
                Path filePath = Paths.get(uploadPath, newFileName);
                Files.write(filePath, file.getBytes());

                // Update the Option entity with the new filename (not the full path)
                savedOption.setImage(newFileName);
            }

            // Associate the Option with the Model
            savedOption.setModel(model);
            model.getOptions().add(savedOption);

            // Save and return the updated Option entity
            return optionRepository.save(savedOption);
        } else {
            throw new RuntimeException("Model not found with id: " + modelId);
        }
    }


    @Override
    public List<Option> retrieveOptions() {
        return optionRepository.findAll();
    }

    @Override
    public List<Option> retrieveOptionByModel(Long modelId) {

            return optionRepository.findByModelId(modelId);

    }

    @Override
    public Option getOptionById(Long optionId) {
        Optional<Option> optionalOption = optionRepository.findById(optionId);
        return optionalOption.orElseThrow(() -> new RuntimeException("Option non trouvé avec id: " + optionId));
    }

    @Override
    public Option updateOption(Long optionId, Option updatedOption, MultipartFile file) throws IOException {
        String uploadPath = getOptionsPath();
        Optional<Option> optionalOption = optionRepository.findById(optionId);

        if (optionalOption.isPresent()) {
            Option existingOption = optionalOption.get();

            // Update the existing Option's properties
            existingOption.setTitle(updatedOption.getTitle());
            existingOption.setClientPrice(updatedOption.getClientPrice());
            existingOption.setSupplierPrice(updatedOption.getSupplierPrice());
            existingOption.setQuantity(updatedOption.getQuantity());
            existingOption.setDescription(updatedOption.getDescription());
            existingOption.setReparation(updatedOption.getReparation());

            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {
                // Delete the old file if it exists
                if (existingOption.getImage() != null) {
                    File oldFile = new File(uploadPath, existingOption.getImage());
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
                String newFileName = optionId + "_" + originalFileName;

                // Save the new file to the server
                Path filePath = Paths.get(uploadPath, newFileName);
                Files.write(filePath, file.getBytes());

                // Update the Option entity with the new filename (not the full path)
                existingOption.setImage(newFileName);
            }

            return optionRepository.save(existingOption);
        } else {
            throw new RuntimeException("Option non trouvé avec id: " + optionId);
        }
    }


    @Override
    public void deleteOption(Long optionId) {
        String uploadPath = getOptionsPath();
        Optional<Option> optionalOption = optionRepository.findById(optionId);

        if (optionalOption.isPresent()) {
            Option existingOption = optionalOption.get();

            // Delete the associated image file if it exists
            if (existingOption.getImage() != null) {
                File imageFile = new File(uploadPath, existingOption.getImage());
                if (imageFile.exists()) {
                    if (!imageFile.delete()) {
                        // Log or handle if the file deletion fails
                        System.err.println("Failed to delete the image file: " + imageFile.getPath());
                    }
                }
            }

            // Delete the Option entity from the repository
            optionRepository.delete(existingOption);
        } else {
            throw new RuntimeException("Option non trouvé avec id: " + optionId);
        }
    }

}
