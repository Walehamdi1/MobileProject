package com.work.truetech.dto;

import com.work.truetech.entity.SousCategorie;
import lombok.Getter;

@Getter
public class SousCategorieRequest {
    // Getter and Setter
    private SousCategorie sousCategorie;

    public void setSousCategorie(SousCategorie sousCategorie) {
        this.sousCategorie = sousCategorie;
    }
}
