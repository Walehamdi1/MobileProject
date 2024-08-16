package com.work.truetech.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Phone implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String image;

    @OneToMany(cascade = CascadeType.ALL,mappedBy="phone")
    @JsonIgnore
    private Set<Model> models = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private User user;

}
