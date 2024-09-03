package com.work.truetech.controller;

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
    public List<?> getOptionByModel(@PathVariable("id") Long modelId ) {
        try {
            List<Option> listOption = optionService.retrieveOptionByModel(modelId);
            return listOption;
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @GetMapping("/api/option/find-option/{optionId}")
    @ResponseBody
    public Option getOptionById(@PathVariable("optionId") long optionId) {
        try {
            return optionService.getOptionById(optionId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }

    @PutMapping("/admin/option/update-option/{id}")
    @ResponseBody
    public ResponseEntity<?> updateOption(@PathVariable("id") Long optionId, @RequestParam("title") String title,
                               @RequestParam("description") String description,
                               @RequestParam("clientPrice") Long clientPrice,
                               @RequestParam("supplierPrice") Long supplierPrice,
                               @RequestParam("reparation") Long reparation,
                               @RequestParam("quantity") int quantity,
                               @RequestParam(value = "file", required = false) MultipartFile file) {
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

            Option updatedOption = optionService.updateOption(optionId,option, file);
            return new ResponseEntity<>(updatedOption, HttpStatus.CREATED);
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }  catch (IOException e) {
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
}
