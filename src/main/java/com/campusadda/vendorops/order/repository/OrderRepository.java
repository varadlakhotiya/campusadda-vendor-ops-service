package com.campusadda.vendorops.order.repository;

import com.campusadda.vendorops.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndVendor_Id(Long id, Long vendorId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByVendor_IdOrderByPlacedAtDesc(Long vendorId);
    List<Order> findByVendor_IdAndStatusOrderByPlacedAtDesc(Long vendorId, String status);
}