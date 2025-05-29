package com.example.paymentservice.repository;

import com.example.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
    SELECT p FROM Payment p
    WHERE p.isCanceled = false AND (:amountMin IS NULL OR p.amount >= :amountMin)
    """)
    List<Payment> findActivePaymentsWithOptionalMinAmount(@Param("amountMin") BigDecimal amountMin);
}