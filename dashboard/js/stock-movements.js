requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("stock-movements");

    await VendorScope.initVendorSelect("vendorSelect", async () => {
      await handleVendorChange();
    });

    const inventoryItemSelect = document.getElementById("inventoryItemSelect");
    if (inventoryItemSelect) {
      inventoryItemSelect.addEventListener("change", loadStockMovements);
    }
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load stock movements page", "error");
  }
});

async function handleVendorChange() {
  await loadInventoryItems();
}

async function getSelectedVendorId() {
  let vendorId = await VendorScope.getVendorId("vendorSelect");

  if (!vendorId) {
    const ctx = await Portal.getContext();
    vendorId = ctx?.primaryVendorId ? Number(ctx.primaryVendorId) : null;
  }

  return vendorId;
}

async function loadInventoryItems() {
  const vendorId = await getSelectedVendorId();
  const select = document.getElementById("inventoryItemSelect");

  if (!select) return;

  if (!vendorId) {
    select.innerHTML = `<option value="">Select Inventory Item</option>`;
    clearTable("Select a vendor first");
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/inventory-items`);
    const items = response.data || [];

    Utils.populateSelect(
      "inventoryItemSelect",
      items,
      "id",
      "itemName",
      "Select Inventory Item"
    );

    if (items.length > 0) {
      select.value = String(items[0].id);
      await loadStockMovements();
    } else {
      clearTable("No inventory items found");
    }
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load inventory items", "error");
    clearTable("Failed to load inventory items");
  }
}

async function loadStockMovements() {
  const vendorId = await getSelectedVendorId();
  const inventoryItemId = document.getElementById("inventoryItemSelect")?.value;

  if (!vendorId || !inventoryItemId) {
    clearTable("Select an inventory item");
    return;
  }

  try {
    const response = await Api.get(
      `/vendors/${vendorId}/inventory-items/${inventoryItemId}/stock-movements`
    );

    const rows = response.data || [];
    const body = document.getElementById("stockMovementsBody");
    if (!body) return;

    body.innerHTML = "";

    if (rows.length === 0) {
      clearTable("No stock movements found");
      return;
    }

    rows.forEach((row) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${escapeHtml(row.movementType ?? "-")}</td>
        <td>${row.quantityDelta ?? 0}</td>
        <td>${row.quantityBefore ?? 0}</td>
        <td>${row.quantityAfter ?? 0}</td>
        <td>${escapeHtml(row.reason ?? "-")}</td>
        <td>${row.eventTime ? Utils.formatDateTime(row.eventTime) : "-"}</td>
      `;
      body.appendChild(tr);
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load stock movements", "error");
    clearTable("Failed to load stock movements");
  }
}

function clearTable(message = "No data") {
  const body = document.getElementById("stockMovementsBody");
  if (body) {
    body.innerHTML = `<tr><td colspan="6">${escapeHtml(message)}</td></tr>`;
  }
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}