package com.work.truetech.services;

import com.work.truetech.dto.FactureDTO;
import com.work.truetech.dto.FactureOptionDTO;
import com.work.truetech.entity.Facture;
import com.work.truetech.entity.FactureOption;
import com.work.truetech.entity.Option;
import com.work.truetech.repository.FactureOptionRepository;
import com.work.truetech.repository.FactureRepository;
import com.work.truetech.repository.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
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
    @Override
    public Facture createFacture(FactureDTO factureDTO) {
        // Create a new Facture entity
        Facture facture = new Facture();
        facture.setFullName(factureDTO.getFullName());
        facture.setPhone(factureDTO.getPhone());
        facture.setAddress(factureDTO.getAddress());
        facture.setReparationStatus(factureDTO.isReparationStatus());
        facture.setDeliveryStatus(factureDTO.isDeliveryStatus());
        facture.setDeliveryPrice(factureDTO.getLivraisonPrice());

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
}
