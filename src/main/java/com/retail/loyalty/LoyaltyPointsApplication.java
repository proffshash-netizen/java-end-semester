package com.retail.loyalty;

import com.retail.loyalty.entity.Setting;
import com.retail.loyalty.entity.Tier;
import com.retail.loyalty.repository.SettingRepository;
import com.retail.loyalty.repository.TierRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class LoyaltyPointsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoyaltyPointsApplication.class, args);
    }

    @Bean
    CommandLineRunner initializeDefaultData(TierRepository tierRepository, SettingRepository settingRepository) {
        return args -> {
            createTierIfMissing(tierRepository, "Silver", 0L, "5");
            createTierIfMissing(tierRepository, "Gold", 500L, "10");
            createTierIfMissing(tierRepository, "Platinum", 1000L, "15");
            if (settingRepository.findBySettingKey("POINTS_PER_RUPEE").isEmpty()) {
                settingRepository.save(new Setting("POINTS_PER_RUPEE", "1"));
            }
        };
    }

    private void createTierIfMissing(TierRepository tierRepository, String name, long minimumPoints,
                                     String discountPercentage) {
        if (tierRepository.findByName(name).isEmpty()) {
            tierRepository.save(new Tier(name, minimumPoints, new BigDecimal(discountPercentage)));
        }
    }
}
