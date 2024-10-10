package com.work.truetech.services;
import com.work.truetech.dto.FactureDTO;
import com.work.truetech.dto.FactureOptionDTO;
import com.work.truetech.dto.FactureProductDto;
import com.work.truetech.dto.OptionDTO;
import com.work.truetech.entity.*;
import com.work.truetech.repository.*;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.LinkedHashMap;
import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FactureService implements IFactureService{
    @Autowired
    FactureRepository factureRepository;
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    FactureOptionRepository factureOptionRepository;
    @Autowired
    FactureProductRepository factureProductRepository;
    @Autowired
    private JavaMailSender mailSender;

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

                currentUserId = user.getId();

        }

        // Create a new Facture entity
        Facture facture = new Facture();
        facture.setFullName(factureDTO.getFullName());
        facture.setPhone(factureDTO.getPhone());
        facture.setAddress(factureDTO.getAddress());
        facture.setReparationStatus(factureDTO.isReparationStatus());
        facture.setDeliveryStatus(factureDTO.isDeliveryStatus());
        facture.setDeliveryPrice(factureDTO.getLivraisonPrice());
        facture.setQuestions(factureDTO.getQuestions());
        System.out.println("Questions: " + facture.getQuestions());
        facture.setUserId(currentUserId);
        facture.setFactureStatus(false);
        facture.setStatus(Status.Pending);

        double totalCost = factureDTO.getLivraisonPrice(); // Start with the delivery price

        // Generate and set a unique 8-character code for the Facture
        String uniqueCode = generateUniqueCode();
        facture.setCode(uniqueCode);

        // Save the facture first to generate the ID
        factureRepository.save(facture);

        // Create FactureOption entities and calculate total cost
        if (factureDTO.getOptions() != null && !factureDTO.getOptions().isEmpty()) {
            for (FactureOptionDTO optionDTO : factureDTO.getOptions()) {
                Option option = optionRepository.findById(optionDTO.getOptionId())
                        .orElseThrow(() -> new RuntimeException("Option non trouvée"));

                // Reduce the option's quantity by the quantity specified in the DTO
                int remainingQuantity = option.getQuantity() - optionDTO.getQuantity();
                if (remainingQuantity < 0) {
                    throw new RuntimeException("Quantité insuffisante disponible pour l'option: " + option.getTitle());
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

                // Calculate the total cost: clientPrice (multiplied by quantity)
                // Include reparation cost only if reparationStatus is true
                double optionCost = option.getClientPrice() * optionDTO.getQuantity();
                if (factureDTO.isReparationStatus()) {
                    optionCost += option.getReparation() * optionDTO.getQuantity();
                }

                totalCost += optionCost;

                // Add the FactureOption to the Facture's list
                facture.getFactureOptions().add(factureOption);
            }
        }

        // Loop over FactureProduct entities and calculate total cost
        if (factureDTO.getProducts() != null && !factureDTO.getProducts().isEmpty()) {
            for (FactureProductDto productDTO : factureDTO.getProducts()) {
                Product product = productRepository.findById(productDTO.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                int remainingQuantity = product.getQuantity() - productDTO.getQuantity();
                if (remainingQuantity < 0) {
                    throw new RuntimeException("Insufficient quantity available for product: " + product.getTitle());
                }
                product.setQuantity(remainingQuantity);
                productRepository.save(product);

                FactureProduct factureProduct = new FactureProduct();
                factureProduct.setFacture(facture);
                factureProduct.setProduct(product);
                factureProduct.setQuantity(productDTO.getQuantity());
                factureProductRepository.save(factureProduct);

                double productCost = product.getPrice() * productDTO.getQuantity();
                totalCost += productCost;

                facture.getFactureProducts().add(factureProduct);
            }
        }

        sendEmailNotification(facture);
        // Set the calculated total cost
        facture.setTotal(totalCost);
        // Save the facture again with its options and the calculated total
        return factureRepository.save(facture);
    }
    private void sendEmailNotification(Facture facture) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("walahamdi0@gmail.com");
        message.setSubject("New Facture Created: " + facture.getCode());

        // Build the facture details text
        StringBuilder emailText = new StringBuilder();
        emailText.append("A new facture has been created.\n\n")
                .append("Facture Details:\n")
                .append("Full Name: ").append(facture.getFullName()).append("\n")
                .append("Phone: ").append(facture.getPhone()).append("\n")
                .append("Address: ").append(facture.getAddress()).append("\n")
                .append("Code: ").append(facture.getCode()).append("\n\n");

        // Append the list of questions if available
        if (!facture.getQuestions().isEmpty()) {
            emailText.append("Questions:\n");
            for (String question : facture.getQuestions()) {
                emailText.append("- ").append(question).append("\n");
            }
        }

        // Set the text of the message
        message.setText(emailText.toString());

        // Send the email
        mailSender.send(message);
    }


    @Override
    public List<Facture> retrieveAllFacture() {
        return factureRepository.findAll();
    }

    @Override
    public void cancelFacture(Long factureId) {
        // Retrieve the facture
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

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
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'identifiant: " + id));

        List<OptionDTO> dtoList = new ArrayList<>();
        for (FactureOption factureOption : facture.getFactureOptions()) {
            Long optionId = factureOption.getOption().getId(); // Assuming FactureOption has a reference to Option directly.
            Option option = optionRepository.findById(optionId)
                    .orElseThrow(() -> new RuntimeException("Option non trouvée avec l'identifiant: " + optionId));

            OptionDTO opt = new OptionDTO();
            opt.setQuantity(factureOption.getQuantity());
            opt.setTitle(option.getTitle());
            opt.setReparation(option.getReparation());
            if (facture.getUserId() == null) {
                opt.setPrice(option.getClientPrice());
            } else {
                opt.setPrice(option.getSupplierPrice());
            }
            dtoList.add(opt);
        }
        return dtoList;
    }

    @Override
    public double calculateTotalSumOfAllFactures() {
        List<Facture> factures = factureRepository.findAll();
        return factures.stream()
                .mapToDouble(Facture::getTotal)
                .sum();
    }
    @Override
    public long countAllFactures() {
        return factureRepository.count();
    }

    @Override
    public Facture toggleFactureStatus(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture not trouvé avec  id: " + factureId));

        facture.setFactureStatus(!facture.isFactureStatus());
        return factureRepository.save(facture);
    }

    @Override
    public Facture updateFactureStatus(Long factureId, Status newStatus) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture not trouvé avec id: " + factureId));

        facture.setStatus(newStatus);
        return factureRepository.save(facture);
    }

    public Map<LocalDate, Double> getWeeklyTotalByDay() {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // Fetch factures within the date range using LocalDateTime
        List<Facture> factures = factureRepository.findAllByCreationDateBetween(
                startOfWeek.atStartOfDay(),
                endOfWeek.atTime(23, 59, 59)
        );

        // Group by LocalDate and sort by date in ascending order using a LinkedHashMap
        return factures.stream()
                .collect(Collectors.groupingBy(
                        facture -> facture.getCreationDate().toLocalDate(),
                        LinkedHashMap::new,  // Ensure insertion order is maintained
                        Collectors.summingDouble(Facture::getTotal)
                ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey()) // Sort by date (LocalDate) key
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,  // Merge function (not used here)
                        LinkedHashMap::new  // Maintain order in the resulting map
                ));
    }

    public Map<String, Double> getMonthlyTotal() {
        LocalDate startOfYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
        LocalDate endOfYear = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

        List<Facture> factures = factureRepository.findAllByCreationDateBetween(
                startOfYear.atStartOfDay(),
                endOfYear.atTime(23, 59, 59)
        );

        return factures.stream()
                .collect(Collectors.groupingBy(
                        facture -> facture.getCreationDate().getYear() + "-" + String.format("%02d", facture.getCreationDate().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.summingDouble(Facture::getTotal)
                ));
    }



}
