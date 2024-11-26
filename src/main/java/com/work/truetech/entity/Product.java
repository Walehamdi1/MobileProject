package com.work.truetech.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String image;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> colors = new ArrayList<>();
    private int quantity;
    private int price;
    //@Enumerated(EnumType.STRING)
    //private SousCategorie sousCategorie;
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FactureProduct> factureProducts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private User user;

}
