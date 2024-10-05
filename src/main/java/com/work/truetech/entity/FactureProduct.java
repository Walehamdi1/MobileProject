package com.work.truetech.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FactureProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "facture_id", nullable = false)
    @JsonIgnore
    private Facture facture;
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;
}
