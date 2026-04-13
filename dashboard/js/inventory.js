requireAuth();

let latestInventoryItems = [];

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
  const inventoryForm = document.getElementById("inventoryForm");
  if (inventoryForm) {
    inventoryForm.addEventListener("submit", handleCreateInventoryItem);
  }

  const stockInBtn = document.getElementById("stockInBtn");
  if (stockInBtn) {
    stockInBtn.addEventListener("click", stockIn);
  }

  const stockOutBtn = document.getElementById("stockOutBtn");
  if (stockOutBtn) {
    stockOutBtn.addEventListener("click", stockOut);
  }

  const adjustStockBtn = document.getElementById("adjustStockBtn");
  if (adjustStockBtn) {
    adjustStockBtn.addEventListener("click", adjustStock);
  }
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
    alert("Select a vendor");
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
  await loadInventory();
  await loadLowStock();
  populateOperationItemSelect();
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
        <td>${escapeHtml(item.itemCode ?? "-")}</td>
        <td>${escapeHtml(item.itemName ?? "-")}</td>
        <td>${item.currentQuantity ?? 0} ${escapeHtml(item.unit ?? "-")}</td>
        <td>${item.lowStockThreshold ?? 0}</td>
        <td>${item.unitCost != null ? Utils.formatCurrency(item.unitCost) : "-"}</td>
        <td>${escapeHtml(item.status ?? "-")}</td>
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
    body.innerHTML = `<tr><td colspan="3">Select a vendor to view low-stock items</td></tr>`;
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/inventory-items/low-stock`);
    const items = response.data || [];

    body.innerHTML = "";

    if (items.length === 0) {
      body.innerHTML = `<tr><td colspan="3">No low-stock items</td></tr>`;
      return;
    }

    items.forEach((item) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${escapeHtml(item.itemName ?? item.inventoryItemName ?? "-")}</td>
        <td>${item.currentQuantity ?? item.availableQuantity ?? 0} ${escapeHtml(item.unit ?? "-")}</td>
        <td>${item.lowStockThreshold ?? item.threshold ?? 0}</td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load low-stock items", "error");
  }
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

async function stockIn() {
  const vendorId = await getSelectedVendorId();
  const inventoryItemId = document.getElementById("inventoryOperationItemSelect")?.value;
  const quantity = document.getElementById("operationQuantity")?.value;
  const unitCost = document.getElementById("operationUnitCost")?.value;
  const reason = document.getElementById("operationReason")?.value?.trim() || null;

  if (!vendorId) {
    alert("Select a vendor");
    return;
  }

  if (!inventoryItemId) {
    alert("Select an inventory item");
    return;
  }

  if (!quantity || Number(quantity) <= 0) {
    alert("Enter a valid quantity for stock in");
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
    alert("Select a vendor");
    return;
  }

  if (!inventoryItemId) {
    alert("Select an inventory item");
    return;
  }

  if (!quantity || Number(quantity) <= 0) {
    alert("Enter a valid quantity for stock out");
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
    alert("Select a vendor");
    return;
  }

  if (!inventoryItemId) {
    alert("Select an inventory item");
    return;
  }

  if (adjustedQuantity === "" || adjustedQuantity == null || Number(adjustedQuantity) < 0) {
    alert("Enter a valid adjusted final quantity");
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

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}