package com.work.truetech.services;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.dto.FactureOptionDTO;
import com.work.truetech.dto.OptionDTO;
import com.work.truetech.entity.*;
import com.work.truetech.repository.FactureOptionRepository;
import com.work.truetech.repository.FactureRepository;
import com.work.truetech.repository.OptionRepository;
import com.work.truetech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FactureService implements IFactureService{
    @Autowired
    FactureRepository factureRepository;
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    FactureOptionRepository factureOptionRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateUniqueCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    @Autowired
    UserRepository userRepository;
    @Override
    public Facture createFacture(FactureDTO factureDTO) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByUsername(userDetails.getUsername());
            if (user.getRole() == Role.SUPPLIER) {
                currentUserId = user.getId();
            }
        }


        // Create a new Facture entity
        Facture facture = new Facture();
        facture.setFullName(factureDTO.getFullName());
        facture.setPhone(factureDTO.getPhone());
        facture.setAddress(factureDTO.getAddress());
        facture.setReparationStatus(factureDTO.isReparationStatus());
        facture.setDeliveryStatus(factureDTO.isDeliveryStatus());
        facture.setDeliveryPrice(factureDTO.getLivraisonPrice());
        facture.setUserId(currentUserId);

        double totalCost = factureDTO.getLivraisonPrice(); // Start with the delivery price

        // Generate and set a unique 8-character code for the Facture
        String uniqueCode = generateUniqueCode();
        facture.setCode(uniqueCode);

        // Save the facture first to generate the ID
        factureRepository.save(facture);

        // Create FactureOption entities and calculate total cost
        for (FactureOptionDTO optionDTO : factureDTO.getOptions()) {
            Option option = optionRepository.findById(optionDTO.getOptionId())
                    .orElseThrow(() -> new RuntimeException("Option not found"));

            // Reduce the option's quantity by the quantity specified in the DTO
            int remainingQuantity = option.getQuantity() - optionDTO.getQuantity();
            if (remainingQuantity < 0) {
                throw new RuntimeException("Not enough quantity available for option: " + option.getTitle());
            }
            option.setQuantity(remainingQuantity);

            // Save the updated option back to the repository
            optionRepository.save(option);

            FactureOption factureOption = new FactureOption();
            factureOption.setFacture(facture);
            factureOption.setOption(option);
            factureOption.setQuantity(optionDTO.getQuantity());

            // Save the FactureOption entity
            factureOptionRepository.save(factureOption);

            // Calculate the total cost: clientPrice + reparation (multiplied by quantity)
            totalCost += (option.getClientPrice() + option.getReparation()) * optionDTO.getQuantity();

            // Add the FactureOption to the Facture's list
            facture.getFactureOptions().add(factureOption);
        }

        // Set the calculated total cost
        facture.setTotal(totalCost);

        // Save the facture again with its options and the calculated total
        return factureRepository.save(facture);
    }

    @Override
    public List<Facture> retrieveAllFacture() {
        return factureRepository.findAll();
    }

    @Override
    public void cancelFacture(Long factureId) {
        // Retrieve the facture
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture not found"));

        // Restore the quantity of each option associated with the facture
        for (FactureOption factureOption : facture.getFactureOptions()) {
            Option option = factureOption.getOption();
            int restoredQuantity = option.getQuantity() + factureOption.getQuantity();
            option.setQuantity(restoredQuantity);

            // Save the updated option back to the repository
            optionRepository.save(option);
        }

        // Delete all facture options associated with the facture
        factureOptionRepository.deleteAll(facture.getFactureOptions());

        // Delete the facture itself
        factureRepository.delete(facture);
    }

    public Page<Facture> getInvoices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);


        return factureRepository.findAll(pageable);
    }
    public List<OptionDTO> getFactureById(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture not found with id: " + id));

        List<OptionDTO> dtoList = new ArrayList<>();
        for (FactureOption factureOption : facture.getFactureOptions()) {
            Long optionId = factureOption.getOption().getId(); // Assuming FactureOption has a reference to Option directly.
            Option option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new RuntimeException("Option not found with id: " + optionId));

            OptionDTO opt = new OptionDTO();
            opt.setQuantity(factureOption.getQuantity());
            opt.setTitle(option.getTitle());
            dtoList.add(opt);
        }
        return dtoList;
    }

}
