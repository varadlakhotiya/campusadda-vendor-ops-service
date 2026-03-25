requireAuth();
renderSidebar("menu-items");

document.addEventListener("DOMContentLoaded", async () => {
  await loadVendors();

  document.getElementById("vendorSelect").addEventListener("change", async () => {
    await loadCategories();
    await loadMenuItems();
  });

  document.getElementById("menuItemForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const vendorId = document.getElementById("vendorSelect").value;
    if (!vendorId) return alert("Select a vendor");

    try {
      const payload = {
        categoryId: document.getElementById("categorySelect").value || null,
        itemCode: document.getElementById("itemCode").value,
        itemName: document.getElementById("itemName").value,
        price: Number(document.getElementById("price").value),
        costPrice: document.getElementById("costPrice").value ? Number(document.getElementById("costPrice").value) : null,
        prepTimeMinutes: document.getElementById("prepTimeMinutes").value ? Number(document.getElementById("prepTimeMinutes").value) : null,
        primaryImageUrl: document.getElementById("primaryImageUrl").value,
        isVeg: document.getElementById("isVeg").value === "true",
        isAvailable: true,
        isActive: true,
        trackInventory: true,
        sourceSystem: "VENDOR_OPS"
      };

      await Api.post(`/vendors/${vendorId}/menu-items`, payload);
      Utils.showMessage("Menu item created");
      e.target.reset();
      loadMenuItems();
    } catch (err) {
      Utils.showMessage(err.message, "error");
    }
  });
});

async function loadVendors() {
  const response = await Api.get("/vendors");
  Utils.populateSelect("vendorSelect", response.data || [], "id", "name", "Select Vendor");
}

async function loadCategories() {
  const vendorId = document.getElementById("vendorSelect").value;
  if (!vendorId) return;

  const response = await Api.get(`/vendors/${vendorId}/menu-categories`);
  Utils.populateSelect("categorySelect", response.data || [], "id", "categoryName", "Select Category");
}

async function loadMenuItems() {
  const vendorId = document.getElementById("vendorSelect").value;
  if (!vendorId) return;

  const response = await Api.get(`/vendors/${vendorId}/menu-items`);
  const items = response.data || [];
  const body = document.getElementById("menuItemsBody");
  body.innerHTML = "";

  items.forEach(item => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${item.id}</td>
      <td>${item.itemCode}</td>
      <td>${item.itemName}</td>
      <td>${Utils.formatCurrency(item.price)}</td>
      <td>${item.isAvailable}</td>
    `;
    body.appendChild(row);
  });
}