package com.work.truetech.controller;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.dto.OptionDTO;
import com.work.truetech.entity.Facture;
import com.work.truetech.repository.FactureRepository;
import com.work.truetech.services.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/factures")
public class FactureController {

    @Autowired
    private FactureService factureService;

    @Autowired
    private FactureRepository factureRepository;

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

    @GetMapping("/find")
    public List<Facture> retrieveAll(){
        return  factureRepository.findAll();
    }

    @GetMapping("/findpag")
    public Page<Facture> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return factureService.getInvoices(page, size);
    }

    @GetMapping("/total-count")
    public long getTotalInvoiceCount() {
        return factureRepository.count();
    }

    @GetMapping("/item/{id}")
    public List<OptionDTO> getFactureDetails(@PathVariable Long id) {
        return factureService.getFactureById(id);
    }
}