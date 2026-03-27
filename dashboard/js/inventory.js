requireAuth();
renderSidebar("inventory");

document.addEventListener("DOMContentLoaded", async () => {
  await loadVendors();

  document.getElementById("vendorSelect").addEventListener("change", loadInventory);

  document.getElementById("inventoryForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const vendorId = document.getElementById("vendorSelect").value;
    if (!vendorId) return alert("Select a vendor");

    try {
      await Api.post(`/vendors/${vendorId}/inventory-items`, {
        itemCode: document.getElementById("itemCode").value,
        itemName: document.getElementById("itemName").value,
        unit: document.getElementById("unit").value,
        currentQuantity: Number(document.getElementById("currentQuantity").value || 0),
        lowStockThreshold: Number(document.getElementById("lowStockThreshold").value || 0),
        unitCost: document.getElementById("unitCost").value ? Number(document.getElementById("unitCost").value) : null,
        status: "ACTIVE",
        sourceSystem: "VENDOR_OPS"
      });

      Utils.showMessage("Inventory item created");
      e.target.reset();
      loadInventory();
    } catch (err) {
      Utils.showMessage(err.message, "error");
    }
  });
});

async function loadVendors() {
  const response = await Api.get("/vendors");
  Utils.populateSelect("vendorSelect", response.data || [], "id", "name", "Select Vendor");
}

async function loadInventory() {
  const vendorId = document.getElementById("vendorSelect").value;
  if (!vendorId) return;

  const response = await Api.get(`/vendors/${vendorId}/inventory-items`);
  const items = response.data || [];
  const body = document.getElementById("inventoryBody");
  body.innerHTML = "";

  items.forEach(item => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${item.id}</td>
      <td>${item.itemCode}</td>
      <td>${item.itemName}</td>
      <td>${item.currentQuantity} ${item.unit}</td>
      <td>${item.lowStockThreshold}</td>
    `;
    body.appendChild(row);
  });
}