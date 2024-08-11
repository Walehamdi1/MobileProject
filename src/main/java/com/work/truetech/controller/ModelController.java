package com.work.truetech.controller;

import com.google.zxing.qrcode.decoder.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.work.truetech.entity.Model;
import com.work.truetech.entity.Phone;
import com.work.truetech.repository.ModelRepository;
import com.work.truetech.services.IModelService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//@RequestMapping("/api/model")
//@CrossOrigin(origins="http://localhost:4200")
public class ModelController {
    @Autowired
    IModelService modelService;

    @Autowired
    ModelRepository modelRepository;




    @PostMapping("/admin/model/{phoneId}/add-model")
    @ResponseBody
    public ResponseEntity<?> createModel(@RequestParam("title") String title,
                                             @RequestParam("file") MultipartFile file, @PathVariable("phoneId") Long phoneId) {

        try {
            // Check if the model with the same name already exists
            if (modelRepository.findByTitle(title)!= null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Model name already exists, please choose a different one.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            Model model = new Model();
            model.setTitle(title);
            Model createdModel = modelService.createModel(model,phoneId ,file);
            return new ResponseEntity<>(createdModel, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/api/model/find-all-models")
    @ResponseBody
    public List<?> getModels() {
        List<Model> listModel = modelService.retrieveModels();
        return listModel;
    }

    @GetMapping("/api/model/find-models/{id}")
    @ResponseBody
    public List<?> getModelByPhoneName(@PathVariable("id") Long phoneId ) {
        List<Model> listModel = modelService.retrieveModelByPhone(phoneId);
        return listModel;
    }

    @GetMapping("/api/model/find-model/{modelId}")
    @ResponseBody
    public Model getModelById(@PathVariable("modelId") long modelId) {
        return  modelService.getModelById(modelId);
    }

    @PutMapping("/admin/model/update-model/{id}")
    @ResponseBody
    public ResponseEntity<?> updateModel(@PathVariable("id") Long modelId, @RequestParam("title") String title,
                             @RequestParam("file") MultipartFile file) {
        try {
            Model model = new Model();
            model.setTitle(title);
            Model updatedModel = modelService.updateModel(modelId,model, file);
            return new ResponseEntity<>(updatedModel, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/admin/model/delete-model/{id}")
    @ResponseBody
    public void deleteModel(@PathVariable("id") Long modelId) {
        modelService.deleteModel(modelId);
    }
}
