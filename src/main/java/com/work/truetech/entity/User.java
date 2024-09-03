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
    public class User implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String username;
        private String email;
        private String password;
        private Role role;
        private int phone;
        private boolean valid;

        @JsonIgnore
        @OneToMany(cascade = CascadeType.ALL,mappedBy="user")
        private Set<Phone> phones = new HashSet<>();

    }
