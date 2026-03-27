requireAuth();
renderSidebar("recipe");

document.addEventListener("DOMContentLoaded", async () => {
  await loadVendors();

  document.getElementById("vendorSelect").addEventListener("change", async () => {
    await loadMenuItems();
    await loadInventoryItems();
  });

  document.getElementById("menuItemSelect").addEventListener("change", loadIngredients);

  document.getElementById("addIngredientBtn").addEventListener("click", addIngredient);
  document.getElementById("validateRecipeBtn").addEventListener("click", validateRecipe);
});

async function loadVendors() {
  const response = await Api.get("/vendors");
  Utils.populateSelect("vendorSelect", response.data || [], "id", "name", "Select Vendor");
}

async function loadMenuItems() {
  const vendorId = document.getElementById("vendorSelect").value;
  const response = await Api.get(`/vendors/${vendorId}/menu-items`);
  Utils.populateSelect("menuItemSelect", response.data || [], "id", "itemName", "Select Menu Item");
}

async function loadInventoryItems() {
  const vendorId = document.getElementById("vendorSelect").value;
  const response = await Api.get(`/vendors/${vendorId}/inventory-items`);
  Utils.populateSelect("inventoryItemSelect", response.data || [], "id", "itemName", "Select Inventory Item");
}

async function addIngredient() {
  const vendorId = document.getElementById("vendorSelect").value;
  const menuItemId = document.getElementById("menuItemSelect").value;

  try {
    await Api.post(`/vendors/${vendorId}/menu-items/${menuItemId}/ingredients`, {
      inventoryItemId: Number(document.getElementById("inventoryItemSelect").value),
      quantityRequired: Number(document.getElementById("quantityRequired").value),
      wastagePct: 0,
      isOptional: false,
      isActive: true
    });

    Utils.showMessage("Ingredient added");
    loadIngredients();
  } catch (err) {
    Utils.showMessage(err.message, "error");
  }
}

async function loadIngredients() {
  const vendorId = document.getElementById("vendorSelect").value;
  const menuItemId = document.getElementById("menuItemSelect").value;
  if (!vendorId || !menuItemId) return;

  const response = await Api.get(`/vendors/${vendorId}/menu-items/${menuItemId}/ingredients`);
  const items = response.data || [];
  const body = document.getElementById("ingredientsBody");
  body.innerHTML = "";

  items.forEach(item => {
    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${item.inventoryItemName}</td>
      <td>${item.quantityRequired}</td>
      <td>${item.inventoryUnit}</td>
    `;
    body.appendChild(row);
  });
}

async function validateRecipe() {
  const vendorId = document.getElementById("vendorSelect").value;
  const menuItemId = document.getElementById("menuItemSelect").value;
  if (!vendorId || !menuItemId) return;

  const response = await Api.get(`/vendors/${vendorId}/menu-items/${menuItemId}/ingredients/validate`);
  document.getElementById("recipeValidationBox").textContent =
    JSON.stringify(response.data, null, 2);
}