package com.retail.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Long availablePoints = 0L;

    @Column(nullable = false)
    private Long lifetimePoints = 0L;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private Tier tier;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Customer() { }

    @PrePersist
    public void setCreationTime() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (availablePoints == null) availablePoints = 0L;
        if (lifetimePoints == null) lifetimePoints = 0L;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(Long availablePoints) { this.availablePoints = availablePoints; }
    public Long getLifetimePoints() { return lifetimePoints; }
    public void setLifetimePoints(Long lifetimePoints) { this.lifetimePoints = lifetimePoints; }
    public Tier getTier() { return tier; }
    public void setTier(Tier tier) { this.tier = tier; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
