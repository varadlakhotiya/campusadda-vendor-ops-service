requireAuth();

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await renderSidebar("recipe");
    bindRecipeEvents();

    await VendorScope.initVendorSelect("vendorSelect", async () => {
      await handleVendorChange();
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load recipe page", "error");
  }
});

function bindRecipeEvents() {
  const menuItemSelect = document.getElementById("menuItemSelect");
  if (menuItemSelect) {
    menuItemSelect.addEventListener("change", async () => {
      clearValidationBox();
      clearAvailabilityBox();
      await loadIngredients();
    });
  }

  const addIngredientBtn = document.getElementById("addIngredientBtn");
  if (addIngredientBtn) {
    addIngredientBtn.addEventListener("click", addIngredient);
  }

  const validateRecipeBtn = document.getElementById("validateRecipeBtn");
  if (validateRecipeBtn) {
    validateRecipeBtn.addEventListener("click", validateRecipe);
  }

  const checkAvailabilityBtn = document.getElementById("checkAvailabilityBtn");
  if (checkAvailabilityBtn) {
    checkAvailabilityBtn.addEventListener("click", checkAvailability);
  }
}

async function handleVendorChange() {
  await loadMenuItems();
  await loadInventoryItems();
  clearIngredientsTable();
  clearValidationBox();
  clearAvailabilityBox();
}

async function getSelectedVendorId() {
  let vendorId = await VendorScope.getVendorId("vendorSelect");

  if (!vendorId) {
    const ctx = await Portal.getContext();
    vendorId = ctx?.primaryVendorId ? Number(ctx.primaryVendorId) : null;
  }

  return vendorId;
}

async function loadMenuItems() {
  const vendorId = await getSelectedVendorId();
  const select = document.getElementById("menuItemSelect");

  if (!select) return;

  if (!vendorId) {
    select.innerHTML = `<option value="">Select Menu Item</option>`;
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/menu-items`);
    Utils.populateSelect(
      "menuItemSelect",
      response.data || [],
      "id",
      "itemName",
      "Select Menu Item"
    );
  } catch (err) {
    select.innerHTML = `<option value="">Select Menu Item</option>`;
    Utils.showMessage(err.message || "Failed to load menu items", "error");
  }
}

async function loadInventoryItems() {
  const vendorId = await getSelectedVendorId();
  const select = document.getElementById("inventoryItemSelect");

  if (!select) return;

  if (!vendorId) {
    select.innerHTML = `<option value="">Select Inventory Item</option>`;
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/inventory-items`);
    Utils.populateSelect(
      "inventoryItemSelect",
      response.data || [],
      "id",
      "itemName",
      "Select Inventory Item"
    );
  } catch (err) {
    select.innerHTML = `<option value="">Select Inventory Item</option>`;
    Utils.showMessage(err.message || "Failed to load inventory items", "error");
  }
}

async function addIngredient() {
  const vendorId = await getSelectedVendorId();
  const menuItemId = document.getElementById("menuItemSelect")?.value;

  if (!vendorId) {
    alert("Select a vendor");
    return;
  }

  if (!menuItemId) {
    alert("Select a menu item");
    return;
  }

  const inventoryItemId = document.getElementById("inventoryItemSelect")?.value;
  const quantityRequired = document.getElementById("quantityRequired")?.value;

  if (!inventoryItemId) {
    alert("Select an inventory item");
    return;
  }

  if (!quantityRequired || Number(quantityRequired) <= 0) {
    alert("Enter a valid quantity");
    return;
  }

  try {
    await Api.post(`/vendors/${vendorId}/menu-items/${menuItemId}/ingredients`, {
      inventoryItemId: Number(inventoryItemId),
      quantityRequired: Number(quantityRequired),
      wastagePct: 0,
      isOptional: false,
      isActive: true
    });

    Utils.showMessage("Ingredient added to saved recipe");
    document.getElementById("inventoryItemSelect").value = "";
    document.getElementById("quantityRequired").value = "";
    clearValidationBox();
    clearAvailabilityBox();
    await loadIngredients();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to add ingredient", "error");
  }
}

async function loadIngredients() {
  const vendorId = await getSelectedVendorId();
  const menuItemId = document.getElementById("menuItemSelect")?.value;
  const body = document.getElementById("ingredientsBody");

  if (!body) return;

  if (!vendorId || !menuItemId) {
    clearIngredientsTable();
    return;
  }

  try {
    const response = await Api.get(`/vendors/${vendorId}/menu-items/${menuItemId}/ingredients`);
    const items = response.data || [];

    body.innerHTML = "";

    if (items.length === 0) {
      body.innerHTML = `<tr><td colspan="4">No ingredients saved for this menu item</td></tr>`;
      return;
    }

    items.forEach((item) => {
      const row = document.createElement("tr");
      const actionHtml = item.id != null
        ? `<button class="btn-danger" type="button" onclick="deleteIngredient(${item.id})">Delete</button>`
        : "-";

      row.innerHTML = `
        <td>${escapeHtml(item.inventoryItemName ?? "-")}</td>
        <td>${item.quantityRequired ?? 0}</td>
        <td>${escapeHtml(item.inventoryUnit ?? "-")}</td>
        <td>${actionHtml}</td>
      `;

      body.appendChild(row);
    });
  } catch (err) {
    Utils.showMessage(err.message || "Failed to load ingredients", "error");
  }
}

async function validateRecipe() {
  const vendorId = await getSelectedVendorId();
  const menuItemId = document.getElementById("menuItemSelect")?.value;

  if (!vendorId) {
    alert("Select a vendor");
    return;
  }

  if (!menuItemId) {
    alert("Select a menu item");
    return;
  }

  try {
    const response = await Api.get(
      `/vendors/${vendorId}/menu-items/${menuItemId}/ingredients/validate`
    );
    renderRecipeValidation(response.data || {});
  } catch (err) {
    Utils.showMessage(err.message || "Failed to validate recipe", "error");
  }
}

async function checkAvailability() {
  const vendorId = await getSelectedVendorId();
  const menuItemId = document.getElementById("menuItemSelect")?.value;

  if (!vendorId) {
    alert("Select a vendor");
    return;
  }

  if (!menuItemId) {
    alert("Select a menu item");
    return;
  }

  try {
    const response = await Api.get(
      `/vendors/${vendorId}/menu-items/${menuItemId}/availability-check`
    );
    renderAvailabilityCheck(response.data || {});
  } catch (err) {
    Utils.showMessage(err.message || "Failed to check sellability", "error");
  }
}

function renderRecipeValidation(result) {
  const box = document.getElementById("recipeValidationBox");
  if (!box) return;

  const issues = Array.isArray(result.issues) ? result.issues : [];
  const ready = !!result.recipeReady;

  let text = "";
  text += `Menu Item: ${result.itemName ?? "-"}\n`;
  text += `Recipe Ready: ${ready ? "YES" : "NO"}\n\n`;

  if (issues.length === 0) {
    text += "Meaning:\n";
    text += "- The saved recipe has ingredients mapped.\n";
    text += "- The saved ingredient quantities are valid.\n";
  } else {
    text += "Issues:\n";
    issues.forEach((issue, index) => {
      text += `${index + 1}. ${issue}\n`;
    });
  }

  box.textContent = text;
}

function renderAvailabilityCheck(result) {
  const box = document.getElementById("availabilityCheckBox");
  if (!box) return;

  const issues = Array.isArray(result.issues) ? result.issues : [];
  const sellable = !!result.sellable;

  let text = "";
  text += `Menu Item: ${result.menuItemName ?? "-"}\n`;
  text += `Currently Sellable: ${sellable ? "YES" : "NO"}\n\n`;

  if (issues.length === 0) {
    text += "Meaning:\n";
    text += "- The menu item is active.\n";
    text += "- The menu item is available.\n";
    text += "- The required stock is sufficient for its active required ingredients.\n";
  } else {
    text += "Issues:\n";
    issues.forEach((issue, index) => {
      text += `${index + 1}. ${issue}\n`;
    });
  }

  box.textContent = text;
}

async function deleteIngredient(ingredientId) {
  const vendorId = await getSelectedVendorId();
  const menuItemId = document.getElementById("menuItemSelect")?.value;

  if (!vendorId || !menuItemId || !ingredientId) return;

  const confirmed = window.confirm("Delete this ingredient from the saved recipe?");
  if (!confirmed) return;

  try {
    await Api.delete(`/vendors/${vendorId}/menu-items/${menuItemId}/ingredients/${ingredientId}`);
    Utils.showMessage("Ingredient deleted");
    clearValidationBox();
    clearAvailabilityBox();
    await loadIngredients();
  } catch (err) {
    Utils.showMessage(err.message || "Failed to delete ingredient", "error");
  }
}

function clearIngredientsTable() {
  const body = document.getElementById("ingredientsBody");
  if (body) {
    body.innerHTML = `<tr><td colspan="4">Select a menu item to view saved ingredients</td></tr>`;
  }
}

function clearValidationBox() {
  const box = document.getElementById("recipeValidationBox");
  if (box) {
    box.textContent = `Select a menu item and click "Validate Saved Recipe".`;
  }
}

function clearAvailabilityBox() {
  const box = document.getElementById("availabilityCheckBox");
  if (box) {
    box.textContent = `Select a menu item and click "Check Sellability".`;
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