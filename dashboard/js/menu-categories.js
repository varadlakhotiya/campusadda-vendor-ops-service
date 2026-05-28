requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("menu-categories");
    await VendorScope.initVendorSelect("vendorSelect", loadCategories);

    const categoryForm = document.getElementById("categoryForm");
    if (categoryForm) {
      categoryForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const vendorId = document.getElementById("vendorSelect")?.value;
        if (!vendorId) {
          alert("Select a vendor");
          return;
        }

        try {
          await Api.post(`/vendors/${vendorId}/menu-categories`, {
            categoryName: document.getElementById("categoryName").value.trim(),
            displayOrder: Number(document.getElementById("displayOrder").value || 0),
            isActive: true,
            sourceSystem: "VENDOR_OPS"
          });

          Utils.showMessage("Category created");
          e.target.reset();
          await loadCategories();
        } catch (err) {
          Utils.showMessage(err.message || "Failed to create category", "error");
        }
      });
    }
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load menu categories page", "error");
  }
});

async function loadCategories() {
  const vendorId = document.getElementById("vendorSelect")?.value;
  if (!vendorId) return;

  try {
    const response = await Api.get(`/vendors/${vendorId}/menu-categories`);
    const categories = response.data || [];
    const body = document.getElementById("categoryTableBody");
    if (!body) return;

    body.innerHTML = "";

    if (categories.length === 0) {
      const row = document.createElement("tr");
      row.innerHTML = `<td colspan="4">No categories found</td>`;
      body.appendChild(row);
      return;
    }

    categories.forEach((category) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${category.id ?? "-"}</td>
        <td>${escapeHtml(category.categoryName ?? "-")}</td>
        <td>${category.displayOrder ?? 0}</td>
        <td>${category.isActive ? "Yes" : "No"}</td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load categories", "error");
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