package com.work.truetech.controller;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.services.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/factures")
public class FactureController {

    @Autowired
    private FactureService factureService;

    @PostMapping("/create")
    public ResponseEntity<Map<String,String>> createFacture(@RequestBody FactureDTO factureDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            factureService.createFacture(factureDTO);
            response.put("status", "success");
            response.put("message", "Facture successfully added");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to create facture: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelFacture(@PathVariable Long id) {
        try {
        factureService.cancelFacture(id);
        return ResponseEntity.noContent().build();}
            catch (ResourceAccessException ex){
            throw new ResourceAccessException("Network issue encountered.");
        }
    }
}