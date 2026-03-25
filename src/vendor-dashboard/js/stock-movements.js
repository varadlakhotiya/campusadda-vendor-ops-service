requireAuth();
renderSidebar("stock-movements");

document.addEventListener("DOMContentLoaded", async () => {
  await loadVendors();

  document.getElementById("vendorSelect").addEventListener("change", loadInventoryItems);
  document.getElementById("inventoryItemSelect").addEventListener("change", loadStockMovements);
});

async function loadVendors() {
  const response = await Api.get("/vendors");
  Utils.populateSelect("vendorSelect", response.data || [], "id", "name", "Select Vendor");
}

async function loadInventoryItems() {
  const vendorId = document.getElementById("vendorSelect").value;
  if (!vendorId) return;

  const response = await Api.get(`/vendors/${vendorId}/inventory-items`);
  Utils.populateSelect("inventoryItemSelect", response.data || [], "id", "itemName", "Select Inventory Item");
}

async function loadStockMovements() {
  const vendorId = document.getElementById("vendorSelect").value;
  const inventoryItemId = document.getElementById("inventoryItemSelect").value;
  if (!vendorId || !inventoryItemId) return;

  const response = await Api.get(`/vendors/${vendorId}/inventory-items/${inventoryItemId}/stock-movements`);
  const rows = response.data || [];
  const body = document.getElementById("stockMovementsBody");
  body.innerHTML = "";

  rows.forEach(row => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${row.movementType}</td>
      <td>${row.quantityDelta}</td>
      <td>${row.quantityBefore}</td>
      <td>${row.quantityAfter}</td>
      <td>${row.reason || "-"}</td>
      <td>${Utils.formatDateTime(row.eventTime)}</td>
    `;
    body.appendChild(tr);
  });
}