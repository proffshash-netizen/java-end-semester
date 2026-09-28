package com.retail.loyalty.repository;

import com.retail.loyalty.entity.PointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
    List<PointsLedger> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
