package com.retail.loyalty.controller;

import com.retail.loyalty.entity.Purchase;
import com.retail.loyalty.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/purchases")
@Tag(name = "Purchases")
public class PurchaseController {
    private final LoyaltyService loyaltyService;

    public PurchaseController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @PostMapping("/{customerId}")
    @Operation(summary = "Create a purchase and earn points")
    public Purchase createPurchase(@PathVariable Long customerId,
                                   @Valid @RequestBody PurchaseRequest request) {
        return loyaltyService.createPurchase(customerId, request.purchaseAmount());
    }

    public record PurchaseRequest(@NotNull @Positive BigDecimal purchaseAmount) { }
}
