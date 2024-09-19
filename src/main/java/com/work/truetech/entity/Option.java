package com.work.truetech.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`option`")
public class Option implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String image;
    private String description;
    private Long clientPrice;
    private Long supplierPrice;
    private int quantity;
    private Long reparation;
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Model model;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FactureOption> factureOptions = new ArrayList<>();
}
