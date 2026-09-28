package com.retail.loyalty.controller;

import com.retail.loyalty.entity.Tier;
import com.retail.loyalty.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiers")
@Tag(name = "Tiers")
public class TierController {
    private final LoyaltyService loyaltyService;

    public TierController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping
    @Operation(summary = "Get all tiers")
    public List<Tier> getTiers() {
        return loyaltyService.getAllTiers();
    }

    @PostMapping
    @Operation(summary = "Create a tier")
    public Tier createTier(@Valid @RequestBody Tier tier) {
        return loyaltyService.createTier(tier);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a tier")
    public Tier updateTier(@PathVariable Long id, @Valid @RequestBody Tier tier) {
        return loyaltyService.updateTier(id, tier);
    }
}
