package com.work.truetech.controller;

import com.google.zxing.qrcode.decoder.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Phone;
import com.work.truetech.repository.ModelRepository;
import com.work.truetech.services.IModelService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ModelController {
    @Autowired
    IModelService modelService;

    @Autowired
    ModelRepository modelRepository;

    @PostMapping("/admin/model/{phoneId}/add-model")
    @ResponseBody
    public ResponseEntity<?> createModel(@RequestParam("title") String title,
                                         @RequestParam("file") MultipartFile file,
                                         @PathVariable("phoneId") Long phoneId) {

        try {
            if (modelRepository.findByTitle(title)!= null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Le nom du modèle existe déjà, veuillez en choisir un autre.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (file.getSize() > (50 * 1024 * 1024)) { // Example size limit: 50MB
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("message", "La taille de l'image dépasse la limite autorisée (5MB).")
                );
            }
            Model model = new Model();
            model.setTitle(title);
            Model createdModel = modelService.createModel(model,phoneId ,file);
            return new ResponseEntity<>(createdModel, HttpStatus.CREATED);
        }  catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }  catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/model/find-all-models")
    @ResponseBody
    public List<?> getModels() {
        try{
        List<Model> listModel = modelService.retrieveModels();
        return listModel;
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }

    @GetMapping("/api/model/find-models/{id}")
    @ResponseBody
    public ResponseEntity<List<Model>> getModelByPhoneName(@PathVariable("id") Long phoneId) {
        try {
            List<Model> listModel = modelService.retrieveModelByPhone(phoneId);
            return ResponseEntity.ok(listModel);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (Exception e) {
            // Rethrow the exception to be handled by GlobalExceptionHandler
            throw new RuntimeException("Échec de la récupération des modèles: " + e.getMessage(), e);
        }
    }

    @GetMapping("/api/model/find-model/{modelId}")
    @ResponseBody
    public ResponseEntity<Model> getModelById(@PathVariable("modelId") long modelId) {
        try {
            Model model = modelService.getModelById(modelId);
            return ResponseEntity.ok(model);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré lors de la récupération du modèle.");
        } catch (Exception e) {
            // Rethrow the exception to be handled by GlobalExceptionHandler
            throw new RuntimeException("Impossible de récupérer le modèle: " + e.getMessage(), e);
        }
    }

    @PutMapping("/admin/model/update-model/{id}")
    @ResponseBody
    public ResponseEntity<?> updateModel(@PathVariable("id") Long modelId,
                                         @RequestParam("title") String title,
                                         @RequestParam(value = "file", required = false) MultipartFile file) {
        Optional<Model> existingModelOpt = modelRepository.findById(modelId);
        if (!existingModelOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Model non trouvé avec id: " + modelId);
        }
        Model existingModel = existingModelOpt.get();

        if (!title.equals(existingModel.getTitle()) && modelRepository.findByTitle(title) != null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Le nom du modèle existe déjà, veuillez en choisir un autre.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            Model modelToUpdate = new Model();
            modelToUpdate.setTitle(title);

            Model updatedModel = modelService.updateModel(modelId, modelToUpdate, file);
            return new ResponseEntity<>(updatedModel, HttpStatus.OK);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/admin/model/delete-model/{id}")
    @ResponseBody
    public void deleteModel(@PathVariable("id") Long modelId) {
        try {
            modelService.deleteModel(modelId);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }
}
