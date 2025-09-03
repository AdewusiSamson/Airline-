package com.example.Airline_Project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Random;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 3)
    private String code;

    @Column(nullable = false)
    private String name;

    private String city;
    private String country;

    @PrePersist
    public void generateCode() {
        if (this.code == null || this.code.isEmpty()) {
            this.code = "AIR" + new Random().nextInt(10000);
        }
    }
}