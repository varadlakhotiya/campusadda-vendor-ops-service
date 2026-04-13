package com.campusadda.vendorops.order.repository;

import com.campusadda.vendorops.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndVendor_Id(Long id, Long vendorId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByOrderNumberIgnoreCase(String orderNumber);

    List<Order> findByVendor_IdOrderByPlacedAtDesc(Long vendorId);

    List<Order> findByVendor_IdAndStatusOrderByPlacedAtDesc(Long vendorId, String status);

    List<Order> findByExternalCustomerIdOrderByPlacedAtDesc(String externalCustomerId);

    List<Order> findByExternalCustomerIdAndStatusInOrderByPlacedAtDesc(
            String externalCustomerId,
            Collection<String> statuses
    );

    Optional<Order> findByIdAndExternalCustomerId(Long id, String externalCustomerId);

    List<Order> findByCustomerPhoneAndExternalCustomerIdIsNullOrderByPlacedAtDesc(String customerPhone);

    long countByVendor_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long vendorId,
            LocalDateTime start,
            LocalDateTime end
    );
}