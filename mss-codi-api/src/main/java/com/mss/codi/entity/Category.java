package com.mss.codi.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "category", indexes = {@Index(unique = true, name = "unique_category_by_name", columnList = "name")})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "display_order")
    private int displayOrder;
}
