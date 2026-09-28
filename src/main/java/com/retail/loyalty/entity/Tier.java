package com.retail.loyalty.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Entity
@Table(name = "tiers")
public class Tier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Long minimumPoints;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    public Tier() { }

    public Tier(String name, Long minimumPoints, BigDecimal discountPercentage) {
        this.name = name;
        this.minimumPoints = minimumPoints;
        this.discountPercentage = discountPercentage;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getMinimumPoints() { return minimumPoints; }
    public void setMinimumPoints(Long minimumPoints) { this.minimumPoints = minimumPoints; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
}
