package com.work.truetech.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FactureOptionDTO {
    private Long optionId;
    private int quantity;

    public FactureOptionDTO(Long id) {
    }
}
