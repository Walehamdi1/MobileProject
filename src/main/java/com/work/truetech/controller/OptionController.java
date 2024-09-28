package com.work.truetech.controller;

import com.work.truetech.entity.OptionType;
import com.work.truetech.entity.Phone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Option;
import com.work.truetech.repository.ModelRepository;
import com.work.truetech.repository.OptionRepository;
import com.work.truetech.services.IOptionService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
//@RequestMapping("/api/option")
//@CrossOrigin(origins="http://localhost:4200")
public class OptionController {
    @Autowired
    IOptionService optionService;

    @Autowired
    OptionRepository optionRepository;

    @PostMapping("/admin/option/{modelId}/add-option")
    @ResponseBody
    public ResponseEntity<?> createOption(@RequestParam("title") String title,
                                               @RequestParam("description") String description,
                                               @RequestParam("clientPrice") Long clientPrice,
                                               @RequestParam("supplierPrice") Long supplierPrice,
                                               @RequestParam("reparation") Long reparation,
                                               @RequestParam("quantity") int quantity,
                                               @RequestParam("optionType") OptionType optionType,
                                               @RequestParam("file") MultipartFile file,
                                               @PathVariable("modelId") Long modelId) {
        if (optionRepository.findByTitle(title)!= null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le nom de l'option existe déjà, veuillez en choisir un autre.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            Option option = new Option();
            option.setTitle(title);
            option.setDescription(description);
            option.setSupplierPrice(supplierPrice);
            option.setClientPrice(clientPrice);
            option.setReparation(reparation);
            option.setQuantity(quantity);
            option.setOptionType(optionType);

            Option createdOption = optionService.createOption(option,modelId ,file);
            return new ResponseEntity<>(createdOption, HttpStatus.CREATED);
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }  catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/api/option/find-all-options")
    @ResponseBody
    public List<?> getOptions() {
        try {
            List<Option> listOption = optionService.retrieveOptions();
            return listOption;
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @GetMapping("/api/option/find-options/{id}")
    @ResponseBody
    public ResponseEntity<List<Option>> getOptionByModel(@PathVariable("id") Long modelId) {
        try {
            List<Option> listOption = optionService.retrieveOptionByModel(modelId);
            return ResponseEntity.ok(listOption);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Network issue encountered while retrieving options.");
        } catch (Exception e) {
            // Rethrow the exception to be handled by GlobalExceptionHandler
            throw new RuntimeException("Failed to retrieve options: " + e.getMessage(), e);
        }
    }


    @GetMapping("/api/option/find-option/{optionId}")
    @ResponseBody
    public ResponseEntity<Option> getOptionById(@PathVariable("optionId") long optionId) {
        try {
            Option option = optionService.getOptionById(optionId);
            return ResponseEntity.ok(option);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Network issue encountered while retrieving option.");
        } catch (Exception e) {
            // Rethrow the exception to be handled by GlobalExceptionHandler
            throw new RuntimeException("Failed to retrieve option: " + e.getMessage(), e);
        }
    }

    @PutMapping("/admin/option/update-option/{id}")
    @ResponseBody
    public ResponseEntity<?> updateOption(@PathVariable("id") Long optionId,
                                          @RequestParam("title") String title,
                                          @RequestParam("description") String description,
                                          @RequestParam("clientPrice") Long clientPrice,
                                          @RequestParam("supplierPrice") Long supplierPrice,
                                          @RequestParam("reparation") Long reparation,
                                          @RequestParam("optionType") OptionType optionType,
                                          @RequestParam("quantity") int quantity,
                                          @RequestParam(value = "file", required = false) MultipartFile file) {

        // Retrieve the existing option by ID
        Optional<Option> existingOptionOpt = optionRepository.findById(optionId);
        if (!existingOptionOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Option non trouvé avec id: " + optionId);
        }

        Option existingOption = existingOptionOpt.get();

        // Check if the title is being changed to one that already exists for another option
        if (!title.equals(existingOption.getTitle()) && optionRepository.findByTitle(title) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le nom de l'option existe déjà, veuillez en choisir un autre.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Create an updated Option object with the provided details
            Option optionToUpdate = new Option();
            optionToUpdate.setTitle(title);
            optionToUpdate.setDescription(description);
            optionToUpdate.setSupplierPrice(supplierPrice);
            optionToUpdate.setClientPrice(clientPrice);
            optionToUpdate.setReparation(reparation);
            optionToUpdate.setQuantity(quantity);
            optionToUpdate.setOptionType(optionType);

            // Call the service to update the option
            Option updatedOption = optionService.updateOption(optionId, optionToUpdate, file);
            return new ResponseEntity<>(updatedOption, HttpStatus.OK);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Network issue encountered.");
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping("/admin/option/delete-option/{id}")
    @ResponseBody
    public void deleteOption(@PathVariable("id") Long optionid) {
        try {
            optionService.deleteOption(optionid);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }
    @GetMapping("/api/option/most-bought-options")
    public ResponseEntity<List<Map<String, Long>>> getTotalOptionsBought() {
        try {
            List<Map<String, Long>> totalOptionsBought = optionService.getTotalOptionsBoughtByPhone();
            return ResponseEntity.ok(totalOptionsBought);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Network issue encountered while retrieving total options bought.");
        } catch (Exception e) {
            // Rethrow the exception to be handled by GlobalExceptionHandler
            throw new RuntimeException("Failed to retrieve total options bought: " + e.getMessage(), e);
        }
    }
}
