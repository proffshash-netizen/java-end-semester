package com.retail.loyalty.service;

import com.retail.loyalty.entity.*;
import com.retail.loyalty.enums.PointsType;
import com.retail.loyalty.exception.InsufficientPointsException;
import com.retail.loyalty.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {
    @Mock CustomerRepository customerRepository;
    @Mock PurchaseRepository purchaseRepository;
    @Mock PointsLedgerRepository pointsLedgerRepository;
    @Mock TierRepository tierRepository;
    @Mock SettingRepository settingRepository;

    private LoyaltyService loyaltyService;
    private Tier silver;
    private Tier gold;
    private Tier platinum;
    private Customer customer;

    @BeforeEach
    void setUp() {
        loyaltyService = new LoyaltyService(customerRepository, purchaseRepository,
                pointsLedgerRepository, tierRepository, settingRepository);
        silver = new Tier("Silver", 0L, new BigDecimal("5"));
        gold = new Tier("Gold", 500L, new BigDecimal("10"));
        platinum = new Tier("Platinum", 1000L, new BigDecimal("15"));
        customer = new Customer();
        customer.setAvailablePoints(0L);
        customer.setLifetimePoints(0L);
        customer.setTier(silver);
    }

    @Test
    void customerEarnsPointsFromPurchase() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(settingRepository.findBySettingKey("POINTS_PER_RUPEE"))
                .thenReturn(Optional.of(new Setting("POINTS_PER_RUPEE", "1")));
        when(tierRepository.findTopByMinimumPointsLessThanEqualOrderByMinimumPointsDesc(100L))
                .thenReturn(Optional.of(silver));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Purchase purchase = loyaltyService.createPurchase(1L, new BigDecimal("100"));

        assertEquals(100L, customer.getAvailablePoints());
        assertEquals(100L, customer.getLifetimePoints());
        assertEquals(100L, purchase.getPointsEarned());
        verify(pointsLedgerRepository).save(argThat(ledger -> ledger.getType() == PointsType.EARNED));
    }

    @Test
    void customerUpgradesFromSilverToGold() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(settingRepository.findBySettingKey("POINTS_PER_RUPEE"))
                .thenReturn(Optional.of(new Setting("POINTS_PER_RUPEE", "1")));
        when(tierRepository.findTopByMinimumPointsLessThanEqualOrderByMinimumPointsDesc(500L))
                .thenReturn(Optional.of(gold));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loyaltyService.createPurchase(1L, new BigDecimal("500"));

        assertSame(gold, customer.getTier());
    }

    @Test
    void customerCannotRedeemMoreThanAvailablePoints() {
        customer.setAvailablePoints(100L);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(InsufficientPointsException.class,
                () -> loyaltyService.redeemPoints(1L, 101L));
        verify(pointsLedgerRepository, never()).save(any());
    }
}
