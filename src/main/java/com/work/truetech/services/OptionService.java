package com.work.truetech.services;

import com.work.truetech.repository.FactureOptionRepository;
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
import java.util.*;

@Service
public class OptionService implements IOptionService {

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Value("${upload.path}")
    private String upload;

    @Autowired
    private FileStorageService fileStorageService;

    public String getOptionsPath() {
        return upload + "/options";
    }
    @Override
    public Option createOption(Option option, Long modelId, MultipartFile file) throws IOException {
        String uploadPath = getOptionsPath();
        Optional<Model> optModel = modelRepository.findById(modelId);

        if (option.getOptionType() == null) {
            throw new RuntimeException("OptionType doit être spécifié (CLIENT, FOURNISSEUR ou LES DEUX).");
        }
        if (option.getQuantity() == null) {
            option.setQuantity(0);
        }

        Option savedOption = optionRepository.save(option);

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        if (optModel.isPresent()) {
            Model model = optModel.get();

            if (file != null && !file.isEmpty()) {
                String originalFileName = file.getOriginalFilename();
                String newFileName = savedOption.getId() + "_" + originalFileName;

                fileStorageService.saveFile(file, newFileName, uploadPath);

                savedOption.setImage(newFileName);
            }

            savedOption.setModel(model);
            model.getOptions().add(savedOption);
            return optionRepository.save(savedOption);
        } else {
            throw new RuntimeException("Modèle non trouvé avec l'ID: " + modelId);
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

            existingOption.setTitle(updatedOption.getTitle() != null ? updatedOption.getTitle() : existingOption.getTitle());
            existingOption.setClientPrice(updatedOption.getClientPrice() != null ? updatedOption.getClientPrice() : existingOption.getClientPrice());
            existingOption.setSupplierPrice(updatedOption.getSupplierPrice() != null ? updatedOption.getSupplierPrice() : existingOption.getSupplierPrice());
            existingOption.setQuantity(updatedOption.getQuantity() > 0 ? updatedOption.getQuantity() : existingOption.getQuantity());
            existingOption.setDescription(updatedOption.getDescription() != null ? updatedOption.getDescription() : existingOption.getDescription());
            existingOption.setReparation(updatedOption.getReparation() != null ? updatedOption.getReparation() : existingOption.getReparation());
            existingOption.setOptionType(updatedOption.getOptionType() != null ? updatedOption.getOptionType() : existingOption.getOptionType());

            if (updatedOption.getOptionType() != null) {
                existingOption.setOptionType(updatedOption.getOptionType());
            }
            if (file != null && !file.isEmpty()) {
                if (existingOption.getImage() != null) {
                    File oldFile = new File(uploadPath, existingOption.getImage());
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
                String originalFileName = file.getOriginalFilename();
                String newFileName = optionId + "_" + originalFileName;

                fileStorageService.saveFile(file, newFileName, uploadPath);
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

            if (existingOption.getImage() != null) {
                File imageFile = new File(uploadPath, existingOption.getImage());
                if (imageFile.exists()) {
                    if (!imageFile.delete()) {
                        System.err.println("Impossible de supprimer le fichier image: " + imageFile.getPath());
                    }
                }
            }

            optionRepository.delete(existingOption);
        } else {
            throw new RuntimeException("Option non trouvé avec id: " + optionId);
        }
    }

    @Autowired
    FactureOptionRepository factureOptionRepository;

    public List<Map<String, Long>> getTotalOptionsBoughtByPhone() {
        List<Object[]> result = factureOptionRepository.findTotalOptionsBoughtByPhone();
        List<Map<String, Long>> phoneOptionsTotal = new ArrayList<>();

        for (Object[] row : result) {
            String phoneTitle = (String) row[0];
            Long totalQuantity = (Long) row[1];

            Map<String, Long> phoneData = new HashMap<>();
            phoneData.put(phoneTitle, totalQuantity);
            phoneOptionsTotal.add(phoneData);
        }
        return phoneOptionsTotal;
    }
}
