package com.work.truetech.dto;

import com.work.truetech.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FactureListDTO {
    private Long id;
    private String username;
    private LocalDateTime creationDate;
    private Long phone;
    private String address;
    private String code;
    private double total;
    private boolean factureStatus;
    private double deliveryPrice;
    private Status status;
    private boolean reparationStatus;
    private boolean deliveryStatus;
    private double livraisonPrice;
    private String role;
    private List<String> questions;
    private List<FactureOptionDTO> options;
    private List<FactureProductDto> products;

}
