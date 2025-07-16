package com.anistebbal.starter.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Street {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    @JsonBackReference // 👈 Add this
    private District district;

    @OneToMany(mappedBy = "street", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<User> users;

    @OneToMany(mappedBy = "street", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Report> reports;
}
