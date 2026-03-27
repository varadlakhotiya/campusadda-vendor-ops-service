package com.campusadda.vendorops.inventory.validator;

import com.campusadda.vendorops.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StockOperationValidatorTest {

    private final StockOperationValidator validator = new StockOperationValidator();

    @Test
    void shouldThrowWhenStockOutExceedsCurrentStock() {
        assertThrows(BusinessException.class, () ->
                validator.validateStockOutAllowed(
                        new BigDecimal("5"),
                        new BigDecimal("10")
                )
        );
    }
}