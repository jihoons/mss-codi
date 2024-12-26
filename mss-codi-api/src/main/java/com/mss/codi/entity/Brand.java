package com.mss.codi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "brand", indexes = {@Index(unique = true, name = "unique_brand_by_name", columnList = "name")})
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
}
