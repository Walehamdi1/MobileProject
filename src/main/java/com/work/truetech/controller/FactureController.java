package com.work.truetech.controller;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.dto.FactureListDTO;
import com.work.truetech.dto.OptionDTO;
import com.work.truetech.entity.Facture;
import com.work.truetech.entity.Status;
import com.work.truetech.entity.User;
import com.work.truetech.repository.FactureRepository;
import com.work.truetech.repository.UserRepository;
import com.work.truetech.services.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.util.Collections;
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

    @Autowired
    UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<Map<String,String>> createFacture(@RequestBody FactureDTO factureDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            factureService.createFacture(factureDTO);
            response.put("status", "success");
            response.put("message", "Facture ajoutée avec succès");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Échec de la création de la facture: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelFacture(@PathVariable Long id) {
        try {
        factureService.cancelFacture(id);
        return ResponseEntity.noContent().build();}
            catch (ResourceAccessException ex){
            throw new ResourceAccessException("Problème de réseau rencontré.");
        }
    }

    @GetMapping("/find")
    public List<Facture> retrieveAll(){
        return  factureRepository.findAll();
    }

    @GetMapping("/findpag")
    public ResponseEntity<Page<Facture>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Facture> invoices = factureService.getInvoices(page, size);
            return ResponseEntity.ok(invoices);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Problème de réseau rencontré lors de la récupération des factures.");
        } catch (Exception e) {
            throw new RuntimeException("Échec de la récupération des factures: " + e.getMessage(), e);
        }
    }


    @GetMapping("/total-count")
    public ResponseEntity<?> getTotalInvoiceCount() {
        long count = factureRepository.count();
        return ResponseEntity.ok().body(Collections.singletonMap("totalCount", count));
    }

    @GetMapping("/item/{id}")
    public ResponseEntity<Map<String, List<?>>> getFactureDetails(@PathVariable Long id) {
        try {
            Map<String, List<?>> factureDetails = factureService.getFactureById(id);
            return ResponseEntity.ok(factureDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", List.of(e.getMessage())));
        }
    }

    @GetMapping("/total-sum")
    public ResponseEntity<?> getTotalSumOfAllFactures() {
        try {
            double totalSum = factureService.calculateTotalSumOfAllFactures();
            Map<String, Double> response = Collections.singletonMap("totalSum", totalSum);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = Collections.singletonMap("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Facture> confirmFacture(@PathVariable Long id) {
        Facture updatedFacture = factureService.toggleFactureStatus(id);
        return ResponseEntity.ok(updatedFacture);
    }

    @GetMapping("/weekly-totals")
    public Map<LocalDate, Double> getWeeklyTotalsByDay() {
        return factureService.getWeeklyTotalByDay();
    }

    @GetMapping("/monthly-totals")
    public Map<String, Double> getMonthlyTotals() {
        return factureService.getMonthlyTotal();
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateFactureStatus(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String newStatusString = requestBody.get("newStatus");

        Map<String, String> response = new HashMap<>();
        try {
            Status newStatus = Status.valueOf(newStatusString);
            factureService.updateFactureStatus(id, newStatus);
            response.put("message", "Statut mis à jour avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", "Valeur d'état non valide");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/fetch")
    public ResponseEntity<Page<FactureListDTO>> getPaginatedFacturesWithUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<FactureListDTO> paginatedFactures = factureService.getAllFacturesWithUsers(page, size);
            return ResponseEntity.ok(paginatedFactures);
        } catch (ResourceAccessException ex) {
            throw new ResourceAccessException("Network issue encountered while retrieving invoices.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve invoices: " + e.getMessage(), e);
        }
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFacture() {
        factureService.deleteFacture();
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/history")
    public List<Facture> getFacturesForCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = null;

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userRepository.findByUsername(userDetails.getUsername());
                currentUserId = user.getId();
            }

            return factureService.getFacturesByUserId(currentUserId);
        } catch (ResourceAccessException ex) {
        throw new ResourceAccessException("Network issue encountered while retrieving invoices.");
    } catch (Exception e) {
        throw new RuntimeException("Failed to retrieve invoices: " + e.getMessage(), e);
    }
    }

}