requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("menu-items");
    bindStaticEvents();

    await VendorScope.initVendorSelect("vendorSelect", async () => {
      await handleVendorChange();
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load menu items page", "error");
  }
});

function bindStaticEvents() {
  const categorySelect = document.getElementById("categorySelect");
  if (categorySelect) {
    categorySelect.addEventListener("change", async () => {
      await loadMenuItems();
    });
  }

  const menuItemForm = document.getElementById("menuItemForm");
  if (menuItemForm) {
    menuItemForm.addEventListener("submit", handleCreateMenuItem);
  }
}

async function handleVendorChange() {
  await loadCategories({ preserveSelection: false });
  await loadMenuItems();
}

async function getSelectedVendorId() {
  let vendorId = await VendorScope.getVendorId("vendorSelect");

  if (!vendorId) {
    const ctx = await Portal.getContext();
    vendorId = ctx?.primaryVendorId ? Number(ctx.primaryVendorId) : null;
  }

  return vendorId;
}

function getSelectedCategoryId() {
  const value = document.getElementById("categorySelect")?.value;
  return value ? Number(value) : null;
}

async function loadCategories({ preserveSelection = false } = {}) {
  const vendorId = await getSelectedVendorId();
  const select = document.getElementById("categorySelect");

  if (!select) return;

  if (!vendorId) {
    clearCategorySelect();
    clearMenuItemsTable("Select a vendor to view menu items");
    return;
  }

  const previousValue = preserveSelection ? select.value : "";

  try {
    const response = await Api.get(`/vendors/${vendorId}/menu-categories`);
    const categories = response.data || [];

    Utils.populateSelect(
      "categorySelect",
      categories,
      "id",
      "categoryName",
      "Select Category"
    );

    if (
      preserveSelection &&
      previousValue &&
      [...select.options].some((option) => option.value === previousValue)
    ) {
      select.value = previousValue;
    } else {
      select.value = "";
    }
  } catch (err) {
    clearCategorySelect();
    Utils.showMessage(err.message || "Failed to load categories", "error");
  }
}

async function loadMenuItems() {
  const vendorId = await getSelectedVendorId();
  const body = document.getElementById("menuItemsBody");

  if (!body) return;

  if (!vendorId) {
    clearMenuItemsTable("Select a vendor to view menu items");
    return;
  }

  const categoryId = getSelectedCategoryId();
  const endpoint = categoryId
    ? `/vendors/${vendorId}/menu-items?categoryId=${encodeURIComponent(categoryId)}`
    : `/vendors/${vendorId}/menu-items`;

  try {
    const response = await Api.get(endpoint);
    const items = response.data || [];

    body.innerHTML = "";

    if (items.length === 0) {
      const row = document.createElement("tr");
      row.innerHTML = `<td colspan="5">No menu items found</td>`;
      body.appendChild(row);
      return;
    }

    items.forEach((item) => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${item.id ?? "-"}</td>
        <td>${escapeHtml(item.itemCode ?? "-")}</td>
        <td>${escapeHtml(item.itemName ?? "-")}</td>
        <td>${Utils.formatCurrency(item.price ?? 0)}</td>
        <td>${item.isAvailable ? "Yes" : "No"}</td>
      `;
      body.appendChild(row);
    });
  } catch (err) {
    clearMenuItemsTable("Failed to load menu items");
    Utils.showMessage(err.message || "Failed to load menu items", "error");
  }
}

async function handleCreateMenuItem(e) {
  e.preventDefault();

  const vendorId = await getSelectedVendorId();
  if (!vendorId) {
    alert("Select a vendor");
    return;
  }

  const categoryId = getSelectedCategoryId();

  try {
    const payload = {
      categoryId,
      itemCode: document.getElementById("itemCode").value.trim(),
      itemName: document.getElementById("itemName").value.trim(),
      price: Number(document.getElementById("price").value),
      costPrice: document.getElementById("costPrice").value
        ? Number(document.getElementById("costPrice").value)
        : null,
      prepTimeMinutes: document.getElementById("prepTimeMinutes").value
        ? Number(document.getElementById("prepTimeMinutes").value)
        : null,
      primaryImageUrl: document.getElementById("primaryImageUrl").value.trim(),
      isVeg: document.getElementById("isVeg").value === "true",
      isAvailable: true,
      isActive: true,
      trackInventory: true,
      sourceSystem: "VENDOR_OPS"
    };

    await Api.post(`/vendors/${vendorId}/menu-items`, payload);
    Utils.showMessage("Menu item created");

    clearCreateFormButKeepCurrentCategory();
    await loadCategories({ preserveSelection: true });
    await loadMenuItems();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to create menu item", "error");
  }
}

function clearCreateFormButKeepCurrentCategory() {
  const itemCode = document.getElementById("itemCode");
  const itemName = document.getElementById("itemName");
  const price = document.getElementById("price");
  const costPrice = document.getElementById("costPrice");
  const prepTimeMinutes = document.getElementById("prepTimeMinutes");
  const primaryImageUrl = document.getElementById("primaryImageUrl");
  const isVeg = document.getElementById("isVeg");

  if (itemCode) itemCode.value = "";
  if (itemName) itemName.value = "";
  if (price) price.value = "";
  if (costPrice) costPrice.value = "";
  if (prepTimeMinutes) prepTimeMinutes.value = "";
  if (primaryImageUrl) primaryImageUrl.value = "";
  if (isVeg) isVeg.value = "true";
}

function clearCategorySelect() {
  const select = document.getElementById("categorySelect");
  if (!select) return;

  select.innerHTML = `<option value="">Select Category</option>`;
  select.value = "";
}

function clearMenuItemsTable(message = "No menu items found") {
  const body = document.getElementById("menuItemsBody");
  if (!body) return;

  body.innerHTML = "";
  const row = document.createElement("tr");
  row.innerHTML = `<td colspan="5">${escapeHtml(message)}</td>`;
  body.appendChild(row);
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}