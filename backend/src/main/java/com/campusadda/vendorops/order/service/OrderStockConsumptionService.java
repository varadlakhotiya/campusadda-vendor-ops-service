package com.campusadda.vendorops.order.service;

import com.campusadda.vendorops.order.dto.response.StockConsumptionPreviewResponse;
import com.campusadda.vendorops.order.entity.Order;

public interface OrderStockConsumptionService {
    StockConsumptionPreviewResponse previewStockConsumption(Long orderId);
    void consumeStock(Order order);
    void reverseStock(Order order);
}