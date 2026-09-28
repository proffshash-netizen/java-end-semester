package com.retail.loyalty.service;

import com.retail.loyalty.entity.*;
import com.retail.loyalty.enums.PointsType;
import com.retail.loyalty.exception.InsufficientPointsException;
import com.retail.loyalty.exception.ResourceNotFoundException;
import com.retail.loyalty.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoyaltyService {
    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final TierRepository tierRepository;
    private final SettingRepository settingRepository;

    public LoyaltyService(CustomerRepository customerRepository, PurchaseRepository purchaseRepository,
                          PointsLedgerRepository pointsLedgerRepository, TierRepository tierRepository,
                          SettingRepository settingRepository) {
        this.customerRepository = customerRepository;
        this.purchaseRepository = purchaseRepository;
        this.pointsLedgerRepository = pointsLedgerRepository;
        this.tierRepository = tierRepository;
        this.settingRepository = settingRepository;
    }

    @Transactional
    public Customer createCustomer(String name, String email) {
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setAvailablePoints(0L);
        customer.setLifetimePoints(0L);
        customer.setTier(getTierByName("Silver"));
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    @Transactional
    public Purchase createPurchase(Long customerId, BigDecimal purchaseAmount) {
        Customer customer = getCustomer(customerId);
        Tier currentTier = customer.getTier();
        BigDecimal discountAmount = purchaseAmount.multiply(currentTier.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        long pointsEarned = purchaseAmount.multiply(getPointsPerRupee()).longValue();
        customer.setAvailablePoints(customer.getAvailablePoints() + pointsEarned);
        customer.setLifetimePoints(customer.getLifetimePoints() + pointsEarned);
        updateTier(customer);

        Purchase purchase = new Purchase();
        purchase.setCustomer(customer);
        purchase.setPurchaseAmount(purchaseAmount);
        purchase.setDiscountAmount(discountAmount);
        purchase.setFinalAmount(purchaseAmount.subtract(discountAmount));
        purchase.setPointsEarned(pointsEarned);
        Purchase savedPurchase = purchaseRepository.save(purchase);

        PointsLedger ledger = new PointsLedger();
        ledger.setCustomer(customer);
        ledger.setPurchase(savedPurchase);
        ledger.setPoints(pointsEarned);
        ledger.setType(PointsType.EARNED);
        ledger.setDescription("Points earned from purchase");
        pointsLedgerRepository.save(ledger);
        return savedPurchase;
    }

    @Transactional
    public Customer redeemPoints(Long customerId, Long points) {
        Customer customer = getCustomer(customerId);
        if (points <= 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        if (points > customer.getAvailablePoints()) {
            throw new InsufficientPointsException("Insufficient points");
        }
        customer.setAvailablePoints(customer.getAvailablePoints() - points);
        Customer savedCustomer = customerRepository.save(customer);

        PointsLedger ledger = new PointsLedger();
        ledger.setCustomer(customer);
        ledger.setPoints(points);
        ledger.setType(PointsType.REDEEMED);
        ledger.setDescription("Points redeemed");
        pointsLedgerRepository.save(ledger);
        return savedCustomer;
    }

    public Map<String, Object> getPoints(Long customerId) {
        Customer customer = getCustomer(customerId);
        Map<String, Object> points = new LinkedHashMap<>();
        points.put("customerId", customer.getId());
        points.put("availablePoints", customer.getAvailablePoints());
        points.put("lifetimePoints", customer.getLifetimePoints());
        points.put("tier", customer.getTier().getName());
        return points;
    }

    public List<PointsLedger> getPointsHistory(Long customerId) {
        getCustomer(customerId);
        return pointsLedgerRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Tier> getAllTiers() {
        return tierRepository.findAll();
    }

    public Tier getTier(Long tierId) {
        return tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + tierId));
    }

    public Tier createTier(Tier tier) {
        return tierRepository.save(tier);
    }

    public Tier updateTier(Long tierId, Tier updatedTier) {
        Tier tier = getTier(tierId);
        tier.setName(updatedTier.getName());
        tier.setMinimumPoints(updatedTier.getMinimumPoints());
        tier.setDiscountPercentage(updatedTier.getDiscountPercentage());
        return tierRepository.save(tier);
    }

    private Tier getTierByName(String name) {
        return tierRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Default tier not found: " + name));
    }

    private BigDecimal getPointsPerRupee() {
        Setting setting = settingRepository.findBySettingKey("POINTS_PER_RUPEE")
                .orElseThrow(() -> new ResourceNotFoundException("POINTS_PER_RUPEE setting not found"));
        return new BigDecimal(setting.getSettingValue());
    }

    private void updateTier(Customer customer) {
        Tier tier = tierRepository.findTopByMinimumPointsLessThanEqualOrderByMinimumPointsDesc(customer.getLifetimePoints())
                .orElseGet(() -> getTierByName("Silver"));
        if (tier.getMinimumPoints() >= customer.getTier().getMinimumPoints()) {
            customer.setTier(tier);
        }
    }
}
