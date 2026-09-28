package com.retail.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.retail.loyalty.enums.PointsType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_ledger")
public class PointsLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_id")
    @JsonIgnore
    private Purchase purchase;

    @Column(nullable = false)
    private Long points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointsType type;

    private String description;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public PointsLedger() { }

    @PrePersist
    public void setCreationTime() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public Purchase getPurchase() { return purchase; }
    public void setPurchase(Purchase purchase) { this.purchase = purchase; }
    public Long getPoints() { return points; }
    public void setPoints(Long points) { this.points = points; }
    public PointsType getType() { return type; }
    public void setType(PointsType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
