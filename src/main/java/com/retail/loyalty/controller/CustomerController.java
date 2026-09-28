package com.retail.loyalty.controller;

import com.retail.loyalty.entity.Customer;
import com.retail.loyalty.service.LoyaltyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers")
public class CustomerController {
    private final LoyaltyService loyaltyService;

    public CustomerController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a customer")
    public Customer createCustomer(@Valid @RequestBody CustomerRequest request) {
        return loyaltyService.createCustomer(request.name(), request.email());
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public List<Customer> getCustomers() {
        return loyaltyService.getAllCustomers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by ID")
    public Customer getCustomer(@PathVariable Long id) {
        return loyaltyService.getCustomer(id);
    }

    public record CustomerRequest(@NotBlank String name, @NotBlank @Email String email) { }
}
