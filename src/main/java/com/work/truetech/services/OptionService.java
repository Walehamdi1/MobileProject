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
    public Option createOption(Option option,Long modelId, MultipartFile file) throws IOException{
        String uploadPath = getOptionsPath();
        Optional<Model> OptModel = modelRepository.findById(modelId);
        Option savedOption = optionRepository.save(option);
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        if (OptModel.isPresent()) {
            // Save the file to the server
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(uploadPath, savedOption.getId() + "_" + fileName);
            Files.write(filePath, file.getBytes());

            Model model = OptModel.get();
            savedOption.setModel(model);
            model.getOptions().add(savedOption);
            savedOption.setImage(filePath.toString());
            return optionRepository.save(savedOption);
        }else {
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
        return optionalOption.orElseThrow(() -> new RuntimeException("Option not found with id: " + optionId));
    }

    @Override
    public Option updateOption(Long optionId, Option updatedOption, MultipartFile file) throws IOException  {
        String uploadPath = getOptionsPath();
        Optional<Option> optionalOption = optionRepository.findById(optionId);
        if (optionalOption.isPresent()) {
            Option existingOption = optionalOption.get();
            existingOption.setTitle(updatedOption.getTitle());
            existingOption.setClientPrice(updatedOption.getClientPrice());
            existingOption.setSupplierPrice(updatedOption.getSupplierPrice());
            existingOption.setQuantity(updatedOption.getQuantity());
            existingOption.setDescription(updatedOption.getDescription());
            // Check if a new file is provided
            if (file != null && !file.isEmpty()) {

                // Delete the old file if it exists
                if (existingOption.getImage() != null) {
                    File oldFile = new File(existingOption.getImage());
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
                Path filePath = Paths.get(uploadPath, existingOption.getId() + "_" + fileName);
                Files.write(filePath, file.getBytes());

                // Update the Phone entity with the new file path
                existingOption.setImage(filePath.toString());
            } else {
                // If no new file is provided, retain the existing image path
                existingOption.setImage(updatedOption.getImage());
            }

            return optionRepository.save(existingOption);
        } else {
            throw new RuntimeException("Option not found with id: " + optionId);
        }
    }

    @Override
    public void deleteOption(Long id) {
        optionRepository.deleteById(id);
    }
}
