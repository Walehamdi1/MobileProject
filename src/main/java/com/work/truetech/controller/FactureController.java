package com.work.truetech.controller;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.entity.Facture;
import com.work.truetech.services.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/factures")
public class FactureController {

    @Autowired
    private FactureService factureService;

    @PostMapping("/create")
    public ResponseEntity<String> createFacture(@RequestBody FactureDTO factureDTO) {
        factureService.createFacture(factureDTO);
        return new ResponseEntity<>("Facture successfully added", HttpStatus.CREATED);
    }
}