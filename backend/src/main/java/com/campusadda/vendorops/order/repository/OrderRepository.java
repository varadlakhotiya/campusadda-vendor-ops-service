package com.campusadda.vendorops.order.repository;

import com.campusadda.vendorops.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;






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

    List<Order> findByPlacedAtBetween(
        LocalDateTime start,
        LocalDateTime end
);

    @Query("""
SELECT COUNT(o)
FROM Order o
WHERE o.vendor.id = :vendorId
AND o.placedAt BETWEEN :startDate AND :endDate
""")
Long countTotalOrders(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
);

@Query("""
SELECT COUNT(o)
FROM Order o
WHERE o.vendor.id = :vendorId
AND o.status = 'COMPLETED'
AND o.placedAt BETWEEN :startDate AND :endDate
""")
Long countCompletedOrders(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
);

@Query("""
SELECT COUNT(o)
FROM Order o
WHERE o.vendor.id = :vendorId
AND o.status = 'CANCELLED'
AND o.placedAt BETWEEN :startDate AND :endDate
""")
Long countCancelledOrders(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
);

@Query("""
SELECT COALESCE(SUM(o.totalAmount), 0)
FROM Order o
WHERE o.vendor.id = :vendorId
AND o.status = 'COMPLETED'
AND o.placedAt BETWEEN :startDate AND :endDate
""")
BigDecimal getCompletedRevenue(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
);
}