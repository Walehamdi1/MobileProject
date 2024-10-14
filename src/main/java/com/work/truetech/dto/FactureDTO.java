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
    private boolean reparationStatus;
    private boolean deliveryStatus;
    private double livraisonPrice;
    private List<String> questions;
    private List<FactureOptionDTO> options;
    private List<FactureProductDto> products;
}
