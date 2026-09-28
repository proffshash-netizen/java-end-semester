package com.retail.loyalty.controller;

import com.retail.loyalty.entity.Customer;
import com.retail.loyalty.entity.PointsLedger;
import com.retail.loyalty.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@Tag(name = "Points")
public class PointsController {
    private final LoyaltyService loyaltyService;

    public PointsController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @PostMapping("/redeem/{customerId}")
    @Operation(summary = "Redeem available points")
    public Customer redeemPoints(@PathVariable Long customerId,
                                 @Valid @RequestBody RedeemRequest request) {
        return loyaltyService.redeemPoints(customerId, request.points());
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get current point balances")
    public Map<String, Object> getPoints(@PathVariable Long customerId) {
        return loyaltyService.getPoints(customerId);
    }

    @GetMapping("/{customerId}/history")
    @Operation(summary = "Get point transaction history")
    public List<PointsLedger> getHistory(@PathVariable Long customerId) {
        return loyaltyService.getPointsHistory(customerId);
    }

    public record RedeemRequest(@NotNull @Positive Long points) { }
}
