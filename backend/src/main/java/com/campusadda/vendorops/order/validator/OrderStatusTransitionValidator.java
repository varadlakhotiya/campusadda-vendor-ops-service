package com.campusadda.vendorops.order.validator;

import com.campusadda.vendorops.common.enums.OrderStatus;
import com.campusadda.vendorops.common.exception.ConflictException;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusTransitionValidator {

    public void validateTransition(String fromStatus, String toStatus) {
        if (fromStatus == null) {
            return;
        }

        if (fromStatus.equals(toStatus)) {
            throw new ConflictException("Order is already in status: " + toStatus);
        }

        boolean valid = switch (OrderStatus.valueOf(fromStatus)) {
            case CREATED -> toStatus.equals(OrderStatus.ACCEPTED.name()) || toStatus.equals(OrderStatus.CANCELLED.name());
            case ACCEPTED -> toStatus.equals(OrderStatus.PREPARING.name()) || toStatus.equals(OrderStatus.CANCELLED.name());
            case PREPARING -> toStatus.equals(OrderStatus.READY.name()) || toStatus.equals(OrderStatus.CANCELLED.name());
            case READY -> toStatus.equals(OrderStatus.COMPLETED.name());
            case COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new ConflictException("Invalid status transition from " + fromStatus + " to " + toStatus);
        }
    }
}