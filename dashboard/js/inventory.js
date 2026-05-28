requireAuth();

let latestInventoryItems = [];
let latestLowStockItems = [];
let latestReorderRecommendations = [];

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("inventory");
    bindInventoryEvents();

    await VendorScope.initVendorSelect("vendorSelect", async () => {
      await handleVendorChange();
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load inventory page", "error");
  }
});

function bindInventoryEvents() {
  document.getElementById("inventoryForm")?.addEventListener("submit", handleCreateInventoryItem);
  document.getElementById("stockInBtn")?.addEventListener("click", stockIn);
  document.getElementById("stockOutBtn")?.addEventListener("click", stockOut);
  document.getElementById("adjustStockBtn")?.addEventListener("click", adjustStock);
  document.getElementById("refreshInventoryInsightsBtn")?.addEventListener("click", refreshInventoryScreen);
  document.getElementById("generateInventoryRecommendationsBtn")?.addEventListener("click", generateRecommendations);
}

async function handleVendorChange() {
  await refreshInventoryScreen();
}

async function getSelectedVendorId() {
  let vendorId = await VendorScope.getVendorId("vendorSelect");

  if (!vendorId) {
    const ctx = await Portal.getContext();
    vendorId = ctx?.primaryVendorId ? Number(ctx.primaryVendorId) : null;
  }

  return vendorId;
}

async function handleCreateInventoryItem(e) {
  e.preventDefault();

  const vendorId = await getSelectedVendorId();
  if (!vendorId) {
    Utils.showMessage("Select a vendor", "error");
    return;
  }

  try {
    await Api.post(`/vendors/${vendorId}/inventory-items`, {
      itemCode: document.getElementById("itemCode").value.trim(),
      itemName: document.getElementById("itemName").value.trim(),
      unit: document.getElementById("unit").value.trim(),
      currentQuantity: document.getElementById("currentQuantity").value
        ? Number(document.getElementById("currentQuantity").value)
        : 0,
      lowStockThreshold: document.getElementById("lowStockThreshold").value
        ? Number(document.getElementById("lowStockThreshold").value)
        : 0,
      unitCost: document.getElementById("unitCost").value
        ? Number(document.getElementById("unitCost").value)
        : null,
      status: "ACTIVE",
      sourceSystem: "VENDOR_OPS"
    });

    Utils.showMessage("Inventory item created");
    e.target.reset();
    await refreshInventoryScreen();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to create inventory item", "error");
  }
}

async function refreshInventoryScreen() {
  await Promise.all([
    loadInventory(),
    loadLowStock(),
    loadReorderRecommendations()
  ]);
  populateOperationItemSelect();
  renderInventorySummary();
  renderInventoryRiskCards();
}

async function loadInventory() {
  const vendorId = await getSelectedVendorId();
  const body = document.getElementById("inventoryBody");

  if (!body) return;

  if (!vendorId) {
    latestInventoryItems = [];
    body.innerHTML = `<tr><td colspan="7">Select a vendor to view inventory</td></tr>`;
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/inventory-items`);
    const items = response.data || [];
    latestInventoryItems = items;

    body.innerHTML = "";

    if (items.length === 0) {
      body.innerHTML = `<tr><td colspan="7">No inventory items found</td></tr>`;
      return;
    }

    items.forEach((item) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${item.id ?? "-"}</td>
        <td>${Utils.escapeHtml(item.itemCode ?? "-")}</td>
        <td>${Utils.escapeHtml(item.itemName ?? "-")}</td>
        <td>${Utils.formatNumber(item.currentQuantity ?? 0)} ${Utils.escapeHtml(item.unit ?? "-")}</td>
        <td>${Utils.formatNumber(item.lowStockThreshold ?? 0)}</td>
        <td>${item.unitCost != null ? Utils.formatCurrency(item.unitCost) : "-"}</td>
        <td><span class="pill pill-neutral">${Utils.escapeHtml(item.status ?? "-")}</span></td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    latestInventoryItems = [];
    Utils.showMessage(err.message || "Failed to load inventory", "error");
  }
}

async function loadLowStock() {
  const vendorId = await getSelectedVendorId();
  const body = document.getElementById("lowStockBody");

  if (!body) return;

  if (!vendorId) {
    latestLowStockItems = [];
    body.innerHTML = `<tr><td colspan="3">Select a vendor to view low-stock items</td></tr>`;
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/inventory-items/low-stock`);
    const items = response.data || [];
    latestLowStockItems = items;

    body.innerHTML = "";

    if (items.length === 0) {
      body.innerHTML = `<tr><td colspan="3">No low-stock items</td></tr>`;
      return;
    }

    items.forEach((item) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${Utils.escapeHtml(item.itemName ?? item.inventoryItemName ?? "-")}</td>
        <td>${Utils.formatNumber(item.currentQuantity ?? item.availableQuantity ?? 0)} ${Utils.escapeHtml(item.unit ?? "-")}</td>
        <td>${Utils.formatNumber(item.lowStockThreshold ?? item.threshold ?? 0)}</td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    latestLowStockItems = [];
    Utils.showMessage(err.message || "Failed to load low-stock items", "error");
  }
}

async function loadReorderRecommendations() {
  const vendorId = await getSelectedVendorId();
  const body = document.getElementById("reorderRecommendationsBody");

  if (!body) return;

  if (!vendorId) {
    latestReorderRecommendations = [];
    body.innerHTML = `<tr><td colspan="6">Select a vendor to view reorder recommendations</td></tr>`;
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/reorder-recommendations`);
    const rows = response.data || [];
    latestReorderRecommendations = rows;

    const inventoryNameMap = new Map(latestInventoryItems.map((item) => [Number(item.id), item.itemName]));
    const actionable = rows
      .filter((row) => Number(row.suggestedReorderQty || 0) > 0)
      .sort((a, b) => Number(b.suggestedReorderQty || 0) - Number(a.suggestedReorderQty || 0));

    body.innerHTML = "";

    if (!actionable.length) {
      body.innerHTML = `<tr><td colspan="6">No active reorder recommendations</td></tr>`;
      return;
    }

    actionable.forEach((rec) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${Utils.escapeHtml(inventoryNameMap.get(Number(rec.inventoryItemId)) || `Inventory #${rec.inventoryItemId}`)}</td>
        <td>${Utils.formatNumber(rec.currentStockQty ?? 0)}</td>
        <td>${Utils.formatNumber(rec.forecastDemandQty ?? 0)}</td>
        <td>${Utils.formatNumber(rec.reorderPointQty ?? 0)}</td>
        <td>${Utils.formatNumber(rec.suggestedReorderQty ?? 0)}</td>
        <td><span class="pill pill-warning">${Utils.escapeHtml(rec.recommendationStatus ?? "OPEN")}</span></td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    latestReorderRecommendations = [];
    Utils.showMessage(err.message || "Failed to load reorder recommendations", "error");
  }
}

function renderInventorySummary() {
  Utils.setText("inventoryItemCount", latestInventoryItems.length);
  Utils.setText("inventoryLowStockCount", latestLowStockItems.length);

  const actionable = latestReorderRecommendations.filter((item) => Number(item.suggestedReorderQty || 0) > 0);
  Utils.setText("inventoryOpenReorderCount", actionable.length);

  const riskCount = actionable.filter((item) => Number(item.currentStockQty || 0) <= Number(item.reorderPointQty || 0)).length;
  Utils.setText("inventoryRiskCount", riskCount);
}

function renderInventoryRiskCards() {
  const container = document.getElementById("inventoryRiskCards");
  if (!container) return;

  const inventoryNameMap = new Map(latestInventoryItems.map((item) => [Number(item.id), item.itemName]));
  const topRisk = latestReorderRecommendations
    .filter((item) => Number(item.suggestedReorderQty || 0) > 0)
    .sort((a, b) => Number(b.suggestedReorderQty || 0) - Number(a.suggestedReorderQty || 0))
    .slice(0, 4);

  if (!topRisk.length) {
    container.innerHTML = `<div class="empty-state">No immediate stock-risk items for the selected vendor.</div>`;
    return;
  }

  container.innerHTML = topRisk.map((rec) => `
    <div class="mini-card">
      <div class="mini-card-head">
        <span class="badge subtle-badge">Reorder</span>
        <span class="pill pill-warning">${Utils.escapeHtml(rec.recommendationStatus ?? "OPEN")}</span>
      </div>
      <h4>${Utils.escapeHtml(inventoryNameMap.get(Number(rec.inventoryItemId)) || `Inventory #${rec.inventoryItemId}`)}</h4>
      <div class="mini-metrics">
        <div>
          <span class="mini-label">Current</span>
          <strong>${Utils.formatNumber(rec.currentStockQty ?? 0)}</strong>
        </div>
        <div>
          <span class="mini-label">Reorder Point</span>
          <strong>${Utils.formatNumber(rec.reorderPointQty ?? 0)}</strong>
        </div>
      </div>
      <div class="mini-metrics">
        <div>
          <span class="mini-label">Suggested Qty</span>
          <strong>${Utils.formatNumber(rec.suggestedReorderQty ?? 0)}</strong>
        </div>
        <div>
          <span class="mini-label">Lead Time</span>
          <strong>${Utils.escapeHtml(rec.leadTimeDays ?? "-")} day(s)</strong>
        </div>
      </div>
      <p class="mini-note">${Utils.escapeHtml(rec.explanation ?? "Forecast demand during lead time is above current safe stock.")}</p>
    </div>
  `).join("");
}

function populateOperationItemSelect() {
  const select = document.getElementById("inventoryOperationItemSelect");
  if (!select) return;

  Utils.populateSelect(
    "inventoryOperationItemSelect",
    latestInventoryItems,
    "id",
    "itemName",
    "Select Inventory Item"
  );
}

async function generateRecommendations() {
  const vendorId = await getSelectedVendorId();
  if (!vendorId) {
    Utils.showMessage("Select a vendor first", "error");
    return;
  }

  const button = document.getElementById("generateInventoryRecommendationsBtn");
  try {
    Utils.setButtonLoading(button, true, "Generating...");
    await Api.post(`/vendors/${vendorId}/reorder-recommendations/generate`, {});
    Utils.showMessage("Reorder recommendations generated");
    await refreshInventoryScreen();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to generate recommendations", "error");
  } finally {
    Utils.setButtonLoading(button, false);
  }
}

async function stockIn() {
  const vendorId = await getSelectedVendorId();
  const inventoryItemId = document.getElementById("inventoryOperationItemSelect")?.value;
  const quantity = document.getElementById("operationQuantity")?.value;
  const unitCost = document.getElementById("operationUnitCost")?.value;
  const reason = document.getElementById("operationReason")?.value?.trim() || null;

  if (!vendorId) {
    Utils.showMessage("Select a vendor", "error");
    return;
  }

  if (!inventoryItemId) {
    Utils.showMessage("Select an inventory item", "error");
    return;
  }

  if (!quantity || Number(quantity) <= 0) {
    Utils.showMessage("Enter a valid quantity for stock in", "error");
    return;
  }

  try {
    await Api.post(`/vendors/${vendorId}/inventory-items/${inventoryItemId}/stock-in`, {
      quantity: Number(quantity),
      unitCost: unitCost ? Number(unitCost) : null,
      reason
    });

    Utils.showMessage("Stock added successfully");
    clearOperationInputs();
    await refreshInventoryScreen();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to stock in", "error");
  }
}

async function stockOut() {
  const vendorId = await getSelectedVendorId();
  const inventoryItemId = document.getElementById("inventoryOperationItemSelect")?.value;
  const quantity = document.getElementById("operationQuantity")?.value;
  const reason = document.getElementById("operationReason")?.value?.trim() || null;

  if (!vendorId) {
    Utils.showMessage("Select a vendor", "error");
    return;
  }

  if (!inventoryItemId) {
    Utils.showMessage("Select an inventory item", "error");
    return;
  }

  if (!quantity || Number(quantity) <= 0) {
    Utils.showMessage("Enter a valid quantity for stock out", "error");
    return;
  }

  try {
    await Api.post(`/vendors/${vendorId}/inventory-items/${inventoryItemId}/stock-out`, {
      quantity: Number(quantity),
      reason
    });

    Utils.showMessage("Stock deducted successfully");
    clearOperationInputs();
    await refreshInventoryScreen();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to stock out", "error");
  }
}

async function adjustStock() {
  const vendorId = await getSelectedVendorId();
  const inventoryItemId = document.getElementById("inventoryOperationItemSelect")?.value;
  const adjustedQuantity = document.getElementById("adjustedQuantity")?.value;
  const reason = document.getElementById("operationReason")?.value?.trim() || null;

  if (!vendorId) {
    Utils.showMessage("Select a vendor", "error");
    return;
  }

  if (!inventoryItemId) {
    Utils.showMessage("Select an inventory item", "error");
    return;
  }

  if (adjustedQuantity === "" || adjustedQuantity == null || Number(adjustedQuantity) < 0) {
    Utils.showMessage("Enter a valid adjusted final quantity", "error");
    return;
  }

  try {
    await Api.post(`/vendors/${vendorId}/inventory-items/${inventoryItemId}/adjustments`, {
      adjustedQuantity: Number(adjustedQuantity),
      reason
    });

    Utils.showMessage("Stock adjusted successfully");
    clearOperationInputs();
    await refreshInventoryScreen();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to adjust stock", "error");
  }
}

function clearOperationInputs() {
  const operationQuantity = document.getElementById("operationQuantity");
  const operationUnitCost = document.getElementById("operationUnitCost");
  const adjustedQuantity = document.getElementById("adjustedQuantity");
  const operationReason = document.getElementById("operationReason");

  if (operationQuantity) operationQuantity.value = "";
  if (operationUnitCost) operationUnitCost.value = "";
  if (adjustedQuantity) adjustedQuantity.value = "";
  if (operationReason) operationReason.value = "";
}
