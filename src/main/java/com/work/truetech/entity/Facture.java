package com.work.truetech.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private int phone;
    private String address;
    private boolean reparationStatus;
    private boolean factureStatus;
    private boolean deliveryStatus;
    private double deliveryPrice;
    private double total;

    @Column(name = "user_id")
    private Long userId;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = true)
    private LocalDateTime creationDate = LocalDateTime.now();


    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<FactureOption> factureOptions = new ArrayList<>();
}
