package com.campusadda.vendorops.inventory.validator;

import com.campusadda.vendorops.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockOperationValidator {

    public void validatePositiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be greater than zero");
        }
    }

    public void validateStockOutAllowed(BigDecimal currentQuantity, BigDecimal stockOutQty) {
        if (currentQuantity.subtract(stockOutQty).compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient stock for this operation");
        }
    }
}