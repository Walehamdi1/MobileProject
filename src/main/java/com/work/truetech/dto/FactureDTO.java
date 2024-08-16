package com.work.truetech.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FactureDTO {
    private String fullName;
    private int phone;
    private String address;
    private boolean reparationStatus;
    private boolean deliveryStatus;
    private double livraisonPrice;
    private List<FactureOptionDTO> options;
}
