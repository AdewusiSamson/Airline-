package com.example.Airline_Project.Repository;

import com.example.Airline_Project.model.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository  extends JpaRepository<PaymentOrder,Long> {
    Optional<PaymentOrder> findByReference(String reference);

}
