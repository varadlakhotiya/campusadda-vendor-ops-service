requireAuth();
renderSidebar("menu-categories");

document.addEventListener("DOMContentLoaded", async () => {
  await loadVendors();

  document.getElementById("vendorSelect").addEventListener("change", loadCategories);

  document.getElementById("categoryForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const vendorId = document.getElementById("vendorSelect").value;
    if (!vendorId) return alert("Select a vendor");

    try {
      await Api.post(`/vendors/${vendorId}/menu-categories`, {
        categoryName: document.getElementById("categoryName").value,
        displayOrder: Number(document.getElementById("displayOrder").value || 0),
        isActive: true,
        sourceSystem: "VENDOR_OPS"
      });

      Utils.showMessage("Category created");
      e.target.reset();
      loadCategories();
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
  const categories = response.data || [];
  const body = document.getElementById("categoryTableBody");
  body.innerHTML = "";

  categories.forEach(category => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${category.id}</td>
      <td>${category.categoryName}</td>
      <td>${category.displayOrder}</td>
      <td>${category.isActive}</td>
    `;
    body.appendChild(row);
  });
}