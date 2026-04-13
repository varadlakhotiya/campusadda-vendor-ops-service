const TRACKING_STORAGE_KEY = "campusadda_customer_tracking";
const TRACKING_POLL_INTERVAL_MS = 15000;

const state = {
  vendors: [],
  vendorMenusById: {},
  vendorSearchIndexById: {},
  selectedVendor: null,
  menu: null,
  selectedCategoryId: null,
  cart: [],
  globalQuery: "",
  vendorQuery: "",
  itemQuery: "",
  tracking: {
    orderNumber: "",
    phone: "",
    data: null,
    autoRefreshTimer: null
  }
};

const els = {};

document.addEventListener("DOMContentLoaded", async () => {
  cacheElements();
  bindStaticEvents();
  bindCheckout();
  bindTracking();

  try {
    renderVendorSkeletons();
    renderMenuSkeletons();
    renderTrackingState("Enter your order number and phone number to see live status updates.");
    await loadVendors();
    restoreTrackingSession();
  } catch (error) {
    showToast(error.message, "error");
    renderMenuEmpty("We could not load the menu right now. Please try again.");
  }
});

function cacheElements() {
  [
    "vendorsList",
    "vendorTitle",
    "vendorSubtitle",
    "vendorDescription",
    "selectedVendorMeta",
    "selectedVendorItemsCount",
    "selectedVendorCategoriesCount",
    "selectedVendorAvgPrep",
    "categoriesRow",
    "menuGrid",
    "cartPanel",
    "cartItems",
    "cartVendorInfo",
    "cartTotal",
    "cartItemsBadge",
    "cartCountBadge",
    "cartSummaryMeta",
    "checkoutForm",
    "customerName",
    "customerPhone",
    "customerNotes",
    "orderResult",
    "globalSearchInput",
    "vendorSearchInput",
    "itemSearchInput",
    "clearItemSearchBtn",
    "resultCountLabel",
    "vendorsCountLabel",
    "scrollToCartBtn",
    "toastContainer",
    "heroVendorCount",
    "heroItemCount",
    "heroAvgPrep",
    "heroFeaturedVendorName",
    "heroFeaturedMeta",
    "heroFeaturedDescription",
    "heroFeaturedTags",
    "heroFeaturedItems",
    "trackingForm",
    "trackingOrderNumber",
    "trackingPhone",
    "trackOrderBtn",
    "refreshTrackingBtn",
    "trackingState",
    "trackingPanel",
    "trackingVendorName",
    "trackingOrderMeta",
    "trackingStatusBadge",
    "trackingSteps",
    "trackingItems",
    "trackingHistory",
    "trackingLastUpdated"
  ].forEach((id) => {
    els[id] = document.getElementById(id);
  });
}

function bindStaticEvents() {
  els.globalSearchInput?.addEventListener("input", (event) => {
    state.globalQuery = normalizeText(event.target.value);
    renderVendors();
    syncSelectedVendorWithFilters();
    renderMenuItems();
    updateHeroPreview();
  });

  els.vendorSearchInput?.addEventListener("input", (event) => {
    state.vendorQuery = normalizeText(event.target.value);
    renderVendors();
    syncSelectedVendorWithFilters();
    renderMenuItems();
    updateHeroPreview();
  });

  els.itemSearchInput?.addEventListener("input", (event) => {
    state.itemQuery = normalizeText(event.target.value);
    renderMenuItems();
    updateHeroPreview();
  });

  els.clearItemSearchBtn?.addEventListener("click", () => {
    if (els.itemSearchInput) {
      els.itemSearchInput.value = "";
    }
    state.itemQuery = "";
    renderMenuItems();
    updateHeroPreview();
  });

  els.scrollToCartBtn?.addEventListener("click", () => {
    els.cartPanel?.scrollIntoView({ behavior: "smooth", block: "start" });
  });

  els.vendorsList?.addEventListener("click", async (event) => {
    const trigger = event.target.closest("[data-vendor-id]");
    if (!trigger) return;

    const vendorId = Number(trigger.getAttribute("data-vendor-id"));
    if (!vendorId || state.selectedVendor?.id === vendorId) return;

    try {
      await selectVendor(vendorId);
    } catch (error) {
      showToast(error.message, "error");
    }
  });

  els.categoriesRow?.addEventListener("click", (event) => {
    const trigger = event.target.closest("[data-category-id]");
    if (!trigger) return;

    const rawValue = trigger.getAttribute("data-category-id");
    state.selectedCategoryId = rawValue === "all" ? null : Number(rawValue);
    renderCategories();
    renderMenuItems();
    updateHeroPreview();
  });

  els.menuGrid?.addEventListener("click", (event) => {
    const actionButton = event.target.closest("[data-action]");
    if (!actionButton) return;

    const action = actionButton.getAttribute("data-action");
    const itemId = Number(actionButton.getAttribute("data-item-id"));
    if (!itemId) return;

    if (action === "qty-inc") {
      updateMenuQuantityInput(itemId, 1);
      return;
    }

    if (action === "qty-dec") {
      updateMenuQuantityInput(itemId, -1);
      return;
    }

    if (action === "add-to-cart") {
      addToCart(itemId);
    }
  });

  els.cartItems?.addEventListener("click", (event) => {
    const actionButton = event.target.closest("[data-cart-action]");
    if (!actionButton) return;

    const action = actionButton.getAttribute("data-cart-action");
    const index = Number(actionButton.getAttribute("data-index"));
    if (Number.isNaN(index)) return;

    if (action === "inc") {
      updateCartQuantity(index, 1);
      return;
    }

    if (action === "dec") {
      updateCartQuantity(index, -1);
      return;
    }

    if (action === "remove") {
      removeFromCart(index);
    }
  });
}

async function loadVendors() {
  const response = await CustomerApi.get("/vendors");
  state.vendors = Array.isArray(response.data) ? response.data : [];
  state.vendorMenusById = {};
  state.vendorSearchIndexById = {};

  if (state.vendors.length > 0) {
    await warmVendorSearchIndex();
  }

  state.selectedVendor = null;
  state.menu = null;
  state.selectedCategoryId = null;

  updateHeroStats();
  renderVendors();
  renderVendorHeader();
  renderCategories();
  renderMenuItems();
  updateHeroPreview();

  if (state.vendors.length === 0) {
    renderMenuEmpty("No vendors are available right now.");
    if (els.resultCountLabel) {
      els.resultCountLabel.textContent = "No vendors available.";
    }
  }
}

async function selectVendor(vendorId, options = {}) {
  const numericVendorId = Number(vendorId);
  if (!numericVendorId) {
    state.selectedVendor = null;
    state.menu = null;
    state.selectedCategoryId = null;
    renderVendorHeader();
    renderCategories();
    renderMenuItems();
    updateHeroPreview();
    return;
  }

  if (!options.silentLoading) {
    renderMenuSkeletons();
  }

  let menu = state.vendorMenusById[numericVendorId];

  if (!menu) {
    const response = await CustomerApi.get(`/vendors/${numericVendorId}/menu`);
    menu = response.data || { items: [], categories: [] };
    state.vendorMenusById[numericVendorId] = menu;
  }

  const vendor = state.vendors.find((entry) => Number(entry.id) === numericVendorId) || null;
  if (vendor) {
    state.vendorSearchIndexById[numericVendorId] = buildVendorSearchIndex(vendor, menu);
  }

  state.menu = menu;
  state.selectedVendor = vendor;
  state.selectedCategoryId = null;

  renderVendors();
  renderVendorHeader();
  renderCategories();
  renderMenuItems();
  renderCart();
  updateHeroStats();
  updateHeroPreview();

  if (!options.silentLoading && window.innerWidth <= 1140) {
    document.getElementById("menuPanel")?.scrollIntoView({
      behavior: "smooth",
      block: "start"
    });
  }
}

function renderVendors() {
  const vendors = getFilteredVendors();
  els.vendorsList.innerHTML = "";
  els.vendorsCountLabel.textContent = `${vendors.length} vendor${vendors.length === 1 ? "" : "s"}`;

  if (vendors.length === 0) {
    els.vendorsList.innerHTML = `<div class="empty-state">No vendors match your search.</div>`;
    return;
  }

  vendors.forEach((vendor) => {
    const isActive = vendor.id === state.selectedVendor?.id;
    const card = document.createElement("button");
    card.type = "button";
    card.className = `vendor-card ${isActive ? "active" : ""}`;
    card.setAttribute("data-vendor-id", String(vendor.id));

    const menu = state.vendorMenusById[Number(vendor.id)] || { items: [], categories: [] };
    const itemCount = menu.items?.length || 0;
    const categoryCount = menu.categories?.length || 0;
    const avgPrep = calculateAveragePrep(menu.items || []);
    const initials = getInitials(vendor.name);
    const location = [vendor.locationLabel, vendor.campusArea].filter(Boolean).join(" • ") || "Campus pickup available";

    const summaryLine = itemCount
      ? `${itemCount} dish${itemCount === 1 ? "" : "es"} • ${categoryCount} categor${categoryCount === 1 ? "y" : "ies"}`
      : "Fresh campus menu";

    const prepLine = avgPrep > 0 ? `${avgPrep} min avg prep` : "Pickup ready";

    card.innerHTML = `
      <div class="vendor-card-top">
        <div class="vendor-avatar">${escapeHtml(initials)}</div>
        <div class="vendor-card-copy">
          <div class="vendor-card-heading">
            <h3>${escapeHtml(vendor.name || "Vendor")}</h3>
            ${isActive ? '<span class="vendor-active-badge">Selected</span>' : ''}
          </div>
          <div class="vendor-meta-line">${escapeHtml(location)}</div>
        </div>
      </div>
      <p class="vendor-card-description">${escapeHtml(vendor.description || "Fresh menu available for pickup orders.")}</p>
      <div class="vendor-quick-stats">
        <span class="vendor-stat-chip">${escapeHtml(summaryLine)}</span>
        <span class="vendor-stat-chip">${escapeHtml(prepLine)}</span>
      </div>
      <div class="vendor-card-footer">
        <span class="vendor-tag">Pickup</span>
        <span class="vendor-tag">Campus ready</span>
        <span class="vendor-tag">Live menu</span>
      </div>
    `;

    els.vendorsList.appendChild(card);
  });
}

function renderVendorHeader() {
  if (!state.selectedVendor || !state.menu) {
    els.vendorTitle.textContent = "Select a vendor";
    els.vendorSubtitle.textContent = "";
    els.vendorDescription.textContent = "Pick a vendor from the left to see a redesigned menu experience.";
    els.selectedVendorItemsCount.textContent = "0";
    els.selectedVendorCategoriesCount.textContent = "0";
    els.selectedVendorAvgPrep.textContent = "0 min";
    els.selectedVendorMeta.innerHTML = "";
    return;
  }

  const vendorName = state.menu?.vendorName || state.selectedVendor?.name || "Select a vendor";
  const subtitle = [state.menu?.locationLabel, state.menu?.campusArea].filter(Boolean).join(" • ");
  const description = state.selectedVendor?.description || "Browse a curated menu with a polished, modern food ordering layout.";
  const items = state.menu?.items || [];
  const categories = state.menu?.categories || [];
  const avgPrep = calculateAveragePrep(items);

  els.vendorTitle.textContent = vendorName;
  els.vendorSubtitle.textContent = subtitle;
  els.vendorDescription.textContent = description;
  els.selectedVendorItemsCount.textContent = String(items.length);
  els.selectedVendorCategoriesCount.textContent = String(categories.length);
  els.selectedVendorAvgPrep.textContent = `${avgPrep} min`;

  els.selectedVendorMeta.innerHTML = "";
  [
    subtitle || "Campus pickup",
    `${items.length} dishes available`,
    `${categories.length} categories`,
    `Avg prep ${avgPrep} min`
  ].forEach((label) => {
    const pill = document.createElement("span");
    pill.className = "meta-pill";
    pill.textContent = label;
    els.selectedVendorMeta.appendChild(pill);
  });
}

function renderCategories() {
  if (!state.selectedVendor || !state.menu) {
    els.categoriesRow.innerHTML = "";
    return;
  }

  const categories = state.menu?.categories || [];
  els.categoriesRow.innerHTML = "";

  const allChip = document.createElement("button");
  allChip.type = "button";
  allChip.className = `category-chip ${state.selectedCategoryId === null ? "active" : ""}`;
  allChip.setAttribute("data-category-id", "all");
  allChip.textContent = "All";
  els.categoriesRow.appendChild(allChip);

  categories.forEach((category) => {
    const chip = document.createElement("button");
    chip.type = "button";
    chip.className = `category-chip ${state.selectedCategoryId === category.id ? "active" : ""}`;
    chip.setAttribute("data-category-id", String(category.id));
    chip.textContent = category.categoryName;
    els.categoriesRow.appendChild(chip);
  });
}

function renderMenuItems() {
  const items = getFilteredMenuItems();
  const totalItems = state.menu?.items?.length || 0;
  els.menuGrid.innerHTML = "";

  if (!state.selectedVendor || !state.menu) {
    renderMenuEmpty("Select a vendor to see the menu.");
    els.resultCountLabel.textContent = "No vendor selected.";
    return;
  }

  els.resultCountLabel.textContent = items.length === totalItems
    ? `Showing all ${items.length} dishes`
    : `Showing ${items.length} of ${totalItems} dishes`;

  if (items.length === 0) {
    renderMenuEmpty("No menu items match your search or selected category.");
    return;
  }

  items.forEach((item, index) => {
    const isUnavailable = item.isAvailable === false;
    const card = document.createElement("article");
    card.className = `menu-card animate-up ${isUnavailable ? "unavailable" : ""}`;
    card.style.animationDelay = `${Math.min(index * 40, 240)}ms`;

    const imageUrl = item.primaryImageUrl || `https://placehold.co/800x600/ede9fe/4338ca?text=${encodeURIComponent(item.itemName || "Dish")}`;
    const foodBadge = item.isVeg ? "Veg" : "Non-Veg";
    const price = formatCurrency(item.price);
    const prep = item.prepTimeMinutes ? `${item.prepTimeMinutes} min prep` : "Freshly prepared";
    const category = getCategoryName(item.categoryId);
    const actionLabel = isUnavailable ? "Unavailable" : "Add";
    const availabilityLabel = isUnavailable ? "Currently unavailable" : "Pickup ready";
    const disabledAttribute = isUnavailable ? "disabled" : "";

    card.innerHTML = `
      <div class="menu-card-media">
        <img src="${escapeAttribute(imageUrl)}" alt="${escapeAttribute(item.itemName || "Menu Item")}" loading="lazy" />
        <div class="media-overlay">
          <span class="item-type-badge">${foodBadge}</span>
        </div>
      </div>
      <div class="menu-card-body">
        <div class="menu-card-heading">
          <div>
            <div class="menu-card-kicker">${escapeHtml(category)}</div>
            <h3>${escapeHtml(item.itemName || "Menu Item")}</h3>
          </div>
          <div class="price">${price}</div>
        </div>
        <div class="menu-description">${escapeHtml(item.description || "Tasty campus special prepared on order.")}</div>
        <div class="menu-meta-row">
          <span class="vendor-tag">${escapeHtml(foodBadge)}</span>
          <span class="vendor-tag">${escapeHtml(prep)}</span>
          <span class="vendor-tag">${escapeHtml(availabilityLabel)}</span>
        </div>
        <div class="menu-card-actions">
          <div class="quantity-stepper">
            <button type="button" class="qty-button" data-action="qty-dec" data-item-id="${item.id}" ${disabledAttribute}>−</button>
            <input id="menuQty-${item.id}" type="number" min="1" value="1" ${disabledAttribute} />
            <button type="button" class="qty-button" data-action="qty-inc" data-item-id="${item.id}" ${disabledAttribute}>+</button>
          </div>
          <button type="button" class="btn-primary" data-action="add-to-cart" data-item-id="${item.id}" ${disabledAttribute}>${actionLabel}</button>
        </div>
      </div>
    `;

    els.menuGrid.appendChild(card);
  });
}

function renderCart() {
  const cart = state.cart;
  els.cartItems.innerHTML = "";

  if (cart.length === 0) {
    els.cartVendorInfo.textContent = "Your cart is empty. Add a few dishes to get started.";
    els.cartItems.innerHTML = `<div class="empty-state">No dishes added yet.</div>`;
    els.cartTotal.textContent = "₹0.00";
    els.cartItemsBadge.textContent = "0 items";
    els.cartCountBadge.textContent = "0";
    els.cartSummaryMeta.innerHTML = "";
    return;
  }

  const itemCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  const total = cart.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);
  const estimatedPrep = cart.reduce((sum, item) => sum + (Number(item.prepTimeMinutes) || 0), 0) || calculateAveragePrep(state.menu?.items || []);

  els.cartVendorInfo.textContent = `Ordering from ${cart[0].vendorName}`;
  els.cartItemsBadge.textContent = `${itemCount} item${itemCount === 1 ? "" : "s"}`;
  els.cartCountBadge.textContent = String(itemCount);
  els.cartTotal.textContent = formatCurrency(total);
  els.cartSummaryMeta.innerHTML = `
    <span class="cart-summary-pill">${itemCount} item${itemCount === 1 ? "" : "s"}</span>
    <span class="cart-summary-pill">Estimated prep ${estimatedPrep} min</span>
  `;

  cart.forEach((item, index) => {
    const row = document.createElement("div");
    row.className = "cart-item animate-up";
    row.style.animationDelay = `${Math.min(index * 40, 180)}ms`;
    row.innerHTML = `
      <div class="cart-item-header">
        <div>
          <strong>${escapeHtml(item.itemName)}</strong>
          <div class="muted">${formatCurrency(item.unitPrice)} each</div>
        </div>
        <div class="cart-item-price">${formatCurrency(item.unitPrice * item.quantity)}</div>
      </div>
      <div class="cart-item-footer">
        <div class="cart-item-qty">
          <button type="button" class="qty-button" data-cart-action="dec" data-index="${index}">−</button>
          <span>${item.quantity}</span>
          <button type="button" class="qty-button" data-cart-action="inc" data-index="${index}">+</button>
        </div>
        <button type="button" class="remove-link" data-cart-action="remove" data-index="${index}">Remove</button>
      </div>
    `;
    els.cartItems.appendChild(row);
  });
}

function renderVendorSkeletons() {
  if (!els.vendorsList) return;
  els.vendorsList.innerHTML = Array.from({ length: 4 }, () => `
    <div class="vendor-skeleton skeleton-card">
      <div class="skeleton-avatar skeleton-card"></div>
      <div class="skeleton-line md"></div>
      <div class="skeleton-line sm"></div>
      <div class="skeleton-line lg"></div>
      <div class="skeleton-line md"></div>
    </div>
  `).join("");
}

function renderMenuSkeletons() {
  if (!els.menuGrid) return;
  els.menuGrid.innerHTML = Array.from({ length: 6 }, () => `
    <div class="menu-skeleton skeleton-card">
      <div class="skeleton-block"></div>
      <div class="skeleton-line md"></div>
      <div class="skeleton-line lg" style="margin-top: 12px;"></div>
      <div class="skeleton-line md" style="margin-top: 10px;"></div>
      <div class="skeleton-line sm" style="margin-top: 18px;"></div>
    </div>
  `).join("");
}

function renderMenuEmpty(message) {
  els.menuGrid.innerHTML = `<div class="empty-state">${escapeHtml(message)}</div>`;
}

function updateHeroStats() {
  const totalVendors = state.vendors.length;
  const allMenus = Object.values(state.vendorMenusById || {});
  const allItems = allMenus.flatMap((menu) => menu?.items || []);
  const totalItems = allItems.length;
  const avgPrep = calculateAveragePrep(allItems);

  els.heroVendorCount.textContent = String(totalVendors);
  els.heroItemCount.textContent = String(totalItems || state.menu?.items?.length || 0);
  els.heroAvgPrep.textContent = `${avgPrep || calculateAveragePrep(state.menu?.items || [])} min`;
}

function updateHeroPreview() {
  const vendor = state.selectedVendor;
  const items = getFilteredMenuItems().slice(0, 3);
  const categories = state.menu?.categories || [];

  els.heroFeaturedVendorName.textContent = vendor?.name || "CampusAdda Preview";
  els.heroFeaturedMeta.textContent = vendor
    ? [vendor.locationLabel, vendor.campusArea].filter(Boolean).join(" • ") || "Campus pickup ready"
    : "Choose a vendor to preview menu highlights";
  els.heroFeaturedDescription.textContent = vendor?.description || "Smooth browsing, elegant cards, premium colors, and stronger hierarchy.";

  els.heroFeaturedTags.innerHTML = "";
  (vendor
    ? [
        `${state.menu?.items?.length || 0} dishes`,
        `${categories.length} categories`,
        `Avg prep ${calculateAveragePrep(state.menu?.items || [])} min`
      ]
    : ["Modern UI", "Animated interactions", "Premium layout"]
  ).forEach((tag) => {
    const pill = document.createElement("span");
    pill.className = "meta-pill";
    pill.textContent = tag;
    els.heroFeaturedTags.appendChild(pill);
  });

  els.heroFeaturedItems.innerHTML = "";
  if (!items.length) {
    els.heroFeaturedItems.innerHTML = `
      <div class="mini-menu-item">
        <div>
          <strong>Start exploring</strong>
          <span>Pick a vendor and search for dishes you want to feature here.</span>
        </div>
        <div class="mini-price">Live</div>
      </div>
    `;
    return;
  }

  items.forEach((item) => {
    const preview = document.createElement("div");
    preview.className = "mini-menu-item";
    preview.innerHTML = `
      <div>
        <strong>${escapeHtml(item.itemName)}</strong>
        <span>${escapeHtml(getCategoryName(item.categoryId))} • ${escapeHtml(item.prepTimeMinutes ? `${item.prepTimeMinutes} min prep` : "Freshly prepared")}</span>
      </div>
      <div class="mini-price">${formatCurrency(item.price)}</div>
    `;
    els.heroFeaturedItems.appendChild(preview);
  });
}

function getFilteredVendors() {
  const queryParts = [state.globalQuery, state.vendorQuery]
    .filter(Boolean)
    .join(" ")
    .split(" ")
    .filter(Boolean);

  if (!queryParts.length) {
    return state.vendors;
  }

  return state.vendors.filter((vendor) => {
    const vendorId = Number(vendor.id);
    const haystack =
      state.vendorSearchIndexById[vendorId] ||
      buildVendorSearchIndex(vendor, state.vendorMenusById[vendorId]);

    return queryParts.every((word) => haystack.includes(word));
  });
}

async function warmVendorSearchIndex() {
  await Promise.all(
    state.vendors.map(async (vendor) => {
      const vendorId = Number(vendor.id);

      try {
        const response = await CustomerApi.get(`/vendors/${vendorId}/menu`);
        const menu = response.data || { items: [], categories: [] };
        state.vendorMenusById[vendorId] = menu;
        state.vendorSearchIndexById[vendorId] = buildVendorSearchIndex(vendor, menu);
      } catch (_) {
        state.vendorSearchIndexById[vendorId] = buildVendorSearchIndex(vendor, null);
      }
    })
  );
}

function buildVendorSearchIndex(vendor, menu) {
  const categoryNames = (menu?.categories || []).map((entry) => entry.categoryName);
  const itemText = (menu?.items || []).flatMap((item) => [
    item.itemName,
    item.description
  ]);

  return normalizeText(
    [
      vendor?.name,
      vendor?.locationLabel,
      vendor?.campusArea,
      vendor?.description,
      ...categoryNames,
      ...itemText
    ]
      .filter(Boolean)
      .join(" ")
  );
}

function syncSelectedVendorWithFilters() {
  if (!state.selectedVendor) {
    return;
  }

  const visibleVendorIds = new Set(
    getFilteredVendors().map((vendor) => Number(vendor.id))
  );

  if (visibleVendorIds.has(Number(state.selectedVendor.id))) {
    return;
  }

  state.selectedVendor = null;
  state.menu = null;
  state.selectedCategoryId = null;

  renderVendorHeader();
  renderCategories();
  renderMenuItems();
  updateHeroPreview();
}


function getFilteredMenuItems() {
  const menu = state.menu;
  if (!menu) return [];

  const queryParts = [state.globalQuery, state.itemQuery].filter(Boolean).join(" ").split(" ").filter(Boolean);

  return (menu.items || []).filter((item) => {
    const matchesCategory = state.selectedCategoryId === null || Number(item.categoryId) === Number(state.selectedCategoryId);
    if (!matchesCategory) {
      return false;
    }

    if (queryParts.length === 0) {
      return true;
    }

    const haystack = normalizeText([
      item.itemName,
      item.description,
      getCategoryName(item.categoryId)
    ].filter(Boolean).join(" "));

    return queryParts.every((word) => haystack.includes(word));
  });
}

function getCategoryName(categoryId) {
  const category = (state.menu?.categories || []).find((entry) => Number(entry.id) === Number(categoryId));
  return category?.categoryName || "Menu";
}

function updateMenuQuantityInput(itemId, delta) {
  const input = document.getElementById(`menuQty-${itemId}`);
  if (!input) return;

  const current = Math.max(1, Number(input.value) || 1);
  input.value = String(Math.max(1, current + delta));
}

function addToCart(menuItemId) {
  hideOrderResult();
  const item = (state.menu?.items || []).find((entry) => Number(entry.id) === Number(menuItemId));
  if (!item) return;

  const qtyInput = document.getElementById(`menuQty-${menuItemId}`);
  const quantity = Math.max(1, Number(qtyInput?.value || 1));

  if (state.cart.length > 0 && Number(state.cart[0].vendorId) !== Number(state.menu.vendorId)) {
    showToast("You can only order from one vendor at a time. Clear the cart or stay with the same vendor.", "info");
    return;
  }

  const existing = state.cart.find((row) => Number(row.menuItemId) === Number(menuItemId));
  if (existing) {
    existing.quantity += quantity;
  } else {
    state.cart.push({
      vendorId: state.menu.vendorId,
      vendorName: state.menu.vendorName,
      menuItemId: item.id,
      itemName: item.itemName,
      unitPrice: Number(item.price) || 0,
      quantity,
      prepTimeMinutes: Number(item.prepTimeMinutes) || 0
    });
  }

  if (qtyInput) {
    qtyInput.value = "1";
  }

  renderCart();
  showToast(`${item.itemName} added to cart`, "success");
}

function updateCartQuantity(index, delta) {
  hideOrderResult();
  const item = state.cart[index];
  if (!item) return;

  item.quantity += delta;
  if (item.quantity <= 0) {
    state.cart.splice(index, 1);
  }

  renderCart();
}

function removeFromCart(index) {
  hideOrderResult();
  const removed = state.cart[index];
  state.cart.splice(index, 1);
  renderCart();

  if (removed) {
    showToast(`${removed.itemName} removed`, "info");
  }
}

function bindCheckout() {
  els.checkoutForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (state.cart.length === 0) {
      showToast("Add at least one dish before placing the order.", "info");
      return;
    }

    const payload = {
      vendorId: state.cart[0].vendorId,
      customerName: els.customerName.value.trim(),
      customerPhone: els.customerPhone.value.trim(),
      notes: els.customerNotes.value.trim(),
      items: state.cart.map((item) => ({
        menuItemId: item.menuItemId,
        quantity: item.quantity,
        specialInstructions: null
      }))
    };

    if (!payload.customerName || !payload.customerPhone) {
      showToast("Name and phone number are required.", "info");
      return;
    }

    const submitButton = els.checkoutForm.querySelector("button[type='submit']");
    const originalLabel = submitButton?.textContent || "Place Order";

    try {
      if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "Placing order...";
      }

      const response = await CustomerApi.post("/orders", payload);
      const order = response.data || {};
      const totalAmount =
        order.totalAmount ||
        state.cart.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0);

      els.orderResult.classList.remove("hidden");
      els.orderResult.innerHTML = `
        <strong>Order placed successfully.</strong><br>
        Order Number: ${escapeHtml(order.orderNumber || "-")}<br>
        Vendor: ${escapeHtml(order.vendorName || state.cart[0]?.vendorName || "-")}<br>
        Status: ${escapeHtml(order.status || "CREATED")}<br>
        Total: ${formatCurrency(totalAmount)}<br><br>
        <span class="muted">Live tracking has been started below.</span>
      `;

      if (typeof window.refreshCustomerAccountData === "function") {
        await window.refreshCustomerAccountData();
      }

      const trackingPhone = payload.customerPhone;
      const trackingOrderNumber = order.orderNumber || "";

      if (els.trackingOrderNumber) {
        els.trackingOrderNumber.value = trackingOrderNumber;
      }
      if (els.trackingPhone) {
        els.trackingPhone.value = trackingPhone;
      }

      persistTrackingSession(trackingOrderNumber, trackingPhone);

      state.cart = [];
      renderCart();
      els.checkoutForm.reset();

      if (typeof CustomerAuth !== "undefined" && typeof CustomerAuth.getStoredUser === "function") {
        const storedUser = CustomerAuth.getStoredUser();
        if (storedUser) {
          if (els.customerName) {
            els.customerName.value = storedUser.fullName || "";
          }
          if (els.customerPhone) {
            els.customerPhone.value = storedUser.phone || "";
          }
        }
      }

      await trackOrder({
        orderNumber: trackingOrderNumber,
        phone: trackingPhone,
        showToastOnSuccess: false,
        showToastOnError: true,
        startPolling: true
      });

      showToast("Order placed successfully", "success");
    } catch (error) {
      showToast(error.message, "error");
    } finally {
      if (submitButton) {
        submitButton.disabled = false;
        submitButton.textContent = originalLabel;
      }
    }
  });
}

function bindTracking() {
  els.trackingForm?.addEventListener("submit", async (event) => {
    event.preventDefault();

    await trackOrder({
      orderNumber: els.trackingOrderNumber?.value || "",
      phone: els.trackingPhone?.value || "",
      showToastOnSuccess: true,
      showToastOnError: true,
      startPolling: true
    });
  });

  els.refreshTrackingBtn?.addEventListener("click", async () => {
    const orderNumber = els.trackingOrderNumber?.value || state.tracking.orderNumber;
    const phone = els.trackingPhone?.value || state.tracking.phone;

    if (!orderNumber.trim() || !phone.trim()) {
      showToast("Enter order number and phone number first.", "info");
      return;
    }

    await trackOrder({
      orderNumber,
      phone,
      showToastOnSuccess: true,
      showToastOnError: true,
      startPolling: true
    });
  });
}

function restoreTrackingSession() {
  stopTrackingAutoRefresh();

  state.tracking.orderNumber = "";
  state.tracking.phone = "";
  state.tracking.data = null;

  try {
    window.localStorage.removeItem(TRACKING_STORAGE_KEY);
  } catch (_) {
    // ignore local storage issues
  }

  if (els.trackingOrderNumber) {
    els.trackingOrderNumber.value = "";
  }

  if (els.trackingPhone) {
    els.trackingPhone.value = "";
  }

  renderTrackingPanel(null);
  renderTrackingState("Enter your order number and phone number to see live status updates.");
}

async function trackOrder({
  orderNumber,
  phone,
  showToastOnSuccess = false,
  showToastOnError = true,
  startPolling = true
}) {
  const normalizedOrderNumber = String(orderNumber || "").trim();
  const normalizedPhone = String(phone || "").trim();

  if (!normalizedOrderNumber || !normalizedPhone) {
    renderTrackingState("Enter your order number and phone number to see live status updates.");
    renderTrackingPanel(null);
    stopTrackingAutoRefresh();
    return;
  }

  const originalButtonLabel = els.trackOrderBtn?.textContent || "Track Order";

  try {
    if (els.trackOrderBtn) {
      els.trackOrderBtn.disabled = true;
      els.trackOrderBtn.textContent = "Tracking...";
    }

    renderTrackingState("Fetching latest order status...");

    const path =
      `/orders/track?orderNumber=${encodeURIComponent(normalizedOrderNumber)}` +
      `&phone=${encodeURIComponent(normalizedPhone)}`;

    const response = await CustomerApi.get(path);
    const trackingData = response.data || null;

    state.tracking.orderNumber = normalizedOrderNumber;
    state.tracking.phone = normalizedPhone;
    state.tracking.data = trackingData;

    persistTrackingSession(normalizedOrderNumber, normalizedPhone);
    renderTrackingPanel(trackingData);

    if (startPolling) {
      startTrackingAutoRefresh();
    }

    if (showToastOnSuccess) {
      showToast("Tracking refreshed", "success");
    }
  } catch (error) {
    state.tracking.data = null;
    renderTrackingPanel(null);
    renderTrackingState(error.message || "Failed to fetch order status.");
    stopTrackingAutoRefresh();

    if (showToastOnError) {
      showToast(error.message || "Failed to fetch order status.", "error");
    }
  } finally {
    if (els.trackOrderBtn) {
      els.trackOrderBtn.disabled = false;
      els.trackOrderBtn.textContent = originalButtonLabel;
    }
  }
}

function startTrackingAutoRefresh() {
  stopTrackingAutoRefresh();

  if (!state.tracking.orderNumber || !state.tracking.phone) {
    return;
  }

  const terminalStatuses = ["COMPLETED", "CANCELLED"];
  if (terminalStatuses.includes(String(state.tracking.data?.status || "").toUpperCase())) {
    return;
  }

  state.tracking.autoRefreshTimer = window.setInterval(async () => {
    try {
      await trackOrder({
        orderNumber: state.tracking.orderNumber,
        phone: state.tracking.phone,
        showToastOnSuccess: false,
        showToastOnError: false,
        startPolling: false
      });
    } catch (_) {
      // ignore silent polling errors
    }
  }, TRACKING_POLL_INTERVAL_MS);
}

function stopTrackingAutoRefresh() {
  if (state.tracking.autoRefreshTimer) {
    window.clearInterval(state.tracking.autoRefreshTimer);
    state.tracking.autoRefreshTimer = null;
  }
}

function persistTrackingSession() {
  // intentionally disabled:
  // tracking should not survive a page reload for anonymous/public browsing
}

function renderTrackingState(message) {
  if (!els.trackingState) return;
  els.trackingState.textContent = message || "";
}

function renderTrackingPanel(order) {
  if (!order) {
    els.trackingPanel?.classList.add("hidden");
    if (els.trackingVendorName) {
      els.trackingVendorName.textContent = "Vendor";
    }
    if (els.trackingOrderMeta) {
      els.trackingOrderMeta.innerHTML = "";
    }
    if (els.trackingStatusBadge) {
      els.trackingStatusBadge.textContent = "TRACKING";
      els.trackingStatusBadge.className = "tracking-status-badge";
    }
    if (els.trackingSteps) {
      els.trackingSteps.innerHTML = "";
    }
    if (els.trackingItems) {
      els.trackingItems.innerHTML = "";
    }
    if (els.trackingHistory) {
      els.trackingHistory.innerHTML = "";
    }
    if (els.trackingLastUpdated) {
      els.trackingLastUpdated.textContent = "";
    }
    return;
  }

  els.trackingPanel?.classList.remove("hidden");
  renderTrackingState("Live tracking active. Status refreshes automatically.");

  if (els.trackingVendorName) {
    els.trackingVendorName.textContent = order.vendorName || "Vendor";
  }

  if (els.trackingOrderMeta) {
    const metaParts = [
      `Order: ${escapeHtml(order.orderNumber || "-")}`,
      `Total: ${formatCurrency(order.totalAmount || 0)}`,
      order.placedAt ? `Placed: ${escapeHtml(formatDateTimeSafe(order.placedAt))}` : ""
    ].filter(Boolean);

    els.trackingOrderMeta.innerHTML = metaParts
      .map((part) => `<span class="tracking-meta-pill">${part}</span>`)
      .join("");
  }

  if (els.trackingStatusBadge) {
    const normalizedStatus = String(order.status || "CREATED").toUpperCase();
    els.trackingStatusBadge.textContent = statusLabel(normalizedStatus);
    els.trackingStatusBadge.className = `tracking-status-badge status-${normalizedStatus.toLowerCase()}`;
  }

  renderTrackingSteps(order);
  renderTrackingItems(order.items || []);
  renderTrackingHistory(order.statusHistory || []);

  if (els.trackingLastUpdated) {
    els.trackingLastUpdated.textContent = `Last updated: ${formatDateTimeSafe(new Date().toISOString())}`;
  }

  const terminalStatuses = ["COMPLETED", "CANCELLED"];
  if (terminalStatuses.includes(String(order.status || "").toUpperCase())) {
    stopTrackingAutoRefresh();
  }
}

function renderTrackingSteps(order) {
  if (!els.trackingSteps) return;

  const currentStatus = String(order.status || "CREATED").toUpperCase();
  const flow = ["CREATED", "ACCEPTED", "PREPARING", "READY", "COMPLETED"];

  if (currentStatus === "CANCELLED") {
    els.trackingSteps.innerHTML = `
      <div class="tracking-step done">
        <span class="tracking-step-dot"></span>
        <span class="tracking-step-label">Created</span>
      </div>
      <div class="tracking-step cancelled active">
        <span class="tracking-step-dot"></span>
        <span class="tracking-step-label">Cancelled</span>
      </div>
    `;
    return;
  }

  const currentIndex = flow.indexOf(currentStatus);

  els.trackingSteps.innerHTML = flow
    .map((status, index) => {
      let className = "tracking-step";
      if (index < currentIndex) {
        className += " done";
      } else if (index === currentIndex) {
        className += " active";
      }

      return `
        <div class="${className}">
          <span class="tracking-step-dot"></span>
          <span class="tracking-step-label">${statusLabel(status)}</span>
        </div>
      `;
    })
    .join("");
}

function renderTrackingItems(items) {
  if (!els.trackingItems) return;

  if (!items.length) {
    els.trackingItems.innerHTML = `<div class="tracking-empty-row">No order items found.</div>`;
    return;
  }

  els.trackingItems.innerHTML = items
    .map((item) => `
      <div class="tracking-item-card">
        <div>
          <strong>${escapeHtml(item.itemName || "-")}</strong>
          <div class="muted">
            Qty ${escapeHtml(item.quantity ?? "-")} • ${formatCurrency(item.unitPrice || 0)} each
          </div>
          ${
            item.specialInstructions
              ? `<div class="tracking-item-note">Note: ${escapeHtml(item.specialInstructions)}</div>`
              : ""
          }
        </div>
        <div class="tracking-item-total">${formatCurrency(item.lineTotal || 0)}</div>
      </div>
    `)
    .join("");
}

function renderTrackingHistory(history) {
  if (!els.trackingHistory) return;

  if (!history.length) {
    els.trackingHistory.innerHTML = `<div class="tracking-empty-row">No tracking history yet.</div>`;
    return;
  }

  els.trackingHistory.innerHTML = history
    .map((entry) => `
      <div class="tracking-history-row">
        <div class="tracking-history-dot"></div>
        <div class="tracking-history-content">
          <strong>${escapeHtml(statusLabel(entry.toStatus || "-"))}</strong>
          <div class="muted">${escapeHtml(entry.remarks || "Status updated")}</div>
          <div class="tracking-history-time">${escapeHtml(formatDateTimeSafe(entry.changedAt))}</div>
        </div>
      </div>
    `)
    .join("");
}

function statusLabel(status) {
  const value = String(status || "").toUpperCase();

  switch (value) {
    case "CREATED":
      return "Created";
    case "ACCEPTED":
      return "Accepted";
    case "PREPARING":
      return "Preparing";
    case "READY":
      return "Ready";
    case "COMPLETED":
      return "Completed";
    case "CANCELLED":
      return "Cancelled";
    default:
      return value || "-";
  }
}

function formatDateTimeSafe(value) {
  if (!value) return "-";

  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return date.toLocaleString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit"
  });
}

function hideOrderResult() {
  els.orderResult?.classList.add("hidden");
}

function showToast(message, type = "info") {
  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.textContent = message;
  els.toastContainer?.appendChild(toast);

  window.setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(-8px)";
    window.setTimeout(() => toast.remove(), 220);
  }, 2800);
}

function calculateAveragePrep(items) {
  const validTimes = items
    .map((item) => Number(item.prepTimeMinutes))
    .filter((value) => Number.isFinite(value) && value > 0);

  if (!validTimes.length) return 0;
  return Math.round(validTimes.reduce((sum, value) => sum + value, 0) / validTimes.length);
}

function formatCurrency(value) {
  const amount = Number(value) || 0;
  return `₹${amount.toFixed(2)}`;
}

function normalizeText(value) {
  return String(value || "").toLowerCase().trim().replace(/\s+/g, " ");
}

function getInitials(name) {
  return String(name || "CA")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() || "")
    .join("") || "CA";
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function escapeAttribute(value) {
  return escapeHtml(value).replace(/`/g, "&#96;");
}