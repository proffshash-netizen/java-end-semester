package com.retail.loyalty.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnore
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal purchaseAmount;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;
    @Column(nullable = false)
    private Long pointsEarned;
    @Column(nullable = false)
    private Long pointsRedeemed = 0L;
    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    public Purchase() { }

    @PrePersist
    public void setPurchaseTime() {
        if (purchaseDate == null) purchaseDate = LocalDateTime.now();
        if (pointsRedeemed == null) pointsRedeemed = 0L;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public BigDecimal getPurchaseAmount() { return purchaseAmount; }
    public void setPurchaseAmount(BigDecimal purchaseAmount) { this.purchaseAmount = purchaseAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public Long getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Long pointsEarned) { this.pointsEarned = pointsEarned; }
    public Long getPointsRedeemed() { return pointsRedeemed; }
    public void setPointsRedeemed(Long pointsRedeemed) { this.pointsRedeemed = pointsRedeemed; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
}
